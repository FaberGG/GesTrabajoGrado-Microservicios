package com.unicauca.identity.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RateLimitingTest {

    @Mock
    private FilterChain filterChain;

    @Mock
    private HandlerMappingIntrospector introspector;

    private RateLimitingConfig rateLimitingConfig;
    private OncePerRequestFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rateLimitingConfig = new RateLimitingConfig();

        // Configurar con valores de prueba
        ReflectionTestUtils.setField(rateLimitingConfig, "enabled", true);
        ReflectionTestUtils.setField(rateLimitingConfig, "capacity", 2); // Capacidad baja para pruebas
        ReflectionTestUtils.setField(rateLimitingConfig, "refillTokens", 1);
        ReflectionTestUtils.setField(rateLimitingConfig, "refillDuration", 1);

        rateLimitFilter = rateLimitingConfig.rateLimitFilter(introspector);
    }

    @Test
    void shouldAllowRequestsUnderLimit() throws ServletException, IOException {
        // Given - Una solicitud HTTP para /api/auth/login
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When - Llamamos al filtro dentro de la capacidad permitida
        rateLimitFilter.doFilter(request, response, filterChain);
        rateLimitFilter.doFilter(request, response, filterChain);

        // Then - No debería bloquear las solicitudes dentro del límite
        verify(filterChain, times(2)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldBlockRequestsOverLimit() throws ServletException, IOException {
        // Given - Una solicitud HTTP para /api/auth/login
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("192.168.1.2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Preparar la respuesta para capturar la salida
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // When - Excedemos la capacidad permitida (2+1 solicitudes)
        rateLimitFilter.doFilter(request, response, filterChain);
        rateLimitFilter.doFilter(request, response, filterChain);

        // Crear nueva respuesta para la tercera solicitud que debería ser bloqueada
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        StringWriter blockedWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(blockedWriter);
        when(blockedResponse.getWriter()).thenReturn(printWriter);

        rateLimitFilter.doFilter(request, blockedResponse, filterChain);

        // Then - Debería permitir 2 y bloquear 1
        verify(filterChain, times(2)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        assertEquals(429, blockedResponse.getStatus());
    }

    @Test
    void shouldNotApplyLimitingToNonSensitiveEndpoints() throws ServletException, IOException {
        // Given - Una solicitud HTTP para un endpoint no sensible
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/profile");
        request.setRemoteAddr("192.168.1.3");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When - Realizamos múltiples solicitudes (más que el límite)
        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilter(request, response, filterChain);
        }

        // Then - No debería aplicar límite a este endpoint
        verify(filterChain, times(5)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        assertEquals(200, response.getStatus());
    }
}

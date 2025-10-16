package com.unicauca.identity.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SecurityUtilTest {

    private SecurityUtil securityUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityUtil = new SecurityUtil();

        // Preparar SecurityContextHolder para las pruebas
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCurrentUserEmail_ShouldReturnEmail_WhenUserIsAuthenticated() {
        // Given
        UserDetails userDetails = new User("jperez@unicauca.edu.co", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ESTUDIANTE")));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        Optional<String> result = securityUtil.getCurrentUserEmail();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("jperez@unicauca.edu.co");
    }

    @Test
    void getCurrentUserEmail_ShouldReturnEmpty_WhenUserIsNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        Optional<String> result = securityUtil.getCurrentUserEmail();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void hasRole_ShouldReturnTrue_WhenUserHasRole() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
                (Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // When
        boolean result = securityUtil.hasRole("ADMIN");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasRole_ShouldReturnFalse_WhenUserDoesNotHaveRole() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
                (Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_ESTUDIANTE")));

        // When
        boolean result = securityUtil.hasRole("ADMIN");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getCurrentUserRoles_ShouldReturnRoles_WhenUserIsAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn((Collection) List.of(
                new SimpleGrantedAuthority("ROLE_ESTUDIANTE"),
                new SimpleGrantedAuthority("ROLE_ADMIN")));

        // When
        Collection<String> roles = securityUtil.getCurrentUserRoles();

        // Then
        assertThat(roles).hasSize(2);
        assertThat(roles).contains("ESTUDIANTE", "ADMIN");
    }

    @Test
    void getClientIp_ShouldReturnIp_FromXForwardedFor() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.10, 10.0.0.1");

        // When
        String ip = securityUtil.getClientIp(request);

        // Then
        assertThat(ip).isEqualTo("192.168.1.10");
    }

    @Test
    void getClientIp_ShouldReturnIp_FromRemoteAddr() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.20");

        // When
        String ip = securityUtil.getClientIp(request);

        // Then
        assertThat(ip).isEqualTo("192.168.1.20");
    }
}

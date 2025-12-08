package com.unicauca.identity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configuración para limitar el número de solicitudes por IP
 */
@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {

    @Value("${rate-limiting.enabled:true}")
    private boolean enabled;

    @Value("${rate-limiting.capacity:20}")
    private int capacity;

    @Value("${rate-limiting.refill-tokens:10}")
    private int refillTokens;

    @Value("${rate-limiting.refill-duration:1}")
    private int refillDuration;

    @Bean
    public OncePerRequestFilter rateLimitFilter(HandlerMappingIntrospector introspector) {
        // Matcher para rutas sensibles que queremos limitar
        MvcRequestMatcher loginMatcher = new MvcRequestMatcher(introspector, "/api/auth/login");
        loginMatcher.setMethod(HttpMethod.POST);

        MvcRequestMatcher verifyTokenMatcher = new MvcRequestMatcher(introspector, "/api/auth/verify-token");
        verifyTokenMatcher.setMethod(HttpMethod.POST);

        return new OncePerRequestFilter() {
            private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {

                // Si el rate limiting está deshabilitado o no es una ruta sensible, continuamos
                if (!enabled || (!loginMatcher.matches(request) && !verifyTokenMatcher.matches(request))) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Obtener la IP real del cliente (considerando proxies)
                String ip = getClientIP(request);

                // Crear o obtener el bucket para esta IP
                Bucket bucket = buckets.computeIfAbsent(ip, key -> createBucket());

                // Consumir un token del bucket
                if (bucket.tryConsume(1)) {
                    // Si hay tokens disponibles, continuamos
                    filterChain.doFilter(request, response);
                } else {
                    // Si no hay tokens, devolvemos un error 429 Too Many Requests
                    response.setStatus(429); // Código para Too Many Requests
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"message\":\"Demasiadas solicitudes. Intente de nuevo más tarde.\"}");
                }
            }

            private Bucket createBucket() {
                Refill refill = Refill.intervally(refillTokens, java.time.Duration.ofSeconds(refillDuration));
                Bandwidth limit = Bandwidth.classic(capacity, refill);
                return Bucket.builder().addLimit(limit).build();
            }

            private String getClientIP(HttpServletRequest request) {
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }

                // Si hay múltiples IPs (en caso de varios proxies), tomamos la primera
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }

                return ip;
            }
        };
    }
}

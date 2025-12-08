package co.unicauca.gateway.config;

import co.unicauca.gateway.logging.RequestResponseLoggingFilter;
import co.unicauca.gateway.security.JwtGatewayFilter;
import co.unicauca.gateway.security.RoleFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración completa de rutas del API Gateway.
 *
 * Microservicios configurados:
 * - Identity Service (8081): autenticación y gestión de usuarios
 * - Submission Service (8082): gestión de entregas (Formato A, anteproyecto)
 * - Notification Service (8083): notificaciones asíncronas
 * - Review Service (8084): evaluación de documentos y asignación de evaluadores
 * - Progress Tracking (8085): historial de eventos y consulta de estado (CQRS)
 *
 * Seguridad:
 * - Rutas públicas: /api/identity/**, /api/auth/**
 * - Rutas protegidas: Todos los demás endpoints requieren JWT válido
 *
 * @author Gateway Team
 * @version 2.0.0
 */
@Configuration
public class RouteConfig {

    @Value("${services.identity.url}")
    private String identityServiceUrl;

    @Value("${services.submission.url}")
    private String submissionServiceUrl;

    @Value("${services.notification.url}")
    private String notificationServiceUrl;

    @Value("${services.review.url}")
    private String reviewServiceUrl;

    @Value("${services.tracking.url}")
    private String trackingServiceUrl;

    private final JwtGatewayFilter jwtGatewayFilter;
    private final RoleFilter roleFilter;
    private final RequestResponseLoggingFilter loggingFilter;

    public RouteConfig(JwtGatewayFilter jwtGatewayFilter,
                       RoleFilter roleFilter,
                       RequestResponseLoggingFilter loggingFilter) {
        this.jwtGatewayFilter = jwtGatewayFilter;
        this.roleFilter = roleFilter;
        this.loggingFilter = loggingFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ============================================================
                // IDENTITY SERVICE - Rutas PÚBLICAS (No requieren JWT)
                // ============================================================

                .route("identity-service-main", r -> r
                        .path("/api/identity/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new RequestResponseLoggingFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("identityService")
                                        .setFallbackUri("forward:/api/gateway/fallback/identity")))
                        .uri(identityServiceUrl))

                .route("identity-service-auth", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new RequestResponseLoggingFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("identityService")
                                        .setFallbackUri("forward:/api/gateway/fallback/identity")))
                        .uri(identityServiceUrl))

                // ============================================================
                // SUBMISSION SERVICE - Rutas PROTEGIDAS (Requieren JWT)
                // ============================================================

                .route("submission-service", r -> r
                        .path("/api/submissions/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new RequestResponseLoggingFilter.Config()))
                                .filter(jwtGatewayFilter.apply(new JwtGatewayFilter.Config()))
                                .filter(roleFilter.apply(new RoleFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("submissionService")
                                        .setFallbackUri("forward:/api/gateway/fallback/submission")))
                        .uri(submissionServiceUrl))

                // ============================================================
                // NOTIFICATION SERVICE - Rutas PROTEGIDAS (Requieren JWT)
                // ============================================================

                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new RequestResponseLoggingFilter.Config()))
                                .filter(jwtGatewayFilter.apply(new JwtGatewayFilter.Config()))
                                .filter(roleFilter.apply(new RoleFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("notificationService")
                                        .setFallbackUri("forward:/api/gateway/fallback/notification")))
                        .uri(notificationServiceUrl))

                // ============================================================
                // REVIEW SERVICE - Rutas PROTEGIDAS (Requieren JWT)
                // ============================================================

                .route("review-service", r -> r
                        .path("/api/review/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new RequestResponseLoggingFilter.Config()))
                                .filter(jwtGatewayFilter.apply(new JwtGatewayFilter.Config()))
                                .filter(roleFilter.apply(new RoleFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("reviewService")
                                        .setFallbackUri("forward:/api/gateway/fallback/review")))
                        .uri(reviewServiceUrl))

                // ============================================================
                // PROGRESS TRACKING SERVICE - Rutas PROTEGIDAS (Requieren JWT)
                // ============================================================

                .route("tracking-service", r -> r
                        .path("/api/progress/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new RequestResponseLoggingFilter.Config()))
                                .filter(jwtGatewayFilter.apply(new JwtGatewayFilter.Config()))
                                .filter(roleFilter.apply(new RoleFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("trackingService")
                                        .setFallbackUri("forward:/api/gateway/fallback/tracking")))
                        .uri(trackingServiceUrl))

                .build();
    }
}
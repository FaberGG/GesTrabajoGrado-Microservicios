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
 * Configuración de rutas del API Gateway.
 *
 * Define el enrutamiento de peticiones hacia los microservicios backend:
 * - /api/auth/** → identity-service (público)
 * - /api/submissions/** → submission-service (protegido con JWT)
 * - /api/notifications/** → notification-service (protegido con JWT)
 *
 * Aplica filtros en el orden:
 * 1. RequestResponseLoggingFilter - logging de peticiones
 * 2. JwtGatewayFilter - validación JWT y extracción de claims
 * 3. RoleFilter - autorización por rol (opcional)
 *
 * Los filtros se aplican mediante el metodo filters() en cada ruta.
 *
 * @author Gateway Team
 */
@Configuration
public class RouteConfig {

    @Value("${services.identity.url}")
    private String identityServiceUrl;

    @Value("${services.submission.url}")
    private String submissionServiceUrl;

    @Value("${services.notification.url}")
    private String notificationServiceUrl;

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

    /**
     * Define las rutas del gateway y sus filtros asociados.
     *
     * Rutas públicas (identity) no requieren JWT.
     * Rutas protegidas (submissions, notifications) validan JWT y roles.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Ruta hacia Identity Service (PÚBLICA - no requiere JWT)
                .route("identity-service", r -> r
                        .path("/api/identity/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new RequestResponseLoggingFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("identityService")
                                        .setFallbackUri("forward:/api/gateway/fallback/identity")))
                        .uri(identityServiceUrl))

                // Ruta alternativa para Identity Service (sin prefijo /identity)
                .route("identity-service-shortcut", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new RequestResponseLoggingFilter.Config()))
                                .rewritePath("/api/auth/(?<segment>.*)", "/api/auth/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("identityService")
                                        .setFallbackUri("forward:/api/gateway/fallback/identity")))
                        .uri(identityServiceUrl))

                // Ruta hacia Submission Service (PROTEGIDA - requiere JWT)
                .route("submission-service", r -> r
                        .path("/api/submissions/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new RequestResponseLoggingFilter.Config()))
                                .filter(jwtGatewayFilter.apply(new JwtGatewayFilter.Config()))
                                .filter(roleFilter.apply(new RoleFilter.Config()))
                                .rewritePath("/api/submissions/(?<segment>.*)", "/api/submissions/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("submissionService")
                                        .setFallbackUri("forward:/api/gateway/fallback/submission")))
                        .uri(submissionServiceUrl))

                // Ruta hacia Notification Service (PROTEGIDA - requiere JWT)
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .filter(loggingFilter.apply(new RequestResponseLoggingFilter.Config()))
                                .filter(jwtGatewayFilter.apply(new JwtGatewayFilter.Config()))
                                .filter(roleFilter.apply(new RoleFilter.Config()))
                                .rewritePath("/api/notifications(?<segment>/.*)?", "/notifications${segment}")
                                .circuitBreaker(config -> config
                                        .setName("notificationService")
                                        .setFallbackUri("forward:/api/gateway/fallback/notification")))
                        .uri(notificationServiceUrl))

                .build();
    }
}
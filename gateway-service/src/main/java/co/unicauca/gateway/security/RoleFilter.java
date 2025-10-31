package co.unicauca.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filtro de autorización basada en roles para el sistema de gestión de trabajos de grado.
 *
 * Roles del sistema:
 * - DOCENTE: Puede subir Formato A, anteproyectos y nueva versiones
 * - ESTUDIANTE: Puede consultar estado de su proyecto
 * - COORDINADOR: Puede evaluar Formato A
 * - JEFE_DEPARTAMENTO: Puede ver anteproyectos y asignar evaluadores
 *
 * IMPORTANTE: Este filtro es una capa adicional de seguridad a nivel de gateway.
 * Los microservicios DEBEN implementar su propia autorización de negocio.
 *
 * Activación: gateway.security.enforceRoleCheck=true/false
 *
 * @author Gateway Team
 * @version 2.0.0
 */
@Component
public class RoleFilter extends AbstractGatewayFilterFactory<RoleFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(RoleFilter.class);

    @Value("${gateway.security.enforceRoleCheck:false}")
    private boolean enforceRoleCheck;

    /**
     * Definición de requisitos de rol por endpoint.
     *
     * Mapeo basado en los requisitos funcionales del sistema:
     * - RF2: Docente sube Formato A
     * - RF3: Coordinador evalúa Formato A
     * - RF4: Docente sube nueva versión Formato A
     * - RF5: Estudiante consulta estado de proyecto
     * - RF6: Docente sube anteproyecto
     * - RF7: Jefe de departamento lista anteproyectos
     */
    private static final Map<String, List<String>> roleRequirements = new HashMap<>();

    static {
        // ===== SUBMISSION SERVICE =====

        // RF2: Subir Formato A - Solo DOCENTE
        roleRequirements.put("/api/submissions/formatoA", List.of("DOCENTE"));

        // RF4: Subir nueva versión Formato A - Solo DOCENTE
        roleRequirements.put("/api/submissions/formatoA/*/nueva-version", List.of("DOCENTE"));

        // RF6: Subir anteproyecto - Solo DOCENTE
        roleRequirements.put("/api/submissions/anteproyecto", List.of("DOCENTE"));

        // RF7: Listar anteproyectos - JEFE_DEPARTAMENTO y COORDINADOR (por si necesita verlos)
        roleRequirements.put("/api/submissions/anteproyectos", List.of("JEFE_DEPARTAMENTO", "COORDINADOR", "DOCENTE"));

        // Cambiar estado de submission - Solo COORDINADOR o JEFE_DEPARTAMENTO
        roleRequirements.put("/api/submissions/formatoA/*/estado", List.of("COORDINADOR"));
        roleRequirements.put("/api/submissions/anteproyectos/*/estado", List.of("JEFE_DEPARTAMENTO"));


        // ===== REVIEW SERVICE =====

        // RF3: Ver Formato A pendientes de evaluación - Solo COORDINADOR
        roleRequirements.put("/api/review/formatoA/pendientes", List.of("COORDINADOR"));

        // RF3: Evaluar Formato A - Solo COORDINADOR
        roleRequirements.put("/api/review/formatoA/*/evaluar", List.of("COORDINADOR"));

        // RF7: Asignar evaluadores a anteproyecto - Solo JEFE_DEPARTAMENTO
        roleRequirements.put("/api/review/anteproyectos/asignar", List.of("JEFE_DEPARTAMENTO"));

        // Ver asignaciones - JEFE_DEPARTAMENTO y evaluadores asignados
        roleRequirements.put("/api/review/anteproyectos/asignaciones", List.of("JEFE_DEPARTAMENTO", "DOCENTE"));

        // Evaluar anteproyecto - Evaluadores asignados (DOCENTE)
        roleRequirements.put("/api/review/anteproyectos/*/evaluar", List.of("DOCENTE", "JEFE_DEPARTAMENTO"));


        // ===== PROGRESS TRACKING SERVICE =====

        // RF5: Consultar estado de proyecto - ESTUDIANTE, DOCENTE, COORDINADOR
        roleRequirements.put("/api/progress/proyectos/*/estado", List.of("ESTUDIANTE", "DOCENTE", "COORDINADOR", "JEFE_DEPARTAMENTO"));

        // Consultar historial - Todos los roles autenticados
        roleRequirements.put("/api/progress/proyectos/*/historial", List.of("ESTUDIANTE", "DOCENTE", "COORDINADOR", "JEFE_DEPARTAMENTO"));

        // Crear evento - Solo servicios internos (normalmente no se expone directamente)
        roleRequirements.put("/api/progress/eventos", List.of("COORDINADOR", "JEFE_DEPARTAMENTO"));


        // ===== NOTIFICATION SERVICE =====

        // Notificaciones - Todos los usuarios autenticados pueden consultar sus propias notificaciones
        roleRequirements.put("/api/notifications/**", List.of("ESTUDIANTE", "DOCENTE", "COORDINADOR", "JEFE_DEPARTAMENTO"));
    }

    public RoleFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!enforceRoleCheck) {
                log.debug("Verificación de roles desactivada globalmente");
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            List<String> requiredRoles = getRequiredRoles(path);

            if (requiredRoles == null || requiredRoles.isEmpty()) {
                log.debug("No hay requisitos de rol específicos para: {}", path);
                return chain.filter(exchange);
            }

            String userRole = request.getHeaders().getFirst("X-User-Role");
            String userId = request.getHeaders().getFirst("X-User-Id");

            if (userRole == null || userRole.isEmpty()) {
                log.warn("Header X-User-Role ausente para ruta protegida: {} | userId: {}", path, userId);
                return forbiddenResponse(exchange, "Role information missing");
            }

            if (!requiredRoles.contains(userRole)) {
                log.warn("Acceso denegado: usuario {} con rol {} intentó acceder a {} (roles requeridos: {})",
                        userId, userRole, path, requiredRoles);
                return forbiddenResponse(exchange,
                        String.format("Access denied. Required roles: %s", String.join(", ", requiredRoles)));
            }

            log.debug("Verificación de rol exitosa: userId={} con rol {} autorizado para {}",
                    userId, userRole, path);
            return chain.filter(exchange);
        };
    }

    /**
     * Obtiene los roles requeridos para un path específico.
     * Soporta wildcards (* y **) para paths dinámicos.
     */
    private List<String> getRequiredRoles(String path) {
        // Match exacto
        if (roleRequirements.containsKey(path)) {
            return roleRequirements.get(path);
        }

        // Match con wildcards
        for (Map.Entry<String, List<String>> entry : roleRequirements.entrySet()) {
            String pattern = entry.getKey();

            if (pattern.contains("*")) {
                String regex = pattern
                        .replace("/**", "/.*")
                        .replace("/*", "/[^/]+")
                        .replace("*", "[^/]+");

                if (path.matches(regex)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonResponse = String.format(
                "{\"error\":\"Forbidden\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                message,
                java.time.Instant.now().toString()
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Configuración adicional si es necesaria
    }
}
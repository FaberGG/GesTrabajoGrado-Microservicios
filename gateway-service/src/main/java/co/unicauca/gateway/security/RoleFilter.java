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
 * Filtro opcional para autorización basada en roles a nivel de gateway.
 *
 * Este filtro puede activarse/desactivarse mediante la propiedad:
 * gateway.security.enforceRoleCheck=true/false
 *
 * IMPORTANTE: Este filtro NO reemplaza la autorización de negocio en los microservicios.
 * Es una capa adicional de seguridad para bloquear peticiones obviamente inválidas
 * antes de que lleguen a los servicios backend.
 *
 * Ejemplo de uso:
 * - Bloquear acceso a /api/submission/formatoA si el rol no es DOCENTE
 * - Bloquear acceso a endpoints administrativos si no es COORDINADOR
 *
 * Los requisitos de rol por endpoint se configuran en el mapa roleRequirements.
 *
 * @author Gateway Team
 */
@Component
public class RoleFilter extends AbstractGatewayFilterFactory<RoleFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(RoleFilter.class);

    @Value("${gateway.security.enforceRoleCheck:false}")
    private boolean enforceRoleCheck;

    // Mapa de endpoints y roles requeridos
    // Formato: path -> lista de roles permitidos
    private static final Map<String, List<String>> roleRequirements = new HashMap<>();

    static {
        // Definir requisitos de rol para endpoints específicos
        roleRequirements.put("/api/submission/formatoA", List.of("DOCENTE"));
        roleRequirements.put("/api/submission/anteproyecto", List.of("ESTUDIANTE", "DOCENTE"));

        // Ejemplos adicionales (ajustar según necesidades del sistema)
        // roleRequirements.put("/api/submission/aprobar", List.of("COORDINADOR", "JEFE_DEPARTAMENTO"));
        // roleRequirements.put("/api/notification/broadcast", List.of("COORDINADOR"));
    }

    public RoleFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Si la verificación de roles está desactivada, continuar sin validar
            if (!enforceRoleCheck) {
                log.debug("Verificación de roles desactivada globalmente");
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Verificar si este endpoint tiene requisitos de rol
            List<String> requiredRoles = getRequiredRoles(path);

            if (requiredRoles == null || requiredRoles.isEmpty()) {
                log.debug("No hay requisitos de rol para: {}", path);
                return chain.filter(exchange);
            }

            // Extraer el rol del header X-User-Role (añadido por JwtGatewayFilter)
            String userRole = request.getHeaders().getFirst("X-User-Role");

            if (userRole == null || userRole.isEmpty()) {
                log.warn("Header X-User-Role ausente para ruta protegida: {}", path);
                return forbiddenResponse(exchange, "Role information missing");
            }

            // Verificar si el rol del usuario está en la lista de roles permitidos
            if (!requiredRoles.contains(userRole)) {
                log.warn("Acceso denegado: usuario con rol {} intentó acceder a {} (roles requeridos: {})",
                        userRole, path, requiredRoles);
                return forbiddenResponse(exchange,
                        String.format("Access denied. Required roles: %s", String.join(", ", requiredRoles)));
            }

            log.debug("Verificación de rol exitosa: rol {} autorizado para {}", userRole, path);
            return chain.filter(exchange);
        };
    }

    /**
     * Obtiene los roles requeridos para un path específico.
     *
     * Soporta matching exacto y con wildcards.
     */
    private List<String> getRequiredRoles(String path) {
        // Primero intentar match exacto
        if (roleRequirements.containsKey(path)) {
            return roleRequirements.get(path);
        }

        // Intentar match con prefijos (para paths dinámicos)
        for (Map.Entry<String, List<String>> entry : roleRequirements.entrySet()) {
            String pattern = entry.getKey();

            // Soportar wildcards al final: /api/submission/*
            if (pattern.endsWith("/*") || pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.lastIndexOf('/'));
                if (path.startsWith(prefix)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Genera una respuesta 403 Forbidden con cuerpo JSON.
     */
    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonResponse = String.format(
                "{\"error\":\"Forbidden\",\"message\":\"%s\"}",
                message
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Clase de configuración para el filtro.
     */
    public static class Config {
        // Configuración adicional si es necesaria
    }
}
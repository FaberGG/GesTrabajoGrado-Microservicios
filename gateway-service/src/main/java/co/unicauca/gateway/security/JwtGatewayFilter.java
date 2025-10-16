package co.unicauca.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Filtro de Gateway para validación JWT y extracción de claims.
 *
 * Responsabilidades:
 * 1. Verificar la presencia del header Authorization
 * 2. Validar el token JWT (firma y expiración)
 * 3. Extraer claims del token
 * 4. Añadir headers X-User-* a la petición reenviada
 * 5. Rechazar peticiones con token inválido (401 Unauthorized)
 *
 * Este filtro NO se aplica a rutas públicas definidas en application.yml.
 *
 * Headers añadidos a la petición proxy:
 * - X-User-Id: identificador del usuario
 * - X-User-Role: rol del usuario
 * - X-User-Email: email del usuario (si existe)
 *
 * @author Gateway Team
 */
@Component
public class JwtGatewayFilter extends AbstractGatewayFilterFactory<JwtGatewayFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtGatewayFilter.class);

    private final List<String> publicPaths;
    private final JwtUtils jwtUtils;

    public JwtGatewayFilter(
            JwtUtils jwtUtils,
            @Value("${gateway.security.publicPaths:}") List<String> publicPaths) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
        this.publicPaths = publicPaths != null ? publicPaths : new ArrayList<>();

        log.info("JwtGatewayFilter inicializado con {} rutas públicas", this.publicPaths.size());
        if (!this.publicPaths.isEmpty()) {
            log.debug("Rutas públicas configuradas: {}", this.publicPaths);
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Verificar si la ruta es pública (no requiere JWT)
            if (isPublicPath(path)) {
                log.debug("Ruta pública detectada: {}", path);
                return chain.filter(exchange);
            }

            // Extraer header Authorization
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("Petición sin header Authorization a ruta protegida: {}", path);
                return unauthorizedResponse(exchange, "Token missing");
            }

            // Extraer token del header
            String token = jwtUtils.extractTokenFromHeader(authHeader);

            if (token == null) {
                log.warn("Formato de token inválido en header Authorization");
                return unauthorizedResponse(exchange, "Invalid token format");
            }

            // Validar token
            if (!jwtUtils.validateToken(token)) {
                log.warn("Token JWT inválido o expirado para ruta: {}", path);
                return unauthorizedResponse(exchange, "Token invalid or expired");
            }

            // Extraer claims del token
            Map<String, String> claims = jwtUtils.extractClaims(token);

            if (claims.isEmpty()) {
                log.error("No se pudieron extraer claims del token");
                return unauthorizedResponse(exchange, "Invalid token claims");
            }

            // Crear nueva petición con headers X-User-*
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", claims.getOrDefault("userId", ""))
                    .header("X-User-Role", claims.getOrDefault("role", ""))
                    .header("X-User-Email", claims.getOrDefault("email", ""))
                    .build();

            log.debug("JWT validado correctamente. UserId: {}, Role: {}",
                    claims.get("userId"), claims.get("role"));

            // Continuar con la petición mutada
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    /**
     * Verifica si una ruta es pública (no requiere autenticación).
     *
     * Usa pattern matching con wildcards:
     * - /api/identity/** permite /api/identity/login, /api/identity/register, etc.
     */
    private boolean isPublicPath(String path) {
        if (publicPaths.isEmpty()) {
            log.warn("No hay rutas públicas configuradas. Todas las rutas requerirán autenticación.");
            return false;
        }

        for (String publicPath : publicPaths) {
            String pattern = publicPath.trim();

            // Convertir pattern con ** a regex
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                if (path.startsWith(prefix)) {
                    return true;
                }
            } else if (pattern.equals(path)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Genera una respuesta 401 Unauthorized con cuerpo JSON.
     *
     * Formato: { "error": "Unauthorized", "message": "<detalle>" }
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonResponse = String.format(
                "{\"error\":\"Unauthorized\",\"message\":\"%s\"}",
                message
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Clase de configuración para el filtro (requerida por AbstractGatewayFilterFactory).
     */
    public static class Config {
        // Configuración adicional si es necesaria en el futuro
    }
}
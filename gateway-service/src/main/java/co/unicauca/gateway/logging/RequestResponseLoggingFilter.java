package co.unicauca.gateway.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Filtro para logging estructurado de peticiones y respuestas.
 *
 * Registra información de cada petición que pasa por el gateway:
 * - Timestamp de la petición
 * - Método HTTP (GET, POST, etc.)
 * - Path de la petición
 * - UserId (si está disponible en headers X-User-Id)
 * - IP remota del cliente
 * - Código de estado HTTP de la respuesta
 * - Tiempo de procesamiento en milisegundos
 *
 * NOTA DE SEGURIDAD:
 * - No loguea el body de las peticiones (puede contener información sensible)
 * - No loguea tokens JWT completos
 * - No loguea passwords o datos personales sensibles
 *
 * El formato de log facilita el análisis con herramientas como ELK Stack o Splunk.
 *
 * @author Gateway Team
 */
@Component
public class RequestResponseLoggingFilter
        extends AbstractGatewayFilterFactory<RequestResponseLoggingFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    public RequestResponseLoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Capturar información de la petición
            String method = request.getMethod().name();
            String path = request.getURI().getPath();
            String queryParams = request.getURI().getQuery();
            String remoteAddress = extractRemoteAddress(request);
            long startTime = System.currentTimeMillis();
            Instant timestamp = Instant.now();

            // Log de petición entrante
            logRequest(method, path, queryParams, remoteAddress, timestamp);

            // Continuar con la cadena de filtros y capturar la respuesta
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                long duration = System.currentTimeMillis() - startTime;

                // Extraer userId si está disponible (añadido por JwtGatewayFilter)
                String userId = request.getHeaders().getFirst("X-User-Id");

                // Log de respuesta
                logResponse(method, path, response.getStatusCode().value(),
                        duration, userId, remoteAddress);
            }));
        };
    }

    /**
     * Loguea información de la petición entrante.
     */
    private void logRequest(String method, String path, String queryParams,
                            String remoteAddress, Instant timestamp) {
        String fullPath = queryParams != null ? path + "?" + queryParams : path;

        log.info("INCOMING REQUEST | timestamp={} | method={} | path={} | remoteIP={}",
                timestamp, method, fullPath, remoteAddress);
    }

    /**
     * Loguea información de la respuesta.
     */
    private void logResponse(String method, String path, int statusCode,
                             long duration, String userId, String remoteAddress) {
        String userInfo = userId != null ? "userId=" + userId : "userId=anonymous";

        log.info("OUTGOING RESPONSE | method={} | path={} | status={} | duration={}ms | {} | remoteIP={}",
                method, path, statusCode, duration, userInfo, remoteAddress);
    }

    /**
     * Extrae la dirección IP remota del cliente.
     *
     * Considera headers de proxy (X-Forwarded-For, X-Real-IP) para obtener
     * la IP original del cliente cuando el gateway está detrás de un load balancer.
     */
    private String extractRemoteAddress(ServerHttpRequest request) {
        // Intentar obtener IP real desde headers de proxy
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For puede contener múltiples IPs: "client, proxy1, proxy2"
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fallback a la IP directa
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    /**
     * Clase de configuración para el filtro.
     */
    public static class Config {
        // Configuración adicional si es necesaria (ej: nivel de detalle de logs)
        private boolean logHeaders = false;
        private boolean logQueryParams = true;

        public boolean isLogHeaders() {
            return logHeaders;
        }

        public void setLogHeaders(boolean logHeaders) {
            this.logHeaders = logHeaders;
        }

        public boolean isLogQueryParams() {
            return logQueryParams;
        }

        public void setLogQueryParams(boolean logQueryParams) {
            this.logQueryParams = logQueryParams;
        }
    }
}
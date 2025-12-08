package co.unicauca.gateway.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Manejador global de excepciones para el API Gateway.
 *
 * Captura y procesa todas las excepciones no manejadas que ocurren durante
 * el procesamiento de peticiones en el gateway, incluyendo:
 * - Errores de conexión con servicios backend
 * - Timeouts
 * - Excepciones internas del gateway
 * - Circuit breaker abierto
 *
 * Devuelve respuestas JSON estructuradas con formato:
 * {
 *   "timestamp": "2025-10-15T10:30:00Z",
 *   "status": 500,
 *   "error": "Internal Server Error",
 *   "message": "Descripción del error",
 *   "path": "/api/submission/formatoA"
 * }
 *
 * NOTA: Este handler tiene prioridad alta (@Order(-1)) para capturar
 * excepciones antes que otros handlers por defecto de Spring.
 *
 * @author Gateway Team
 */
@Component
@Order(-1)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Error en gateway: {} | Path: {}",
                ex.getMessage(), exchange.getRequest().getURI().getPath(), ex);

        HttpStatus status;
        String errorMessage;

        // Determinar el código de estado apropiado según el tipo de excepción
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = (HttpStatus) rse.getStatusCode();
            errorMessage = rse.getReason() != null ? rse.getReason() : ex.getMessage();
        } else if (ex instanceof java.net.ConnectException) {
            // Error de conexión con servicio backend
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorMessage = "Service temporarily unavailable. Please try again later.";
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            // Timeout en petición a servicio backend
            status = HttpStatus.GATEWAY_TIMEOUT;
            errorMessage = "Request timeout. The service took too long to respond.";
        } else if (ex.getCause() instanceof java.net.ConnectException) {
            // Error de conexión encapsulado
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorMessage = "Service temporarily unavailable. Please try again later.";
        } else {
            // Error genérico
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorMessage = "An unexpected error occurred. Please try again later.";
        }

        // Construir respuesta JSON
        String jsonResponse = buildErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                errorMessage,
                exchange.getRequest().getURI().getPath()
        );

        // Configurar respuesta
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /**
     * Construye una respuesta de error en formato JSON.
     */
    private String buildErrorResponse(int status, String error, String message, String path) {
        Instant timestamp = Instant.now();

        return String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                timestamp.toString(),
                status,
                escapeJson(error),
                escapeJson(message),
                escapeJson(path)
        );
    }

    /**
     * Escapa caracteres especiales en strings JSON para evitar inyección.
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
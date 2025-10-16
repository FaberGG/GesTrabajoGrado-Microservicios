package co.unicauca.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para endpoints de salud y diagnóstico del gateway.
 *
 * Endpoints disponibles:
 * - GET /api/gateway/health - health check básico
 * - GET /api/gateway/info - información de la aplicación
 *
 * Estos endpoints son públicos (no requieren autenticación) y se usan para:
 * - Verificar que el gateway está activo
 * - Monitoreo de servicios (Kubernetes liveness/readiness probes)
 * - Obtener información de versión y configuración
 *
 * @author Gateway Team
 */
@RestController
@RequestMapping("/api/gateway")
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${info.app.version:1.0.0}")
    private String version;

    @Value("${info.app.description:API Gateway}")
    private String description;

    /**
     * Health check endpoint.
     *
     * Retorna 200 OK si el gateway está funcionando correctamente.
     * Usado por orquestadores (Docker, Kubernetes) para verificar el estado del servicio.
     *
     * Respuesta:
     * {
     *   "status": "UP",
     *   "timestamp": "2025-10-15T10:30:00Z"
     * }
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());

        return Mono.just(ResponseEntity.ok(response));
    }

    /**
     * Info endpoint.
     *
     * Retorna información sobre la aplicación:
     * - Nombre del servicio
     * - Versión
     * - Descripción
     * - Timestamp actual
     *
     * Respuesta:
     * {
     *   "application": "gateway-service",
     *   "version": "1.0.0",
     *   "description": "API Gateway para Sistema de Gestión de Trabajo de Grado",
     *   "timestamp": "2025-10-15T10:30:00Z"
     * }
     */
    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", applicationName);
        response.put("version", version);
        response.put("description", description);
        response.put("timestamp", Instant.now().toString());

        return Mono.just(ResponseEntity.ok(response));
    }

    /**
     * Fallback endpoint para Identity Service.
     *
     * Se activa cuando el circuit breaker del identity service está abierto
     * o cuando el servicio no responde.
     */
    @GetMapping("/fallback/identity")
    public Mono<ResponseEntity<Map<String, Object>>> identityFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Identity service is temporarily unavailable");
        response.put("timestamp", Instant.now().toString());

        return Mono.just(ResponseEntity.status(503).body(response));
    }

    /**
     * Fallback endpoint para Submission Service.
     */
    @GetMapping("/fallback/submission")
    public Mono<ResponseEntity<Map<String, Object>>> submissionFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Submission service is temporarily unavailable");
        response.put("timestamp", Instant.now().toString());

        return Mono.just(ResponseEntity.status(503).body(response));
    }

    /**
     * Fallback endpoint para Notification Service.
     */
    @GetMapping("/fallback/notification")
    public Mono<ResponseEntity<Map<String, Object>>> notificationFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Notification service is temporarily unavailable");
        response.put("timestamp", Instant.now().toString());

        return Mono.just(ResponseEntity.status(503).body(response));
    }
}
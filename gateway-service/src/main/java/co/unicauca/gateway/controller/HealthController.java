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
 * Incluye fallbacks para todos los 5 microservicios del sistema.
 *
 * @author Gateway Team
 * @version 2.0.0
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

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("service", applicationName);

        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", applicationName);
        response.put("version", version);
        response.put("description", description);
        response.put("timestamp", Instant.now().toString());

        Map<String, String> services = new HashMap<>();
        services.put("identity", "Authentication and User Management");
        services.put("submission", "Document Submission Management");
        services.put("notification", "Asynchronous Notifications");
        services.put("review", "Document Review and Evaluation");
        services.put("tracking", "Progress Tracking and Event History");

        response.put("connectedServices", services);

        return Mono.just(ResponseEntity.ok(response));
    }

    // ============================================================
    // FALLBACK ENDPOINTS - Circuit Breaker
    // ============================================================

    @GetMapping("/fallback/identity")
    public Mono<ResponseEntity<Map<String, Object>>> identityFallback() {
        return createFallbackResponse("Identity Service");
    }

    @GetMapping("/fallback/submission")
    public Mono<ResponseEntity<Map<String, Object>>> submissionFallback() {
        return createFallbackResponse("Submission Service");
    }

    @GetMapping("/fallback/notification")
    public Mono<ResponseEntity<Map<String, Object>>> notificationFallback() {
        return createFallbackResponse("Notification Service");
    }

    @GetMapping("/fallback/review")
    public Mono<ResponseEntity<Map<String, Object>>> reviewFallback() {
        return createFallbackResponse("Review Service");
    }

    @GetMapping("/fallback/tracking")
    public Mono<ResponseEntity<Map<String, Object>>> trackingFallback() {
        return createFallbackResponse("Progress Tracking Service");
    }

    @GetMapping("/fallback/generic")
    public Mono<ResponseEntity<Map<String, Object>>> genericFallback() {
        return createFallbackResponse("Backend Service");
    }

    /**
     * Helper para crear respuestas de fallback consistentes
     */
    private Mono<ResponseEntity<Map<String, Object>>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", serviceName + " is temporarily unavailable. Please try again later.");
        response.put("timestamp", Instant.now().toString());
        response.put("suggestion", "Check service health or contact system administrator");

        return Mono.just(ResponseEntity.status(503).body(response));
    }
}
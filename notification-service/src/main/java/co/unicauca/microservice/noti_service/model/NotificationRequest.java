package co.unicauca.microservice.noti_service.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Request para envío de notificaciones.
 * Soporta múltiples destinatarios y contexto de negocio parametrizado.
 */
public record NotificationRequest(
        @NotNull(message = "Notification type is required")
        NotificationType notificationType,

        @NotBlank(message = "Channel is required")
        String channel,  // "email" | "sms"

        @NotEmpty(message = "At least one recipient is required")
        @Valid
        List<Recipient> recipients,

        /**
         * Contexto de negocio con información variable según el tipo de notificación.
         * Ejemplos:
         * - projectId: UUID del proyecto
         * - projectTitle: Título del proyecto
         * - documentType: "FORMATO_A" | "ANTEPROYECTO"
         * - documentVersion: Número de versión
         * - evaluationResult: "APPROVED" | "REJECTED" | "OBSERVATIONS"
         * - observations: Texto de observaciones
         * - submittedBy: Nombre del docente que envió
         * - evaluatedBy: Nombre del coordinador/jefe
         * - evaluatorNames: Lista de nombres de evaluadores
         */
        @NotNull(message = "Business context is required")
        Map<String, Object> businessContext,

        String message,      // Mensaje custom opcional (si no se usa plantilla)
        String templateId,   // ID de plantilla (si es null, se usa el default del tipo)
        boolean forceFail    // Para simular fallos en testing
) {
    /**
     * Constructor convenience para notificaciones simples con un solo destinatario
     */
    public NotificationRequest(
            NotificationType notificationType,
            String channel,
            String recipientEmail,
            Map<String, Object> businessContext
    ) {
        this(
                notificationType,
                channel,
                List.of(new Recipient(recipientEmail)),
                businessContext,
                null,
                null,
                false
        );
    }

    /**
     * Obtiene un valor del contexto de negocio con tipo seguro
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextValue(String key, Class<T> type) {
        Object value = businessContext.get(key);
        if (value == null) return null;
        return (T) value;
    }

    /**
     * Obtiene un valor del contexto o un default si no existe
     */
    public <T> T getContextValueOrDefault(String key, T defaultValue) {
        Object value = businessContext.get(key);
        if (value == null) return defaultValue;
        try {
            @SuppressWarnings("unchecked")
            T typedValue = (T) value;
            return typedValue;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
}
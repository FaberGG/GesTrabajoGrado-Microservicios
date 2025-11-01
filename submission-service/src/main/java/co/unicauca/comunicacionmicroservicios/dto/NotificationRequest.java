package co.unicauca.comunicacionmicroservicios.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Request para envío de notificaciones asíncronas a través de RabbitMQ.
 * Este DTO debe coincidir con el NotificationRequest del notification-service.
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
         * - projectTitle: Título del proyecto
         * - documentType: "FORMATO_A" | "ANTEPROYECTO"
         * - documentVersion: Número de versión
         * - submittedBy: Nombre del que envió
         * - submissionDate: Fecha de envío
         */
        @NotNull(message = "Business context is required")
        Map<String, Object> businessContext,

        String message,      // Mensaje custom opcional
        String templateId,   // ID de plantilla
        boolean forceFail    // Para testing
) {
    /**
     * Constructor convenience para notificaciones simples
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
}


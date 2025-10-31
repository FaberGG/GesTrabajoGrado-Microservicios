package co.unicauca.microservice.noti_service.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta del envío de notificación con información detallada.
 */
public record NotificationResponse(
        UUID id,
        NotificationType notificationType,
        String status,  // SENT, PARTIALLY_SENT, FAILED, QUEUED
        String correlationId,
        int recipientCount,
        List<String> failedRecipients,
        LocalDateTime timestamp
) {
    /**
     * Constructor convenience para respuestas simples exitosas
     */
    public NotificationResponse(UUID id, String status, String correlationId) {
        this(
                id,
                null,
                status,
                correlationId,
                1,
                List.of(),
                LocalDateTime.now()
        );
    }

    /**
     * Verifica si la notificación fue completamente exitosa
     */
    public boolean isSuccess() {
        return "SENT".equals(status) && (failedRecipients == null || failedRecipients.isEmpty());
    }

    /**
     * Verifica si hubo fallos parciales
     */
    public boolean isPartialFailure() {
        return "PARTIALLY_SENT".equals(status) ||
                (failedRecipients != null && !failedRecipients.isEmpty());
    }
}
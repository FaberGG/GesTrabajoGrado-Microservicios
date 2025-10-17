package co.unicauca.microservice.noti_service.services;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio dedicado para logging estructurado de notificaciones.
 * Genera logs en formato JSON para mejor trazabilidad y consulta.
 */
@Service
public class NotificationLogger {
    private static final Logger log = LoggerFactory.getLogger("NOTIFICATION_LOGGER");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Registra una notificación enviada (síncrona o asíncrona)
     */
    public void logNotificationSent(String channel, String recipient, String message,
                                     String correlationId, boolean isAsync, String status) {
        log.info("Notification sent",
                StructuredArguments.kv("event", "NOTIFICATION_SENT"),
                StructuredArguments.kv("timestamp", LocalDateTime.now().format(formatter)),
                StructuredArguments.kv("type", channel.toUpperCase()),
                StructuredArguments.kv("recipient", recipient),
                StructuredArguments.kv("message", message),
                StructuredArguments.kv("correlationId", correlationId),
                StructuredArguments.kv("mode", isAsync ? "ASYNC" : "SYNC"),
                StructuredArguments.kv("status", status)
        );
    }

    /**
     * Registra la publicación de una notificación en la cola
     */
    public void logNotificationPublished(String correlationId, String channel, String recipient) {
        log.info("Notification published",
                StructuredArguments.kv("event", "NOTIFICATION_PUBLISHED"),
                StructuredArguments.kv("timestamp", LocalDateTime.now().format(formatter)),
                StructuredArguments.kv("correlationId", correlationId),
                StructuredArguments.kv("type", channel.toUpperCase()),
                StructuredArguments.kv("recipient", recipient),
                StructuredArguments.kv("mode", "ASYNC"),
                StructuredArguments.kv("status", "QUEUED")
        );
    }

    /**
     * Registra un error en el envío de notificación
     */
    public void logNotificationError(String correlationId, String errorMessage,
                                      boolean isAsync, String channel) {
        log.error("Notification error",
                StructuredArguments.kv("event", "NOTIFICATION_ERROR"),
                StructuredArguments.kv("timestamp", LocalDateTime.now().format(formatter)),
                StructuredArguments.kv("correlationId", correlationId),
                StructuredArguments.kv("error", errorMessage),
                StructuredArguments.kv("mode", isAsync ? "ASYNC" : "SYNC"),
                StructuredArguments.kv("type", channel != null ? channel.toUpperCase() : "UNKNOWN"),
                StructuredArguments.kv("status", "FAILED")
        );
    }

    /**
     * Registra el inicio del procesamiento de una notificación
     */
    public void logNotificationProcessing(String correlationId, String channel,
                                          String recipient, boolean isAsync) {
        log.info("Notification processing",
                StructuredArguments.kv("event", "NOTIFICATION_PROCESSING"),
                StructuredArguments.kv("timestamp", LocalDateTime.now().format(formatter)),
                StructuredArguments.kv("correlationId", correlationId),
                StructuredArguments.kv("type", channel.toUpperCase()),
                StructuredArguments.kv("recipient", recipient),
                StructuredArguments.kv("mode", isAsync ? "ASYNC" : "SYNC"),
                StructuredArguments.kv("status", "PROCESSING")
        );
    }
}

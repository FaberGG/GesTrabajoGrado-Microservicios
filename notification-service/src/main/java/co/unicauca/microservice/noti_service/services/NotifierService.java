package co.unicauca.microservice.noti_service.services;

import co.unicauca.microservice.noti_service.model.NotificationRequest;
import co.unicauca.microservice.noti_service.model.NotificationResponse;
import co.unicauca.microservice.noti_service.rabbit.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotifierService {
    private static final Logger log = LoggerFactory.getLogger(NotifierService.class);
    private final RabbitTemplate rabbitTemplate;
    private final NotificationLogger notificationLogger;

    public NotifierService(RabbitTemplate rabbitTemplate, NotificationLogger notificationLogger) {
        this.rabbitTemplate = rabbitTemplate;
        this.notificationLogger = notificationLogger;
    }

    public NotificationResponse sendSync(NotificationRequest request) {
        String correlationId = MDC.get("correlationId");

        try {
            // Log inicio del procesamiento
            notificationLogger.logNotificationProcessing(correlationId, request.channel(), request.to(), false);

            if (request.forceFail()) {
                throw new RuntimeException("Forced failure in synchronous send");
            }

            // Simular envío según el canal
            simulateNotificationSend(request, correlationId, false);

            // Log de éxito
            notificationLogger.logNotificationSent(
                request.channel(),
                request.to(),
                request.message(),
                correlationId,
                false,
                "SENT"
            );

            return new NotificationResponse(UUID.randomUUID(), "SENT", correlationId);

        } catch (Exception e) {
            // Log de error
            notificationLogger.logNotificationError(correlationId, e.getMessage(), false, request.channel());
            throw e;
        }
    }

    public void publishAsync(NotificationRequest request, String correlationId) {
        try {
            MessagePostProcessor processor = msg -> {
                msg.getMessageProperties().setHeader(RabbitConfig.HEADER_CORRELATION, correlationId);
                return msg;
            };

            rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATIONS_QUEUE, request, processor);

            // Log de publicación en la cola
            notificationLogger.logNotificationPublished(correlationId, request.channel(), request.to());

        } catch (Exception e) {
            notificationLogger.logNotificationError(correlationId, e.getMessage(), true, request.channel());
            throw e;
        }
    }

    public void send(NotificationRequest request, String correlationId) {
        try {
            // Log inicio del procesamiento asíncrono
            notificationLogger.logNotificationProcessing(correlationId, request.channel(), request.to(), true);

            if (request.forceFail()) {
                throw new RuntimeException("Forced failure for async processing");
            }

            // Simular envío según el canal
            simulateNotificationSend(request, correlationId, true);

            // Log de éxito
            notificationLogger.logNotificationSent(
                request.channel(),
                request.to(),
                request.message(),
                correlationId,
                true,
                "SENT"
            );

        } catch (Exception e) {
            notificationLogger.logNotificationError(correlationId, e.getMessage(), true, request.channel());
            throw e;
        }
    }

    /**
     * Simula el envío de notificación según el canal (para propósitos de demo/mock)
     */
    private void simulateNotificationSend(NotificationRequest request, String correlationId, boolean isAsync) {
        String prefix = isAsync ? "ASYNC" : "SYNC";

        if ("email".equalsIgnoreCase(request.channel())) {
            log.info("📧 [EMAIL MOCK {}] Enviando correo a: {}", prefix, request.to());
            log.info("   Asunto: Notificación del Sistema");
            log.info("   Mensaje: {}", request.message());
            log.info("   CorrelationId: {}", correlationId);
        } else if ("sms".equalsIgnoreCase(request.channel())) {
            log.info("📱 [SMS MOCK {}] Enviando SMS a: {}", prefix, request.to());
            log.info("   Mensaje: {}", request.message());
            log.info("   CorrelationId: {}", correlationId);
        } else {
            log.info("🔔 [NOTIFICATION MOCK {}] Canal: {} - Destinatario: {}",
                    prefix, request.channel(), request.to());
            log.info("   Mensaje: {}", request.message());
            log.info("   CorrelationId: {}", correlationId);
        }
    }
}
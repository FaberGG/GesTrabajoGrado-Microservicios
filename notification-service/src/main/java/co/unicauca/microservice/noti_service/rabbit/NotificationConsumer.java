package co.unicauca.microservice.noti_service.rabbit;

import co.unicauca.microservice.noti_service.model.NotificationRequest;
import co.unicauca.microservice.noti_service.services.notifier.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumidor de mensajes de RabbitMQ para notificaciones asíncronas.
 * Procesa notificaciones de la cola con reintentos automáticos.
 */
@Component
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final Notifier notifier;
    private final RabbitTemplate rabbitTemplate;

    public NotificationConsumer(Notifier notifier, RabbitTemplate rabbitTemplate) {
        this.notifier = notifier;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Procesa mensajes de la cola de notificaciones.
     * Implementa lógica de reintentos con exponential backoff.
     */
    @RabbitListener(queues = RabbitConfig.NOTIFICATIONS_QUEUE)
    public void onMessage(
            NotificationRequest request,
            @Header(name = RabbitConfig.HEADER_CORRELATION, required = false) String correlationId,
            @Header(name = RabbitConfig.HEADER_RETRIES, required = false) Integer retries
    ) {
        int attempt = (retries == null) ? 0 : retries;

        log.debug("Processing notification, attempt {}, correlationId: {}", attempt + 1, correlationId);

        try {
            notifier.send(request, correlationId);
            log.debug("Notification processed successfully, correlationId: {}", correlationId);

        } catch (RuntimeException ex) {
            log.warn("Error sending notification, attempt {}, correlationId: {}",
                    attempt + 1, correlationId, ex);
            retryOrDeadLetter(request, correlationId, attempt);
        }
    }

    /**
     * Maneja reintentos o envío a Dead Letter Queue.
     * Estrategia: 1 reintento con delay de 5 segundos, luego DLQ.
     */
    private void retryOrDeadLetter(NotificationRequest request, String correlationId, int attempt) {
        if (attempt < 1) {
            // Reintento
            log.info("Scheduling retry for notification, correlationId: {}", correlationId);

            MessagePostProcessor retryMsg = msg -> {
                msg.getMessageProperties().setHeader(RabbitConfig.HEADER_RETRIES, attempt + 1);
                msg.getMessageProperties().setHeader(RabbitConfig.HEADER_CORRELATION, correlationId);
                msg.getMessageProperties().setExpiration(String.valueOf(RabbitConfig.RETRY_TTL_MS));
                return msg;
            };

            rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATIONS_RETRY_QUEUE, request, retryMsg);

        } else {
            // Dead Letter Queue
            log.error("Max retries exceeded, sending to DLQ, correlationId: {}", correlationId);

            MessagePostProcessor dlqMsg = msg -> {
                msg.getMessageProperties().setHeader(RabbitConfig.HEADER_RETRIES, attempt);
                msg.getMessageProperties().setHeader(RabbitConfig.HEADER_CORRELATION, correlationId);
                return msg;
            };

            rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATIONS_DLQ, request, dlqMsg);
        }
    }
}
package co.unicauca.microservice.noti_service.rabbit;

import co.unicauca.microservice.noti_service.model.NotificationRequest;
import co.unicauca.microservice.noti_service.services.NotifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotifierService notifierService;
    private final RabbitTemplate rabbitTemplate;

    public NotificationConsumer(NotifierService notifierService, RabbitTemplate rabbitTemplate) {
        this.notifierService = notifierService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATIONS_QUEUE)
    public void onMessage(
            NotificationRequest request,
            @Header(name = RabbitConfig.HEADER_CORRELATION, required = false) String correlationId,
            @Header(name = RabbitConfig.HEADER_RETRIES, required = false) Integer retries
    ) {
        int attempt = (retries == null) ? 0 : retries;
        try {
            notifierService.send(request, correlationId);
        } catch (RuntimeException ex) {
            log.warn("Error sending notification, attempt {}", attempt + 1);
            retryOrDeadLetter(request, correlationId, attempt);
        }
    }

    private void retryOrDeadLetter(NotificationRequest request, String correlationId, int attempt) {
        if (attempt < 1) {
            MessagePostProcessor retryMsg = msg -> {
                msg.getMessageProperties().setHeader(RabbitConfig.HEADER_RETRIES, attempt + 1);
                msg.getMessageProperties().setHeader(RabbitConfig.HEADER_CORRELATION, correlationId);
                msg.getMessageProperties().setExpiration(String.valueOf(RabbitConfig.RETRY_TTL_MS));
                return msg;
            };
            rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATIONS_RETRY_QUEUE, request, retryMsg);
        } else {
            MessagePostProcessor dlqMsg = msg -> {
                msg.getMessageProperties().setHeader(RabbitConfig.HEADER_RETRIES, attempt);
                msg.getMessageProperties().setHeader(RabbitConfig.HEADER_CORRELATION, correlationId);
                return msg;
            };
            rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATIONS_DLQ, request, dlqMsg);
        }
    }
}

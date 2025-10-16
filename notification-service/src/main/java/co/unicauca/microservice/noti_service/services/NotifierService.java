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

    public NotifierService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public NotificationResponse sendSync(NotificationRequest request) {
        if (request.forceFail()) {
            throw new RuntimeException("Forced failure in synchronous send");
        }

        String correlationId = MDC.get("correlationId");
        log.info("Sending synchronous notification to {} via {} with correlationId={}",
                request.to(), request.channel(), correlationId);

        return new NotificationResponse(UUID.randomUUID(), "SENT", correlationId);
    }

    public void publishAsync(NotificationRequest request, String correlationId) {
        MessagePostProcessor processor = msg -> {
            msg.getMessageProperties().setHeader(RabbitConfig.HEADER_CORRELATION, correlationId);
            return msg;
        };
        rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATIONS_QUEUE, request, processor);
        log.info("Published async notification to queue with correlationId={}", correlationId);
    }

    public void send(NotificationRequest request, String correlationId) {
        if (request.forceFail()) {
            throw new RuntimeException("Forced failure for async processing");
        }
        log.info("Sending async notification to {} via {} with correlationId={}",
                request.to(), request.channel(), correlationId);
    }
}

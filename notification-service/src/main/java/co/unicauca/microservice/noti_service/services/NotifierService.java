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

        // Simular envío según el canal
        if ("email".equalsIgnoreCase(request.channel())) {
            log.info("📧 [EMAIL MOCK] Enviando correo a: {}", request.to());
            log.info("   Asunto: Notificación del Sistema");
            log.info("   Mensaje: {}", request.message());
            log.info("   CorrelationId: {}", correlationId);
        } else if ("sms".equalsIgnoreCase(request.channel())) {
            log.info("📱 [SMS MOCK] Enviando SMS a: {}", request.to());
            log.info("   Mensaje: {}", request.message());
            log.info("   CorrelationId: {}", correlationId);
        } else {
            log.info("🔔 [NOTIFICATION MOCK] Canal: {} - Destinatario: {}",
                    request.channel(), request.to());
            log.info("   Mensaje: {}", request.message());
            log.info("   CorrelationId: {}", correlationId);
        }

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

        // Simular envío según el canal
        if ("email".equalsIgnoreCase(request.channel())) {
            log.info("📧 [EMAIL MOCK ASYNC] Enviando correo a: {}", request.to());
            log.info("   Asunto: Notificación del Sistema");
            log.info("   Mensaje: {}", request.message());
            log.info("   CorrelationId: {}", correlationId);
        } else if ("sms".equalsIgnoreCase(request.channel())) {
            log.info("📱 [SMS MOCK ASYNC] Enviando SMS a: {}", request.to());
            log.info("   Mensaje: {}", request.message());
            log.info("   CorrelationId: {}", correlationId);
        } else {
            log.info("🔔 [NOTIFICATION MOCK ASYNC] Canal: {} - Destinatario: {}",
                    request.channel(), request.to());
            log.info("   Mensaje: {}", request.message());
            log.info("   CorrelationId: {}", correlationId);
        }
    }
}
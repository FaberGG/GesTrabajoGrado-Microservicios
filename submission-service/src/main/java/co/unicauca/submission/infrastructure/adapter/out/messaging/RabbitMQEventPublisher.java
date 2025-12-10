package co.unicauca.submission.infrastructure.adapter.out.messaging;

import co.unicauca.submission.application.port.out.IEventPublisherPort;
import co.unicauca.submission.domain.event.DomainEvent;
import co.unicauca.submission.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador para publicar eventos de dominio a RabbitMQ.
 * Implementa el puerto IEventPublisherPort.
 */
@Component
public class RabbitMQEventPublisher implements IEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            String exchange = determinarExchange(event);
            String routingKey = determinarRoutingKey(event);

            log.info("📤 Publicando evento: {} → Exchange: {}, Routing Key: {}",
                     event.getEventType(), exchange, routingKey);

            rabbitTemplate.convertAndSend(exchange, routingKey, event);

            log.info("✅ Evento publicado exitosamente: {} → {}", event.getEventType(), routingKey);

        } catch (Exception e) {
            log.error("❌ Error al publicar evento {}: {}", event.getEventType(), e.getMessage(), e);
            // No lanzamos excepción para no afectar la transacción principal
        }
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            log.debug("No hay eventos para publicar");
            return;
        }

        log.debug("Publicando {} eventos", events.size());

        events.forEach(this::publish);
    }

    /**
     * Determina el exchange según el tipo de evento.
     */
    private String determinarExchange(DomainEvent event) {
        String eventType = event.getEventType();

        return switch (eventType) {
            case "FormatoACreado", "FormatoAReenviado", "FormatoAEvaluado" ->
                RabbitMQConfig.FORMATO_A_EXCHANGE;
            case "AnteproyectoSubido", "EvaluadoresAsignados", "AnteproyectoEvaluado" ->
                RabbitMQConfig.ANTEPROYECTO_EXCHANGE;
            default -> {
                log.warn("Tipo de evento desconocido: {}, usando exchange por defecto", eventType);
                yield RabbitMQConfig.FORMATO_A_EXCHANGE;
            }
        };
    }

    /**
     * Determina el routing key según el tipo de evento.
     */
    private String determinarRoutingKey(DomainEvent event) {
        String eventType = event.getEventType();

        return switch (eventType) {
            case "FormatoACreado" -> RabbitMQConfig.FORMATO_A_ENVIADO_KEY;
            case "FormatoAReenviado" -> RabbitMQConfig.FORMATO_A_REENVIADO_KEY;
            case "AnteproyectoSubido" -> RabbitMQConfig.ANTEPROYECTO_ENVIADO_KEY;
            case "FormatoAEvaluado" -> "formatoa.evaluado";  // Para review-service
            case "EvaluadoresAsignados" -> "evaluadores.asignados";  // Para review-service
            case "AnteproyectoEvaluado" -> "anteproyecto.evaluado";  // Para review-service
            default -> {
                log.warn("Tipo de evento desconocido: {}", eventType);
                yield "unknown";
            }
        };
    }
}


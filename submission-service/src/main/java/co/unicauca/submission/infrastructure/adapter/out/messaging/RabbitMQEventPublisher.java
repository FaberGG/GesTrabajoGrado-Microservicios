package co.unicauca.submission.infrastructure.adapter.out.messaging;

import co.unicauca.submission.application.port.out.IEventPublisherPort;
import co.unicauca.submission.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador para publicar eventos de dominio a RabbitMQ.
 * Implementa el puerto IEventPublisherPort.
 */
@Component
@Profile("!local")
public class RabbitMQEventPublisher implements IEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventPublisher.class);

    private static final String EXCHANGE = "progress.exchange";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            String routingKey = determinarRoutingKey(event);

            log.debug("Publicando evento: {} con routing key: {}",
                     event.getEventType(), routingKey);

            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);

            log.info("Evento publicado exitosamente: {}", event.getEventType());

        } catch (Exception e) {
            log.error("Error al publicar evento {}: {}", event.getEventType(), e.getMessage(), e);
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
     * Determina el routing key según el tipo de evento.
     */
    private String determinarRoutingKey(DomainEvent event) {
        String eventType = event.getEventType();

        return switch (eventType) {
            case "FormatoACreado" -> "progress.formatoA.creado";
            case "FormatoAEvaluado" -> "progress.formatoA.evaluado";
            case "FormatoAReenviado" -> "progress.formatoA.reenviado";
            case "AnteproyectoSubido" -> "progress.anteproyecto.subido";
            case "EvaluadoresAsignados" -> "progress.anteproyecto.evaluadores.asignados";
            case "AnteproyectoEvaluado" -> "progress.anteproyecto.evaluado";
            default -> "progress.unknown";
        };
    }
}


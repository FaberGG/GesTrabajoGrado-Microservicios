package co.unicauca.submission.infrastructure.adapter.out.messaging;

import co.unicauca.submission.application.port.out.IEventPublisherPort;
import co.unicauca.submission.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador MOCK para eventos - Solo para desarrollo local.
 */
@Component
@Profile("local")
public class MockEventPublisher implements IEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(MockEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        log.info("📤 [MOCK] Evento: {} - AggregateID: {}", event.getEventType(), event.getAggregateId());
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        if (events != null) {
            events.forEach(this::publish);
        }
    }
}


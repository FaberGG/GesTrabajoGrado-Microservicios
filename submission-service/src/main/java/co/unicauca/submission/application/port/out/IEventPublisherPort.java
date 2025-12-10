package co.unicauca.submission.application.port.out;

import co.unicauca.submission.domain.event.DomainEvent;

import java.util.List;

/**
 * Puerto de salida para publicación de eventos de dominio.
 */
public interface IEventPublisherPort {

    /**
     * Publica un evento de dominio de forma asíncrona.
     */
    void publish(DomainEvent event);

    /**
     * Publica múltiples eventos en batch.
     */
    void publishAll(List<DomainEvent> events);
}


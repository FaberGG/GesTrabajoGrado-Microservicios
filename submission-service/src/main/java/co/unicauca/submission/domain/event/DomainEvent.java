package co.unicauca.submission.domain.event;

import java.time.LocalDateTime;

/**
 * Interfaz base para todos los eventos de dominio.
 * Los eventos son inmutables y representan algo que ya ocurrió.
 */
public interface DomainEvent {

    /**
     * ID del aggregate que generó el evento.
     */
    Long getAggregateId();

    /**
     * Timestamp de cuando ocurrió el evento.
     */
    LocalDateTime getOccurredOn();

    /**
     * Tipo de evento (nombre de la clase).
     */
    default String getEventType() {
        return this.getClass().getSimpleName();
    }
}


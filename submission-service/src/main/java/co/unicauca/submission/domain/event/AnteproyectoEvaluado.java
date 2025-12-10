package co.unicauca.submission.domain.event;

import java.time.LocalDateTime;

/**
 * Evento: El anteproyecto ha sido evaluado (aprobado o rechazado).
 */
public class AnteproyectoEvaluado implements DomainEvent {

    private final Long proyectoId;
    private final boolean aprobado;
    private final String comentarios;
    private final LocalDateTime occurredOn;

    public AnteproyectoEvaluado(Long proyectoId, boolean aprobado, String comentarios) {
        this.proyectoId = proyectoId;
        this.aprobado = aprobado;
        this.comentarios = comentarios;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public Long getAggregateId() {
        return proyectoId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    // Getters
    public Long getProyectoId() { return proyectoId; }
    public boolean isAprobado() { return aprobado; }
    public String getComentarios() { return comentarios; }
}


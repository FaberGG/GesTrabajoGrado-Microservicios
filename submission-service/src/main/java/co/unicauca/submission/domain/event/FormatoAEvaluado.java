package co.unicauca.submission.domain.event;

import java.time.LocalDateTime;

/**
 * Evento: El Formato A ha sido evaluado (aprobado o rechazado).
 */
public class FormatoAEvaluado implements DomainEvent {

    private final Long proyectoId;
    private final boolean aprobado;
    private final String comentarios;
    private final Long evaluadorId;
    private final int version;
    private final LocalDateTime occurredOn;

    public FormatoAEvaluado(Long proyectoId, boolean aprobado, String comentarios,
                           Long evaluadorId, int version) {
        this.proyectoId = proyectoId;
        this.aprobado = aprobado;
        this.comentarios = comentarios;
        this.evaluadorId = evaluadorId;
        this.version = version;
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
    public Long getEvaluadorId() { return evaluadorId; }
    public int getVersion() { return version; }
}


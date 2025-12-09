package co.unicauca.submission.domain.event;

import java.time.LocalDateTime;

/**
 * Evento: Se ha reenviado una nueva versión del Formato A.
 */
public class FormatoAReenviado implements DomainEvent {

    private final Long proyectoId;
    private final String titulo;
    private final int version;
    private final LocalDateTime occurredOn;

    public FormatoAReenviado(Long proyectoId, String titulo, int version) {
        this.proyectoId = proyectoId;
        this.titulo = titulo;
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
    public String getTitulo() { return titulo; }
    public int getVersion() { return version; }
}


package co.unicauca.submission.domain.event;

import java.time.LocalDateTime;

/**
 * Evento: Un nuevo Formato A ha sido creado.
 */
public class FormatoACreado implements DomainEvent {

    private final Long proyectoId;
    private final String titulo;
    private final String modalidad;
    private final Long directorId;
    private final int version;
    private final LocalDateTime occurredOn;

    public FormatoACreado(Long proyectoId, String titulo, String modalidad,
                         Long directorId, int version) {
        this.proyectoId = proyectoId;
        this.titulo = titulo;
        this.modalidad = modalidad;
        this.directorId = directorId;
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
    public String getModalidad() { return modalidad; }
    public Long getDirectorId() { return directorId; }
    public int getVersion() { return version; }
}


package co.unicauca.submission.domain.event;

import java.time.LocalDateTime;

/**
 * Evento: Se ha subido un anteproyecto.
 */
public class AnteproyectoSubido implements DomainEvent {

    private final Long proyectoId;
    private final String titulo;
    private final String rutaArchivo;
    private final LocalDateTime occurredOn;

    public AnteproyectoSubido(Long proyectoId, String titulo, String rutaArchivo) {
        this.proyectoId = proyectoId;
        this.titulo = titulo;
        this.rutaArchivo = rutaArchivo;
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
    public String getRutaArchivo() { return rutaArchivo; }
}


package co.unicauca.submission.domain.event;

import java.time.LocalDateTime;

/**
 * Evento: Se han asignado evaluadores al anteproyecto.
 */
public class EvaluadoresAsignados implements DomainEvent {

    private final Long proyectoId;
    private final Long evaluador1Id;
    private final Long evaluador2Id;
    private final LocalDateTime occurredOn;

    public EvaluadoresAsignados(Long proyectoId, Long evaluador1Id, Long evaluador2Id) {
        this.proyectoId = proyectoId;
        this.evaluador1Id = evaluador1Id;
        this.evaluador2Id = evaluador2Id;
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
    public Long getEvaluador1Id() { return evaluador1Id; }
    public Long getEvaluador2Id() { return evaluador2Id; }
}


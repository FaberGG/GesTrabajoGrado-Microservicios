package co.unicauca.review.dto.response;

import co.unicauca.review.enums.AsignacionEstado;
import co.unicauca.review.enums.Decision;

import java.time.LocalDateTime;

public record AsignacionDTO(
    Long asignacionId,
    Long anteproyectoId,
    String tituloAnteproyecto,
    EvaluadorInfoDTO evaluador1,
    EvaluadorInfoDTO evaluador2,
    AsignacionEstado estado,
    LocalDateTime fechaAsignacion,
    LocalDateTime fechaCompletado,
    Decision finalDecision
) {}


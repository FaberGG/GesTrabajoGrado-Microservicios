package co.unicauca.review.dto.request;

import co.unicauca.review.enums.Decision;
import co.unicauca.review.enums.EvaluatorRole;
import jakarta.validation.constraints.NotNull;

public record EvaluationRequestDTO(
    @NotNull(message = "El ID del documento es obligatorio")
    Long documentId,

    @NotNull(message = "La decisión es obligatoria")
    Decision decision,

    String observaciones,

    @NotNull(message = "El ID del evaluador es obligatorio")
    Long evaluatorId,

    @NotNull(message = "El rol del evaluador es obligatorio")
    EvaluatorRole evaluatorRole
) {}


package co.unicauca.review.dto.response;

import co.unicauca.review.enums.Decision;
import co.unicauca.review.enums.DocumentType;

import java.time.LocalDateTime;

public record EvaluationResultDTO(
    Long evaluationId,
    Long documentId,
    DocumentType documentType,
    Decision decision,
    String observaciones,
    LocalDateTime fechaEvaluacion,
    boolean notificacionEnviada
) {}


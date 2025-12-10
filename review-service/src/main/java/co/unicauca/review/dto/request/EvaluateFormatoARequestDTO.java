package co.unicauca.review.dto.request;

import co.unicauca.review.enums.Decision;
import jakarta.validation.constraints.NotNull;

/**
 * DTO simplificado para evaluar Formato A.
 * Solo contiene los campos que van en el body JSON.
 * El documentId viene del path y userId/userRole de los headers.
 */
public record EvaluateFormatoARequestDTO(
    @NotNull(message = "La decisión es obligatoria")
    Decision decision,

    String observaciones
) {}


package co.unicauca.review.dto.response;

import co.unicauca.review.enums.Decision;

public record EvaluadorInfoDTO(
    Long id,
    String nombre,
    String email,
    Decision decision,
    String observaciones
) {}


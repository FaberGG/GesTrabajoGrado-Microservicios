package co.unicauca.review.dto.request;

import jakarta.validation.constraints.NotNull;

public record AsignacionRequestDTO(
    @NotNull(message = "El ID del anteproyecto es obligatorio")
    Long anteproyectoId,

    @NotNull(message = "El ID del evaluador 1 es obligatorio")
    Long evaluador1Id,

    @NotNull(message = "El ID del evaluador 2 es obligatorio")
    Long evaluador2Id
) {
    // Validación custom en constructor compacto
    public AsignacionRequestDTO {
        if (evaluador1Id != null && evaluador2Id != null && evaluador1Id.equals(evaluador2Id)) {
            throw new IllegalArgumentException("Los evaluadores deben ser diferentes");
        }
    }
}

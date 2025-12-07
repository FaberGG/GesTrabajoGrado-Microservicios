package co.unicauca.comunicacionmicroservicios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Datos JSON para subir el anteproyecto (RF6). */
@Schema(description = "Datos para subir un anteproyecto")
public class AnteproyectoData {

    @Schema(description = "ID del proyecto de grado asociado", example = "1", required = true)
    @NotNull(message = "El ID del proyecto es obligatorio")
    private Long proyectoId;

    public Long getProyectoId() { return proyectoId; }
    public void setProyectoId(Long proyectoId) { this.proyectoId = proyectoId; }
}

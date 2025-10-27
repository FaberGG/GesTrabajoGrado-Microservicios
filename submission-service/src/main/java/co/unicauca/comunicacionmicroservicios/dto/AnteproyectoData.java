package co.unicauca.comunicacionmicroservicios.dto;

import jakarta.validation.constraints.NotNull;

/** Datos JSON para subir el anteproyecto (RF6). */
public class AnteproyectoData {

    @NotNull
    private Long proyectoId;

    public Long getProyectoId() { return proyectoId; }
    public void setProyectoId(Long proyectoId) { this.proyectoId = proyectoId; }
}

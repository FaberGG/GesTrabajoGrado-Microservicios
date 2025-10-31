package co.unicauca.comunicacionmicroservicios.dto;

import co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoFormato;
import jakarta.validation.constraints.*;

/** Cambiar estado de una versión de Formato A: APROBADO o RECHAZADO. */
public class EvaluacionRequest {

    @NotNull
    private enumEstadoFormato estado; // APROBADO | RECHAZADO

    @Size(max = 2000)
    private String observaciones;

    @NotNull
    private Integer evaluadoPor; // coordinadorId

    public enumEstadoFormato getEstado() { return estado; }
    public void setEstado(enumEstadoFormato estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Integer getEvaluadoPor() { return evaluadoPor; }
    public void setEvaluadoPor(Integer evaluadoPor) { this.evaluadoPor = evaluadoPor; }
}

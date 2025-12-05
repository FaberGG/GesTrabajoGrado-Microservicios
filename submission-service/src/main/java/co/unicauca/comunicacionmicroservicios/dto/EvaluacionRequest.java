package co.unicauca.comunicacionmicroservicios.dto;

import co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoFormato;
import jakarta.validation.constraints.*;

/** Cambiar estado de una versión de Formato A: APROBADO o RECHAZADO. */
public class EvaluacionRequest {

    @NotNull
    @Pattern(regexp = "APROBADO|RECHAZADO", message = "El estado debe ser APROBADO o RECHAZADO")
    private String estado; // "APROBADO" | "RECHAZADO" - Cambiado a String para compatibilidad

    @Size(max = 2000)
    private String observaciones;

    @NotNull
    private Long evaluadoPor; // coordinadorId - Cambiado a Long para compatibilidad

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Convierte el estado String a enum para uso interno
     */
    public enumEstadoFormato getEstadoAsEnum() {
        if (estado == null) return null;
        return enumEstadoFormato.valueOf(estado.toUpperCase());
    }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Long getEvaluadoPor() { return evaluadoPor; }
    public void setEvaluadoPor(Long evaluadoPor) { this.evaluadoPor = evaluadoPor; }
}

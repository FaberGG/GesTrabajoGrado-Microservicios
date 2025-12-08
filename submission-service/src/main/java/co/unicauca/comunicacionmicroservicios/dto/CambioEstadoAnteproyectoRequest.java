package co.unicauca.comunicacionmicroservicios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cambiar estado del anteproyecto (si lo maneja Review/Jefe). */
public class CambioEstadoAnteproyectoRequest {

    @NotBlank
    private String estado; // usa un enum tuyo si ya lo tienes

    @Size(max = 2000)
    private String observaciones;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}

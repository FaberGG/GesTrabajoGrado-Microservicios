package co.unicauca.review.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para enviar la evaluación al submission-service.
 * IMPORTANTE: El campo 'estado' debe enviarse como String con los valores:
 * "APROBADO" o "RECHAZADO" que el Submission Service convertirá a su enum.
 */
public class EvaluacionRequest {

    @JsonProperty("estado")
    private String estado; // "APROBADO" o "RECHAZADO"

    @JsonProperty("observaciones")
    private String observaciones;

    @JsonProperty("evaluadoPor")
    private Long evaluadoPor; // Cambio de Integer a Long para consistencia

    public EvaluacionRequest() {}

    public EvaluacionRequest(String estado, String observaciones, Long evaluadoPor) {
        this.estado = estado;
        this.observaciones = observaciones;
        this.evaluadoPor = evaluadoPor;
    }

    // Getters y Setters
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getEvaluadoPor() {
        return evaluadoPor;
    }

    public void setEvaluadoPor(Long evaluadoPor) {
        this.evaluadoPor = evaluadoPor;
    }

    @Override
    public String toString() {
        return "EvaluacionRequest{" +
                "estado='" + estado + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", evaluadoPor=" + evaluadoPor +
                '}';
    }
}

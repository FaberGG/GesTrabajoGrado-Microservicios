package co.unicauca.review.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para enviar la evaluación al submission-service.
 * Debe coincidir exactamente con EvaluacionRequest del submission-service.
 */
public class EvaluacionRequest {

    @JsonProperty("estado")
    private String estado; // "APROBADO" o "RECHAZADO" - será convertido al enum en el submission-service

    @JsonProperty("observaciones")
    private String observaciones;

    @JsonProperty("evaluadoPor")
    private Integer evaluadoPor;

    public EvaluacionRequest() {}

    public EvaluacionRequest(String estado, String observaciones, Integer evaluadoPor) {
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

    public Integer getEvaluadoPor() {
        return evaluadoPor;
    }

    public void setEvaluadoPor(Integer evaluadoPor) {
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


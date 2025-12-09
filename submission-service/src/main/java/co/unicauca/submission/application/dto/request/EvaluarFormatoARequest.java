package co.unicauca.submission.application.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de request para evaluar el Formato A.
 * RF3: Coordinador evalúa Formato A.
 */
public class EvaluarFormatoARequest {

    @NotNull(message = "El resultado de la evaluación es obligatorio")
    private Boolean aprobado;

    private String comentarios;

    // Constructores
    public EvaluarFormatoARequest() {}

    public EvaluarFormatoARequest(Boolean aprobado, String comentarios) {
        this.aprobado = aprobado;
        this.comentarios = comentarios;
    }

    // Getters y Setters

    public Boolean isAprobado() {
        return aprobado;
    }

    public void setAprobado(Boolean aprobado) {
        this.aprobado = aprobado;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
}


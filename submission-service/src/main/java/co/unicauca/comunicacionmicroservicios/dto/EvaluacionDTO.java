package co.unicauca.comunicacionmicroservicios.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para la evaluación del coordinador (RF-3)
 */
public class EvaluacionDTO {

    @NotNull(message = "El resultado de la evaluación es obligatorio")
    private Boolean aprobado;

    private String comentarios;

    public EvaluacionDTO() {
    }

    public EvaluacionDTO(Boolean aprobado, String comentarios) {
        this.aprobado = aprobado;
        this.comentarios = comentarios;
    }

    public Boolean getAprobado() {
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


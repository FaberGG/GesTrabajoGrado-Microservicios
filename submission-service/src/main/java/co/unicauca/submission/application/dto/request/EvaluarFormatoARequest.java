package co.unicauca.submission.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de request para evaluar el Formato A.
 * RF3: Coordinador evalúa Formato A.
 */
@Schema(description = "Request para evaluar un Formato A por el coordinador")
public class EvaluarFormatoARequest {

    @Schema(description = "Indica si el Formato A es aprobado (true) o requiere correcciones (false)",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El resultado de la evaluación es obligatorio")
    private Boolean aprobado;

    @Schema(description = "Comentarios u observaciones del coordinador sobre la evaluación",
            example = "El proyecto cumple con todos los requisitos. Aprobado para continuar con el anteproyecto.",
            nullable = true)
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


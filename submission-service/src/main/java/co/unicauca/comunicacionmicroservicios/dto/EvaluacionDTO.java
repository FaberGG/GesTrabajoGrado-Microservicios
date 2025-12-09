package co.unicauca.comunicacionmicroservicios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la evaluación del coordinador (RF-3).
 * Utilizado en el endpoint PUT /api/submissions/{id}/evaluar para registrar
 * la decisión del coordinador sobre un Formato A.
 * 
 * <p>Validaciones aplicadas:</p>
 * <ul>
 *   <li>aprobado: obligatorio, true para aprobar, false para rechazar</li>
 *   <li>comentarios: opcional, máximo 2000 caracteres</li>
 * </ul>
 * 
 * @see EvaluacionRequest DTO más completo usado en otros endpoints
 */
@Schema(description = "Datos de evaluación del coordinador para un Formato A")
public class EvaluacionDTO {

    @Schema(description = "Evaluation result: true=approved, false=rejected", 
            example = "true", 
            required = true)
    @NotNull(message = "Evaluation result is required")
    private Boolean aprobado;

    @Schema(description = "Coordinator's comments or observations on the evaluation", 
            example = "El proyecto cumple con todos los requisitos metodológicos y técnicos", 
            nullable = true)
    @Size(max = 2000, message = "Comments cannot exceed 2000 characters")
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


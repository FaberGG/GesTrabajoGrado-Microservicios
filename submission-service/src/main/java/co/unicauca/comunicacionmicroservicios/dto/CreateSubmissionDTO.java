package co.unicauca.comunicacionmicroservicios.dto;

import co.unicauca.comunicacionmicroservicios.domain.model.enumModalidad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear un nuevo proyecto submission.
 * Este DTO se utiliza en el endpoint POST /api/submissions para crear proyectos.
 * 
 * <p>Validaciones aplicadas:</p>
 * <ul>
 *   <li>titulo: obligatorio, no vacío</li>
 *   <li>modalidad: obligatorio, debe ser INVESTIGACION o PRACTICA_PROFESIONAL</li>
 *   <li>docenteDirectorId: obligatorio</li>
 * </ul>
 * 
 * @see SubmissionResponseDTO DTO de respuesta para proyectos
 * @see enumModalidad Enum con las modalidades permitidas
 */
@Schema(description = "Datos para crear un nuevo proyecto de trabajo de grado")
public class CreateSubmissionDTO {

    @Schema(description = "Título del trabajo de grado", 
            example = "Sistema de Gestión de Inventarios con IoT", 
            required = true)
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 300, message = "El título no puede exceder 300 caracteres")
    private String titulo;

    @Schema(description = "Descripción general del trabajo de grado", 
            example = "Desarrollo de un sistema IoT para la gestión automatizada de inventarios", 
            nullable = true)
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @Schema(description = "Modalidad del trabajo de grado", 
            example = "INVESTIGACION", 
            required = true,
            allowableValues = {"INVESTIGACION", "PRACTICA_PROFESIONAL"})
    @NotNull(message = "La modalidad es obligatoria")
    private enumModalidad modalidad;

    @Schema(description = "ID del docente director del trabajo", 
            example = "101", 
            required = true)
    @NotNull(message = "El ID del docente director es obligatorio")
    private Long docenteDirectorId;

    @Schema(description = "ID del docente codirector (opcional)", 
            example = "102", 
            nullable = true)
    private Long docenteCodirectorId;
    
    @Schema(description = "ID del estudiante principal", 
            example = "1001", 
            nullable = true)
    private Long estudianteId;

    @Schema(description = "Objetivo general del trabajo de grado", 
            example = "Desarrollar un sistema de gestión de inventarios utilizando tecnologías IoT", 
            nullable = true)
    @Size(max = 1000, message = "El objetivo general no puede exceder 1000 caracteres")
    private String objetivoGeneral;
    
    @Schema(description = "Objetivos específicos del trabajo (separados por líneas o formato específico)", 
            example = "1. Diseñar arquitectura IoT\n2. Implementar sensores\n3. Desarrollar dashboard", 
            nullable = true)
    @Size(max = 2000, message = "Los objetivos específicos no pueden exceder 2000 caracteres")
    private String objetivosEspecificos;

    @Schema(description = "Ruta del archivo del Formato A", 
            example = "/uploads/formato-a/documento.pdf", 
            nullable = true)
    private String rutaFormatoA;
    
    @Schema(description = "Ruta de la carta de aceptación (obligatoria para PRACTICA_PROFESIONAL)", 
            example = "/uploads/cartas/carta-aceptacion.pdf", 
            nullable = true)
    private String rutaCarta;

    // Getters y Setters

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public enumModalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(enumModalidad modalidad) {
        this.modalidad = modalidad;
    }

    public Long getDocenteDirectorId() {
        return docenteDirectorId;
    }

    public void setDocenteDirectorId(Long docenteDirectorId) {
        this.docenteDirectorId = docenteDirectorId;
    }

    public Long getDocenteCodirectorId() {
        return docenteCodirectorId;
    }

    public void setDocenteCodirectorId(Long docenteCodirectorId) {
        this.docenteCodirectorId = docenteCodirectorId;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getObjetivoGeneral() {
        return objetivoGeneral;
    }

    public void setObjetivoGeneral(String objetivoGeneral) {
        this.objetivoGeneral = objetivoGeneral;
    }

    public String getObjetivosEspecificos() {
        return objetivosEspecificos;
    }

    public void setObjetivosEspecificos(String objetivosEspecificos) {
        this.objetivosEspecificos = objetivosEspecificos;
    }

    public String getRutaFormatoA() {
        return rutaFormatoA;
    }

    public void setRutaFormatoA(String rutaFormatoA) {
        this.rutaFormatoA = rutaFormatoA;
    }

    public String getRutaCarta() {
        return rutaCarta;
    }

    public void setRutaCarta(String rutaCarta) {
        this.rutaCarta = rutaCarta;
    }
}


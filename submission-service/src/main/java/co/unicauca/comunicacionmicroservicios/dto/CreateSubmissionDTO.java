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

    @Schema(description = "Project title", 
            example = "Sistema de Gestión de Inventarios con IoT", 
            required = true)
    @NotBlank(message = "Title is required")
    @Size(max = 300, message = "Title cannot exceed 300 characters")
    private String titulo;

    @Schema(description = "General description of the project", 
            example = "Desarrollo de un sistema IoT para la gestión automatizada de inventarios", 
            nullable = true)
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String descripcion;

    @Schema(description = "Project modality", 
            example = "INVESTIGACION", 
            required = true,
            allowableValues = {"INVESTIGACION", "PRACTICA_PROFESIONAL"})
    @NotNull(message = "Modality is required")
    private enumModalidad modalidad;

    @Schema(description = "Director professor ID", 
            example = "101", 
            required = true)
    @NotNull(message = "Director professor ID is required")
    private Long docenteDirectorId;

    @Schema(description = "Co-director professor ID (optional)", 
            example = "102", 
            nullable = true)
    private Long docenteCodirectorId;
    
    @Schema(description = "Student ID", 
            example = "1001", 
            nullable = true)
    private Long estudianteId;

    @Schema(description = "General objective of the project", 
            example = "Desarrollar un sistema de gestión de inventarios utilizando tecnologías IoT", 
            nullable = true)
    @Size(max = 1000, message = "General objective cannot exceed 1000 characters")
    private String objetivoGeneral;
    
    @Schema(description = "Specific objectives (separated by lines or specific format)", 
            example = "1. Diseñar arquitectura IoT\n2. Implementar sensores\n3. Desarrollar dashboard", 
            nullable = true)
    @Size(max = 2000, message = "Specific objectives cannot exceed 2000 characters")
    private String objetivosEspecificos;

    @Schema(description = "Path to Formato A file", 
            example = "/uploads/formato-a/documento.pdf", 
            nullable = true)
    private String rutaFormatoA;
    
    @Schema(description = "Path to acceptance letter (required for PRACTICA_PROFESIONAL)", 
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


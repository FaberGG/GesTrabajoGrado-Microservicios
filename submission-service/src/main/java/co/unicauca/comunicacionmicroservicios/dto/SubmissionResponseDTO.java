package co.unicauca.comunicacionmicroservicios.dto;

import co.unicauca.comunicacionmicroservicios.domain.model.enumModalidad;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para ProyectoSubmission.
 * Utilizado en las operaciones de lectura (GET) para mostrar información completa de un proyecto.
 * 
 * <p>Este DTO incluye:</p>
 * <ul>
 *   <li>Datos básicos del proyecto (título, descripción, modalidad)</li>
 *   <li>Información de participantes (director, codirector, estudiante)</li>
 *   <li>Objetivos del proyecto</li>
 *   <li>Estado actual y seguimiento (intentos, comentarios)</li>
 *   <li>Referencias a archivos (Formato A, carta)</li>
 * </ul>
 * 
 * @see CreateSubmissionDTO DTO de entrada para crear proyectos
 */
@Schema(description = "Información completa de un proyecto de trabajo de grado")
public class SubmissionResponseDTO {

    @Schema(description = "Unique project identifier", example = "1")
    private Long id;
    
    @Schema(description = "Project title", example = "Sistema de Gestión de Inventarios con IoT")
    private String titulo;
    
    @Schema(description = "General project description", 
            example = "Desarrollo de un sistema IoT para gestión de inventarios")
    private String descripcion;
    
    @Schema(description = "Project modality", example = "INVESTIGACION",
            allowableValues = {"INVESTIGACION", "PRACTICA_PROFESIONAL"})
    private enumModalidad modalidad;
    
    @Schema(description = "Project creation date and time", example = "2025-11-01T10:00:00")
    private LocalDateTime fechaCreacion;
    
    @Schema(description = "Last modification date and time", example = "2025-11-03T15:30:00")
    private LocalDateTime fechaUltimaModificacion;

    @Schema(description = "Director professor ID", example = "101")
    private Long docenteDirectorId;
    
    @Schema(description = "Co-director professor ID (optional)", example = "102", nullable = true)
    private Long docenteCodirectorId;
    
    @Schema(description = "Student ID", example = "1001", nullable = true)
    private Long estudianteId;

    @Schema(description = "General objective of the project", 
            example = "Desarrollar un sistema de gestión de inventarios utilizando tecnologías IoT")
    private String objetivoGeneral;
    
    @Schema(description = "Specific objectives", 
            example = "1. Diseñar arquitectura IoT\n2. Implementar sensores\n3. Desarrollar dashboard")
    private String objetivosEspecificos;

    @Schema(description = "Current project state", example = "EN_EVALUACION_COORDINADOR",
            allowableValues = {"FORMATO_A_DILIGENCIADO", "EN_EVALUACION_COORDINADOR", 
                              "FORMATO_A_APROBADO", "CORRECCIONES_SOLICITADAS", 
                              "FORMATO_A_RECHAZADO", "ANTEPROYECTO_ENVIADO", 
                              "ANTEPROYECTO_EN_EVALUACION", "ANTEPROYECTO_APROBADO", 
                              "ANTEPROYECTO_RECHAZADO"})
    private String estadoActual;
    
    @Schema(description = "Number of Formato A submission attempts (maximum 3)", 
            example = "1", minimum = "0", maximum = "3")
    private Integer numeroIntentos;
    
    @Schema(description = "Evaluation committee comments", 
            example = "Cumple con todos los requisitos", nullable = true)
    private String comentariosComite;
    
    @Schema(description = "Indicates if the current state is final (no more changes allowed)", 
            example = "false")
    private boolean esEstadoFinal;

    @Schema(description = "Path to Formato A file", 
            example = "/uploads/formato-a/documento.pdf", nullable = true)
    private String rutaFormatoA;
    
    @Schema(description = "Path to acceptance letter", 
            example = "/uploads/cartas/carta-aceptacion.pdf", nullable = true)
    private String rutaCarta;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaUltimaModificacion() {
        return fechaUltimaModificacion;
    }

    public void setFechaUltimaModificacion(LocalDateTime fechaUltimaModificacion) {
        this.fechaUltimaModificacion = fechaUltimaModificacion;
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

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public Integer getNumeroIntentos() {
        return numeroIntentos;
    }

    public void setNumeroIntentos(Integer numeroIntentos) {
        this.numeroIntentos = numeroIntentos;
    }

    public String getComentariosComite() {
        return comentariosComite;
    }

    public void setComentariosComite(String comentariosComite) {
        this.comentariosComite = comentariosComite;
    }

    public boolean isEsEstadoFinal() {
        return esEstadoFinal;
    }

    public void setEsEstadoFinal(boolean esEstadoFinal) {
        this.esEstadoFinal = esEstadoFinal;
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


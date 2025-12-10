package co.unicauca.submission.application.dto.response;

import co.unicauca.submission.domain.model.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de response con información completa de un proyecto.
 * Usado para retornar al cliente después de operaciones.
 */
@Schema(description = "Respuesta con información completa de un proyecto de grado")
public class ProyectoResponse {

    @Schema(description = "ID único del proyecto", example = "123")
    private Long id;

    @Schema(description = "Título del proyecto de grado", example = "Sistema de IA para análisis de datos educativos")
    private String titulo;

    @Schema(description = "Modalidad del trabajo de grado", example = "INVESTIGACION",
            allowableValues = {"INVESTIGACION", "DESARROLLO_SOFTWARE", "PRACTICA_PROFESIONAL", "EMPRENDIMIENTO"})
    private String modalidad;

    @Schema(description = "Objetivo general del proyecto", example = "Desarrollar un sistema de inteligencia artificial")
    private String objetivoGeneral;

    @Schema(description = "Lista de objetivos específicos del proyecto")
    private List<String> objetivosEspecificos;

    // Participantes
    @Schema(description = "ID del docente director del proyecto", example = "12")
    private Long directorId;

    @Schema(description = "ID del codirector (opcional)", example = "15", nullable = true)
    private Long codirectorId;

    @Schema(description = "ID del estudiante principal", example = "1001")
    private Long estudiante1Id;

    @Schema(description = "ID del segundo estudiante (opcional, para duplas)", example = "1002", nullable = true)
    private Long estudiante2Id;

    // Información enriquecida (para clientes externos como review-service)
    @Schema(description = "Nombre completo del docente director", example = "Dr. Juan Pérez García")
    private String docenteDirectorNombre;

    @Schema(description = "Email del docente director", example = "jperez@unicauca.edu.co")
    private String docenteDirectorEmail;

    @Schema(description = "Lista de emails de los estudiantes")
    private List<String> estudiantesEmails;

    @Schema(description = "Fecha de envío del Formato A", example = "2025-12-01T10:30:00")
    private LocalDateTime fechaEnvio;

    // Estado
    @Schema(description = "Estado actual del proyecto", example = "FORMATO_A_ENVIADO",
            allowableValues = {"FORMATO_A_ENVIADO", "EN_EVALUACION_COORDINADOR", "FORMATO_A_APROBADO",
                             "CORRECCIONES_SOLICITADAS", "FORMATO_A_RECHAZADO", "ANTEPROYECTO_ENVIADO",
                             "ANTEPROYECTO_EN_EVALUACION", "ANTEPROYECTO_APROBADO", "ANTEPROYECTO_RECHAZADO"})
    private String estado;

    @Schema(description = "Descripción legible del estado", example = "Formato A enviado, pendiente de evaluación")
    private String estadoDescripcion;

    @Schema(description = "Indica si el proyecto está en un estado final", example = "false")
    private boolean esEstadoFinal;

    // Formato A
    @Schema(description = "Número de intento actual del Formato A (máximo 3)", example = "1", minimum = "1", maximum = "3")
    private Integer numeroIntento;

    @Schema(description = "Ruta al archivo PDF del Formato A", example = "/uploads/formatoA/proyecto-123-v1.pdf")
    private String rutaPdfFormatoA;

    @Schema(description = "Ruta a la carta de aceptación (para prácticas profesionales)", example = "/uploads/cartas/carta-123.pdf", nullable = true)
    private String rutaCarta;

    @Schema(description = "Indica si el proyecto tiene carta de aceptación adjunta", example = "false")
    private boolean tieneCartaAceptacion;

    // Anteproyecto (opcional)
    @Schema(description = "Ruta al archivo PDF del anteproyecto", example = "/uploads/anteproyecto/proyecto-123.pdf", nullable = true)
    private String rutaPdfAnteproyecto;

    @Schema(description = "Fecha de envío del anteproyecto", example = "2025-12-15T14:00:00", nullable = true)
    private LocalDateTime fechaEnvioAnteproyecto;

    @Schema(description = "ID del primer evaluador asignado", example = "20", nullable = true)
    private Long evaluador1Id;

    @Schema(description = "ID del segundo evaluador asignado", example = "21", nullable = true)
    private Long evaluador2Id;

    @Schema(description = "Indica si el proyecto tiene evaluadores asignados", example = "false")
    private boolean tieneEvaluadoresAsignados;

    // Auditoría
    @Schema(description = "Fecha de creación del proyecto", example = "2025-12-01T10:30:00")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de última modificación", example = "2025-12-05T16:45:00")
    private LocalDateTime fechaModificacion;

    // Constructores
    public ProyectoResponse() {}

    /**
     * Factory method para crear desde el domain model.
     */
    public static ProyectoResponse fromDomain(Proyecto proyecto) {
        ProyectoResponse response = new ProyectoResponse();

        // ID
        response.id = proyecto.getId() != null ? proyecto.getId().getValue() : null;

        // Información básica
        response.titulo = proyecto.getTitulo().getValue();
        response.modalidad = proyecto.getModalidad().name();
        response.objetivoGeneral = proyecto.getObjetivos().getObjetivoGeneral();
        response.objetivosEspecificos = proyecto.getObjetivos().getObjetivosEspecificos();

        // Participantes
        response.directorId = proyecto.getParticipantes().getDirectorId();
        response.codirectorId = proyecto.getParticipantes().getCodirectorId();
        response.estudiante1Id = proyecto.getParticipantes().getEstudiante1Id();
        response.estudiante2Id = proyecto.getParticipantes().getEstudiante2Id();

        // Estado
        response.estado = proyecto.getEstado().name();
        response.estadoDescripcion = proyecto.getEstado().getDescripcion();
        response.esEstadoFinal = proyecto.esEstadoFinal();

        // Formato A
        FormatoAInfo formatoA = proyecto.getFormatoA();
        response.numeroIntento = formatoA.getNumeroIntento();
        response.rutaPdfFormatoA = formatoA.getPdfFormatoA().getRuta();
        response.tieneCartaAceptacion = formatoA.tieneCarta();
        if (formatoA.tieneCarta()) {
            response.rutaCarta = formatoA.getCartaAceptacion().getRuta();
        }

        // Anteproyecto (si existe)
        AnteproyectoInfo anteproyecto = proyecto.getAnteproyecto();
        if (anteproyecto != null) {
            response.rutaPdfAnteproyecto = anteproyecto.getPdfAnteproyecto().getRuta();
            response.fechaEnvioAnteproyecto = anteproyecto.getFechaEnvio();
            response.evaluador1Id = anteproyecto.getEvaluador1Id();
            response.evaluador2Id = anteproyecto.getEvaluador2Id();
            response.tieneEvaluadoresAsignados = anteproyecto.tieneEvaluadoresAsignados();
        }

        // Auditoría
        response.fechaCreacion = proyecto.getFechaCreacion();
        response.fechaModificacion = proyecto.getFechaModificacion();

        return response;
    }

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

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getObjetivoGeneral() {
        return objetivoGeneral;
    }

    public void setObjetivoGeneral(String objetivoGeneral) {
        this.objetivoGeneral = objetivoGeneral;
    }

    public List<String> getObjetivosEspecificos() {
        return objetivosEspecificos;
    }

    public void setObjetivosEspecificos(List<String> objetivosEspecificos) {
        this.objetivosEspecificos = objetivosEspecificos;
    }

    public Long getDirectorId() {
        return directorId;
    }

    public void setDirectorId(Long directorId) {
        this.directorId = directorId;
    }

    public Long getCodirectorId() {
        return codirectorId;
    }

    public void setCodirectorId(Long codirectorId) {
        this.codirectorId = codirectorId;
    }

    public Long getEstudiante1Id() {
        return estudiante1Id;
    }

    public void setEstudiante1Id(Long estudiante1Id) {
        this.estudiante1Id = estudiante1Id;
    }

    public Long getEstudiante2Id() {
        return estudiante2Id;
    }

    public void setEstudiante2Id(Long estudiante2Id) {
        this.estudiante2Id = estudiante2Id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoDescripcion() {
        return estadoDescripcion;
    }

    public void setEstadoDescripcion(String estadoDescripcion) {
        this.estadoDescripcion = estadoDescripcion;
    }

    public boolean isEsEstadoFinal() {
        return esEstadoFinal;
    }

    public void setEsEstadoFinal(boolean esEstadoFinal) {
        this.esEstadoFinal = esEstadoFinal;
    }

    public Integer getNumeroIntento() {
        return numeroIntento;
    }

    public void setNumeroIntento(Integer numeroIntento) {
        this.numeroIntento = numeroIntento;
    }

    public String getRutaPdfFormatoA() {
        return rutaPdfFormatoA;
    }

    public void setRutaPdfFormatoA(String rutaPdfFormatoA) {
        this.rutaPdfFormatoA = rutaPdfFormatoA;
    }

    public String getRutaCarta() {
        return rutaCarta;
    }

    public void setRutaCarta(String rutaCarta) {
        this.rutaCarta = rutaCarta;
    }

    public boolean isTieneCartaAceptacion() {
        return tieneCartaAceptacion;
    }

    public void setTieneCartaAceptacion(boolean tieneCartaAceptacion) {
        this.tieneCartaAceptacion = tieneCartaAceptacion;
    }

    public String getRutaPdfAnteproyecto() {
        return rutaPdfAnteproyecto;
    }

    public void setRutaPdfAnteproyecto(String rutaPdfAnteproyecto) {
        this.rutaPdfAnteproyecto = rutaPdfAnteproyecto;
    }

    public LocalDateTime getFechaEnvioAnteproyecto() {
        return fechaEnvioAnteproyecto;
    }

    public void setFechaEnvioAnteproyecto(LocalDateTime fechaEnvioAnteproyecto) {
        this.fechaEnvioAnteproyecto = fechaEnvioAnteproyecto;
    }

    public Long getEvaluador1Id() {
        return evaluador1Id;
    }

    public void setEvaluador1Id(Long evaluador1Id) {
        this.evaluador1Id = evaluador1Id;
    }

    public Long getEvaluador2Id() {
        return evaluador2Id;
    }

    public void setEvaluador2Id(Long evaluador2Id) {
        this.evaluador2Id = evaluador2Id;
    }

    public boolean isTieneEvaluadoresAsignados() {
        return tieneEvaluadoresAsignados;
    }

    public void setTieneEvaluadoresAsignados(boolean tieneEvaluadoresAsignados) {
        this.tieneEvaluadoresAsignados = tieneEvaluadoresAsignados;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    // Getters y Setters para campos enriquecidos

    public String getDocenteDirectorNombre() {
        return docenteDirectorNombre;
    }

    public void setDocenteDirectorNombre(String docenteDirectorNombre) {
        this.docenteDirectorNombre = docenteDirectorNombre;
    }

    public String getDocenteDirectorEmail() {
        return docenteDirectorEmail;
    }

    public void setDocenteDirectorEmail(String docenteDirectorEmail) {
        this.docenteDirectorEmail = docenteDirectorEmail;
    }

    public List<String> getEstudiantesEmails() {
        return estudiantesEmails;
    }

    public void setEstudiantesEmails(List<String> estudiantesEmails) {
        this.estudiantesEmails = estudiantesEmails;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}


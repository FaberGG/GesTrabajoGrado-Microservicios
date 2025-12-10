package co.unicauca.submission.application.dto.request;

import co.unicauca.submission.domain.model.Modalidad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.InputStream;
import java.util.List;

/**
 * DTO de request para crear un Formato A inicial.
 * RF2: Docente crea Formato A.
 */
@Schema(description = "Request para crear un nuevo Formato A (propuesta inicial de proyecto de grado)")
public class CrearFormatoARequest {

    @Schema(description = "Título del proyecto de grado",
            example = "Sistema de IA para análisis de datos educativos",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @Schema(description = "Modalidad del trabajo de grado",
            example = "INVESTIGACION",
            allowableValues = {"INVESTIGACION", "DESARROLLO_SOFTWARE", "PRACTICA_PROFESIONAL", "EMPRENDIMIENTO"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La modalidad es obligatoria")
    private Modalidad modalidad;

    @Schema(description = "Objetivo general del proyecto",
            example = "Desarrollar un sistema de inteligencia artificial para el análisis de datos educativos",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El objetivo general es obligatorio")
    private String objetivoGeneral;

    @Schema(description = "Lista de objetivos específicos del proyecto",
            example = "[\"Analizar los requerimientos del sistema\", \"Diseñar la arquitectura de la solución\", \"Implementar los módulos principales\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Debe haber al menos un objetivo específico")
    private List<String> objetivosEspecificos;

    @Schema(description = "ID del estudiante principal (obligatorio)",
            example = "1001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El estudiante 1 es obligatorio")
    private Long estudiante1Id;

    @Schema(description = "ID del segundo estudiante (opcional, para trabajos en dupla)",
            example = "1002",
            nullable = true)
    private Long estudiante2Id;

    @Schema(description = "ID del codirector del proyecto (opcional)",
            example = "15",
            nullable = true)
    private Long codirectorId;

    // Streams de archivos (se setean desde el controller, no aparecen en Swagger)
    @Schema(hidden = true)
    private InputStream pdfStream;
    @Schema(hidden = true)
    private String pdfNombreArchivo;

    @Schema(hidden = true)
    private InputStream cartaStream;
    @Schema(hidden = true)
    private String cartaNombreArchivo;

    // Constructores
    public CrearFormatoARequest() {}

    // Getters y Setters

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
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

    public Long getCodirectorId() {
        return codirectorId;
    }

    public void setCodirectorId(Long codirectorId) {
        this.codirectorId = codirectorId;
    }

    public InputStream getPdfStream() {
        return pdfStream;
    }

    public void setPdfStream(InputStream pdfStream) {
        this.pdfStream = pdfStream;
    }

    public String getPdfNombreArchivo() {
        return pdfNombreArchivo;
    }

    public void setPdfNombreArchivo(String pdfNombreArchivo) {
        this.pdfNombreArchivo = pdfNombreArchivo;
    }

    public InputStream getCartaStream() {
        return cartaStream;
    }

    public void setCartaStream(InputStream cartaStream) {
        this.cartaStream = cartaStream;
    }

    public String getCartaNombreArchivo() {
        return cartaNombreArchivo;
    }

    public void setCartaNombreArchivo(String cartaNombreArchivo) {
        this.cartaNombreArchivo = cartaNombreArchivo;
    }
}


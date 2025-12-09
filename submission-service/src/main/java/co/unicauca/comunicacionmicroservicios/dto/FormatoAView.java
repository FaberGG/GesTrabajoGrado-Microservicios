package co.unicauca.comunicacionmicroservicios.dto;

import co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoFormato;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para visualizar datos de una versión de Formato A.
 * Utilizado en las operaciones de lectura (GET) para mostrar información completa de una versión del Formato A.
 * 
 * @see FormatoAData DTO de entrada para crear Formato A inicial
 * @see FormatoAPage DTO contenedor para respuestas paginadas
 */
@Schema(description = "Vista completa de una versión de Formato A")
public class FormatoAView {
    @Schema(description = "Identificador único de la versión del Formato A", example = "1")
    private Long id;
    
    @Schema(description = "Identificador del proyecto de grado asociado", example = "1")
    private Long proyectoId;
    
    @Schema(description = "Título del trabajo de grado", example = "Sistema de Gestión de Inventarios con IoT")
    private String titulo;
    
    @Schema(description = "Número de versión/intento (1, 2, o 3)", example = "1", minimum = "1", maximum = "3")
    private Integer version;
    
    @Schema(description = "Estado actual de la versión", example = "PENDIENTE",
            allowableValues = {"PENDIENTE", "APROBADO", "RECHAZADO"})
    private enumEstadoFormato estado;
    
    @Schema(description = "Observaciones del coordinador al evaluar", 
            example = "Cumple con todos los requisitos", nullable = true)
    private String observaciones;
    
    @Schema(description = "Nombre original del archivo PDF", example = "formato_a_v1.pdf")
    private String nombreArchivo;
    
    @Schema(description = "URL o ruta del archivo PDF del Formato A", 
            example = "/app/uploads/formato-a/1/v1/documento.pdf")
    private String pdfUrl;
    
    @Schema(description = "URL o ruta de la carta de aceptación (solo para PRACTICA_PROFESIONAL)", 
            example = "/app/uploads/formato-a/1/v1/carta.pdf", nullable = true)
    private String cartaUrl;
    
    @Schema(description = "Fecha y hora en que se envió esta versión", example = "2025-11-03T10:30:00")
    private LocalDateTime fechaEnvio;
    
    @Schema(description = "Nombre completo del docente director", example = "Dr. Juan Pérez")
    private String docenteDirectorNombre;
    
    @Schema(description = "Email del docente director", example = "juan.perez@unicauca.edu.co")
    private String docenteDirectorEmail;
    
    @Schema(description = "List of student emails associated with the project",
            example = "[\"student1@unicauca.edu.co\", \"student2@unicauca.edu.co\"]")
    private List<String> estudiantesEmails;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProyectoId() { return proyectoId; }
    public void setProyectoId(Long proyectoId) { this.proyectoId = proyectoId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public enumEstadoFormato getEstado() { return estado; }
    public void setEstado(enumEstadoFormato estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getCartaUrl() { return cartaUrl; }
    public void setCartaUrl(String cartaUrl) { this.cartaUrl = cartaUrl; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public String getDocenteDirectorNombre() { return docenteDirectorNombre; }
    public void setDocenteDirectorNombre(String docenteDirectorNombre) { this.docenteDirectorNombre = docenteDirectorNombre; }

    public String getDocenteDirectorEmail() { return docenteDirectorEmail; }
    public void setDocenteDirectorEmail(String docenteDirectorEmail) { this.docenteDirectorEmail = docenteDirectorEmail; }

    public List<String> getEstudiantesEmails() { return estudiantesEmails; }
    public void setEstudiantesEmails(List<String> estudiantesEmails) { this.estudiantesEmails = estudiantesEmails; }
}

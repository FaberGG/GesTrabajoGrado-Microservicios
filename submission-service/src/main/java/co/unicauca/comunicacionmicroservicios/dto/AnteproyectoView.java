package co.unicauca.comunicacionmicroservicios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para visualizar datos de un anteproyecto.
 * Utilizado en las operaciones de lectura (GET) para mostrar información resumida del anteproyecto.
 * 
 * @see AnteproyectoData DTO de entrada para crear anteproyectos
 * @see AnteproyectoPage DTO contenedor para respuestas paginadas
 */
@Schema(description = "Vista de un anteproyecto con sus datos básicos")
public class AnteproyectoView {
    
    @Schema(description = "Identificador único del anteproyecto", example = "1")
    private Long id;
    
    @Schema(description = "Identificador del proyecto de grado asociado", example = "1")
    private Long proyectoId;
    
    @Schema(description = "URL o ruta del archivo PDF del anteproyecto", 
            example = "/app/uploads/anteproyectos/1/documento.pdf")
    private String pdfUrl;
    
    @Schema(description = "Fecha y hora en que se envió el anteproyecto", 
            example = "2025-11-03T15:45:00")
    private LocalDateTime fechaEnvio;
    
    @Schema(description = "Estado actual del anteproyecto", 
            example = "EN_EVALUACION",
            allowableValues = {"PENDIENTE", "EN_EVALUACION", "APROBADO", "RECHAZADO"})
    private String estado;

    // Getters and Setters
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProyectoId() { return proyectoId; }
    public void setProyectoId(Long proyectoId) { this.proyectoId = proyectoId; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

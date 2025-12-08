package co.unicauca.comunicacionmicroservicios.dto;

import java.time.LocalDateTime;

public class AnteproyectoView {
    private Long id;
    private Long proyectoId;
    private String pdfUrl;
    private LocalDateTime fechaEnvio;
    private String estado; // si manejas estados de anteproyecto

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

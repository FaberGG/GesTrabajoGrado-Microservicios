package co.unicauca.comunicacionmicroservicios.dto;

import co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoFormato;
import java.time.LocalDateTime;
import java.util.List;

/** Vista simple de una versión de Formato A. */
public class FormatoAView {
    private Long id;
    private Long proyectoId;
    private String titulo;                    // título del proyecto
    private Integer version;                  // número de intento
    private enumEstadoFormato estado;         // PENDIENTE/APROBADO/RECHAZADO
    private String observaciones;             // si ya fue evaluado
    private String nombreArchivo;             // nombre original
    private String pdfUrl;                    // URL o path (si expones)
    private String cartaUrl;                  // si aplica
    private LocalDateTime fechaEnvio;
    private String docenteDirectorNombre;     // nombre completo del director
    private String docenteDirectorEmail;      // email del director
    private List<String> estudiantesEmails;   // lista de emails de estudiantes

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

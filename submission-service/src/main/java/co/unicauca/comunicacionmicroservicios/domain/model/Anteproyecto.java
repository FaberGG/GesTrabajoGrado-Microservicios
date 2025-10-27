package co.unicauca.comunicacionmicroservicios.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "anteproyectos",
       indexes = {
         @Index(name = "idx_anteproyecto_proyecto", columnList = "proyecto_id")
       })
public class Anteproyecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // FK a proyecto
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private ProyectoGrado proyecto;

    @Column(nullable = false)
    private String rutaArchivo;

    @Column(nullable = false)
    private String nombreArchivo;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(length = 30)
    private String estado; // opcional (si luego quieres manejar estados)

    @PrePersist
    public void prePersist() {
        if (this.fechaEnvio == null) {
            this.fechaEnvio = LocalDateTime.now();
        }
    }

    // Getters/Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public ProyectoGrado getProyecto() { return proyecto; }
    public void setProyecto(ProyectoGrado proyecto) { this.proyecto = proyecto; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

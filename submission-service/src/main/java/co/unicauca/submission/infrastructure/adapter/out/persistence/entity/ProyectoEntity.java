package co.unicauca.submission.infrastructure.adapter.out.persistence.entity;

import co.unicauca.submission.domain.model.EstadoProyecto;
import co.unicauca.submission.domain.model.Modalidad;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity JPA para persistencia de Proyecto.
 * Mapeo entre Domain Model y Base de Datos.
 *
 * NOTA: Esta entidad NO tiene lógica de negocio.
 * Solo es un contenedor de datos para JPA.
 */
@Entity
@Table(name = "proyectos")
public class ProyectoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Información básica
    @Column(nullable = false, length = 500)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Modalidad modalidad;

    @Column(nullable = false, length = 1000)
    private String objetivoGeneral;

    @Column(nullable = false, length = 3000)
    private String objetivosEspecificos; // Separados por ";"

    // Participantes
    @Column(nullable = false)
    private Long directorId;

    private Long codirectorId;

    @Column(nullable = false)
    private Long estudiante1Id;

    private Long estudiante2Id;

    // Estado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoProyecto estado;

    // Formato A
    @Column(nullable = false)
    private Integer numeroIntento;

    @Column(nullable = false, length = 500)
    private String rutaPdfFormatoA;

    @Column(length = 500)
    private String rutaCarta;

    // Anteproyecto (opcional)
    @Column(length = 500)
    private String rutaPdfAnteproyecto;

    private LocalDateTime fechaEnvioAnteproyecto;

    private Long evaluador1Id;

    private Long evaluador2Id;

    // Auditoría
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaModificacion;

    // Constructores
    public ProyectoEntity() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Lifecycle callbacks
    @PreUpdate
    protected void onUpdate() {
        this.fechaModificacion = LocalDateTime.now();
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

    public String getObjetivosEspecificos() {
        return objetivosEspecificos;
    }

    public void setObjetivosEspecificos(String objetivosEspecificos) {
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

    public EstadoProyecto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProyecto estado) {
        this.estado = estado;
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
}


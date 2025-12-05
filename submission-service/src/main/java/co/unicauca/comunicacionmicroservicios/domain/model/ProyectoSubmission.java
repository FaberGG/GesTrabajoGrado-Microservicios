package co.unicauca.comunicacionmicroservicios.domain.model;

import co.unicauca.comunicacionmicroservicios.domain.state.IEstadoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.concrete.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un Proyecto con su Formato A
 * Implementa el patrón State para gestionar el ciclo de vida del proceso de submisión
 */
@Entity
@Table(name = "proyecto_submissions")
public class ProyectoSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Column(nullable = false, length = 500)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "La modalidad es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private enumModalidad modalidad;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column
    private LocalDateTime fechaUltimaModificacion;

    // IDs de los actores involucrados
    @NotNull
    @Column(nullable = false)
    private Long docenteDirectorId;

    @Column
    private Long docenteCodirectorId;

    // ✨ SOPORTE PARA 2 ESTUDIANTES
    @NotNull(message = "Al menos un estudiante es obligatorio")
    @Column(nullable = false)
    private Long estudiante1Id;

    @Column
    private Long estudiante2Id; // Opcional

    // Objetivos del proyecto
    @Column(columnDefinition = "TEXT")
    private String objetivoGeneral;

    @Column(columnDefinition = "TEXT")
    private String objetivosEspecificos;

    // Estado del proceso (persistido como String en BD)
    @Column(nullable = false, length = 50)
    private String estadoNombre;

    // Estado actual (patrón State - no persistido, se reconstruye)
    @Transient
    private IEstadoSubmission estadoActual;

    // Número de intentos de evaluación
    @Column(nullable = false)
    private Integer numeroIntentos = 0;

    // Comentarios del comité
    @Column(columnDefinition = "TEXT")
    private String comentariosComite;

    // Ruta del archivo del Formato A
    @Column
    private String rutaFormatoA;

    // Ruta del archivo de carta (si aplica)
    @Column
    private String rutaCarta;

    // Constructor por defecto
    public ProyectoSubmission() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimaModificacion = LocalDateTime.now();
        this.numeroIntentos = 0;
        // Iniciar en el primer estado
        this.estadoActual = FormatoADiligenciadoState.getInstance();
        this.estadoNombre = this.estadoActual.getNombreEstado();
    }

    // Métodos del patrón State (delegan al estado actual)

    public void presentarAlCoordinador() {
        estadoActual.presentarAlCoordinador(this);
        actualizarFechaModificacion();
    }

    public void enviarAComite() {
        estadoActual.enviarAComite(this);
        actualizarFechaModificacion();
    }

    public void evaluar(boolean aprobado, String comentarios) {
        estadoActual.evaluar(this, aprobado, comentarios);
        actualizarFechaModificacion();
    }

    public void subirNuevaVersion() {
        estadoActual.subirNuevaVersion(this);
        actualizarFechaModificacion();
    }

    // Método para reconstruir el estado desde la BD
    @PostLoad
    public void reconstruirEstado() {
        this.estadoActual = obtenerEstadoPorNombre(this.estadoNombre);
    }

    // Método auxiliar para obtener la instancia del estado según su nombre
    private IEstadoSubmission obtenerEstadoPorNombre(String nombre) {
        if (nombre == null) {
            return FormatoADiligenciadoState.getInstance();
        }

        return switch (nombre) {
            case "FORMATO_A_DILIGENCIADO" -> FormatoADiligenciadoState.getInstance();
            case "PRESENTADO_AL_COORDINADOR" -> PresentadoAlCoordinadorState.getInstance();
            case "EN_EVALUACION_COMITE" -> EnEvaluacionComiteState.getInstance();
            case "CORRECCIONES_COMITE" -> CorreccionesComiteState.getInstance();
            case "ACEPTADO_POR_COMITE" -> AceptadoPorComiteState.getInstance();
            case "RECHAZADO_POR_COMITE" -> RechazadoPorComiteState.getInstance();
            default -> FormatoADiligenciadoState.getInstance();
        };
    }

    // Método para actualizar el estado (llamado desde los estados)
    public void setEstadoActual(IEstadoSubmission nuevoEstado) {
        this.estadoActual = nuevoEstado;
        this.estadoNombre = nuevoEstado.getNombreEstado();
    }

    private void actualizarFechaModificacion() {
        this.fechaUltimaModificacion = LocalDateTime.now();
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

    public String getEstadoNombre() {
        return estadoNombre;
    }

    public void setEstadoNombre(String estadoNombre) {
        this.estadoNombre = estadoNombre;
    }

    public IEstadoSubmission getEstadoActual() {
        if (estadoActual == null) {
            reconstruirEstado();
        }
        return estadoActual;
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

    public boolean esEstadoFinal() {
        return estadoActual != null && estadoActual.esEstadoFinal();
    }
}


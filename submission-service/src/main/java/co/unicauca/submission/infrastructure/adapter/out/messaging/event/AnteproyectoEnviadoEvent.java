package co.unicauca.submission.infrastructure.adapter.out.messaging.event;

import co.unicauca.submission.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Evento de integración: Anteproyecto Enviado.
 *
 * Evento enriquecido con información completa para progress-tracking.
 */
public class AnteproyectoEnviadoEvent implements DomainEvent {

    @JsonProperty("proyectoId")
    private final Long proyectoId;

    @JsonProperty("titulo")
    private final String titulo;

    @JsonProperty("modalidad")
    private final String modalidad;

    @JsonProperty("programa")
    private final String programa;

    @JsonProperty("timestamp")
    private final String timestamp;

    @JsonProperty("directorId")
    private final Long directorId;

    @JsonProperty("directorNombre")
    private final String directorNombre;

    @JsonProperty("usuarioResponsableId")
    private final Long usuarioResponsableId;

    @JsonProperty("usuarioResponsableNombre")
    private final String usuarioResponsableNombre;

    @JsonProperty("usuarioResponsableRol")
    private final String usuarioResponsableRol;

    @JsonProperty("codirectorId")
    private final Long codirectorId;

    @JsonProperty("codirectorNombre")
    private final String codirectorNombre;

    @JsonProperty("estudiante1Id")
    private final Long estudiante1Id;

    @JsonProperty("estudiante1Nombre")
    private final String estudiante1Nombre;

    @JsonProperty("estudiante1Email")
    private final String estudiante1Email;

    @JsonProperty("estudiante2Id")
    private final Long estudiante2Id;

    @JsonProperty("estudiante2Nombre")
    private final String estudiante2Nombre;

    @JsonProperty("estudiante2Email")
    private final String estudiante2Email;

    @JsonProperty("estudiantes")
    private final List<Map<String, Object>> estudiantes;

    @JsonProperty("rutaArchivo")
    private final String rutaArchivo;

    @JsonProperty("descripcion")
    private final String descripcion;

    private final LocalDateTime occurredOn;

    private AnteproyectoEnviadoEvent(Builder builder) {
        this.proyectoId = builder.proyectoId;
        this.titulo = builder.titulo;
        this.modalidad = builder.modalidad;
        this.programa = builder.programa;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now().toString();
        this.directorId = builder.directorId;
        this.directorNombre = builder.directorNombre;
        this.usuarioResponsableId = builder.usuarioResponsableId != null ? builder.usuarioResponsableId : builder.directorId;
        this.usuarioResponsableNombre = builder.usuarioResponsableNombre != null ? builder.usuarioResponsableNombre : builder.directorNombre;
        this.usuarioResponsableRol = builder.usuarioResponsableRol != null ? builder.usuarioResponsableRol : "DIRECTOR";
        this.codirectorId = builder.codirectorId;
        this.codirectorNombre = builder.codirectorNombre;
        this.estudiante1Id = builder.estudiante1Id;
        this.estudiante1Nombre = builder.estudiante1Nombre;
        this.estudiante1Email = builder.estudiante1Email;
        this.estudiante2Id = builder.estudiante2Id;
        this.estudiante2Nombre = builder.estudiante2Nombre;
        this.estudiante2Email = builder.estudiante2Email;
        this.estudiantes = builder.estudiantes;
        this.rutaArchivo = builder.rutaArchivo;
        this.descripcion = builder.descripcion != null ? builder.descripcion : "Anteproyecto enviado";
        this.occurredOn = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getEventType() {
        return "AnteproyectoSubido";
    }

    @Override
    public Long getAggregateId() {
        return proyectoId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    // Getters completos
    public Long getProyectoId() { return proyectoId; }
    public String getTitulo() { return titulo; }
    public String getModalidad() { return modalidad; }
    public String getPrograma() { return programa; }
    public String getTimestamp() { return timestamp; }
    public Long getDirectorId() { return directorId; }
    public String getDirectorNombre() { return directorNombre; }
    public Long getUsuarioResponsableId() { return usuarioResponsableId; }
    public String getUsuarioResponsableNombre() { return usuarioResponsableNombre; }
    public String getUsuarioResponsableRol() { return usuarioResponsableRol; }
    public Long getCodirectorId() { return codirectorId; }
    public String getCodirectorNombre() { return codirectorNombre; }
    public Long getEstudiante1Id() { return estudiante1Id; }
    public String getEstudiante1Nombre() { return estudiante1Nombre; }
    public String getEstudiante1Email() { return estudiante1Email; }
    public Long getEstudiante2Id() { return estudiante2Id; }
    public String getEstudiante2Nombre() { return estudiante2Nombre; }
    public String getEstudiante2Email() { return estudiante2Email; }
    public List<Map<String, Object>> getEstudiantes() { return estudiantes; }
    public String getRutaArchivo() { return rutaArchivo; }
    public String getDescripcion() { return descripcion; }

    public static class Builder {
        private Long proyectoId;
        private String titulo;
        private String modalidad;
        private String programa;
        private String timestamp;
        private Long directorId;
        private String directorNombre;
        private Long usuarioResponsableId;
        private String usuarioResponsableNombre;
        private String usuarioResponsableRol;
        private Long codirectorId;
        private String codirectorNombre;
        private Long estudiante1Id;
        private String estudiante1Nombre;
        private String estudiante1Email;
        private Long estudiante2Id;
        private String estudiante2Nombre;
        private String estudiante2Email;
        private List<Map<String, Object>> estudiantes;
        private String rutaArchivo;
        private String descripcion;

        public Builder proyectoId(Long proyectoId) { this.proyectoId = proyectoId; return this; }
        public Builder titulo(String titulo) { this.titulo = titulo; return this; }
        public Builder modalidad(String modalidad) { this.modalidad = modalidad; return this; }
        public Builder programa(String programa) { this.programa = programa; return this; }
        public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
        public Builder directorId(Long directorId) { this.directorId = directorId; return this; }
        public Builder directorNombre(String directorNombre) { this.directorNombre = directorNombre; return this; }
        public Builder usuarioResponsableId(Long id) { this.usuarioResponsableId = id; return this; }
        public Builder usuarioResponsableNombre(String nombre) { this.usuarioResponsableNombre = nombre; return this; }
        public Builder usuarioResponsableRol(String rol) { this.usuarioResponsableRol = rol; return this; }
        public Builder codirectorId(Long codirectorId) { this.codirectorId = codirectorId; return this; }
        public Builder codirectorNombre(String codirectorNombre) { this.codirectorNombre = codirectorNombre; return this; }
        public Builder estudiante1Id(Long id) { this.estudiante1Id = id; return this; }
        public Builder estudiante1Nombre(String nombre) { this.estudiante1Nombre = nombre; return this; }
        public Builder estudiante1Email(String email) { this.estudiante1Email = email; return this; }
        public Builder estudiante2Id(Long id) { this.estudiante2Id = id; return this; }
        public Builder estudiante2Nombre(String nombre) { this.estudiante2Nombre = nombre; return this; }
        public Builder estudiante2Email(String email) { this.estudiante2Email = email; return this; }
        public Builder estudiantes(List<Map<String, Object>> estudiantes) { this.estudiantes = estudiantes; return this; }
        public Builder rutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; return this; }
        public Builder descripcion(String descripcion) { this.descripcion = descripcion; return this; }

        public AnteproyectoEnviadoEvent build() {
            if (proyectoId == null) throw new IllegalStateException("proyectoId es requerido");
            if (titulo == null) throw new IllegalStateException("titulo es requerido");
            if (directorId == null) throw new IllegalStateException("directorId es requerido");
            if (estudiante1Id == null) throw new IllegalStateException("estudiante1Id es requerido");
            return new AnteproyectoEnviadoEvent(this);
        }
    }
}


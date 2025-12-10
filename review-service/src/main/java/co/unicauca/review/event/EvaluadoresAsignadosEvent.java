package co.unicauca.review.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Evento de integración: Evaluadores Asignados.
 *
 * Publicado cuando el jefe de departamento asigna evaluadores al anteproyecto.
 * Incluye información completa para progress-tracking.
 */
public class EvaluadoresAsignadosEvent {

    @JsonProperty("proyectoId")
    private final Long proyectoId;

    @JsonProperty("evaluadores")
    private final List<Map<String, Object>> evaluadores;

    @JsonProperty("timestamp")
    private final String timestamp;

    // Usuario responsable (jefe de departamento)
    @JsonProperty("usuarioResponsableId")
    private final Long usuarioResponsableId;

    @JsonProperty("usuarioResponsableNombre")
    private final String usuarioResponsableNombre;

    @JsonProperty("usuarioResponsableRol")
    private final String usuarioResponsableRol;

    private EvaluadoresAsignadosEvent(Builder builder) {
        this.proyectoId = builder.proyectoId;
        this.evaluadores = builder.evaluadores;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now().toString();
        this.usuarioResponsableId = builder.usuarioResponsableId;
        this.usuarioResponsableNombre = builder.usuarioResponsableNombre;
        this.usuarioResponsableRol = builder.usuarioResponsableRol != null ? builder.usuarioResponsableRol : "JEFE_DEPARTAMENTO";
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getProyectoId() { return proyectoId; }
    public List<Map<String, Object>> getEvaluadores() { return evaluadores; }
    public String getTimestamp() { return timestamp; }
    public Long getUsuarioResponsableId() { return usuarioResponsableId; }
    public String getUsuarioResponsableNombre() { return usuarioResponsableNombre; }
    public String getUsuarioResponsableRol() { return usuarioResponsableRol; }

    public static class Builder {
        private Long proyectoId;
        private List<Map<String, Object>> evaluadores;
        private String timestamp;
        private Long usuarioResponsableId;
        private String usuarioResponsableNombre;
        private String usuarioResponsableRol;

        public Builder proyectoId(Long proyectoId) { this.proyectoId = proyectoId; return this; }
        public Builder evaluadores(List<Map<String, Object>> evaluadores) { this.evaluadores = evaluadores; return this; }
        public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
        public Builder usuarioResponsableId(Long id) { this.usuarioResponsableId = id; return this; }
        public Builder usuarioResponsableNombre(String nombre) { this.usuarioResponsableNombre = nombre; return this; }
        public Builder usuarioResponsableRol(String rol) { this.usuarioResponsableRol = rol; return this; }

        public EvaluadoresAsignadosEvent build() {
            if (proyectoId == null) throw new IllegalStateException("proyectoId es requerido");
            if (evaluadores == null || evaluadores.isEmpty()) throw new IllegalStateException("evaluadores es requerido");
            return new EvaluadoresAsignadosEvent(this);
        }
    }
}


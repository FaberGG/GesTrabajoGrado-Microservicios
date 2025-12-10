package co.unicauca.review.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Evento de integración: Anteproyecto Evaluado.
 *
 * Publicado cuando los evaluadores completan la evaluación del anteproyecto.
 * Incluye información completa para progress-tracking.
 */
public class AnteproyectoEvaluadoEvent {

    @JsonProperty("proyectoId")
    private final Long proyectoId;

    @JsonProperty("resultado")
    private final String resultado; // "APROBADO" o "RECHAZADO"

    @JsonProperty("observaciones")
    private final String observaciones;

    @JsonProperty("timestamp")
    private final String timestamp;

    // Usuario responsable (evaluador)
    @JsonProperty("usuarioResponsableId")
    private final Long usuarioResponsableId;

    @JsonProperty("usuarioResponsableNombre")
    private final String usuarioResponsableNombre;

    @JsonProperty("usuarioResponsableRol")
    private final String usuarioResponsableRol;

    // Estudiantes (opcional)
    @JsonProperty("estudiantes")
    private final List<Map<String, Object>> estudiantes;

    private AnteproyectoEvaluadoEvent(Builder builder) {
        this.proyectoId = builder.proyectoId;
        this.resultado = builder.resultado;
        this.observaciones = builder.observaciones;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now().toString();
        this.usuarioResponsableId = builder.usuarioResponsableId;
        this.usuarioResponsableNombre = builder.usuarioResponsableNombre;
        this.usuarioResponsableRol = builder.usuarioResponsableRol != null ? builder.usuarioResponsableRol : "EVALUADOR";
        this.estudiantes = builder.estudiantes;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getProyectoId() { return proyectoId; }
    public String getResultado() { return resultado; }
    public String getObservaciones() { return observaciones; }
    public String getTimestamp() { return timestamp; }
    public Long getUsuarioResponsableId() { return usuarioResponsableId; }
    public String getUsuarioResponsableNombre() { return usuarioResponsableNombre; }
    public String getUsuarioResponsableRol() { return usuarioResponsableRol; }
    public List<Map<String, Object>> getEstudiantes() { return estudiantes; }

    public static class Builder {
        private Long proyectoId;
        private String resultado;
        private String observaciones;
        private String timestamp;
        private Long usuarioResponsableId;
        private String usuarioResponsableNombre;
        private String usuarioResponsableRol;
        private List<Map<String, Object>> estudiantes;

        public Builder proyectoId(Long proyectoId) { this.proyectoId = proyectoId; return this; }
        public Builder resultado(String resultado) { this.resultado = resultado; return this; }
        public Builder observaciones(String observaciones) { this.observaciones = observaciones; return this; }
        public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
        public Builder usuarioResponsableId(Long id) { this.usuarioResponsableId = id; return this; }
        public Builder usuarioResponsableNombre(String nombre) { this.usuarioResponsableNombre = nombre; return this; }
        public Builder usuarioResponsableRol(String rol) { this.usuarioResponsableRol = rol; return this; }
        public Builder estudiantes(List<Map<String, Object>> estudiantes) { this.estudiantes = estudiantes; return this; }

        public AnteproyectoEvaluadoEvent build() {
            if (proyectoId == null) throw new IllegalStateException("proyectoId es requerido");
            if (resultado == null) throw new IllegalStateException("resultado es requerido");
            return new AnteproyectoEvaluadoEvent(this);
        }
    }
}


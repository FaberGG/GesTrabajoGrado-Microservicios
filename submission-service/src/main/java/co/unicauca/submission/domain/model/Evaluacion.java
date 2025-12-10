package co.unicauca.submission.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value Object que representa una evaluación (de Formato A o Anteproyecto).
 */
public class Evaluacion {

    private final boolean aprobado;
    private final String comentarios;
    private final Long evaluadorId;
    private final LocalDateTime fecha;

    public Evaluacion(boolean aprobado, String comentarios, Long evaluadorId) {
        this(aprobado, comentarios, evaluadorId, LocalDateTime.now());
    }

    public Evaluacion(boolean aprobado, String comentarios, Long evaluadorId, LocalDateTime fecha) {
        if (evaluadorId == null) {
            throw new IllegalArgumentException("El evaluador es obligatorio");
        }
        if (comentarios != null && comentarios.trim().isEmpty()) {
            comentarios = null; // Normalizar strings vacíos a null
        }

        this.aprobado = aprobado;
        this.comentarios = comentarios;
        this.evaluadorId = evaluadorId;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
    }

    public boolean isAprobado() {
        return aprobado;
    }

    public boolean isRechazado() {
        return !aprobado;
    }

    public String getComentarios() {
        return comentarios;
    }

    public Long getEvaluadorId() {
        return evaluadorId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evaluacion that = (Evaluacion) o;
        return aprobado == that.aprobado &&
               Objects.equals(comentarios, that.comentarios) &&
               Objects.equals(evaluadorId, that.evaluadorId) &&
               Objects.equals(fecha, that.fecha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aprobado, comentarios, evaluadorId, fecha);
    }

    @Override
    public String toString() {
        return "Evaluacion{" +
                "aprobado=" + aprobado +
                ", evaluadorId=" + evaluadorId +
                ", fecha=" + fecha +
                '}';
    }
}


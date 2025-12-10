package co.unicauca.submission.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entity: AnteproyectoInfo
 *
 * Representa la información del anteproyecto dentro del aggregate Proyecto.
 * Gestiona el archivo, evaluadores y evaluaciones del anteproyecto.
 */
public class AnteproyectoInfo {

    private ArchivoAdjunto pdfAnteproyecto;
    private LocalDateTime fechaEnvio;
    private Long evaluador1Id;
    private Long evaluador2Id;
    private List<Evaluacion> evaluaciones;

    /**
     * Constructor para crear un nuevo anteproyecto.
     *
     * @param pdfAnteproyecto PDF del anteproyecto (obligatorio)
     */
    public AnteproyectoInfo(ArchivoAdjunto pdfAnteproyecto) {
        if (pdfAnteproyecto == null) {
            throw new IllegalArgumentException("El PDF del anteproyecto es obligatorio");
        }
        if (!pdfAnteproyecto.esPDF()) {
            throw new IllegalArgumentException("El archivo debe ser PDF");
        }

        this.pdfAnteproyecto = pdfAnteproyecto;
        this.fechaEnvio = LocalDateTime.now();
        this.evaluaciones = new ArrayList<>();
    }

    /**
     * Asigna los evaluadores al anteproyecto.
     * RF8: El jefe de departamento asigna 2 evaluadores.
     *
     * @param evaluador1Id ID del primer evaluador
     * @param evaluador2Id ID del segundo evaluador
     */
    public void asignarEvaluadores(Long evaluador1Id, Long evaluador2Id) {
        if (evaluador1Id == null || evaluador2Id == null) {
            throw new IllegalArgumentException("Ambos evaluadores son obligatorios");
        }
        if (evaluador1Id.equals(evaluador2Id)) {
            throw new IllegalArgumentException("Los evaluadores deben ser diferentes");
        }
        if (this.evaluador1Id != null || this.evaluador2Id != null) {
            throw new IllegalStateException("Los evaluadores ya fueron asignados");
        }

        this.evaluador1Id = evaluador1Id;
        this.evaluador2Id = evaluador2Id;
    }

    /**
     * Agrega una evaluación al historial.
     *
     * @param evaluacion Evaluación realizada por un evaluador
     */
    public void agregarEvaluacion(Evaluacion evaluacion) {
        if (evaluacion == null) {
            throw new IllegalArgumentException("La evaluación no puede ser null");
        }

        // Validar que el evaluador es uno de los asignados
        Long evaluadorId = evaluacion.getEvaluadorId();
        if (!esEvaluadorAsignado(evaluadorId)) {
            throw new IllegalArgumentException(
                "El evaluador " + evaluadorId + " no está asignado a este anteproyecto"
            );
        }

        this.evaluaciones.add(evaluacion);
    }

    /**
     * Verifica si un usuario es evaluador asignado al anteproyecto.
     */
    public boolean esEvaluadorAsignado(Long userId) {
        if (userId == null) {
            return false;
        }
        return userId.equals(this.evaluador1Id) || userId.equals(this.evaluador2Id);
    }

    /**
     * Verifica si tiene evaluadores asignados.
     */
    public boolean tieneEvaluadoresAsignados() {
        return this.evaluador1Id != null && this.evaluador2Id != null;
    }

    /**
     * Obtiene la última evaluación realizada.
     */
    public Evaluacion getUltimaEvaluacion() {
        if (evaluaciones.isEmpty()) {
            return null;
        }
        return evaluaciones.get(evaluaciones.size() - 1);
    }

    /**
     * Verifica si el anteproyecto fue evaluado.
     */
    public boolean fueEvaluado() {
        return !this.evaluaciones.isEmpty();
    }

    /**
     * Verifica si ambos evaluadores ya evaluaron.
     */
    public boolean ambosEvaluadoresEvaluaron() {
        if (!tieneEvaluadoresAsignados()) {
            return false;
        }

        boolean evaluador1Evaluo = evaluaciones.stream()
            .anyMatch(e -> e.getEvaluadorId().equals(evaluador1Id));
        boolean evaluador2Evaluo = evaluaciones.stream()
            .anyMatch(e -> e.getEvaluadorId().equals(evaluador2Id));

        return evaluador1Evaluo && evaluador2Evaluo;
    }

    // Getters

    public ArchivoAdjunto getPdfAnteproyecto() {
        return pdfAnteproyecto;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public Long getEvaluador1Id() {
        return evaluador1Id;
    }

    public Long getEvaluador2Id() {
        return evaluador2Id;
    }

    public List<Evaluacion> getEvaluaciones() {
        return Collections.unmodifiableList(evaluaciones);
    }

    @Override
    public String toString() {
        return "AnteproyectoInfo{" +
                "fechaEnvio=" + fechaEnvio +
                ", tieneEvaluadores=" + tieneEvaluadoresAsignados() +
                ", evaluaciones=" + evaluaciones.size() +
                '}';
    }
}


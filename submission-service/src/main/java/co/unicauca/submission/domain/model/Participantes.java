package co.unicauca.submission.domain.model;

import java.util.Objects;

/**
 * Value Object que representa los participantes del proyecto.
 * Incluye director, codirector (opcional) y estudiantes (1 o 2).
 */
public class Participantes {

    private final Long directorId;
    private final Long codirectorId; // Opcional
    private final Long estudiante1Id;
    private final Long estudiante2Id; // Opcional

    private Participantes(Long directorId, Long codirectorId,
                         Long estudiante1Id, Long estudiante2Id) {
        if (directorId == null) {
            throw new IllegalArgumentException("El director es obligatorio");
        }
        if (estudiante1Id == null) {
            throw new IllegalArgumentException("Al menos un estudiante es obligatorio");
        }

        this.directorId = directorId;
        this.codirectorId = codirectorId;
        this.estudiante1Id = estudiante1Id;
        this.estudiante2Id = estudiante2Id;
    }

    public static Participantes of(Long directorId, Long codirectorId,
                                   Long estudiante1Id, Long estudiante2Id) {
        return new Participantes(directorId, codirectorId, estudiante1Id, estudiante2Id);
    }

    /**
     * Verifica si un usuario es el director del proyecto
     */
    public boolean esDirector(Long userId) {
        return this.directorId.equals(userId);
    }

    /**
     * Verifica si un usuario es estudiante del proyecto
     */
    public boolean esEstudiante(Long userId) {
        return this.estudiante1Id.equals(userId) ||
               (this.estudiante2Id != null && this.estudiante2Id.equals(userId));
    }

    /**
     * Verifica si el proyecto tiene codirector
     */
    public boolean tieneCodirector() {
        return this.codirectorId != null;
    }

    /**
     * Verifica si el proyecto tiene segundo estudiante
     */
    public boolean tieneSegundoEstudiante() {
        return this.estudiante2Id != null;
    }

    // Getters
    public Long getDirectorId() {
        return directorId;
    }

    public Long getCodirectorId() {
        return codirectorId;
    }

    public Long getEstudiante1Id() {
        return estudiante1Id;
    }

    public Long getEstudiante2Id() {
        return estudiante2Id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participantes that = (Participantes) o;
        return Objects.equals(directorId, that.directorId) &&
               Objects.equals(codirectorId, that.codirectorId) &&
               Objects.equals(estudiante1Id, that.estudiante1Id) &&
               Objects.equals(estudiante2Id, that.estudiante2Id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directorId, codirectorId, estudiante1Id, estudiante2Id);
    }
}


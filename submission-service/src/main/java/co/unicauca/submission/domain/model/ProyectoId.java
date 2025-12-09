package co.unicauca.submission.domain.model;

import java.util.Objects;

/**
 * Value Object que representa el identificador único de un Proyecto.
 * Inmutable y sin lógica de negocio compleja.
 */
public class ProyectoId {

    private final Long value;

    private ProyectoId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ID de proyecto inválido: " + value);
        }
        this.value = value;
    }

    /**
     * Crea un ProyectoId a partir de un Long
     */
    public static ProyectoId of(Long value) {
        return new ProyectoId(value);
    }

    /**
     * Para cuando el ID aún no existe (será generado por la BD)
     */
    public static ProyectoId empty() {
        return null;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProyectoId that = (ProyectoId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "ProyectoId{" + value + '}';
    }
}


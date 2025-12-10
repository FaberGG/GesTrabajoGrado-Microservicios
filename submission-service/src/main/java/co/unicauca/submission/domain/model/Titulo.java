package co.unicauca.submission.domain.model;

import java.util.Objects;

/**
 * Value Object que representa el título del proyecto.
 * Encapsula las reglas de validación del título.
 */
public class Titulo {

    private static final int MAX_LENGTH = 500;
    private static final int MIN_LENGTH = 10;

    private final String value;

    private Titulo(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }

        String trimmed = value.trim();

        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                "El título debe tener al menos " + MIN_LENGTH + " caracteres"
            );
        }

        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "El título no puede exceder " + MAX_LENGTH + " caracteres"
            );
        }

        this.value = trimmed;
    }

    public static Titulo of(String value) {
        return new Titulo(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Titulo titulo = (Titulo) o;
        return Objects.equals(value, titulo.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}


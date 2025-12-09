package co.unicauca.submission.domain.model;

/**
 * Enum que representa la modalidad del proyecto de grado.
 */
public enum Modalidad {

    INVESTIGACION("Investigación"),
    PRACTICA_PROFESIONAL("Práctica Profesional");

    private final String descripcion;

    Modalidad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean requiereCarta() {
        return this == PRACTICA_PROFESIONAL;
    }
}


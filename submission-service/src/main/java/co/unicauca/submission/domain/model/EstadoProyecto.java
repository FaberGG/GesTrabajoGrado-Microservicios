package co.unicauca.submission.domain.model;

/**
 * Enum que representa el estado actual del proyecto en el flujo de trabajo.
 * Reemplaza al patrón State con un enum más simple.
 */
public enum EstadoProyecto {

    // Estados del Formato A
    FORMATO_A_DILIGENCIADO("Formato A diligenciado", false),
    EN_EVALUACION_COORDINADOR("En evaluación por coordinador", false),
    CORRECCIONES_SOLICITADAS("Correcciones solicitadas", false),
    FORMATO_A_APROBADO("Formato A aprobado", false),
    FORMATO_A_RECHAZADO("Formato A rechazado definitivamente", true),

    // Estados del Anteproyecto
    ANTEPROYECTO_ENVIADO("Anteproyecto enviado", false),
    ANTEPROYECTO_EN_EVALUACION("Anteproyecto en evaluación", false),
    ANTEPROYECTO_APROBADO("Anteproyecto aprobado", true),
    ANTEPROYECTO_RECHAZADO("Anteproyecto rechazado", true);

    private final String descripcion;
    private final boolean estadoFinal;

    EstadoProyecto(String descripcion, boolean estadoFinal) {
        this.descripcion = descripcion;
        this.estadoFinal = estadoFinal;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEstadoFinal() {
        return estadoFinal;
    }

    public boolean puedeReenviarFormatoA() {
        return this == CORRECCIONES_SOLICITADAS;
    }

    public boolean puedeSubirAnteproyecto() {
        return this == FORMATO_A_APROBADO;
    }

    public boolean esEstadoFormatoA() {
        return this == FORMATO_A_DILIGENCIADO ||
               this == EN_EVALUACION_COORDINADOR ||
               this == CORRECCIONES_SOLICITADAS ||
               this == FORMATO_A_APROBADO ||
               this == FORMATO_A_RECHAZADO;
    }

    public boolean esEstadoAnteproyecto() {
        return this == ANTEPROYECTO_ENVIADO ||
               this == ANTEPROYECTO_EN_EVALUACION ||
               this == ANTEPROYECTO_APROBADO ||
               this == ANTEPROYECTO_RECHAZADO;
    }
}


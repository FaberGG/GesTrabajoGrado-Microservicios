package co.unicauca.submission.domain.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación sobre
 * un Formato A que ya ha sido evaluado.
 *
 * Regla de negocio: Un Formato A ya evaluado no puede ser evaluado nuevamente.
 */
public class FormatoAYaEvaluadoException extends EstadoProyectoInvalidoException {

    private final Long proyectoId;
    private final String estadoActual;

    public FormatoAYaEvaluadoException(Long proyectoId, String estadoActual) {
        super(String.format("El Formato A del proyecto %d ya ha sido evaluado (Estado: %s). " +
                           "No se puede evaluar nuevamente.",
                           proyectoId, estadoActual));
        this.proyectoId = proyectoId;
        this.estadoActual = estadoActual;
    }

    public Long getProyectoId() {
        return proyectoId;
    }

    public String getEstadoActual() {
        return estadoActual;
    }
}


package co.unicauca.submission.domain.exception;

/**
 * Excepción lanzada cuando el estado del proyecto no permite la operación solicitada.
 */
public class EstadoProyectoInvalidoException extends ReglaNegocioException {

    public EstadoProyectoInvalidoException(String message) {
        super(message);
    }

    public EstadoProyectoInvalidoException(String estadoActual, String operacion) {
        super(String.format("No se puede realizar la operación '%s' en el estado actual: %s",
                           operacion, estadoActual));
    }
}


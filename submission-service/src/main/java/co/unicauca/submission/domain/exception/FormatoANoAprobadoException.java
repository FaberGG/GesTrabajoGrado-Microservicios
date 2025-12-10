package co.unicauca.submission.domain.exception;

/**
 * Excepción lanzada cuando se intenta subir anteproyecto sin que el Formato A esté aprobado.
 */
public class FormatoANoAprobadoException extends DomainException {

    public FormatoANoAprobadoException() {
        super("El Formato A debe estar aprobado antes de subir el anteproyecto");
    }

    public FormatoANoAprobadoException(String message) {
        super(message);
    }
}


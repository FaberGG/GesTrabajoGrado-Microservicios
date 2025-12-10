package co.unicauca.submission.domain.exception;

/**
 * Excepción lanzada cuando se excede el máximo de intentos para reenviar el Formato A.
 */
public class MaximosIntentosExcedidosException extends DomainException {

    public MaximosIntentosExcedidosException() {
        super("Se alcanzó el máximo de 3 intentos para el Formato A");
    }

    public MaximosIntentosExcedidosException(String message) {
        super(message);
    }
}


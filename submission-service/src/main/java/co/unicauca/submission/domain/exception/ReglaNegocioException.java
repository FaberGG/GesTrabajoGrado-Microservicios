package co.unicauca.submission.domain.exception;

/**
 * Excepción lanzada cuando se viola una regla de negocio.
 * Clase base para excepciones de reglas de negocio específicas.
 */
public class ReglaNegocioException extends DomainException {

    public ReglaNegocioException(String message) {
        super(message);
    }

    public ReglaNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}


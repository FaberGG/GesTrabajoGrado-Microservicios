package co.unicauca.submission.domain.exception;

/**
 * Excepción lanzada cuando un usuario no está autorizado para realizar una acción.
 */
public class UsuarioNoAutorizadoException extends DomainException {

    public UsuarioNoAutorizadoException(String message) {
        super(message);
    }

    public UsuarioNoAutorizadoException(String accion, String rolRequerido) {
        super(String.format("No autorizado para %s. Rol requerido: %s", accion, rolRequerido));
    }
}


package co.unicauca.submission.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un proyecto.
 */
public class ProyectoNotFoundException extends DomainException {

    public ProyectoNotFoundException(Long proyectoId) {
        super("Proyecto no encontrado con ID: " + proyectoId);
    }

    public ProyectoNotFoundException(String message) {
        super(message);
    }
}


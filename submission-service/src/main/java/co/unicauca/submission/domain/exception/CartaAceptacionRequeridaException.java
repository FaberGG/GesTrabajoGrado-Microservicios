package co.unicauca.submission.domain.exception;

/**
 * Excepción lanzada cuando se requiere una carta de aceptación pero no se proporciona.
 *
 * Regla de negocio: Los proyectos de modalidad PRACTICA_PROFESIONAL requieren
 * carta de aceptación de la empresa.
 */
public class CartaAceptacionRequeridaException extends ReglaNegocioException {

    public CartaAceptacionRequeridaException() {
        super("La carta de aceptación de la empresa es obligatoria para proyectos de Práctica Profesional");
    }

    public CartaAceptacionRequeridaException(String modalidad) {
        super(String.format("La carta de aceptación es obligatoria para proyectos de modalidad: %s", modalidad));
    }
}


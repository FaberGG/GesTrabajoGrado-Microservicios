package co.unicauca.submission.domain.exception;

import co.unicauca.submission.domain.model.EstadoProyecto;

/**
 * Excepción lanzada cuando se intenta una operación en un estado inválido.
 */
public class EstadoInvalidoException extends DomainException {

    public EstadoInvalidoException(String message) {
        super(message);
    }

    public EstadoInvalidoException(EstadoProyecto estadoActual, String operacion) {
        super(String.format("No se puede %s en el estado %s", operacion, estadoActual.getDescripcion()));
    }

    public EstadoInvalidoException(EstadoProyecto estadoActual, EstadoProyecto estadoEsperado) {
        super(String.format("Estado actual %s no permite esta operación. Esperado: %s",
                          estadoActual.getDescripcion(), estadoEsperado.getDescripcion()));
    }
}


package co.unicauca.comunicacionmicroservicios.domain.state;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;

/**
 * Clase base abstracta para implementar el patrón State
 * Proporciona implementaciones por defecto que lanzan excepciones
 * Cada estado concreto sobrescribe solo los métodos que le corresponden
 */
public abstract class EstadoSubmissionBase implements IEstadoSubmission {

    @Override
    public void presentarAlCoordinador(ProyectoSubmission proyecto) {
        throw new IllegalStateException(
            "No se puede presentar al coordinador desde el estado: " + getNombreEstado()
        );
    }

    @Override
    public void enviarAComite(ProyectoSubmission proyecto) {
        throw new IllegalStateException(
            "No se puede enviar al comité desde el estado: " + getNombreEstado()
        );
    }

    @Override
    public void evaluar(ProyectoSubmission proyecto, boolean aprobado, String comentarios) {
        throw new IllegalStateException(
            "No se puede evaluar desde el estado: " + getNombreEstado()
        );
    }

    @Override
    public void subirNuevaVersion(ProyectoSubmission proyecto) {
        throw new IllegalStateException(
            "No se puede subir nueva versión desde el estado: " + getNombreEstado()
        );
    }

    @Override
    public boolean esEstadoFinal() {
        return false;
    }

    /**
     * Método auxiliar para cambiar el estado del proyecto
     */
    protected void cambiarEstado(ProyectoSubmission proyecto, IEstadoSubmission nuevoEstado) {
        System.out.println("Transición de estado: " + getNombreEstado() + " -> " + nuevoEstado.getNombreEstado());
        proyecto.setEstadoActual(nuevoEstado);
    }
}


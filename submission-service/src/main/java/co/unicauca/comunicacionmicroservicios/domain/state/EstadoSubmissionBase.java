package co.unicauca.comunicacionmicroservicios.domain.state;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;

/**
 * Clase base abstracta para implementar el patron State
 * Proporciona implementaciones por defecto que lanzan excepciones
 * Cada estado concreto sobrescribe solo los metodos que le corresponden
 *
 * Flujo de estados:
 * - Formato A (RF-3): El COORDINADOR evalua el Formato A
 * - Anteproyecto (RF-6, RF-7, RF-8): Jefe asigna evaluadores, evaluadores evaluan
 */
public abstract class EstadoSubmissionBase implements IEstadoSubmission {

    // ==========================================
    // OPERACIONES PARA FORMATO A
    // ==========================================

    @Override
    public void presentarAlCoordinador(ProyectoSubmission proyecto) {
        throw new IllegalStateException(
            "No se puede presentar al coordinador desde el estado: " + getNombreEstado()
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
            "No se puede subir nueva version desde el estado: " + getNombreEstado()
        );
    }

    // ==========================================
    // OPERACIONES PARA ANTEPROYECTO
    // ==========================================

    @Override
    public void subirAnteproyecto(ProyectoSubmission proyecto, String rutaAnteproyecto) {
        throw new IllegalStateException(
            "No se puede subir anteproyecto desde el estado: " + getNombreEstado()
        );
    }

    @Override
    public void asignarEvaluadores(ProyectoSubmission proyecto, Long evaluador1Id, Long evaluador2Id) {
        throw new IllegalStateException(
            "No se pueden asignar evaluadores desde el estado: " + getNombreEstado()
        );
    }

    @Override
    public void evaluarAnteproyecto(ProyectoSubmission proyecto, boolean aprobado, String comentarios) {
        throw new IllegalStateException(
            "No se puede evaluar anteproyecto desde el estado: " + getNombreEstado()
        );
    }

    // ==========================================
    // METODOS DE INFORMACION
    // ==========================================

    @Override
    public boolean esEstadoFinal() {
        return false;
    }

    /**
     * Metodo auxiliar para cambiar el estado del proyecto
     */
    protected void cambiarEstado(ProyectoSubmission proyecto, IEstadoSubmission nuevoEstado) {
        System.out.println("Transicion de estado: " + getNombreEstado() + " -> " + nuevoEstado.getNombreEstado());
        proyecto.setEstadoActual(nuevoEstado);
    }
}


package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado: Formato A Aprobado
 * Estado FINAL - El formato A ha sido aprobado por el COORDINADOR
 * Permite avanzar a la etapa de Anteproyecto
 */
public class FormatoAAprobadoState extends EstadoSubmissionBase {

    private static FormatoAAprobadoState instance;

    private FormatoAAprobadoState() {}

    public static FormatoAAprobadoState getInstance() {
        if (instance == null) {
            instance = new FormatoAAprobadoState();
        }
        return instance;
    }

    @Override
    public String getNombreEstado() {
        return "FORMATO_A_APROBADO";
    }

    @Override
    public boolean esEstadoFinal() {
        return true;
    }

    // No sobrescribe ningún método de transición, por lo que cualquier intento
    // de cambiar el estado lanzará IllegalStateException desde la clase base
}


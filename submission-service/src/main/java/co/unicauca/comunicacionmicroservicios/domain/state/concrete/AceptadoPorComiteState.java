package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado 5: Aceptado Por Comité
 * Estado FINAL - El formato A ha sido aprobado por el comité
 * No permite más transiciones
 */
public class AceptadoPorComiteState extends EstadoSubmissionBase {

    private static AceptadoPorComiteState instance;

    private AceptadoPorComiteState() {}

    public static AceptadoPorComiteState getInstance() {
        if (instance == null) {
            instance = new AceptadoPorComiteState();
        }
        return instance;
    }

    @Override
    public String getNombreEstado() {
        return "ACEPTADO_POR_COMITE";
    }

    @Override
    public boolean esEstadoFinal() {
        return true;
    }

    // No sobrescribe ningún método de transición, por lo que cualquier intento
    // de cambiar el estado lanzará IllegalStateException desde la clase base
}


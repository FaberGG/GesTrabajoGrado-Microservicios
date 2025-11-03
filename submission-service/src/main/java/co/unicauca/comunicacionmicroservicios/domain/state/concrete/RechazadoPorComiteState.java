package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado 6: Rechazado Por Comité
 * Estado FINAL - El formato A ha sido rechazado definitivamente
 * Se alcanza cuando numeroIntentos >= 3
 * No permite más transiciones
 */
public class RechazadoPorComiteState extends EstadoSubmissionBase {

    private static RechazadoPorComiteState instance;

    private RechazadoPorComiteState() {}

    public static RechazadoPorComiteState getInstance() {
        if (instance == null) {
            instance = new RechazadoPorComiteState();
        }
        return instance;
    }

    @Override
    public String getNombreEstado() {
        return "RECHAZADO_POR_COMITE";
    }

    @Override
    public boolean esEstadoFinal() {
        return true;
    }

    // No sobrescribe ningún método de transición, por lo que cualquier intento
    // de cambiar el estado lanzará IllegalStateException desde la clase base
}


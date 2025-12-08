package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado: Formato A Rechazado (Definitivo)
 * Estado FINAL - El formato A ha sido rechazado definitivamente
 * Se alcanza cuando numeroIntentos >= 3
 * No permite más transiciones
 */
public class FormatoARechazadoState extends EstadoSubmissionBase {

    private static FormatoARechazadoState instance;

    private FormatoARechazadoState() {}

    public static FormatoARechazadoState getInstance() {
        if (instance == null) {
            instance = new FormatoARechazadoState();
        }
        return instance;
    }

    @Override
    public String getNombreEstado() {
        return "FORMATO_A_RECHAZADO";
    }

    @Override
    public boolean esEstadoFinal() {
        return true;
    }

    // No sobrescribe ningún método de transición, por lo que cualquier intento
    // de cambiar el estado lanzará IllegalStateException desde la clase base
}


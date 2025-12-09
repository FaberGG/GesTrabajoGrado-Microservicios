package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado FINAL: Formato A Rechazado
 * El Formato A ha sido rechazado definitivamente despues de alcanzar el limite de 3 intentos.
 * No permite mas transiciones.
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
}


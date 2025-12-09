package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado FINAL: Anteproyecto Rechazado
 * El anteproyecto ha sido rechazado por los evaluadores.
 * No permite mas transiciones.
 */
public class AnteproyectoRechazadoState extends EstadoSubmissionBase {

    private static AnteproyectoRechazadoState instance;

    private AnteproyectoRechazadoState() {}

    public static AnteproyectoRechazadoState getInstance() {
        if (instance == null) {
            instance = new AnteproyectoRechazadoState();
        }
        return instance;
    }

    @Override
    public String getNombreEstado() {
        return "ANTEPROYECTO_RECHAZADO";
    }

    @Override
    public boolean esEstadoFinal() {
        return true;
    }
}


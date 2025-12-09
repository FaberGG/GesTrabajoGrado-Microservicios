package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado FINAL: Anteproyecto Aprobado
 * El anteproyecto ha sido aprobado por los 2 evaluadores.
 * El proyecto puede continuar con las siguientes fases (defensa, etc.).
 * No permite mas transiciones.
 */
public class AnteproyectoAprobadoState extends EstadoSubmissionBase {

    private static AnteproyectoAprobadoState instance;

    private AnteproyectoAprobadoState() {}

    public static AnteproyectoAprobadoState getInstance() {
        if (instance == null) {
            instance = new AnteproyectoAprobadoState();
        }
        return instance;
    }

    @Override
    public String getNombreEstado() {
        return "ANTEPROYECTO_APROBADO";
    }

    @Override
    public boolean esEstadoFinal() {
        return true;
    }
}


package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado 2: Presentado al Coordinador
 * El formato A ha sido presentado y espera revisión del coordinador
 * Puede transicionar a: EnEvaluacionComiteState
 */
public class PresentadoAlCoordinadorState extends EstadoSubmissionBase {

    private static PresentadoAlCoordinadorState instance;

    private PresentadoAlCoordinadorState() {}

    public static PresentadoAlCoordinadorState getInstance() {
        if (instance == null) {
            instance = new PresentadoAlCoordinadorState();
        }
        return instance;
    }

    @Override
    public void enviarAComite(ProyectoSubmission proyecto) {
        System.out.println("📨 Coordinador envía el Formato A al comité de evaluación...");
        cambiarEstado(proyecto, EnEvaluacionComiteState.getInstance());
    }

    @Override
    public String getNombreEstado() {
        return "PRESENTADO_AL_COORDINADOR";
    }
}


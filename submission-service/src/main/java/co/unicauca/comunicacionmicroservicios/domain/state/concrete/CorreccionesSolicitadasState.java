package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado: Correcciones Solicitadas
 * El coordinador ha solicitado correcciones al Formato A.
 * El docente puede subir una nueva version.
 *
 * Transiciones posibles:
 * - subirNuevaVersion() hacia EnEvaluacionCoordinadorState
 */
public class CorreccionesSolicitadasState extends EstadoSubmissionBase {

    private static CorreccionesSolicitadasState instance;

    private CorreccionesSolicitadasState() {}

    public static CorreccionesSolicitadasState getInstance() {
        if (instance == null) {
            instance = new CorreccionesSolicitadasState();
        }
        return instance;
    }

    @Override
    public void subirNuevaVersion(ProyectoSubmission proyecto) {
        System.out.println("Subiendo nueva version del Formato A tras correcciones...");
        System.out.println("Enviando nuevamente al coordinador para evaluacion (Intento " +
                         proyecto.getNumeroIntentos() + "/3)");
        cambiarEstado(proyecto, EnEvaluacionCoordinadorState.getInstance());
    }

    @Override
    public String getNombreEstado() {
        return "CORRECCIONES_SOLICITADAS";
    }
}


package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado: Correcciones Solicitadas
 * El COORDINADOR ha solicitado correcciones al Formato A
 * El docente puede subir una nueva versión
 * Puede transicionar a: EnEvaluacionCoordinadorState (al subir nueva versión)
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
        System.out.println("📤 Subiendo nueva versión del Formato A tras correcciones...");
        System.out.println("🔄 Enviando nuevamente al coordinador para evaluación (Intento " +
                         (proyecto.getNumeroIntentos() + 1) + "/3)");
        cambiarEstado(proyecto, EnEvaluacionCoordinadorState.getInstance());
    }

    @Override
    public String getNombreEstado() {
        return "CORRECCIONES_SOLICITADAS";
    }
}


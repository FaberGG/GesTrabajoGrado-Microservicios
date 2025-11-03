package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado 4: Correcciones Comité
 * El comité ha solicitado correcciones al Formato A
 * El docente puede subir una nueva versión
 * Puede transicionar a: EnEvaluacionComiteState (al subir nueva versión)
 */
public class CorreccionesComiteState extends EstadoSubmissionBase {

    private static CorreccionesComiteState instance;

    private CorreccionesComiteState() {}

    public static CorreccionesComiteState getInstance() {
        if (instance == null) {
            instance = new CorreccionesComiteState();
        }
        return instance;
    }

    @Override
    public void subirNuevaVersion(ProyectoSubmission proyecto) {
        System.out.println("📤 Subiendo nueva versión del Formato A tras correcciones...");
        System.out.println("🔄 Enviando nuevamente al comité para evaluación (Intento " +
                         (proyecto.getNumeroIntentos() + 1) + "/3)");
        cambiarEstado(proyecto, EnEvaluacionComiteState.getInstance());
    }

    @Override
    public String getNombreEstado() {
        return "CORRECCIONES_COMITE";
    }
}


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
package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado: En Evaluación Coordinador
 * El COORDINADOR está evaluando el Formato A (según RF-3)
 * Puede transicionar a:
 * - FormatoAAprobadoState (si aprueba)
 * - CorreccionesSolicitadasState (si rechaza y numeroIntentos < 3)
 * - FormatoARechazadoState (si rechaza y numeroIntentos >= 3)
 */
public class EnEvaluacionCoordinadorState extends EstadoSubmissionBase {

    private static EnEvaluacionCoordinadorState instance;

    private EnEvaluacionCoordinadorState() {}

    public static EnEvaluacionCoordinadorState getInstance() {
        if (instance == null) {
            instance = new EnEvaluacionCoordinadorState();
        }
        return instance;
    }

    @Override
    public void evaluar(ProyectoSubmission proyecto, boolean aprobado, String comentarios) {
        proyecto.setComentariosComite(comentarios);

        if (aprobado) {
            System.out.println("✅ Formato A APROBADO por el coordinador");
            cambiarEstado(proyecto, FormatoAAprobadoState.getInstance());
        } else {
            // Incrementar número de intentos
            proyecto.setNumeroIntentos(proyecto.getNumeroIntentos() + 1);
            System.out.println("❌ Formato A RECHAZADO por el coordinador (Intento " +
                             proyecto.getNumeroIntentos() + "/3)");

            // Verificar si se alcanzó el límite de intentos
            if (proyecto.getNumeroIntentos() >= 3) {
                System.out.println("🚫 Se alcanzó el límite de 3 intentos. Proyecto RECHAZADO definitivamente.");
                cambiarEstado(proyecto, FormatoARechazadoState.getInstance());
            } else {
                System.out.println("📝 Se requieren correcciones. Puede subir una nueva versión.");
                cambiarEstado(proyecto, CorreccionesSolicitadasState.getInstance());
            }
        }
    }

    @Override
    public String getNombreEstado() {
        return "EN_EVALUACION_COORDINADOR";
    }
}


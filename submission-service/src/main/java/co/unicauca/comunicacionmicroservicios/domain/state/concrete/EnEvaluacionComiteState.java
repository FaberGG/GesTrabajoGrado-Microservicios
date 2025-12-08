package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado 3: En Evaluación Comité
 * El comité está evaluando el Formato A
 * Puede transicionar a:
 * - AceptadoPorComiteState (si aprueba)
 * - CorreccionesComiteState (si rechaza y numeroIntentos < 3)
 * - RechazadoPorComiteState (si rechaza y numeroIntentos >= 3)
 */
public class EnEvaluacionComiteState extends EstadoSubmissionBase {

    private static EnEvaluacionComiteState instance;

    private EnEvaluacionComiteState() {}

    public static EnEvaluacionComiteState getInstance() {
        if (instance == null) {
            instance = new EnEvaluacionComiteState();
        }
        return instance;
    }

    @Override
    public void evaluar(ProyectoSubmission proyecto, boolean aprobado, String comentarios) {
        proyecto.setComentariosComite(comentarios);

        if (aprobado) {
            System.out.println("✅ Formato A APROBADO por el comité");
            cambiarEstado(proyecto, AceptadoPorComiteState.getInstance());
        } else {
            // Incrementar número de intentos
            proyecto.setNumeroIntentos(proyecto.getNumeroIntentos() + 1);
            System.out.println("❌ Formato A RECHAZADO por el comité (Intento " +
                             proyecto.getNumeroIntentos() + "/3)");

            // Verificar si se alcanzó el límite de intentos
            if (proyecto.getNumeroIntentos() >= 3) {
                System.out.println("🚫 Se alcanzó el límite de 3 intentos. Proyecto RECHAZADO definitivamente.");
                cambiarEstado(proyecto, RechazadoPorComiteState.getInstance());
            } else {
                System.out.println("📝 Se requieren correcciones. Puede subir una nueva versión.");
                cambiarEstado(proyecto, CorreccionesComiteState.getInstance());
            }
        }
    }

    @Override
    public String getNombreEstado() {
        return "EN_EVALUACION_COMITE";
    }
}


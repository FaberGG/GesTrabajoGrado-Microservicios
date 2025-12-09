package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado: En Evaluacion Coordinador
 * El coordinador esta evaluando el Formato A para decidir si lo aprueba o solicita correcciones.
 *
 * Transiciones posibles:
 * - evaluar(aprobado=true) hacia FormatoAAprobadoState
 * - evaluar(aprobado=false, intentos menores a 3) hacia CorreccionesSolicitadasState
 * - evaluar(aprobado=false, intentos >= 3) hacia FormatoARechazadoState
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
            System.out.println("Formato A APROBADO por el coordinador");
            cambiarEstado(proyecto, FormatoAAprobadoState.getInstance());
        } else {
            // Incrementar numero de intentos
            proyecto.setNumeroIntentos(proyecto.getNumeroIntentos() + 1);
            System.out.println("Formato A RECHAZADO por el coordinador (Intento " +
                             proyecto.getNumeroIntentos() + "/3)");

            // Verificar si se alcanzo el limite de intentos
            if (proyecto.getNumeroIntentos() >= 3) {
                System.out.println("Se alcanzo el limite de 3 intentos. Proyecto RECHAZADO definitivamente.");
                cambiarEstado(proyecto, FormatoARechazadoState.getInstance());
            } else {
                System.out.println("Se requieren correcciones. Puede subir una nueva version.");
                cambiarEstado(proyecto, CorreccionesSolicitadasState.getInstance());
            }
        }
    }

    @Override
    public String getNombreEstado() {
        return "EN_EVALUACION_COORDINADOR";
    }
}


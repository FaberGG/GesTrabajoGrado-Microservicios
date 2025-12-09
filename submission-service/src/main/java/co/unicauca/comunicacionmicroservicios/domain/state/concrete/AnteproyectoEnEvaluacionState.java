package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado: Anteproyecto En Evaluacion
 * Los 2 evaluadores asignados por el Jefe de Departamento están evaluando el anteproyecto.
 *
 * Transiciones posibles:
 * - evaluarAnteproyecto(aprobado=true) hacia AnteproyectoAprobadoState
 * - evaluarAnteproyecto(aprobado=false) hacia AnteproyectoRechazadoState
 */
public class AnteproyectoEnEvaluacionState extends EstadoSubmissionBase {

    private static AnteproyectoEnEvaluacionState instance;

    private AnteproyectoEnEvaluacionState() {}

    public static AnteproyectoEnEvaluacionState getInstance() {
        if (instance == null) {
            instance = new AnteproyectoEnEvaluacionState();
        }
        return instance;
    }

    @Override
    public void evaluarAnteproyecto(ProyectoSubmission proyecto, boolean aprobado, String comentarios) {
        proyecto.setComentariosAnteproyecto(comentarios);

        if (aprobado) {
            System.out.println("Anteproyecto APROBADO por los evaluadores");
            cambiarEstado(proyecto, AnteproyectoAprobadoState.getInstance());
        } else {
            System.out.println("Anteproyecto RECHAZADO por los evaluadores");
            System.out.println("Comentarios: " + comentarios);
            cambiarEstado(proyecto, AnteproyectoRechazadoState.getInstance());
        }
    }

    @Override
    public String getNombreEstado() {
        return "ANTEPROYECTO_EN_EVALUACION";
    }
}


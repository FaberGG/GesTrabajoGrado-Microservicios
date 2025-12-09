package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado: Anteproyecto Enviado
 * El docente ha subido el anteproyecto tras la aprobación del Formato A.
 * Espera que el Jefe de Departamento asigne 2 evaluadores.
 *
 * Transiciones posibles:
 * - asignarEvaluadores() hacia AnteproyectoEnEvaluacionState
 */
public class AnteproyectoEnviadoState extends EstadoSubmissionBase {

    private static AnteproyectoEnviadoState instance;

    private AnteproyectoEnviadoState() {}

    public static AnteproyectoEnviadoState getInstance() {
        if (instance == null) {
            instance = new AnteproyectoEnviadoState();
        }
        return instance;
    }

    @Override
    public void asignarEvaluadores(ProyectoSubmission proyecto, Long evaluador1Id, Long evaluador2Id) {
        // Validar que se asignen exactamente 2 evaluadores
        if (evaluador1Id == null || evaluador2Id == null) {
            throw new IllegalStateException("Se deben asignar exactamente 2 evaluadores");
        }

        // Validar que los evaluadores sean diferentes
        if (evaluador1Id.equals(evaluador2Id)) {
            throw new IllegalStateException("Los dos evaluadores deben ser diferentes");
        }

        // Validar que los evaluadores no sean el director o codirector
        if (evaluador1Id.equals(proyecto.getDocenteDirectorId()) ||
            evaluador2Id.equals(proyecto.getDocenteDirectorId())) {
            throw new IllegalStateException("El director no puede ser evaluador");
        }

        if (proyecto.getDocenteCodirectorId() != null) {
            if (evaluador1Id.equals(proyecto.getDocenteCodirectorId()) ||
                evaluador2Id.equals(proyecto.getDocenteCodirectorId())) {
                throw new IllegalStateException("El codirector no puede ser evaluador");
            }
        }

        proyecto.setEvaluador1Id(evaluador1Id);
        proyecto.setEvaluador2Id(evaluador2Id);

        System.out.println("Evaluadores asignados: " + evaluador1Id + " y " + evaluador2Id);
        System.out.println("Anteproyecto en evaluacion por los evaluadores asignados...");
        cambiarEstado(proyecto, AnteproyectoEnEvaluacionState.getInstance());
    }

    @Override
    public String getNombreEstado() {
        return "ANTEPROYECTO_ENVIADO";
    }
}


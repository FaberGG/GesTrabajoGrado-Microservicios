package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado 1: Formato A Diligenciado
 * Estado inicial cuando el docente ha llenado el Formato A
 *
 * Transiciones posibles:
 * - presentarAlCoordinador() hacia EnEvaluacionCoordinadorState
 */
public class FormatoADiligenciadoState extends EstadoSubmissionBase {

    private static FormatoADiligenciadoState instance;

    private FormatoADiligenciadoState() {}

    public static FormatoADiligenciadoState getInstance() {
        if (instance == null) {
            instance = new FormatoADiligenciadoState();
        }
        return instance;
    }

    @Override
    public void presentarAlCoordinador(ProyectoSubmission proyecto) {
        // Validar que el formato A este completo
        if (proyecto.getTitulo() == null || proyecto.getTitulo().isEmpty()) {
            throw new IllegalStateException("El proyecto debe tener un titulo antes de presentarse");
        }

        System.out.println("Presentando Formato A al coordinador para evaluacion...");
        // Transicion directa a evaluacion por el coordinador (RF-3)
        cambiarEstado(proyecto, EnEvaluacionCoordinadorState.getInstance());
    }

    @Override
    public String getNombreEstado() {
        return "FORMATO_A_DILIGENCIADO";
    }
}


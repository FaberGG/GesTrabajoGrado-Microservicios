package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado 1: Formato A Diligenciado
 * Estado inicial cuando el docente ha llenado el Formato A
 * Puede transicionar a: PresentadoAlCoordinadorState
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
        // Validar que el formato A esté completo
        if (proyecto.getTitulo() == null || proyecto.getTitulo().isEmpty()) {
            throw new IllegalStateException("El proyecto debe tener un título antes de presentarse");
        }

        System.out.println("📤 Presentando Formato A al coordinador...");
        cambiarEstado(proyecto, PresentadoAlCoordinadorState.getInstance());
    }

    @Override
    public String getNombreEstado() {
        return "FORMATO_A_DILIGENCIADO";
    }
}


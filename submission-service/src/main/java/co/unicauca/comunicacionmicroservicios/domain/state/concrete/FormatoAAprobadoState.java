package co.unicauca.comunicacionmicroservicios.domain.state.concrete;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.state.EstadoSubmissionBase;

/**
 * Estado: Formato A Aprobado
 * El Formato A ha sido aprobado por el coordinador.
 * Desde este estado, el docente puede subir el anteproyecto.
 *
 * Transiciones posibles:
 * - subirAnteproyecto() hacia AnteproyectoEnviadoState
 */
public class FormatoAAprobadoState extends EstadoSubmissionBase {

    private static FormatoAAprobadoState instance;

    private FormatoAAprobadoState() {}

    public static FormatoAAprobadoState getInstance() {
        if (instance == null) {
            instance = new FormatoAAprobadoState();
        }
        return instance;
    }

    @Override
    public void subirAnteproyecto(ProyectoSubmission proyecto, String rutaAnteproyecto) {
        // Validar que se proporcione la ruta del archivo
        if (rutaAnteproyecto == null || rutaAnteproyecto.trim().isEmpty()) {
            throw new IllegalStateException("La ruta del anteproyecto es obligatoria");
        }

        proyecto.setRutaAnteproyecto(rutaAnteproyecto);
        proyecto.setFechaEnvioAnteproyecto(java.time.LocalDateTime.now());

        System.out.println("Anteproyecto subido exitosamente");
        System.out.println("Enviando al Jefe de Departamento para asignacion de evaluadores...");
        cambiarEstado(proyecto, AnteproyectoEnviadoState.getInstance());
    }

    @Override
    public String getNombreEstado() {
        return "FORMATO_A_APROBADO";
    }

    @Override
    public boolean esEstadoFinal() {
        // Ya no es estado final porque puede transicionar a ANTEPROYECTO_ENVIADO
        return false;
    }
}


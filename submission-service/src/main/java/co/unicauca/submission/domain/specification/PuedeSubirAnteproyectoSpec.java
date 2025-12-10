package co.unicauca.submission.domain.specification;

import co.unicauca.submission.domain.model.EstadoProyecto;
import co.unicauca.submission.domain.model.Proyecto;

/**
 * Specification: Verifica si se puede subir el anteproyecto.
 *
 * Reglas:
 * - El Formato A debe estar APROBADO
 * - No debe existir anteproyecto previo
 */
public class PuedeSubirAnteproyectoSpec implements Specification<Proyecto> {

    @Override
    public boolean isSatisfiedBy(Proyecto proyecto) {
        if (proyecto == null) {
            return false;
        }

        // El Formato A debe estar aprobado
        if (!proyecto.getEstado().equals(EstadoProyecto.FORMATO_A_APROBADO)) {
            return false;
        }

        // No debe existir anteproyecto previo
        if (proyecto.getAnteproyecto() != null) {
            return false;
        }

        return true;
    }

    @Override
    public String getRazonRechazo(Proyecto proyecto) {
        if (proyecto == null) {
            return "El proyecto no existe";
        }

        if (!proyecto.getEstado().equals(EstadoProyecto.FORMATO_A_APROBADO)) {
            return "El Formato A debe estar aprobado antes de subir el anteproyecto. Estado actual: "
                   + proyecto.getEstado().getDescripcion();
        }

        if (proyecto.getAnteproyecto() != null) {
            return "Ya existe un anteproyecto para este proyecto";
        }

        return null;
    }
}


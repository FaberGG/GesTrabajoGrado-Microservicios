package co.unicauca.submission.domain.specification;

import co.unicauca.submission.domain.model.EstadoProyecto;
import co.unicauca.submission.domain.model.Proyecto;

/**
 * Specification: Verifica si se puede reenviar el Formato A.
 *
 * Reglas:
 * - Debe estar en estado CORRECCIONES_SOLICITADAS
 * - No debe haber alcanzado el máximo de intentos (3)
 */
public class PuedeReenviarFormatoASpec implements Specification<Proyecto> {

    @Override
    public boolean isSatisfiedBy(Proyecto proyecto) {
        if (proyecto == null) {
            return false;
        }

        // Debe estar en estado de correcciones solicitadas
        if (!proyecto.getEstado().equals(EstadoProyecto.CORRECCIONES_SOLICITADAS)) {
            return false;
        }

        // No debe haber alcanzado el máximo de intentos
        if (proyecto.getFormatoA().haAlcanzadoMaximoIntentos()) {
            return false;
        }

        return true;
    }

    @Override
    public String getRazonRechazo(Proyecto proyecto) {
        if (proyecto == null) {
            return "El proyecto no existe";
        }

        if (!proyecto.getEstado().equals(EstadoProyecto.CORRECCIONES_SOLICITADAS)) {
            return "El proyecto no está en estado de correcciones solicitadas. Estado actual: "
                   + proyecto.getEstado().getDescripcion();
        }

        if (proyecto.getFormatoA().haAlcanzadoMaximoIntentos()) {
            return "Se alcanzó el máximo de 3 intentos para el Formato A";
        }

        return null;
    }
}


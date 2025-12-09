package co.unicauca.submission.domain.specification;

import co.unicauca.submission.domain.model.Proyecto;

/**
 * Specification: Verifica si un usuario es el director del proyecto.
 *
 * Útil para validaciones de permisos antes de ejecutar operaciones
 * que solo puede realizar el director.
 */
public class EsDirectorDelProyectoSpec implements Specification<Proyecto> {

    private final Long userId;

    public EsDirectorDelProyectoSpec(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El userId no puede ser null");
        }
        this.userId = userId;
    }

    @Override
    public boolean isSatisfiedBy(Proyecto proyecto) {
        if (proyecto == null) {
            return false;
        }

        return proyecto.getParticipantes().esDirector(userId);
    }

    @Override
    public String getRazonRechazo(Proyecto proyecto) {
        if (proyecto == null) {
            return "El proyecto no existe";
        }

        if (!isSatisfiedBy(proyecto)) {
            return "El usuario " + userId + " no es el director del proyecto";
        }

        return null;
    }
}


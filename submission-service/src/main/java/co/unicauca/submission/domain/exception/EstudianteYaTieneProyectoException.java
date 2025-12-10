package co.unicauca.submission.domain.exception;

/**
 * Excepción lanzada cuando un estudiante intenta participar en más de un proyecto activo.
 *
 * Regla de negocio: Un estudiante solo puede estar en UN proyecto activo a la vez.
 */
public class EstudianteYaTieneProyectoException extends ReglaNegocioException {

    private final Long estudianteId;
    private final Long proyectoExistenteId;

    public EstudianteYaTieneProyectoException(Long estudianteId, Long proyectoExistenteId) {
        super(String.format("El estudiante con ID %d ya tiene un proyecto activo (ID: %d). " +
                           "Un estudiante solo puede participar en un proyecto a la vez.",
                           estudianteId, proyectoExistenteId));
        this.estudianteId = estudianteId;
        this.proyectoExistenteId = proyectoExistenteId;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public Long getProyectoExistenteId() {
        return proyectoExistenteId;
    }
}


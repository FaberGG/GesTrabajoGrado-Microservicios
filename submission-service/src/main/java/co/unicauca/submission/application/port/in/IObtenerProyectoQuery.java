package co.unicauca.submission.application.port.in;

import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.domain.exception.ProyectoNotFoundException;
import co.unicauca.submission.domain.model.EstadoProyecto;

/**
 * Puerto de entrada (Query) para obtener información de un proyecto.
 * RF5: Yo como estudiante necesito entrar a la plataforma y ver el estado de mi proyecto de grado.
 */
public interface IObtenerProyectoQuery {

    /**
     * Obtiene un proyecto por su ID.
     *
     * @param proyectoId ID del proyecto
     * @return Información del proyecto
     * @throws ProyectoNotFoundException si el proyecto no existe
     */
    ProyectoResponse obtenerPorId(Long proyectoId);

    /**
     * Obtiene los proyectos de un estudiante específico.
     *
     * @param estudianteId ID del estudiante
     * @return Lista de proyectos del estudiante
     */
    java.util.List<ProyectoResponse> obtenerPorEstudiante(Long estudianteId);

    /**
     * Obtiene los proyectos de un director específico.
     *
     * @param directorId ID del director
     * @return Lista de proyectos del director
     */
    java.util.List<ProyectoResponse> obtenerPorDirector(Long directorId);

    /**
     * Obtiene proyectos filtrados por estado.
     *
     * @param estado Estado del proyecto
     * @return Lista de proyectos en ese estado
     */
    java.util.List<ProyectoResponse> obtenerPorEstado(EstadoProyecto estado);

    /**
     * Obtiene todos los proyectos.
     *
     * @return Lista de todos los proyectos
     */
    java.util.List<ProyectoResponse> obtenerTodos();
}

package co.unicauca.submission.application.port.out;

import co.unicauca.submission.domain.model.Proyecto;
import co.unicauca.submission.domain.model.ProyectoId;
import co.unicauca.submission.domain.model.EstadoProyecto;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de proyectos.
 * Define el contrato que debe implementar el adaptador de persistencia.
 */
public interface IProyectoRepositoryPort {

    /**
     * Guarda un proyecto (crear o actualizar).
     */
    Proyecto save(Proyecto proyecto);

    /**
     * Busca un proyecto por su ID.
     */
    Optional<Proyecto> findById(ProyectoId id);

    /**
     * Busca proyectos por estado.
     */
    List<Proyecto> findByEstado(EstadoProyecto estado);

    /**
     * Busca proyectos por director.
     */
    List<Proyecto> findByDirectorId(Long directorId);

    /**
     * Busca proyectos por estudiante (estudiante1 o estudiante2).
     */
    List<Proyecto> findByEstudianteId(Long estudianteId);

    /**
     * Verifica si existe un proyecto con el ID dado.
     */
    boolean existsById(ProyectoId id);

    /**
     * Elimina un proyecto (si es necesario).
     */
    void delete(ProyectoId id);
}


package co.unicauca.comunicacionmicroservicios.infrastructure.persistence;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para ProyectoSubmission
 */
@Repository
public interface SubmissionRepository extends JpaRepository<ProyectoSubmission, Long> {

    /**
     * Buscar proyectos por estado
     */
    List<ProyectoSubmission> findByEstadoNombre(String estadoNombre);

    /**
     * Buscar proyectos por docente director
     */
    List<ProyectoSubmission> findByDocenteDirectorId(Long docenteId);

    /**
     * Buscar proyectos por estudiante (puede ser estudiante1 o estudiante2)
     */
    @Query("SELECT p FROM ProyectoSubmission p WHERE p.estudiante1Id = :estudianteId OR p.estudiante2Id = :estudianteId")
    List<ProyectoSubmission> findByEstudianteId(Long estudianteId);

    /**
     * Buscar proyectos que no están en estado final
     * Soporta tanto nombres nuevos como legacy para compatibilidad
     */
    @Query("SELECT p FROM ProyectoSubmission p WHERE p.estadoNombre NOT IN ('FORMATO_A_APROBADO', 'FORMATO_A_RECHAZADO', 'ACEPTADO_POR_COMITE', 'RECHAZADO_POR_COMITE')")
    List<ProyectoSubmission> findProyectosEnProceso();

    /**
     * Contar proyectos por estado
     */
    Long countByEstadoNombre(String estadoNombre);
}


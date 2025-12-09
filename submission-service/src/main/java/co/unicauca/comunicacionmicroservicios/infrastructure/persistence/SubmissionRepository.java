package co.unicauca.comunicacionmicroservicios.infrastructure.persistence;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Estados finales: FORMATO_A_RECHAZADO, ANTEPROYECTO_APROBADO, ANTEPROYECTO_RECHAZADO
     */
    @Query("SELECT p FROM ProyectoSubmission p WHERE p.estadoNombre NOT IN ('FORMATO_A_RECHAZADO', 'ANTEPROYECTO_APROBADO', 'ANTEPROYECTO_RECHAZADO', 'RECHAZADO_POR_COMITE')")
    List<ProyectoSubmission> findProyectosEnProceso();

    /**
     * Contar proyectos por estado
     */
    Long countByEstadoNombre(String estadoNombre);

    /**
     * Buscar Formatos A pendientes de evaluación
     * Incluye compatibilidad con estados antiguos en BD
     */
    @Query("SELECT p FROM ProyectoSubmission p WHERE p.estadoNombre IN ('FORMATO_A_DILIGENCIADO', 'EN_EVALUACION_COORDINADOR', 'CORRECCIONES_SOLICITADAS', 'PRESENTADO_AL_COORDINADOR', 'EN_EVALUACION_COMITE', 'CORRECCIONES_COMITE') ORDER BY p.fechaCreacion ASC")
    Page<ProyectoSubmission> findFormatosAPendientes(Pageable pageable);

    /**
     * Buscar anteproyectos pendientes de asignación de evaluadores
     * Estado: ANTEPROYECTO_ENVIADO
     */
    @Query("SELECT p FROM ProyectoSubmission p WHERE p.estadoNombre = 'ANTEPROYECTO_ENVIADO' ORDER BY p.fechaEnvioAnteproyecto ASC")
    List<ProyectoSubmission> findAnteproyectosPendientesAsignacion();

    /**
     * Buscar anteproyectos en evaluación
     * Estado: ANTEPROYECTO_EN_EVALUACION
     */
    @Query("SELECT p FROM ProyectoSubmission p WHERE p.estadoNombre = 'ANTEPROYECTO_EN_EVALUACION'")
    List<ProyectoSubmission> findAnteproyectosEnEvaluacion();

    /**
     * Buscar anteproyectos asignados a un evaluador específico
     */
    @Query("SELECT p FROM ProyectoSubmission p WHERE (p.evaluador1Id = :evaluadorId OR p.evaluador2Id = :evaluadorId) AND p.estadoNombre = 'ANTEPROYECTO_EN_EVALUACION'")
    List<ProyectoSubmission> findAnteproyectosPorEvaluador(Long evaluadorId);
}

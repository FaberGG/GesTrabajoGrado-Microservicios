package edu.unicauca.progresstracking.domain.repository;

import edu.unicauca.progresstracking.domain.entity.ProyectoEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para ProyectoEstado (Vista Materializada)
 */
@Repository
public interface ProyectoEstadoRepository extends JpaRepository<ProyectoEstado, Long> {

    /**
     * Buscar proyectos donde el usuario es director, codirector o estudiante
     */
    @Query("SELECT p FROM ProyectoEstado p WHERE p.directorId = :userId OR p.codirectorId = :userId OR p.estudiante1Id = :userId OR p.estudiante2Id = :userId")
    List<ProyectoEstado> findProyectosByUsuario(@Param("userId") Long userId);

    /**
     * Buscar proyecto por ID de estudiante
     */
    @Query("SELECT p FROM ProyectoEstado p WHERE p.estudiante1Id = :estudianteId OR p.estudiante2Id = :estudianteId")
    Optional<ProyectoEstado> findByEstudianteId(@Param("estudianteId") Long estudianteId);

    /**
     * Buscar proyectos por estado actual
     */
    List<ProyectoEstado> findByEstadoActual(String estadoActual);

    /**
     * Buscar proyectos por fase
     */
    List<ProyectoEstado> findByFase(String fase);

    /**
     * Buscar proyectos por programa
     */
    List<ProyectoEstado> findByPrograma(String programa);

    /**
     * Buscar proyectos por múltiples criterios
     */
    @Query("SELECT p FROM ProyectoEstado p WHERE " +
            "(:estadoActual IS NULL OR p.estadoActual = :estadoActual) AND " +
            "(:fase IS NULL OR p.fase = :fase) AND " +
            "(:programa IS NULL OR p.programa = :programa)")
    List<ProyectoEstado> buscarProyectos(
            @Param("estadoActual") String estadoActual,
            @Param("fase") String fase,
            @Param("programa") String programa
    );
}
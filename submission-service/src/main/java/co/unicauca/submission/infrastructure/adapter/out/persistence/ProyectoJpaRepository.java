package co.unicauca.submission.infrastructure.adapter.out.persistence;

import co.unicauca.submission.domain.model.EstadoProyecto;
import co.unicauca.submission.infrastructure.adapter.out.persistence.entity.ProyectoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository para ProyectoEntity.
 * Spring Data genera automáticamente la implementación.
 */
@Repository
public interface ProyectoJpaRepository extends JpaRepository<ProyectoEntity, Long> {

    /**
     * Busca proyectos por estado.
     */
    List<ProyectoEntity> findByEstado(EstadoProyecto estado);

    /**
     * Busca proyectos por director.
     */
    List<ProyectoEntity> findByDirectorId(Long directorId);

    /**
     * Busca proyectos donde el usuario es estudiante (estudiante1 o estudiante2).
     */
    @Query("SELECT p FROM ProyectoEntity p WHERE p.estudiante1Id = :estudianteId OR p.estudiante2Id = :estudianteId")
    List<ProyectoEntity> findByEstudianteId(@Param("estudianteId") Long estudianteId);
}


package co.unicauca.review.repository;

import co.unicauca.review.entity.AsignacionEvaluadores;
import co.unicauca.review.enums.AsignacionEstado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsignacionEvaluadoresRepository extends JpaRepository<AsignacionEvaluadores, Long> {

    Optional<AsignacionEvaluadores> findByAnteproyectoId(Long anteproyectoId);

    boolean existsByAnteproyectoId(Long anteproyectoId);

    Page<AsignacionEvaluadores> findByEstado(AsignacionEstado estado, Pageable pageable);

    @Query("SELECT a FROM AsignacionEvaluadores a WHERE " +
           "(a.evaluador1Id = :evaluadorId OR a.evaluador2Id = :evaluadorId) " +
           "AND (:estado IS NULL OR a.estado = :estado)")
    Page<AsignacionEvaluadores> findByEvaluador(
            @Param("evaluadorId") Long evaluadorId,
            @Param("estado") AsignacionEstado estado,
            Pageable pageable);
}

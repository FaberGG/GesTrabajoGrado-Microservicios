package edu.unicauca.progresstracking.domain.repository;

import edu.unicauca.progresstracking.domain.entity.HistorialEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para HistorialEvento (Timeline de eventos)
 */
@Repository
public interface HistorialEventoRepository extends JpaRepository<HistorialEvento, Long> {

    /**
     * Obtener eventos de un proyecto ordenados por fecha descendente
     */
    List<HistorialEvento> findByProyectoIdOrderByFechaDesc(Long proyectoId);

    /**
     * Obtener eventos de un proyecto con paginación
     */
    Page<HistorialEvento> findByProyectoIdOrderByFechaDesc(Long proyectoId, Pageable pageable);

    /**
     * Filtrar eventos por tipo
     */
    @Query("SELECT h FROM HistorialEvento h WHERE h.proyectoId = :proyectoId AND h.tipoEvento IN :tipos ORDER BY h.fecha DESC")
    Page<HistorialEvento> findByProyectoIdAndTipoEventoIn(
            @Param("proyectoId") Long proyectoId,
            @Param("tipos") List<String> tipos,
            Pageable pageable
    );

    /**
     * Filtrar eventos por rango de fechas
     */
    @Query("SELECT h FROM HistorialEvento h WHERE h.proyectoId = :proyectoId AND h.fecha BETWEEN :desde AND :hasta ORDER BY h.fecha DESC")
    Page<HistorialEvento> findByProyectoIdAndFechaBetween(
            @Param("proyectoId") Long proyectoId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );

    /**
     * Obtener el último evento de un proyecto
     */
    @Query("SELECT h FROM HistorialEvento h WHERE h.proyectoId = :proyectoId ORDER BY h.fecha DESC LIMIT 1")
    HistorialEvento findUltimoEventoByProyectoId(@Param("proyectoId") Long proyectoId);

    /**
     * Contar eventos por proyecto
     */
    Long countByProyectoId(Long proyectoId);
}
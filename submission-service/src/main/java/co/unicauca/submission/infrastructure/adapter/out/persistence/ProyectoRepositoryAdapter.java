package co.unicauca.submission.infrastructure.adapter.out.persistence;

import co.unicauca.submission.application.port.out.IProyectoRepositoryPort;
import co.unicauca.submission.domain.model.EstadoProyecto;
import co.unicauca.submission.domain.model.Proyecto;
import co.unicauca.submission.domain.model.ProyectoId;
import co.unicauca.submission.infrastructure.adapter.out.persistence.entity.ProyectoEntity;
import co.unicauca.submission.infrastructure.adapter.out.persistence.mapper.ProyectoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia que implementa el puerto IProyectoRepositoryPort.
 * Traduce entre el dominio y la infraestructura JPA.
 *
 * Responsabilidad:
 * - Convertir Domain Model ↔ JPA Entity
 * - Delegar operaciones CRUD a Spring Data JPA
 */
@Component
public class ProyectoRepositoryAdapter implements IProyectoRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(ProyectoRepositoryAdapter.class);

    private final ProyectoJpaRepository jpaRepository;
    private final ProyectoMapper mapper;

    public ProyectoRepositoryAdapter(ProyectoJpaRepository jpaRepository, ProyectoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Proyecto save(Proyecto proyecto) {
        log.debug("Guardando proyecto en BD");

        // Convertir Domain → Entity
        ProyectoEntity entity = mapper.toEntity(proyecto);

        // Guardar en BD
        ProyectoEntity savedEntity = jpaRepository.save(entity);

        log.debug("Proyecto guardado con ID: {}", savedEntity.getId());

        // Convertir Entity → Domain y retornar
        Proyecto savedProyecto = mapper.toDomain(savedEntity);

        // Asegurar que el ID está seteado
        savedProyecto.setId(ProyectoId.of(savedEntity.getId()));

        return savedProyecto;
    }

    @Override
    public Optional<Proyecto> findById(ProyectoId id) {
        log.debug("Buscando proyecto por ID: {}", id.getValue());

        return jpaRepository.findById(id.getValue())
            .map(entity -> {
                Proyecto proyecto = mapper.toDomain(entity);
                proyecto.setId(ProyectoId.of(entity.getId()));
                return proyecto;
            });
    }

    @Override
    public List<Proyecto> findByEstado(EstadoProyecto estado) {
        log.debug("Buscando proyectos por estado: {}", estado);

        return jpaRepository.findByEstado(estado).stream()
            .map(entity -> {
                Proyecto proyecto = mapper.toDomain(entity);
                proyecto.setId(ProyectoId.of(entity.getId()));
                return proyecto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Page<Proyecto> findByEstadoPage(EstadoProyecto estado, Pageable pageable) {
        log.debug("Buscando proyectos por estado con paginación: {}, page: {}, size: {}",
                estado, pageable.getPageNumber(), pageable.getPageSize());

        Page<ProyectoEntity> entityPage = jpaRepository.findByEstado(estado, pageable);

        return entityPage.map(entity -> {
            Proyecto proyecto = mapper.toDomain(entity);
            proyecto.setId(ProyectoId.of(entity.getId()));
            return proyecto;
        });
    }

    @Override
    public List<Proyecto> findByDirectorId(Long directorId) {
        log.debug("Buscando proyectos del director: {}", directorId);

        return jpaRepository.findByDirectorId(directorId).stream()
            .map(entity -> {
                Proyecto proyecto = mapper.toDomain(entity);
                proyecto.setId(ProyectoId.of(entity.getId()));
                return proyecto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<Proyecto> findByEstudianteId(Long estudianteId) {
        log.debug("Buscando proyectos del estudiante: {}", estudianteId);

        return jpaRepository.findByEstudianteId(estudianteId).stream()
            .map(entity -> {
                Proyecto proyecto = mapper.toDomain(entity);
                proyecto.setId(ProyectoId.of(entity.getId()));
                return proyecto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(ProyectoId id) {
        log.debug("Verificando existencia del proyecto: {}", id.getValue());
        return jpaRepository.existsById(id.getValue());
    }

    @Override
    public void delete(ProyectoId id) {
        log.debug("Eliminando proyecto: {}", id.getValue());
        jpaRepository.deleteById(id.getValue());
    }
}


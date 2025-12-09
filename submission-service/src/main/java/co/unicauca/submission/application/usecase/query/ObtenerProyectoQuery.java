package co.unicauca.submission.application.usecase.query;

import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.IObtenerProyectoQuery;
import co.unicauca.submission.application.port.out.IProyectoRepositoryPort;
import co.unicauca.submission.domain.exception.ProyectoNotFoundException;
import co.unicauca.submission.domain.model.EstadoProyecto;
import co.unicauca.submission.domain.model.Proyecto;
import co.unicauca.submission.domain.model.ProyectoId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query: Obtener Proyecto
 * RF5: Yo como estudiante necesito entrar a la plataforma y ver el estado de mi proyecto de grado.
 *
 * Implementa operaciones de solo lectura (queries) sobre proyectos.
 */
@Service
@Transactional(readOnly = true)
public class ObtenerProyectoQuery implements IObtenerProyectoQuery {

    private static final Logger log = LoggerFactory.getLogger(ObtenerProyectoQuery.class);

    private final IProyectoRepositoryPort repositoryPort;

    public ObtenerProyectoQuery(IProyectoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public ProyectoResponse obtenerPorId(Long proyectoId) {
        log.debug("Consultando proyecto por ID: {}", proyectoId);

        Proyecto proyecto = repositoryPort.findById(ProyectoId.of(proyectoId))
            .orElseThrow(() -> new ProyectoNotFoundException(proyectoId));

        return ProyectoResponse.fromDomain(proyecto);
    }

    @Override
    public List<ProyectoResponse> obtenerPorEstudiante(Long estudianteId) {
        log.debug("Consultando proyectos del estudiante: {}", estudianteId);

        List<Proyecto> proyectos = repositoryPort.findByEstudianteId(estudianteId);

        return proyectos.stream()
            .map(ProyectoResponse::fromDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ProyectoResponse> obtenerPorDirector(Long directorId) {
        log.debug("Consultando proyectos del director: {}", directorId);

        List<Proyecto> proyectos = repositoryPort.findByDirectorId(directorId);

        return proyectos.stream()
            .map(ProyectoResponse::fromDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ProyectoResponse> obtenerPorEstado(EstadoProyecto estado) {
        log.debug("Consultando proyectos por estado: {}", estado);

        List<Proyecto> proyectos = repositoryPort.findByEstado(estado);

        return proyectos.stream()
            .map(ProyectoResponse::fromDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ProyectoResponse> obtenerTodos() {
        log.debug("Consultando todos los proyectos");

        // Para obtener todos, usamos un estado común o implementamos un findAll en el puerto
        // Por ahora, retornamos lista de todos los estados
        List<Proyecto> proyectos = java.util.Arrays.stream(EstadoProyecto.values())
            .flatMap(estado -> repositoryPort.findByEstado(estado).stream())
            .distinct()
            .collect(Collectors.toList());

        return proyectos.stream()
            .map(ProyectoResponse::fromDomain)
            .collect(Collectors.toList());
    }
}

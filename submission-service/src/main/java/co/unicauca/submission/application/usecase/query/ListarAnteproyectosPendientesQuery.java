package co.unicauca.submission.application.usecase.query;

import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.IListarAnteproyectosPendientesQuery;
import co.unicauca.submission.application.port.out.IProyectoRepositoryPort;
import co.unicauca.submission.domain.model.EstadoProyecto;
import co.unicauca.submission.domain.model.Proyecto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Query: Listar Anteproyectos Pendientes de Asignación
 * RF8: El jefe de departamento necesita ver anteproyectos pendientes para asignar evaluadores.
 *
 * Implementa la consulta de proyectos que están en estado ANTEPROYECTO_ENVIADO
 * (pendientes de asignación de evaluadores).
 */
@Service
@Transactional(readOnly = true)
public class ListarAnteproyectosPendientesQuery implements IListarAnteproyectosPendientesQuery {

    private static final Logger log = LoggerFactory.getLogger(ListarAnteproyectosPendientesQuery.class);

    private final IProyectoRepositoryPort repositoryPort;

    public ListarAnteproyectosPendientesQuery(IProyectoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Page<ProyectoResponse> listarPendientes(Pageable pageable) {
        log.info("Listando Anteproyectos pendientes de asignación - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Obtener proyectos en estado ANTEPROYECTO_ENVIADO con paginación
        Page<Proyecto> proyectosPage = repositoryPort.findByEstadoPage(
                EstadoProyecto.ANTEPROYECTO_ENVIADO,
                pageable
        );

        log.info("Se encontraron {} Anteproyectos pendientes de {} totales",
                proyectosPage.getNumberOfElements(), proyectosPage.getTotalElements());

        // Convertir a DTOs de respuesta
        return proyectosPage.map(ProyectoResponse::fromDomain);
    }
}


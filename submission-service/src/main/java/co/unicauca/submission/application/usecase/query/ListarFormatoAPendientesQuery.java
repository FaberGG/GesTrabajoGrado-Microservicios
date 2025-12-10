package co.unicauca.submission.application.usecase.query;

import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.IListarFormatoAPendientesQuery;
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
 * Query: Listar Formatos A Pendientes
 * RF3: Yo como coordinador necesito ver los Formatos A pendientes por evaluar.
 *
 * Implementa la consulta de proyectos que están en estado EN_EVALUACION_COORDINADOR.
 */
@Service
@Transactional(readOnly = true)
public class ListarFormatoAPendientesQuery implements IListarFormatoAPendientesQuery {

    private static final Logger log = LoggerFactory.getLogger(ListarFormatoAPendientesQuery.class);

    private final IProyectoRepositoryPort repositoryPort;

    public ListarFormatoAPendientesQuery(IProyectoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Page<ProyectoResponse> listarPendientes(Pageable pageable) {
        log.info("Listando Formatos A pendientes de evaluación - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Obtener proyectos en estado EN_EVALUACION_COORDINADOR con paginación
        Page<Proyecto> proyectosPage = repositoryPort.findByEstadoPage(
                EstadoProyecto.EN_EVALUACION_COORDINADOR,
                pageable
        );

        log.info("Se encontraron {} Formatos A pendientes de {} totales",
                proyectosPage.getNumberOfElements(), proyectosPage.getTotalElements());

        // Convertir a DTOs de respuesta
        return proyectosPage.map(ProyectoResponse::fromDomain);
    }
}


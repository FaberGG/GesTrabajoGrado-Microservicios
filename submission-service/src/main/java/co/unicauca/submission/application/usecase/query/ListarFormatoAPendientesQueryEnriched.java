package co.unicauca.submission.application.usecase.query;

import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.IListarFormatoAPendientesQuery;
import co.unicauca.submission.application.port.out.IIdentityServicePort;
import co.unicauca.submission.application.port.out.IProyectoRepositoryPort;
import co.unicauca.submission.domain.model.EstadoProyecto;
import co.unicauca.submission.domain.model.Proyecto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Query: Listar Formatos A Pendientes con información enriquecida
 * RF3: Yo como coordinador necesito ver los Formatos A pendientes por evaluar.
 *
 * Implementa la consulta de proyectos que están en estado EN_EVALUACION_COORDINADOR
 * e incluye información adicional de los participantes desde identity-service.
 */
@Service("listarFormatoAPendientesQueryEnriched")
@Transactional(readOnly = true)
public class ListarFormatoAPendientesQueryEnriched implements IListarFormatoAPendientesQuery {

    private static final Logger log = LoggerFactory.getLogger(ListarFormatoAPendientesQueryEnriched.class);

    private final IProyectoRepositoryPort repositoryPort;
    private final IIdentityServicePort identityServicePort;

    public ListarFormatoAPendientesQueryEnriched(
            IProyectoRepositoryPort repositoryPort,
            IIdentityServicePort identityServicePort
    ) {
        this.repositoryPort = repositoryPort;
        this.identityServicePort = identityServicePort;
    }

    @Override
    public Page<ProyectoResponse> listarPendientes(Pageable pageable) {
        log.info("Listando Formatos A pendientes de evaluación (enriquecido) - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Obtener proyectos en estado EN_EVALUACION_COORDINADOR con paginación
        Page<Proyecto> proyectosPage = repositoryPort.findByEstadoPage(
                EstadoProyecto.EN_EVALUACION_COORDINADOR,
                pageable
        );

        log.info("Se encontraron {} Formatos A pendientes de {} totales",
                proyectosPage.getNumberOfElements(), proyectosPage.getTotalElements());

        // Convertir a DTOs de respuesta y enriquecer con información de usuarios
        return proyectosPage.map(proyecto -> {
            ProyectoResponse response = ProyectoResponse.fromDomain(proyecto);
            enrichWithUserInfo(response, proyecto);
            return response;
        });
    }

    /**
     * Enriquece el ProyectoResponse con información de usuarios del identity-service.
     */
    private void enrichWithUserInfo(ProyectoResponse response, Proyecto proyecto) {
        try {
            // Obtener información del director
            Long directorId = proyecto.getParticipantes().getDirectorId();
            if (directorId != null) {
                try {
                    var directorInfo = identityServicePort.obtenerUsuario(directorId);
                    response.setDocenteDirectorNombre(directorInfo.nombreCompleto());
                    response.setDocenteDirectorEmail(directorInfo.email());
                } catch (Exception e) {
                    log.warn("No se pudo obtener información del director {}: {}", directorId, e.getMessage());
                    response.setDocenteDirectorNombre("Docente " + directorId);
                    response.setDocenteDirectorEmail("director" + directorId + "@unicauca.edu.co");
                }
            }

            // Obtener emails de estudiantes
            List<String> estudiantesEmails = new ArrayList<>();

            Long est1Id = proyecto.getParticipantes().getEstudiante1Id();
            if (est1Id != null) {
                try {
                    var est1Info = identityServicePort.obtenerUsuario(est1Id);
                    estudiantesEmails.add(est1Info.email());
                } catch (Exception e) {
                    log.warn("No se pudo obtener información del estudiante {}: {}", est1Id, e.getMessage());
                    estudiantesEmails.add("estudiante" + est1Id + "@unicauca.edu.co");
                }
            }

            Long est2Id = proyecto.getParticipantes().getEstudiante2Id();
            if (est2Id != null) {
                try {
                    var est2Info = identityServicePort.obtenerUsuario(est2Id);
                    estudiantesEmails.add(est2Info.email());
                } catch (Exception e) {
                    log.warn("No se pudo obtener información del estudiante {}: {}", est2Id, e.getMessage());
                    estudiantesEmails.add("estudiante" + est2Id + "@unicauca.edu.co");
                }
            }

            response.setEstudiantesEmails(estudiantesEmails);

            // Mapear fechaCreacion a fechaEnvio para compatibilidad con review-service
            response.setFechaEnvio(proyecto.getFechaCreacion());

        } catch (Exception e) {
            log.error("Error enriqueciendo información de usuarios para proyecto {}: {}",
                    response.getId(), e.getMessage());
            // Continuar sin enriquecer (campos quedarán null)
        }
    }
}


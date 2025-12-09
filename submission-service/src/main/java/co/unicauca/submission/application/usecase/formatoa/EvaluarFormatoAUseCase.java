package co.unicauca.submission.application.usecase.formatoa;

import co.unicauca.submission.application.dto.request.EvaluarFormatoARequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.IEvaluarFormatoAUseCase;
import co.unicauca.submission.application.port.out.IEventPublisherPort;
import co.unicauca.submission.application.port.out.IIdentityServicePort;
import co.unicauca.submission.application.port.out.IProyectoRepositoryPort;
import co.unicauca.submission.domain.event.DomainEvent;
import co.unicauca.submission.domain.exception.UsuarioNoAutorizadoException;
import co.unicauca.submission.domain.model.Proyecto;
import co.unicauca.submission.domain.model.ProyectoId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case: Evaluar Formato A
 * RF3: Yo como coordinador de programa necesito evaluar un formato A para aprobar, rechazar y dejar observaciones.
 *
 * Flujo:
 * 1. Validar que el usuario es COORDINADOR
 * 2. Obtener el proyecto
 * 3. Evaluar en el aggregate (delegar lógica al dominio)
 * 4. Persistir cambios
 * 5. Publicar eventos
 * 6. Retornar response
 *
 * Nota: La notificación a estudiantes/docentes la envía el review-service al escuchar el evento.
 */
@Service
@Transactional
public class EvaluarFormatoAUseCase implements IEvaluarFormatoAUseCase {

    private static final Logger log = LoggerFactory.getLogger(EvaluarFormatoAUseCase.class);

    private final IProyectoRepositoryPort repositoryPort;
    private final IEventPublisherPort eventPublisherPort;
    private final IIdentityServicePort identityServicePort;

    public EvaluarFormatoAUseCase(
            IProyectoRepositoryPort repositoryPort,
            IEventPublisherPort eventPublisherPort,
            IIdentityServicePort identityServicePort
    ) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.identityServicePort = identityServicePort;
    }

    @Override
    public ProyectoResponse evaluar(Long proyectoId, EvaluarFormatoARequest request, Long evaluadorId) {
        log.info("Iniciando evaluación de Formato A - ProyectoID: {}, Evaluador: {}, Aprobado: {}",
                proyectoId, evaluadorId, request.isAprobado());

        // 1. Validar que el usuario es COORDINADOR
        if (!identityServicePort.tieneRol(evaluadorId, "COORDINATOR")) {
            throw new UsuarioNoAutorizadoException(
                "evaluar Formato A", "COORDINATOR"
            );
        }

        // 2. Obtener el proyecto
        Proyecto proyecto = repositoryPort.findById(ProyectoId.of(proyectoId))
            .orElseThrow(() -> new co.unicauca.submission.domain.exception.ProyectoNotFoundException(proyectoId));

        log.debug("Proyecto encontrado. Estado actual: {}, Intento: {}",
                 proyecto.getEstado(), proyecto.getFormatoA().getNumeroIntento());

        // 3. Evaluar el Formato A (delegar al aggregate - toda la lógica está allí)
        proyecto.evaluarFormatoA(
            request.isAprobado(),
            request.getComentarios(),
            evaluadorId
        );

        log.debug("Evaluación aplicada. Nuevo estado: {}", proyecto.getEstado());

        // 4. Persistir cambios
        Proyecto proyectoActualizado = repositoryPort.save(proyecto);

        // 5. Publicar eventos de dominio
        // El evento FormatoAEvaluado será escuchado por otros servicios (review-service)
        // para enviar notificaciones a docentes y estudiantes
        List<DomainEvent> eventos = proyectoActualizado.obtenerEventosPendientes();
        if (!eventos.isEmpty()) {
            eventPublisherPort.publishAll(eventos);
            proyectoActualizado.limpiarEventos();
            log.debug("Publicados {} eventos de dominio", eventos.size());
        }

        // 6. Retornar response
        ProyectoResponse response = ProyectoResponse.fromDomain(proyectoActualizado);

        log.info("Formato A evaluado exitosamente - ProyectoID: {}, Estado: {}, Aprobado: {}",
                proyectoId, response.getEstado(), request.isAprobado());

        return response;
    }
}


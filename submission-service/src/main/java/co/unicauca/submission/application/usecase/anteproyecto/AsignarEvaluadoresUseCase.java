package co.unicauca.submission.application.usecase.anteproyecto;

import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.out.IEventPublisherPort;
import co.unicauca.submission.application.port.out.INotificationPort;
import co.unicauca.submission.application.port.out.IProyectoRepositoryPort;
import co.unicauca.submission.domain.event.DomainEvent;
import co.unicauca.submission.domain.model.Proyecto;
import co.unicauca.submission.domain.model.ProyectoId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case: Asignar Evaluadores al Anteproyecto
 * RF8: Yo como jefe de departamento necesito delegar dos docentes del departamento para que evalúen un anteproyecto.
 *
 * Flujo:
 * 1. Obtener el proyecto
 * 2. Asignar evaluadores en el aggregate
 * 3. Persistir cambios
 * 4. Publicar eventos
 * 5. Enviar notificación a los evaluadores
 */
@Service
@Transactional
public class AsignarEvaluadoresUseCase {

    private static final Logger log = LoggerFactory.getLogger(AsignarEvaluadoresUseCase.class);

    private final IProyectoRepositoryPort repositoryPort;
    private final IEventPublisherPort eventPublisherPort;
    private final INotificationPort notificationPort;

    public AsignarEvaluadoresUseCase(
            IProyectoRepositoryPort repositoryPort,
            IEventPublisherPort eventPublisherPort,
            INotificationPort notificationPort
    ) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.notificationPort = notificationPort;
    }

    /**
     * Asigna dos evaluadores a un anteproyecto.
     *
     * @param proyectoId ID del proyecto
     * @param evaluador1Id ID del primer evaluador
     * @param evaluador2Id ID del segundo evaluador
     * @param jefeDepartamentoId ID del jefe de departamento que asigna
     * @return Proyecto actualizado
     */
    public ProyectoResponse asignar(Long proyectoId, Long evaluador1Id, Long evaluador2Id, Long jefeDepartamentoId) {
        log.info("Iniciando asignación de evaluadores - ProyectoID: {}, Eval1: {}, Eval2: {}",
                proyectoId, evaluador1Id, evaluador2Id);

        // 1. Obtener el proyecto
        Proyecto proyecto = repositoryPort.findById(ProyectoId.of(proyectoId))
            .orElseThrow(() -> new co.unicauca.submission.domain.exception.ProyectoNotFoundException(proyectoId));

        log.debug("Proyecto encontrado. Estado actual: {}", proyecto.getEstado());

        // 2. Asignar evaluadores en el aggregate (validaciones dentro del dominio)
        proyecto.asignarEvaluadores(evaluador1Id, evaluador2Id);

        log.debug("Evaluadores asignados. Nuevo estado: {}", proyecto.getEstado());

        // 3. Persistir cambios
        Proyecto proyectoActualizado = repositoryPort.save(proyecto);

        // 4. Publicar eventos de dominio
        List<DomainEvent> eventos = proyectoActualizado.obtenerEventosPendientes();
        if (!eventos.isEmpty()) {
            eventPublisherPort.publishAll(eventos);
            proyectoActualizado.limpiarEventos();
            log.debug("Publicados {} eventos de dominio", eventos.size());
        }

        // 5. Enviar notificación a los evaluadores (RF8)
        try {
            notificationPort.notificarEvaluadoresAsignados(proyectoId, evaluador1Id, evaluador2Id);
            log.debug("Notificaciones enviadas a los evaluadores");
        } catch (Exception e) {
            log.error("Error al enviar notificaciones (no crítico): {}", e.getMessage());
        }

        // 6. Retornar response
        ProyectoResponse response = ProyectoResponse.fromDomain(proyectoActualizado);

        log.info("Evaluadores asignados exitosamente - ProyectoID: {}, Estado: {}",
                proyectoId, response.getEstado());

        return response;
    }
}


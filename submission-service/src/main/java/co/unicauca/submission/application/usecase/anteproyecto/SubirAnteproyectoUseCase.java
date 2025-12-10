package co.unicauca.submission.application.usecase.anteproyecto;

import co.unicauca.submission.application.dto.request.SubirAnteproyectoRequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.ISubirAnteproyectoUseCase;
import co.unicauca.submission.application.port.out.*;
import co.unicauca.submission.domain.event.DomainEvent;
import co.unicauca.submission.domain.model.ArchivoAdjunto;
import co.unicauca.submission.domain.model.Proyecto;
import co.unicauca.submission.domain.model.ProyectoId;
import co.unicauca.submission.domain.specification.EsDirectorDelProyectoSpec;
import co.unicauca.submission.domain.specification.PuedeSubirAnteproyectoSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case: Subir Anteproyecto
 * RF6: Yo como docente necesito subir el anteproyecto para continuar con el proceso de proyecto de grado.
 *
 * Flujo:
 * 1. Obtener el proyecto
 * 2. Validar que el usuario es el director (usando Specification)
 * 3. Validar que puede subir anteproyecto (Formato A aprobado)
 * 4. Guardar archivo PDF
 * 5. Subir anteproyecto en el aggregate
 * 6. Persistir cambios
 * 7. Publicar eventos
 * 8. Enviar notificación al jefe de departamento
 */
@Service
@Transactional
public class SubirAnteproyectoUseCase implements ISubirAnteproyectoUseCase {

    private static final Logger log = LoggerFactory.getLogger(SubirAnteproyectoUseCase.class);

    private final IProyectoRepositoryPort repositoryPort;
    private final IFileStoragePort fileStoragePort;
    private final IEventPublisherPort eventPublisherPort;
    private final INotificationPort notificationPort;
    private final co.unicauca.submission.infrastructure.adapter.out.messaging.EventEnricherService eventEnricher;

    public SubirAnteproyectoUseCase(
            IProyectoRepositoryPort repositoryPort,
            IFileStoragePort fileStoragePort,
            IEventPublisherPort eventPublisherPort,
            INotificationPort notificationPort,
            co.unicauca.submission.infrastructure.adapter.out.messaging.EventEnricherService eventEnricher
    ) {
        this.repositoryPort = repositoryPort;
        this.fileStoragePort = fileStoragePort;
        this.eventPublisherPort = eventPublisherPort;
        this.notificationPort = notificationPort;
        this.eventEnricher = eventEnricher;
    }

    @Override
    public ProyectoResponse subir(Long proyectoId, SubirAnteproyectoRequest request, Long userId) {
        log.info("Iniciando subida de anteproyecto - ProyectoID: {}, Usuario: {}", proyectoId, userId);

        // 1. Obtener el proyecto
        Proyecto proyecto = repositoryPort.findById(ProyectoId.of(proyectoId))
            .orElseThrow(() -> new co.unicauca.submission.domain.exception.ProyectoNotFoundException(proyectoId));

        // 2. Validar que el usuario es el director del proyecto
        EsDirectorDelProyectoSpec esDirectorSpec = new EsDirectorDelProyectoSpec(userId);
        if (!esDirectorSpec.isSatisfiedBy(proyecto)) {
            String razon = esDirectorSpec.getRazonRechazo(proyecto);
            throw new co.unicauca.submission.domain.exception.UsuarioNoAutorizadoException(razon);
        }

        // 3. Validar que puede subir anteproyecto (Formato A aprobado, no existe anteproyecto previo)
        PuedeSubirAnteproyectoSpec puedeSubirSpec = new PuedeSubirAnteproyectoSpec();
        if (!puedeSubirSpec.isSatisfiedBy(proyecto)) {
            String razon = puedeSubirSpec.getRazonRechazo(proyecto);
            throw new IllegalStateException(razon);
        }

        // 4. Validar y guardar archivo PDF del anteproyecto
        if (request.getPdfStream() == null) {
            throw new IllegalArgumentException("El PDF del anteproyecto es obligatorio");
        }

        String directorioBase = "proyectos/anteproyecto/" + proyectoId;

        // Validar y preparar nombre del PDF
        String nombrePdf = (request.getPdfNombreArchivo() != null && !request.getPdfNombreArchivo().trim().isEmpty()) ?
                          request.getPdfNombreArchivo() : "anteproyecto.pdf";

        String rutaPdf = fileStoragePort.guardarArchivo(
            request.getPdfStream(),
            nombrePdf,
            directorioBase
        );

        log.debug("PDF del anteproyecto guardado en: {} con nombre: {}", rutaPdf, nombrePdf);

        // 5. Crear Value Object y subir anteproyecto en el aggregate
        // Usar el nombre validado que no es null/empty
        ArchivoAdjunto pdfAnteproyecto = ArchivoAdjunto.pdf(rutaPdf, nombrePdf);
        proyecto.subirAnteproyecto(pdfAnteproyecto, userId);

        log.debug("Anteproyecto subido. Estado: {}", proyecto.getEstado());

        // 6. Persistir cambios
        Proyecto proyectoActualizado = repositoryPort.save(proyecto);

        // 7. Publicar evento enriquecido para progress-tracking
        try {
            // Limpiar eventos básicos del aggregate
            proyectoActualizado.limpiarEventos();

            // Crear y publicar evento enriquecido
            var eventoEnriquecido = eventEnricher.enrichAnteproyectoSubido(proyectoActualizado, rutaPdf);
            eventPublisherPort.publish(eventoEnriquecido);

            log.info("✅ Evento enriquecido AnteproyectoSubido publicado para proyecto {}", proyectoActualizado.getId());
        } catch (Exception e) {
            log.error("⚠️ Error publicando evento enriquecido (no afecta la transacción): {}", e.getMessage());
        }

        // 8. Enviar notificación al jefe de departamento (RF6)
        try {
            notificationPort.notificarJefeDepartamentoAnteproyecto(proyectoId);
            log.debug("Notificación enviada al jefe de departamento");
        } catch (Exception e) {
            log.error("Error al enviar notificación (no crítico): {}", e.getMessage());
        }

        // 9. Retornar response
        ProyectoResponse response = ProyectoResponse.fromDomain(proyectoActualizado);

        log.info("Anteproyecto subido exitosamente - ProyectoID: {}, Estado: {}",
                proyectoId, response.getEstado());

        return response;
    }
}


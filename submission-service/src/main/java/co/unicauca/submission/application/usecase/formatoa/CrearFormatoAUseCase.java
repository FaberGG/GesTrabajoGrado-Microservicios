package co.unicauca.submission.application.usecase.formatoa;

import co.unicauca.submission.application.dto.request.CrearFormatoARequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.ICrearFormatoAUseCase;
import co.unicauca.submission.application.port.out.*;
import co.unicauca.submission.domain.event.DomainEvent;
import co.unicauca.submission.domain.exception.UsuarioNoAutorizadoException;
import co.unicauca.submission.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case: Crear Formato A
 * RF2: Yo como docente necesito subir el formato A para comenzar el proceso de proyecto de grado.
 *
 * Flujo:
 * 1. Validar que el usuario es DOCENTE
 * 2. Guardar archivos PDF (y carta si aplica)
 * 3. Crear el aggregate Proyecto con Formato A
 * 4. Persistir el proyecto
 * 5. Publicar eventos de dominio
 * 6. Enviar notificación al coordinador
 * 7. Retornar response
 */
@Service
@Transactional
public class CrearFormatoAUseCase implements ICrearFormatoAUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrearFormatoAUseCase.class);

    private final IProyectoRepositoryPort repositoryPort;
    private final IFileStoragePort fileStoragePort;
    private final IEventPublisherPort eventPublisherPort;
    private final INotificationPort notificationPort;
    private final IIdentityServicePort identityServicePort;

    public CrearFormatoAUseCase(
            IProyectoRepositoryPort repositoryPort,
            IFileStoragePort fileStoragePort,
            IEventPublisherPort eventPublisherPort,
            INotificationPort notificationPort,
            IIdentityServicePort identityServicePort
    ) {
        this.repositoryPort = repositoryPort;
        this.fileStoragePort = fileStoragePort;
        this.eventPublisherPort = eventPublisherPort;
        this.notificationPort = notificationPort;
        this.identityServicePort = identityServicePort;
    }

    @Override
    public ProyectoResponse crear(CrearFormatoARequest request, Long userId) {
        log.info("Iniciando creación de Formato A - Usuario: {}, Título: {}", userId, request.getTitulo());

        // 1. Validar que el usuario tiene rol DOCENTE
        if (!identityServicePort.tieneRol(userId, "DOCENTE")) {
            throw new UsuarioNoAutorizadoException(
                "crear Formato A", "DOCENTE"
            );
        }

        // 2. Validar y guardar archivo PDF del Formato A
        if (request.getPdfStream() == null) {
            throw new IllegalArgumentException("El PDF del Formato A es obligatorio");
        }

        String directorioBase = "proyectos/formatoA/" + userId;
        String rutaPdf = fileStoragePort.guardarArchivo(
            request.getPdfStream(),
            request.getPdfNombreArchivo() != null ? request.getPdfNombreArchivo() : "formatoA_v1.pdf",
            directorioBase
        );

        log.debug("PDF del Formato A guardado en: {}", rutaPdf);

        // 3. Guardar carta de aceptación si aplica
        String rutaCarta = null;
        if (request.getModalidad().requiereCarta()) {
            if (request.getCartaStream() == null) {
                throw new IllegalArgumentException(
                    "La carta de aceptación es obligatoria para modalidad PRACTICA_PROFESIONAL"
                );
            }
            rutaCarta = fileStoragePort.guardarArchivo(
                request.getCartaStream(),
                request.getCartaNombreArchivo() != null ? request.getCartaNombreArchivo() : "carta_v1.pdf",
                directorioBase
            );
            log.debug("Carta de aceptación guardada en: {}", rutaCarta);
        }

        // 4. Crear Value Objects del dominio
        Titulo titulo = Titulo.of(request.getTitulo());
        ObjetivosProyecto objetivos = ObjetivosProyecto.of(
            request.getObjetivoGeneral(),
            request.getObjetivosEspecificos()
        );
        Participantes participantes = Participantes.of(
            userId, // El usuario que crea es el director
            request.getCodirectorId(),
            request.getEstudiante1Id(),
            request.getEstudiante2Id()
        );
        ArchivoAdjunto pdfFormatoA = ArchivoAdjunto.pdf(rutaPdf, request.getPdfNombreArchivo());
        ArchivoAdjunto carta = rutaCarta != null ?
            ArchivoAdjunto.pdf(rutaCarta, request.getCartaNombreArchivo()) : null;

        // 5. Crear el Aggregate usando Factory Method del dominio
        Proyecto proyecto = Proyecto.crearConFormatoA(
            titulo,
            request.getModalidad(),
            objetivos,
            participantes,
            pdfFormatoA,
            carta
        );

        log.debug("Proyecto creado con estado: {}", proyecto.getEstado());

        // 6. Persistir el proyecto
        Proyecto proyectoGuardado = repositoryPort.save(proyecto);

        log.info("Proyecto guardado con ID: {}", proyectoGuardado.getId().getValue());

        // 7. Publicar eventos de dominio
        List<DomainEvent> eventos = proyectoGuardado.obtenerEventosPendientes();
        if (!eventos.isEmpty()) {
            eventPublisherPort.publishAll(eventos);
            proyectoGuardado.limpiarEventos();
            log.debug("Publicados {} eventos de dominio", eventos.size());
        }

        // 8. Enviar notificación al coordinador (RF2)
        try {
            notificationPort.notificarCoordinadorFormatoAEnviado(
                proyectoGuardado.getId().getValue(),
                1 // Primera versión
            );
            log.debug("Notificación enviada al coordinador");
        } catch (Exception e) {
            log.error("Error al enviar notificación (no crítico): {}", e.getMessage());
            // No lanzamos excepción para no afectar la operación principal
        }

        // 9. Mapear a Response DTO y retornar
        ProyectoResponse response = ProyectoResponse.fromDomain(proyectoGuardado);

        log.info("Formato A creado exitosamente - ProyectoID: {}", response.getId());

        return response;
    }
}


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
    private final IIdentityServicePort identityServicePort;
    private final INotificationPort notificationPort;
    private final co.unicauca.submission.infrastructure.adapter.out.messaging.EventEnricherService eventEnricher;

    public CrearFormatoAUseCase(
            IProyectoRepositoryPort repositoryPort,
            IFileStoragePort fileStoragePort,
            IEventPublisherPort eventPublisherPort,
            IIdentityServicePort identityServicePort,
            INotificationPort notificationPort,
            co.unicauca.submission.infrastructure.adapter.out.messaging.EventEnricherService eventEnricher
    ) {
        this.repositoryPort = repositoryPort;
        this.fileStoragePort = fileStoragePort;
        this.eventPublisherPort = eventPublisherPort;
        this.identityServicePort = identityServicePort;
        this.notificationPort = notificationPort;
        this.eventEnricher = eventEnricher;
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

        // Validar y preparar nombre del PDF
        String nombrePdf = (request.getPdfNombreArchivo() != null && !request.getPdfNombreArchivo().trim().isEmpty()) ?
                          request.getPdfNombreArchivo() : "formatoA_v1.pdf";

        String rutaPdf = fileStoragePort.guardarArchivo(
            request.getPdfStream(),
            nombrePdf,
            directorioBase
        );

        log.debug("PDF del Formato A guardado en: {} con nombre: {}", rutaPdf, nombrePdf);

        // 3. Guardar carta de aceptación si aplica
        String rutaCarta = null;
        String nombreCarta = null;
        if (request.getModalidad().requiereCarta()) {
            if (request.getCartaStream() == null) {
                throw new IllegalArgumentException(
                    "La carta de aceptación es obligatoria para modalidad PRACTICA_PROFESIONAL"
                );
            }

            // Validar y preparar nombre de la carta
            nombreCarta = (request.getCartaNombreArchivo() != null && !request.getCartaNombreArchivo().trim().isEmpty()) ?
                         request.getCartaNombreArchivo() : "carta_v1.pdf";

            rutaCarta = fileStoragePort.guardarArchivo(
                request.getCartaStream(),
                nombreCarta,
                directorioBase
            );
            log.debug("Carta de aceptación guardada en: {} con nombre: {}", rutaCarta, nombreCarta);
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

        // Usar los nombres validados que no son null/empty
        ArchivoAdjunto pdfFormatoA = ArchivoAdjunto.pdf(rutaPdf, nombrePdf);
        ArchivoAdjunto carta = rutaCarta != null ?
            ArchivoAdjunto.pdf(rutaCarta, nombreCarta) : null;

        // 5. Crear el Aggregate usando Factory Method del dominio
        Proyecto proyecto = Proyecto.crearConFormatoA(
            titulo,
            request.getModalidad(),
            objetivos,
            participantes,
            pdfFormatoA,
            carta
        );

        log.debug("Proyecto creado con estado inicial: {}", proyecto.getEstado());

        // 5.1. Presentar automáticamente al coordinador para evaluación
        // Transición: FORMATO_A_DILIGENCIADO → EN_EVALUACION_COORDINADOR
        proyecto.presentarAlCoordinador();

        log.info("Formato A presentado al coordinador - Estado: {}", proyecto.getEstado());

        // 6. Persistir el proyecto
        Proyecto proyectoGuardado = repositoryPort.save(proyecto);

        log.info("Proyecto guardado con ID: {}", proyectoGuardado.getId().getValue());

        // 7. Publicar evento enriquecido para progress-tracking
        try {
            // Limpiar eventos básicos del aggregate (no los publicamos)
            proyectoGuardado.limpiarEventos();

            // Crear y publicar evento enriquecido con información completa
            var eventoEnriquecido = eventEnricher.enrichFormatoACreado(proyectoGuardado);
            eventPublisherPort.publish(eventoEnriquecido);

            log.info("✅ Evento enriquecido FormatoACreado publicado para proyecto {}", proyectoGuardado.getId());
        } catch (Exception e) {
            log.error("⚠️ Error publicando evento enriquecido (no afecta la transacción): {}", e.getMessage());
            // No propagamos la excepción para no afectar la creación del proyecto
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


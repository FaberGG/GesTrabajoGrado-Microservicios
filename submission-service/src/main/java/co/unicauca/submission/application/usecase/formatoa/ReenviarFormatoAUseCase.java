 package co.unicauca.submission.application.usecase.formatoa;

import co.unicauca.submission.application.dto.request.ReenviarFormatoARequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.IReenviarFormatoAUseCase;
import co.unicauca.submission.application.port.out.*;
import co.unicauca.submission.domain.event.DomainEvent;
import co.unicauca.submission.domain.exception.UsuarioNoAutorizadoException;
import co.unicauca.submission.domain.model.ArchivoAdjunto;
import co.unicauca.submission.domain.model.Proyecto;
import co.unicauca.submission.domain.model.ProyectoId;
import co.unicauca.submission.domain.specification.EsDirectorDelProyectoSpec;
import co.unicauca.submission.domain.specification.PuedeReenviarFormatoASpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case: Reenviar Formato A
 * RF4: Yo como docente necesito subir una nueva versión del formato A cuando hubo una evaluación de rechazado.
 * 
 * Flujo:
 * 1. Obtener el proyecto
 * 2. Validar que el usuario es el director
 * 3. Validar que puede reenviar (usando Specification)
 * 4. Guardar nuevos archivos
 * 5. Reenviar en el aggregate
 * 6. Persistir cambios
 * 7. Publicar eventos
 * 8. Enviar notificación al coordinador
 */
@Service
@Transactional
public class ReenviarFormatoAUseCase implements IReenviarFormatoAUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(ReenviarFormatoAUseCase.class);
    
    private final IProyectoRepositoryPort repositoryPort;
    private final IFileStoragePort fileStoragePort;
    private final IEventPublisherPort eventPublisherPort;
    private final INotificationPort notificationPort;
    
    public ReenviarFormatoAUseCase(
            IProyectoRepositoryPort repositoryPort,
            IFileStoragePort fileStoragePort,
            IEventPublisherPort eventPublisherPort,
            INotificationPort notificationPort
    ) {
        this.repositoryPort = repositoryPort;
        this.fileStoragePort = fileStoragePort;
        this.eventPublisherPort = eventPublisherPort;
        this.notificationPort = notificationPort;
    }
    
    @Override
    public ProyectoResponse reenviar(Long proyectoId, ReenviarFormatoARequest request, Long userId) {
        log.info("Iniciando reenvío de Formato A - ProyectoID: {}, Usuario: {}", proyectoId, userId);
        
        // 1. Obtener el proyecto
        Proyecto proyecto = repositoryPort.findById(ProyectoId.of(proyectoId))
            .orElseThrow(() -> new co.unicauca.submission.domain.exception.ProyectoNotFoundException(proyectoId));
        
        // 2. Validar que el usuario es el director del proyecto
        EsDirectorDelProyectoSpec esDirectorSpec = new EsDirectorDelProyectoSpec(userId);
        if (!esDirectorSpec.isSatisfiedBy(proyecto)) {
            throw new UsuarioNoAutorizadoException(
                "Solo el director del proyecto puede reenviar el Formato A"
            );
        }
        
        // 3. Validar que puede reenviar (estado correcto y no excede intentos)
        PuedeReenviarFormatoASpec puedeReenviarSpec = new PuedeReenviarFormatoASpec();
        if (!puedeReenviarSpec.isSatisfiedBy(proyecto)) {
            String razon = puedeReenviarSpec.getRazonRechazo(proyecto);
            throw new IllegalStateException(razon);
        }
        
        // 4. Guardar nuevos archivos
        int nuevoNumeroIntento = proyecto.getFormatoA().getNumeroIntento() + 1;
        String directorioBase = "proyectos/formatoA/" + proyectoId;
        
        String rutaPdf = null;
        String nombrePdf = null;
        if (request.getPdfStream() != null) {
            // Usar nombre del request o generar uno por defecto
            nombrePdf = (request.getPdfNombreArchivo() != null && !request.getPdfNombreArchivo().trim().isEmpty()) ?
                        request.getPdfNombreArchivo() : "formatoA_v" + nuevoNumeroIntento + ".pdf";

            rutaPdf = fileStoragePort.guardarArchivo(
                request.getPdfStream(),
                nombrePdf,
                directorioBase
            );
            log.debug("Nuevo PDF guardado en: {} con nombre: {}", rutaPdf, nombrePdf);
        }
        
        String rutaCarta = null;
        String nombreCarta = null;
        if (request.getCartaStream() != null) {
            // Usar nombre del request o generar uno por defecto
            nombreCarta = (request.getCartaNombreArchivo() != null && !request.getCartaNombreArchivo().trim().isEmpty()) ?
                          request.getCartaNombreArchivo() : "carta_v" + nuevoNumeroIntento + ".pdf";

            rutaCarta = fileStoragePort.guardarArchivo(
                request.getCartaStream(),
                nombreCarta,
                directorioBase
            );
            log.debug("Nueva carta guardada en: {} con nombre: {}", rutaCarta, nombreCarta);
        }
        
        // 5. Crear Value Objects y reenviar en el aggregate
        // Usar los nombres validados que no son null/empty
        ArchivoAdjunto nuevoPdf = rutaPdf != null ?
            ArchivoAdjunto.pdf(rutaPdf, nombrePdf) : null;
        ArchivoAdjunto nuevaCarta = rutaCarta != null ?
            ArchivoAdjunto.pdf(rutaCarta, nombreCarta) : null;

        proyecto.reenviarFormatoA(nuevoPdf, nuevaCarta);
        
        log.debug("Formato A reenviado. Nuevo intento: {}, Estado: {}", 
                 proyecto.getFormatoA().getNumeroIntento(), proyecto.getEstado());
        
        // 6. Persistir cambios
        Proyecto proyectoActualizado = repositoryPort.save(proyecto);
        
        // 7. Publicar eventos de dominio
        List<DomainEvent> eventos = proyectoActualizado.obtenerEventosPendientes();
        if (!eventos.isEmpty()) {
            eventPublisherPort.publishAll(eventos);
            proyectoActualizado.limpiarEventos();
            log.debug("Publicados {} eventos de dominio", eventos.size());
        }
        
        // 8. Enviar notificación al coordinador (RF4)
        try {
            notificationPort.notificarCoordinadorFormatoAEnviado(
                proyectoId,
                proyectoActualizado.getFormatoA().getNumeroIntento()
            );
            log.debug("Notificación de reenvío enviada al coordinador");
        } catch (Exception e) {
            log.error("Error al enviar notificación (no crítico): {}", e.getMessage());
        }
        
        // 9. Retornar response
        ProyectoResponse response = ProyectoResponse.fromDomain(proyectoActualizado);
        
        log.info("Formato A reenviado exitosamente - ProyectoID: {}, Intento: {}", 
                proyectoId, response.getNumeroIntento());
        
        return response;
    }
}


package co.unicauca.review.service.impl;

import co.unicauca.review.client.SubmissionServiceClient;
import co.unicauca.review.dto.EvaluacionRequest;
import co.unicauca.review.dto.response.FormatoAReviewDTO;
import co.unicauca.review.dto.response.NotificationEventDTO;
import co.unicauca.review.entity.Evaluation;
import co.unicauca.review.enums.Decision;
import co.unicauca.review.enums.DocumentType;
import co.unicauca.review.enums.EvaluatorRole;
import co.unicauca.review.exception.InvalidStateException;
import co.unicauca.review.exception.ResourceNotFoundException;
import co.unicauca.review.service.EvaluationTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación concreta del Template Method para evaluación de Formato A.
 * Solo los coordinadores pueden evaluar Formato A.
 */
@Service("formatoAEvaluationService")
public class FormatoAEvaluationService extends EvaluationTemplate {

    @Autowired
    private SubmissionServiceClient submissionClient;

    @Autowired
    private co.unicauca.review.service.EventPublisherService eventPublisher;

    @Override
    protected DocumentInfo fetchDocument(Long documentId) {
        log.debug("Obteniendo información de Formato A con id: {}", documentId);

        try {
            // Primero intentar obtener directamente por ID
            log.debug("Intentando obtener Formato A {} directamente por ID...", documentId);

            try {
                SubmissionServiceClient.FormatoADTO formatoDirecto = submissionClient.getFormatoA(documentId);
                if (formatoDirecto != null) {
                    DocumentInfo doc = new DocumentInfo();
                    doc.setId(formatoDirecto.getId());
                    doc.setTitulo(formatoDirecto.getTitulo());
                    doc.setEstado(formatoDirecto.getEstado());
                    doc.setDocenteDirectorName(formatoDirecto.getDocenteDirectorNombre());
                    doc.setDocenteDirectorEmail(formatoDirecto.getDocenteDirectorEmail());
                    doc.setAutoresEmails(formatoDirecto.getEstudiantesEmails() != null ?
                            formatoDirecto.getEstudiantesEmails() : new ArrayList<>());

                    log.info("✅ Formato A {} obtenido directamente: {}, Estado: {}",
                            documentId, formatoDirecto.getTitulo(), formatoDirecto.getEstado());
                    return doc;
                }
            } catch (Exception directException) {
                log.warn("No se pudo obtener Formato A {} directamente, buscando en pendientes: {}",
                        documentId, directException.getMessage());
            }

            // Si falla, buscar en el listado de pendientes como fallback
            log.debug("Buscando Formato A {} en el listado de pendientes...", documentId);

            for (int pageNum = 0; pageNum < 10; pageNum++) {
                Page<FormatoAReviewDTO> page = submissionClient.getFormatosAPendientes(pageNum, 100);

                Optional<FormatoAReviewDTO> formatoOpt = page.getContent().stream()
                    .filter(f -> f.formatoAId().equals(documentId))
                    .findFirst();

                if (formatoOpt.isPresent()) {
                    FormatoAReviewDTO formato = formatoOpt.get();

                    DocumentInfo doc = new DocumentInfo();
                    doc.setId(formato.formatoAId());
                    doc.setTitulo(formato.titulo());
                    doc.setEstado(formato.estado());
                    doc.setDocenteDirectorName(formato.docenteDirectorNombre());
                    doc.setDocenteDirectorEmail(formato.docenteDirectorEmail());
                    doc.setAutoresEmails(formato.estudiantesEmails());

                    log.info("✅ Formato A {} encontrado en /pendientes: {}, Estado: {}",
                            documentId, formato.titulo(), formato.estado());
                    return doc;
                }

                if (page.isLast()) break;
            }

            log.error("❌ Formato A {} no encontrado", documentId);
            throw new ResourceNotFoundException("Formato A " + documentId + " no encontrado");

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error buscando Formato A {}: {}", documentId, e.getMessage(), e);
            throw new ResourceNotFoundException("Error al buscar Formato A: " + documentId);
        }
    }

    @Override
    protected void validateDocumentState(DocumentInfo document) {
        String estado = document.getEstado();

        // Estados de submission-service (arquitectura hexagonal)
        // Estados finales que NO se pueden evaluar
        if ("FORMATO_A_APROBADO".equals(estado)) {
            throw new InvalidStateException(
                String.format("Formato A ya fue APROBADO anteriormente. No se puede evaluar nuevamente. Estado actual: %s",
                    estado)
            );
        }

        if ("FORMATO_A_RECHAZADO".equals(estado)) {
            throw new InvalidStateException(
                String.format("Formato A ya fue RECHAZADO definitivamente. No se puede evaluar nuevamente. Estado actual: %s",
                    estado)
            );
        }

        // Estados legacy (por compatibilidad)
        if ("APROBADO".equals(estado) || "ACEPTADO_POR_COMITE".equals(estado)) {
            throw new InvalidStateException(
                String.format("Formato A ya fue APROBADO anteriormente. Estado actual: %s", estado)
            );
        }

        if ("RECHAZADO".equals(estado) || "RECHAZADO_POR_COMITE".equals(estado)) {
            throw new InvalidStateException(
                String.format("Formato A ya fue RECHAZADO anteriormente. Estado actual: %s", estado)
            );
        }

        // Estados válidos para evaluación (submission-service hexagonal + legacy):
        // - EN_EVALUACION_COORDINADOR (nuevo estado principal)
        // - FORMATO_A_DILIGENCIADO (estado inicial)
        // - CORRECCIONES_SOLICITADAS (cuando se reenvía)
        // - Estados legacy (por compatibilidad temporal)
        boolean esEstadoValido =
            "EN_EVALUACION_COORDINADOR".equals(estado) ||  // ← Estado principal nuevo
            "FORMATO_A_DILIGENCIADO".equals(estado) ||
            "CORRECCIONES_SOLICITADAS".equals(estado) ||
            // Estados legacy (compatibilidad)
            "PENDIENTE".equals(estado) ||
            "EN_REVISION".equals(estado) ||
            "EN_EVALUACION_COMITE".equals(estado) ||
            "PRESENTADO_AL_COORDINADOR".equals(estado) ||
            "CORRECCIONES_COMITE".equals(estado);

        if (!esEstadoValido) {
            throw new InvalidStateException(
                String.format("Formato A no está en estado evaluable. Estado actual: %s. " +
                    "Estados válidos: EN_EVALUACION_COORDINADOR, FORMATO_A_DILIGENCIADO, CORRECCIONES_SOLICITADAS",
                    estado)
            );
        }

        log.debug("Estado del documento validado correctamente: {}", estado);
    }

    @Override
    protected void updateSubmissionService(Long docId, Decision decision, String obs, Integer evaluatorId) {
        log.info("Actualizando estado de Formato A {} en Submission Service", docId);

        // ⚠️ IMPORTANTE: Obtener información del Formato A ANTES de actualizar submission
        // para capturar el número de intento ANTES del cambio de estado
        SubmissionServiceClient.FormatoADTO formatoAAntes = null;
        try {
            formatoAAntes = submissionClient.getFormatoA(docId);
            log.debug("Formato A obtenido ANTES de actualizar - NumeroIntento: {}",
                     formatoAAntes != null ? formatoAAntes.getNumeroIntento() : "null");
        } catch (Exception e) {
            log.warn("No se pudo obtener Formato A antes de actualizar: {}", e.getMessage());
        }

        // Publicar evento a progress-tracking ANTES de actualizar submission
        // Esto asegura que progress-tracking reciba el número de intento correcto
        publishProgressTrackingEvent(docId, decision, obs, evaluatorId, formatoAAntes);

        // Convertir Decision enum a Boolean para el nuevo API de submission-service
        Boolean aprobado = (decision == Decision.APROBADO);
        String comentarios = obs != null ? obs : "";
        Long evaluadorIdLong = evaluatorId != null ? evaluatorId.longValue() : null;

        log.debug("Enviando evaluación: aprobado={}, comentarios={}, evaluadorId={}",
                  aprobado, comentarios, evaluadorIdLong);

        // Llamar al nuevo endpoint de submission-service (arquitectura hexagonal)
        submissionClient.updateFormatoAEstado(docId, aprobado, comentarios, evaluadorIdLong);

        log.info("✅ Estado actualizado exitosamente en Submission Service: formatoAId={}, aprobado={}",
                docId, aprobado);
    }

    /**
     * Publica evento de Formato A evaluado a progress-tracking.
     * ⚠️ IMPORTANTE: Este método debe llamarse ANTES de actualizar submission
     * para poder obtener la versión actual antes de que cambie el estado.
     */
    private void publishProgressTrackingEvent(Long docId, Decision decision, String obs, Integer evaluatorId,
                                             SubmissionServiceClient.FormatoADTO formatoA) {
        try {
            log.info("📤 Preparando evento formatoa.evaluado para progress-tracking - Proyecto: {}, Decision: {}",
                     docId, decision);

            // Obtener versión actual del Formato A desde el DTO recibido
            Integer version = (formatoA != null && formatoA.getNumeroIntento() != null) ?
                              formatoA.getNumeroIntento() : 1;

            // Determinar si es rechazo definitivo (tercera versión rechazada)
            boolean rechazadoDefinitivo = (decision == Decision.RECHAZADO && version >= 3);

            log.info("📊 Versión Formato A: {}, Rechazo definitivo: {}", version, rechazadoDefinitivo);

            // Construir lista de estudiantes desde el DTO
            List<java.util.Map<String, Object>> estudiantes = new ArrayList<>();
            if (formatoA != null && formatoA.getEstudiantesEmails() != null) {
                for (int i = 0; i < formatoA.getEstudiantesEmails().size(); i++) {
                    String email = formatoA.getEstudiantesEmails().get(i);
                    estudiantes.add(java.util.Map.of(
                        "id", i + 1, // ID temporal
                        "nombre", "Estudiante " + (i + 1), // Nombre temporal
                        "email", email
                    ));
                }
            }

            // Crear y publicar evento
            co.unicauca.review.event.FormatoAEvaluadoEvent evento =
                co.unicauca.review.event.FormatoAEvaluadoEvent.builder()
                    .proyectoId(docId)
                    .resultado(decision.name())
                    .observaciones(obs != null ? obs : "")
                    .version(version)
                    .rechazadoDefinitivo(rechazadoDefinitivo)
                    .usuarioResponsableId(evaluatorId != null ? evaluatorId.longValue() : null)
                    .usuarioResponsableNombre("Coordinador") // TODO: Obtener nombre real
                    .usuarioResponsableRol("COORDINADOR")
                    .estudiantes(estudiantes)
                    .build();

            eventPublisher.publishFormatoAEvaluado(evento);

            log.info("✅ Evento formatoa.evaluado publicado exitosamente para proyecto {}", docId);

        } catch (Exception e) {
            log.error("❌ Error publicando evento a progress-tracking: {}", e.getMessage(), e);
            // No propagar excepción - permitir que la evaluación continúe
        }
    }

    @Override
    protected boolean publishNotificationEvent(Evaluation eval, DocumentInfo doc) {
        log.info("Publicando evento de notificación para Formato A {}", doc.getId());

        NotificationEventDTO event = NotificationEventDTO.builder()
            .eventType("FORMATO_A_EVALUATED")
            .documentId(doc.getId())
            .documentTitle(doc.getTitulo())
            .documentType("FORMATO_A")
            .decision(eval.getDecision().name())
            .evaluatorName(doc.getDocenteDirectorName())
            .evaluatorRole("COORDINADOR")
            .observaciones(eval.getObservaciones())
            .recipients(buildRecipients(doc))
            .timestamp(LocalDateTime.now())
            .build();

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("✓ Evento FORMATO_A_EVALUATED publicado en RabbitMQ: documentId={}, decision={}",
                    doc.getId(), eval.getDecision());
            return true;
        } catch (Exception e) {
            log.error("✗ Error publicando evento en RabbitMQ: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected DocumentType getDocumentType() {
        return DocumentType.FORMATO_A;
    }

    @Override
    protected EvaluatorRole getRequiredRole() {
        return EvaluatorRole.COORDINADOR;
    }

    /**
     * Construye la lista de destinatarios para las notificaciones
     */
    private List<String> buildRecipients(DocumentInfo doc) {
        List<String> recipients = new ArrayList<>();

        // Agregar docente director
        if (doc.getDocenteDirectorEmail() != null) {
            recipients.add(doc.getDocenteDirectorEmail());
        }

        // Agregar estudiantes autores
        if (doc.getAutoresEmails() != null) {
            recipients.addAll(doc.getAutoresEmails());
        }

        log.debug("Destinatarios de notificación: {}", recipients);
        return recipients;
    }
}

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

        // Si ya está aprobado o rechazado, informar que ya fue evaluado
        if ("APROBADO".equals(estado) || "ACEPTADO_POR_COMITE".equals(estado)) {
            throw new InvalidStateException(
                String.format("Formato A ya fue APROBADO anteriormente. No se puede evaluar nuevamente. Estado actual: %s",
                    estado)
            );
        }

        if ("RECHAZADO".equals(estado) || "RECHAZADO_POR_COMITE".equals(estado)) {
            throw new InvalidStateException(
                String.format("Formato A ya fue RECHAZADO anteriormente. No se puede evaluar nuevamente. Estado actual: %s",
                    estado)
            );
        }

        // Aceptar estados que permiten evaluación: PENDIENTE, EN_REVISION, EN_EVALUACION_COMITE,
        // FORMATO_A_DILIGENCIADO, PRESENTADO_AL_COORDINADOR
        if (!"EN_REVISION".equals(estado) &&
            !"PENDIENTE".equals(estado) &&
            !"EN_EVALUACION_COMITE".equals(estado) &&
            !"FORMATO_A_DILIGENCIADO".equals(estado) &&
            !"PRESENTADO_AL_COORDINADOR".equals(estado) &&
            !"CORRECCIONES_COMITE".equals(estado)) {
            throw new InvalidStateException(
                String.format("Formato A no está en estado evaluable. Estado actual: %s. Estados válidos: PENDIENTE, EN_REVISION, EN_EVALUACION_COMITE, FORMATO_A_DILIGENCIADO, PRESENTADO_AL_COORDINADOR, CORRECCIONES_COMITE",
                    estado)
            );
        }
        log.debug("Estado del documento validado correctamente: {}", estado);
    }

    @Override
    protected void updateSubmissionService(Long docId, Decision decision, String obs, Integer evaluatorId) {
        log.info("Actualizando estado de Formato A {} en Submission Service", docId);

        EvaluacionRequest request = new EvaluacionRequest(
            decision.name(),
            obs != null ? obs : "",
            evaluatorId != null ? evaluatorId.longValue() : null
        );

        log.debug("Enviando EvaluacionRequest: {}", request);
        submissionClient.updateFormatoAEstado(docId, request);
        log.info("Estado actualizado exitosamente en Submission Service: formatoAId={}, estado={}",
                docId, decision);
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

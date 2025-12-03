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

        // WORKAROUND: El endpoint /formatoA/{id} del submission-service falla con 500
        // En su lugar, buscamos el formato en el listado /pendientes que SÍ funciona
        try {
            log.debug("Buscando Formato A {} en el listado de pendientes...", documentId);

            // Buscar en varias páginas hasta encontrarlo
            for (int pageNum = 0; pageNum < 10; pageNum++) {
                Page<FormatoAReviewDTO> page = submissionClient.getFormatosAPendientes(pageNum, 100);

                // Buscar el formato con el ID solicitado
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

                // Si es la última página, no tiene sentido seguir
                if (page.isLast()) break;
            }

            // Si no lo encontramos
            log.error("❌ Formato A {} no encontrado en formatos pendientes", documentId);
            throw new ResourceNotFoundException("Formato A " + documentId + " no encontrado en formatos pendientes");

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error buscando Formato A {}: {}", documentId, e.getMessage(), e);
            throw new ResourceNotFoundException("Error al buscar Formato A: " + documentId);
        }
    }

    @Override
    protected void validateDocumentState(DocumentInfo document) {
        // Aceptar tanto PENDIENTE como EN_REVISION para evaluación
        String estado = document.getEstado();
        if (!"EN_REVISION".equals(estado) && !"PENDIENTE".equals(estado)) {
            throw new InvalidStateException(
                String.format("Formato A no está en estado evaluable. Estado actual: %s. Se requiere: EN_REVISION o PENDIENTE",
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
            evaluatorId
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

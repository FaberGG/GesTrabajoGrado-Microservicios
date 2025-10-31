package co.unicauca.review.service.impl;

import co.unicauca.review.client.SubmissionServiceClient;
import co.unicauca.review.dto.response.NotificationEventDTO;
import co.unicauca.review.entity.Evaluation;
import co.unicauca.review.enums.Decision;
import co.unicauca.review.enums.DocumentType;
import co.unicauca.review.enums.EvaluatorRole;
import co.unicauca.review.exception.InvalidStateException;
import co.unicauca.review.service.EvaluationTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        SubmissionServiceClient.FormatoADTO dto = submissionClient.getFormatoA(documentId);

        DocumentInfo doc = new DocumentInfo();
        doc.setId(dto.getId());
        doc.setTitulo(dto.getTitulo());
        doc.setEstado(dto.getEstado());
        doc.setDocenteDirectorName(dto.getDocenteDirectorNombre());
        doc.setDocenteDirectorEmail(dto.getDocenteDirectorEmail());
        doc.setAutoresEmails(dto.getEstudiantesEmails());

        log.debug("Formato A obtenido: {}, Estado: {}", dto.getTitulo(), dto.getEstado());
        return doc;
    }

    @Override
    protected void validateDocumentState(DocumentInfo document) {
        if (!"EN_REVISION".equals(document.getEstado())) {
            throw new InvalidStateException(
                String.format("Formato A no está en estado evaluable. Estado actual: %s. Se requiere: EN_REVISION",
                    document.getEstado())
            );
        }
        log.debug("Estado del documento validado correctamente: {}", document.getEstado());
    }

    @Override
    protected void updateSubmissionService(Long docId, Decision decision, String obs) {
        log.info("Actualizando estado de Formato A {} en Submission Service", docId);

        Map<String, String> body = Map.of(
            "estado", decision.name(),
            "observaciones", obs != null ? obs : ""
        );

        submissionClient.updateFormatoAEstado(docId, body);
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


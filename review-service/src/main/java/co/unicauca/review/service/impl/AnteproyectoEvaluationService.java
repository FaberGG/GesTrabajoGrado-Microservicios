package co.unicauca.review.service.impl;

import co.unicauca.review.client.SubmissionServiceClient;
import co.unicauca.review.dto.response.NotificationEventDTO;
import co.unicauca.review.entity.AsignacionEvaluadores;
import co.unicauca.review.entity.Evaluation;
import co.unicauca.review.enums.AsignacionEstado;
import co.unicauca.review.enums.Decision;
import co.unicauca.review.enums.DocumentType;
import co.unicauca.review.enums.EvaluatorRole;
import co.unicauca.review.exception.InvalidStateException;
import co.unicauca.review.repository.AsignacionEvaluadoresRepository;
import co.unicauca.review.service.EvaluationTemplate;
import co.unicauca.review.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementación concreta del Template Method para evaluación de Anteproyectos.
 * Requiere evaluación de 2 evaluadores. Solo publica notificación cuando ambos han evaluado.
 */
@Service("anteproyectoEvaluationService")
public class AnteproyectoEvaluationService extends EvaluationTemplate {

    @Autowired
    private SubmissionServiceClient submissionClient;

    @Autowired
    private AsignacionEvaluadoresRepository asignacionRepository;

    @Override
    protected DocumentInfo fetchDocument(Long documentId) {
        log.debug("Obteniendo información de Anteproyecto con id: {}", documentId);

        SubmissionServiceClient.AnteproyectoDTO dto = submissionClient.getAnteproyecto(documentId);

        DocumentInfo doc = new DocumentInfo();
        doc.setId(dto.getId());
        doc.setTitulo(dto.getTitulo());
        doc.setEstado(dto.getEstado());
        doc.setDocenteDirectorName(dto.getDocenteDirectorNombre());
        doc.setDocenteDirectorEmail(dto.getDocenteDirectorEmail());
        doc.setAutoresEmails(dto.getEstudiantesEmails());

        log.debug("Anteproyecto obtenido: {}, Estado: {}", dto.getTitulo(), dto.getEstado());
        return doc;
    }

    @Override
    protected void validateDocumentState(DocumentInfo document) {
        // Verificar que tenga evaluadores asignados
        AsignacionEvaluadores asignacion = asignacionRepository
            .findByAnteproyectoId(document.getId())
            .orElseThrow(() -> new InvalidStateException(
                String.format("Anteproyecto %d no tiene evaluadores asignados", document.getId())
            ));

        // Verificar que el evaluador actual no haya evaluado ya
        Long currentEvaluatorId = getCurrentEvaluatorId();

        if (asignacion.getEvaluador1Id().equals(currentEvaluatorId) &&
            asignacion.getEvaluador1Decision() != null) {
            throw new InvalidStateException("Este evaluador ya registró su evaluación para este anteproyecto");
        }

        if (asignacion.getEvaluador2Id().equals(currentEvaluatorId) &&
            asignacion.getEvaluador2Decision() != null) {
            throw new InvalidStateException("Este evaluador ya registró su evaluación para este anteproyecto");
        }

        // Verificar que el usuario actual sea uno de los evaluadores asignados
        if (!asignacion.getEvaluador1Id().equals(currentEvaluatorId) &&
            !asignacion.getEvaluador2Id().equals(currentEvaluatorId)) {
            throw new InvalidStateException(
                "El evaluador actual no está asignado a este anteproyecto"
            );
        }

        log.debug("Estado del documento y asignación validados correctamente");
    }

    @Override
    protected void updateSubmissionService(Long docId, Decision decision, String obs) {
        AsignacionEvaluadores asignacion = asignacionRepository
            .findByAnteproyectoId(docId)
            .orElseThrow(() -> new IllegalStateException("Asignación no encontrada"));

        // Actualizar decisión del evaluador actual
        Long currentEvaluatorId = getCurrentEvaluatorId();

        if (asignacion.getEvaluador1Id().equals(currentEvaluatorId)) {
            asignacion.setEvaluador1Decision(decision);
            asignacion.setEvaluador1Observaciones(obs);
            log.info("Evaluación registrada para Evaluador 1: decisión={}", decision);
        } else if (asignacion.getEvaluador2Id().equals(currentEvaluatorId)) {
            asignacion.setEvaluador2Decision(decision);
            asignacion.setEvaluador2Observaciones(obs);
            log.info("Evaluación registrada para Evaluador 2: decisión={}", decision);
        }

        // Actualizar estado de la asignación
        if (asignacion.getEstado() == AsignacionEstado.PENDIENTE) {
            asignacion.setEstado(AsignacionEstado.EN_EVALUACION);
        }

        // Solo actualizar Submission Service si AMBOS evaluadores han evaluado
        if (asignacion.isCompletada()) {
            Decision finalDecision = asignacion.getFinalDecision();
            asignacion.setEstado(AsignacionEstado.COMPLETADA);
            asignacion.setFechaCompletado(LocalDateTime.now());

            Map<String, String> body = Map.of(
                "estado", finalDecision.name(),
                "observaciones", "Evaluado por ambos evaluadores"
            );

            submissionClient.updateAnteproyectoEstado(docId, body);
            log.info("✓ Estado final actualizado en Submission Service: anteproyectoId={}, decisión={}",
                    docId, finalDecision);
        } else {
            log.info("⏳ Esperando evaluación del segundo evaluador. Estado no actualizado en Submission Service aún.");
        }

        asignacionRepository.save(asignacion);
    }

    @Override
    protected boolean publishNotificationEvent(Evaluation eval, DocumentInfo doc) {
        AsignacionEvaluadores asignacion = asignacionRepository
            .findByAnteproyectoId(doc.getId())
            .orElseThrow(() -> new IllegalStateException("Asignación no encontrada"));

        // Solo publicar si AMBOS evaluadores completaron
        if (!asignacion.isCompletada()) {
            log.info("⏳ Esperando evaluación del segundo evaluador. No se publica evento de notificación aún.");
            return false;
        }

        log.info("Publicando evento de notificación para Anteproyecto {} (ambos evaluadores completaron)", doc.getId());

        NotificationEventDTO event = NotificationEventDTO.builder()
            .eventType("ANTEPROYECTO_EVALUATED")
            .documentId(doc.getId())
            .documentTitle(doc.getTitulo())
            .documentType("ANTEPROYECTO")
            .decision(asignacion.getFinalDecision().name())
            .evaluatorName("Evaluadores del Departamento")
            .evaluatorRole("EVALUADOR")
            .observaciones(buildFinalObservaciones(asignacion))
            .recipients(buildRecipients(doc))
            .timestamp(LocalDateTime.now())
            .build();

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("✓ Evento ANTEPROYECTO_EVALUATED publicado en RabbitMQ: documentId={}, decisión={}",
                    doc.getId(), asignacion.getFinalDecision());
            return true;
        } catch (Exception e) {
            log.error("✗ Error publicando evento en RabbitMQ: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected DocumentType getDocumentType() {
        return DocumentType.ANTEPROYECTO;
    }

    @Override
    protected EvaluatorRole getRequiredRole() {
        return EvaluatorRole.EVALUADOR;
    }

    /**
     * Obtiene el ID del evaluador actual desde el contexto de seguridad
     */
    private Long getCurrentEvaluatorId() {
        return SecurityUtil.getCurrentUserId();
    }

    /**
     * Construye las observaciones finales combinando ambos evaluadores
     */
    private String buildFinalObservaciones(AsignacionEvaluadores asig) {
        StringBuilder obs = new StringBuilder();
        obs.append("=== EVALUACIÓN COMPLETA ===\n\n");
        obs.append("EVALUADOR 1:\n");
        obs.append("Decisión: ").append(asig.getEvaluador1Decision()).append("\n");
        obs.append("Observaciones: ").append(asig.getEvaluador1Observaciones() != null ?
                asig.getEvaluador1Observaciones() : "Sin observaciones").append("\n\n");
        obs.append("EVALUADOR 2:\n");
        obs.append("Decisión: ").append(asig.getEvaluador2Decision()).append("\n");
        obs.append("Observaciones: ").append(asig.getEvaluador2Observaciones() != null ?
                asig.getEvaluador2Observaciones() : "Sin observaciones").append("\n\n");
        obs.append("DECISIÓN FINAL: ").append(asig.getFinalDecision());

        return obs.toString();
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


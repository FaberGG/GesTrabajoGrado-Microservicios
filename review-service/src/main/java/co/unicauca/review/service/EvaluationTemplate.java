package co.unicauca.review.service;

import co.unicauca.review.dto.request.EvaluationRequestDTO;
import co.unicauca.review.dto.response.EvaluationResultDTO;
import co.unicauca.review.entity.Evaluation;
import co.unicauca.review.enums.Decision;
import co.unicauca.review.enums.DocumentType;
import co.unicauca.review.enums.EvaluatorRole;
import co.unicauca.review.exception.EvaluationException;
import co.unicauca.review.exception.UnauthorizedException;
import co.unicauca.review.repository.EvaluationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase abstracta que implementa el patrón Template Method para evaluaciones académicas.
 * Define el algoritmo general de evaluación y delega pasos específicos a las subclases.
 */
public abstract class EvaluationTemplate {

    protected static final Logger log = LoggerFactory.getLogger(EvaluationTemplate.class);

    @Autowired
    protected EvaluationRepository evaluationRepository;

    @Autowired
    protected RabbitTemplate rabbitTemplate;

    @Autowired
    protected WebClient.Builder webClientBuilder;

    @Value("${evaluation.exchange}")
    protected String exchange;

    @Value("${evaluation.routing-key}")
    protected String routingKey;

    /**
     * TEMPLATE METHOD (final - no se puede override)
     * Define el algoritmo general de evaluación.
     */
    @Transactional
    public final EvaluationResultDTO evaluate(EvaluationRequestDTO request) {
        try {
            log.info("Iniciando evaluación - Documento: {}, Tipo: {}, Evaluador: {}",
                    request.documentId(), getDocumentType(), request.evaluatorId());

            // 1. Validar permisos (común)
            validatePermissions(request);

            // 2. Obtener documento (específico - abstracto)
            DocumentInfo document = fetchDocument(request.documentId());

            // 3. Validar estado (específico - abstracto)
            validateDocumentState(document);

            // 4. Registrar evaluación (común)
            Evaluation evaluation = saveEvaluation(request, document);

            // 5. Actualizar Submission Service (específico - abstracto)
            updateSubmissionService(document.getId(), request.decision(),
                                   request.observaciones());

            // 6. Publicar evento notificación (específico - abstracto)
            boolean notified = publishNotificationEvent(evaluation, document);

            // 7. Retornar resultado (común)
            log.info("Evaluación completada exitosamente - ID: {}", evaluation.getId());
            return buildSuccessResult(evaluation, notified);

        } catch (UnauthorizedException e) {
            log.warn("Evaluación rechazada por permisos: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error evaluando documento {}: {}",
                     request.documentId(), e.getMessage(), e);
            throw new EvaluationException("Error en evaluación: " + e.getMessage(), e);
        }
    }

    // ========== MÉTODOS COMUNES (implementados aquí) ==========

    /**
     * Valida que el rol del evaluador sea el requerido para este tipo de evaluación
     */
    protected void validatePermissions(EvaluationRequestDTO request) {
        EvaluatorRole expectedRole = getRequiredRole();
        if (request.evaluatorRole() != expectedRole) {
            throw new UnauthorizedException(
                String.format("Rol no autorizado para esta evaluación. Esperado: %s, Recibido: %s",
                        expectedRole, request.evaluatorRole())
            );
        }
        log.debug("Permisos validados correctamente para rol: {}", request.evaluatorRole());
    }

    /**
     * Guarda la evaluación en la base de datos
     */
    protected Evaluation saveEvaluation(EvaluationRequestDTO request, DocumentInfo doc) {
        Evaluation eval = new Evaluation();
        eval.setDocumentId(request.documentId());
        eval.setDocumentType(getDocumentType());
        eval.setDecision(request.decision());
        eval.setObservaciones(request.observaciones());
        eval.setEvaluatorId(request.evaluatorId());
        eval.setEvaluatorRole(request.evaluatorRole());
        eval.setFechaEvaluacion(LocalDateTime.now());

        Evaluation saved = evaluationRepository.save(eval);
        log.info("Evaluación guardada - ID: {}, Documento: {}, Decisión: {}",
                saved.getId(), saved.getDocumentId(), saved.getDecision());

        return saved;
    }

    /**
     * Construye el resultado exitoso de la evaluación
     */
    protected EvaluationResultDTO buildSuccessResult(Evaluation eval, boolean notified) {
        return new EvaluationResultDTO(
            eval.getId(),
            eval.getDocumentId(),
            eval.getDocumentType(),
            eval.getDecision(),
            eval.getObservaciones(),
            eval.getFechaEvaluacion(),
            notified
        );
    }

    // ========== MÉTODOS ABSTRACTOS (implementar en subclases) ==========

    /**
     * Obtiene la información del documento desde el servicio de submissions
     */
    protected abstract DocumentInfo fetchDocument(Long documentId);

    /**
     * Valida que el documento esté en un estado válido para ser evaluado
     */
    protected abstract void validateDocumentState(DocumentInfo document);

    /**
     * Actualiza el estado del documento en el servicio de submissions
     */
    protected abstract void updateSubmissionService(Long docId, Decision decision, String obs);

    /**
     * Publica un evento de notificación vía RabbitMQ
     */
    protected abstract boolean publishNotificationEvent(Evaluation eval, DocumentInfo doc);

    /**
     * Retorna el tipo de documento que maneja esta implementación
     */
    protected abstract DocumentType getDocumentType();

    /**
     * Retorna el rol requerido para realizar este tipo de evaluación
     */
    protected abstract EvaluatorRole getRequiredRole();

    // ========== CLASE INTERNA PARA INFO DEL DOCUMENTO ==========

    /**
     * Clase interna que encapsula la información del documento obtenida del servicio externo
     */
    protected static class DocumentInfo {
        private Long id;
        private String titulo;
        private String estado;
        private String docenteDirectorName;
        private String docenteDirectorEmail;
        private List<String> autoresEmails;

        public DocumentInfo() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public String getDocenteDirectorName() { return docenteDirectorName; }
        public void setDocenteDirectorName(String name) { this.docenteDirectorName = name; }

        public String getDocenteDirectorEmail() { return docenteDirectorEmail; }
        public void setDocenteDirectorEmail(String email) { this.docenteDirectorEmail = email; }

        public List<String> getAutoresEmails() { return autoresEmails; }
        public void setAutoresEmails(List<String> emails) { this.autoresEmails = emails; }
    }
}


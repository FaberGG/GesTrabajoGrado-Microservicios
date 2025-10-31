package co.unicauca.microservice.noti_service.services.template;

import co.unicauca.microservice.noti_service.model.NotificationType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Servicio para resolución de plantillas de notificación.
 * Gestiona plantillas parametrizadas para diferentes tipos de notificaciones.
 */
@Service
public class TemplateService {

    private final Map<String, String> templates = new HashMap<>();
    private final Map<String, Set<String>> requiredVariables = new HashMap<>();

    public TemplateService() {
        initializeTemplates();
        initializeRequiredVariables();
    }

    /**
     * Resuelve una plantilla con las variables del contexto
     */
    public String resolveTemplate(String templateId, Map<String, Object> context) {
        String template = templates.get(templateId);
        if (template == null) {
            throw new TemplateNotFoundException("Template not found: " + templateId);
        }

        // Validar variables requeridas
        validateRequiredVariables(templateId, context);

        // Reemplazar variables
        return replaceVariables(template, context);
    }

    /**
     * Obtiene el ID de plantilla por defecto para un tipo de notificación
     */
    public String getDefaultTemplateId(NotificationType type) {
        return switch (type) {
            case DOCUMENT_SUBMITTED -> "document_submitted_coordinator";
            case EVALUATION_COMPLETED -> "evaluation_completed_teacher";
            case STATUS_CHANGED -> "status_changed";
            case EVALUATOR_ASSIGNED -> "evaluator_assigned";
            case DEADLINE_REMINDER -> "deadline_reminder";
        };
    }

    /**
     * Valida que todas las variables requeridas estén presentes
     */
    private void validateRequiredVariables(String templateId, Map<String, Object> context) {
        Set<String> required = requiredVariables.get(templateId);
        if (required == null) return;

        for (String variable : required) {
            if (!context.containsKey(variable) || context.get(variable) == null) {
                throw new MissingTemplateVariableException(
                        "Template '" + templateId + "' requires variable: " + variable
                );
            }
        }
    }

    /**
     * Reemplaza variables en el formato {variable} por sus valores
     */
    private String replaceVariables(String template, Map<String, Object> context) {
        String result = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    /**
     * Inicializa las plantillas por defecto
     */
    private void initializeTemplates() {
        // DOCUMENT_SUBMITTED - Para coordinador
        templates.put("document_submitted_coordinator",
                """
                Estimado(a) Coordinador(a),
                
                Se ha recibido un nuevo documento para revisión:
                
                Proyecto: {projectTitle}
                Tipo de documento: {documentType}
                Versión: {documentVersion}
                Presentado por: {submittedBy}
                Fecha de envío: {submissionDate}
                
                Por favor, ingrese al sistema para revisar y evaluar el documento.
                
                Saludos cordiales,
                Sistema de Gestión de Trabajos de Grado - Universidad del Cauca
                """
        );

        // EVALUATION_COMPLETED - Para docente
        templates.put("evaluation_completed_teacher",
                """
                Estimado(a) {recipientName},
                
                La evaluación de su documento ha sido completada:
                
                Proyecto: {projectTitle}
                Tipo de documento: {documentType}
                Resultado: {evaluationResult}
                Evaluado por: {evaluatedBy}
                Fecha de evaluación: {evaluationDate}
                
                {observations}
                
                Por favor, ingrese al sistema para ver los detalles completos.
                
                Saludos cordiales,
                Sistema de Gestión de Trabajos de Grado - Universidad del Cauca
                """
        );

        // EVALUATION_COMPLETED - Para estudiante
        templates.put("evaluation_completed_student",
                """
                Estimado(a) Estudiante,
                
                Su proyecto de grado ha sido evaluado:
                
                Proyecto: {projectTitle}
                Tipo de documento: {documentType}
                Estado: {evaluationResult}
                
                {observations}
                
                Para más detalles, consulte con su director de proyecto o ingrese al sistema.
                
                Saludos cordiales,
                Sistema de Gestión de Trabajos de Grado - Universidad del Cauca
                """
        );

        // STATUS_CHANGED
        templates.put("status_changed",
                """
                Estimado(a) {recipientName},
                
                El estado de su proyecto ha cambiado:
                
                Proyecto: {projectTitle}
                Estado anterior: {previousStatus}
                Estado actual: {currentStatus}
                Fecha de cambio: {changeDate}
                
                {additionalInfo}
                
                Saludos cordiales,
                Sistema de Gestión de Trabajos de Grado - Universidad del Cauca
                """
        );

        // EVALUATOR_ASSIGNED
        templates.put("evaluator_assigned",
                """
                Estimado(a) {recipientName},
                
                Ha sido asignado como evaluador del siguiente proyecto:
                
                Proyecto: {projectTitle}
                Tipo de documento: {documentType}
                Director: {directorName}
                Fecha límite de evaluación: {dueDate}
                
                Por favor, ingrese al sistema para acceder al documento.
                
                Saludos cordiales,
                Sistema de Gestión de Trabajos de Grado - Universidad del Cauca
                """
        );

        // DEADLINE_REMINDER
        templates.put("deadline_reminder",
                """
                Estimado(a) {recipientName},
                
                Recordatorio de fecha límite:
                
                Proyecto: {projectTitle}
                Actividad pendiente: {pendingActivity}
                Fecha límite: {dueDate}
                Días restantes: {daysRemaining}
                
                Por favor, complete la actividad antes de la fecha límite.
                
                Saludos cordiales,
                Sistema de Gestión de Trabajos de Grado - Universidad del Cauca
                """
        );
    }

    /**
     * Define las variables requeridas por cada plantilla
     */
    private void initializeRequiredVariables() {
        requiredVariables.put("document_submitted_coordinator",
                Set.of("projectTitle", "documentType", "documentVersion", "submittedBy", "submissionDate")
        );

        requiredVariables.put("evaluation_completed_teacher",
                Set.of("recipientName", "projectTitle", "documentType", "evaluationResult",
                        "evaluatedBy", "evaluationDate")
        );

        requiredVariables.put("evaluation_completed_student",
                Set.of("projectTitle", "documentType", "evaluationResult")
        );

        requiredVariables.put("status_changed",
                Set.of("recipientName", "projectTitle", "previousStatus", "currentStatus", "changeDate")
        );

        requiredVariables.put("evaluator_assigned",
                Set.of("recipientName", "projectTitle", "documentType", "directorName", "dueDate")
        );

        requiredVariables.put("deadline_reminder",
                Set.of("recipientName", "projectTitle", "pendingActivity", "dueDate", "daysRemaining")
        );
    }
}

/**
 * Excepción cuando no se encuentra una plantilla
 */
class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException(String message) {
        super(message);
    }
}

/**
 * Excepción cuando falta una variable requerida en el contexto
 */
class MissingTemplateVariableException extends RuntimeException {
    public MissingTemplateVariableException(String message) {
        super(message);
    }
}
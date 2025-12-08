package co.unicauca.microservice.noti_service.services.validation;

import co.unicauca.microservice.noti_service.model.NotificationRequest;
import co.unicauca.microservice.noti_service.model.NotificationType;
import co.unicauca.microservice.noti_service.model.Recipient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validador de notificaciones que verifica campos requeridos según el tipo.
 */
@Component
public class NotificationValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9]\\d{1,14}$"); // E.164 format

    private final Map<NotificationType, Set<String>> requiredContextFields = Map.of(
            NotificationType.DOCUMENT_SUBMITTED,
            Set.of("projectTitle", "documentType", "submittedBy", "submissionDate", "documentVersion"),

            NotificationType.EVALUATION_COMPLETED,
            Set.of("projectTitle", "documentType", "evaluationResult", "evaluatedBy", "evaluationDate"),

            NotificationType.STATUS_CHANGED,
            Set.of("projectTitle", "currentStatus", "previousStatus", "changeDate"),

            NotificationType.EVALUATOR_ASSIGNED,
            Set.of("projectTitle", "documentType", "directorName", "dueDate"),

            NotificationType.DEADLINE_REMINDER,
            Set.of("projectTitle", "pendingActivity", "dueDate", "daysRemaining")
    );

    /**
     * Valida una notificación completa
     * @throws NotificationValidationException si la validación falla
     */
    public void validate(NotificationRequest request) {
        List<String> errors = new ArrayList<>();

        // Validar tipo de notificación
        if (request.notificationType() == null) {
            errors.add("Notification type is required");
        }

        // Validar canal
        if (request.channel() == null || request.channel().isBlank()) {
            errors.add("Channel is required");
        } else {
            validateChannel(request.channel(), errors);
        }

        // Validar destinatarios
        if (request.recipients() == null || request.recipients().isEmpty()) {
            errors.add("At least one recipient is required");
        } else {
            validateRecipients(request.recipients(), request.channel(), errors);
        }

        // Validar contexto de negocio
        if (request.businessContext() == null) {
            errors.add("Business context is required");
        } else if (request.notificationType() != null) {
            validateBusinessContext(request.notificationType(), request.businessContext(), errors);
        }

        if (!errors.isEmpty()) {
            throw new NotificationValidationException(
                    "Notification validation failed: " + String.join(", ", errors)
            );
        }
    }

    /**
     * Valida que el canal sea soportado
     */
    private void validateChannel(String channel, List<String> errors) {
        if (!channel.equalsIgnoreCase("email") && !channel.equalsIgnoreCase("sms")) {
            errors.add("Unsupported channel: " + channel + ". Supported channels: email, sms");
        }
    }

    /**
     * Valida la lista de destinatarios según el canal
     */
    private void validateRecipients(List<Recipient> recipients, String channel, List<String> errors) {
        for (int i = 0; i < recipients.size(); i++) {
            Recipient recipient = recipients.get(i);
            if (recipient.email() == null || recipient.email().isBlank()) {
                errors.add("Recipient[" + i + "] email is required");
                continue;
            }

            if ("email".equalsIgnoreCase(channel)) {
                if (!isValidEmail(recipient.email())) {
                    errors.add("Recipient[" + i + "] has invalid email format: " + recipient.email());
                }
            } else if ("sms".equalsIgnoreCase(channel)) {
                if (!isValidPhone(recipient.email())) { // Para SMS, el campo email contiene el teléfono
                    errors.add("Recipient[" + i + "] has invalid phone format: " + recipient.email());
                }
            }
        }
    }

    /**
     * Valida que el contexto de negocio tenga los campos requeridos según el tipo
     */
    private void validateBusinessContext(NotificationType type,
                                         Map<String, Object> context,
                                         List<String> errors) {
        Set<String> required = requiredContextFields.get(type);
        if (required == null) return;

        for (String field : required) {
            if (!context.containsKey(field) || context.get(field) == null) {
                errors.add("Business context missing required field '" + field +
                        "' for notification type " + type);
            } else if (context.get(field).toString().isBlank()) {
                errors.add("Business context field '" + field + "' cannot be empty");
            }
        }
    }

    /**
     * Valida formato de email
     */
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valida formato de teléfono (E.164)
     */
    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
}

/**
 * Excepción personalizada para errores de validación
 */
class NotificationValidationException extends RuntimeException {
    public NotificationValidationException(String message) {
        super(message);
    }
}
package co.unicauca.review.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador para enviar notificaciones a través de RabbitMQ.
 * Publica mensajes directamente a la cola notifications.q
 * consumida por notification-service.
 */
@Component
public class NotificationAdapter {

    private static final Logger log = LoggerFactory.getLogger(NotificationAdapter.class);
    private static final String NOTIFICATIONS_QUEUE = "notifications.q";

    private final RabbitTemplate rabbitTemplate;

    public NotificationAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Notifica a docentes y estudiantes sobre la evaluación de un Formato A.
     * Cumple con RF3.
     *
     * @param proyectoId ID del proyecto evaluado
     * @param projectTitle Título del proyecto
     * @param decision Decisión de la evaluación (APROBADO/RECHAZADO)
     * @param evaluatedBy Nombre del evaluador (coordinador)
     * @param observaciones Observaciones de la evaluación
     * @param recipients Lista de emails de destinatarios (director, estudiantes)
     */
    public void notificarEvaluacionFormatoA(
            Long proyectoId,
            String projectTitle,
            String decision,
            String evaluatedBy,
            String observaciones,
            List<String> recipients) {

        try {
            log.info("Notificando evaluación de Formato A - ProyectoID: {}, Decisión: {}, Destinatarios: {}",
                    proyectoId, decision, recipients.size());

            if (recipients == null || recipients.isEmpty()) {
                log.warn("⚠️ No hay destinatarios para la notificación de evaluación del proyecto {}", proyectoId);
                return;
            }

            // Construir mensaje en el formato esperado por notification-service
            Map<String, Object> notificacion = new HashMap<>();

            // NotificationType: EVALUATION_COMPLETED
            notificacion.put("notificationType", "EVALUATION_COMPLETED");

            // Channel
            notificacion.put("channel", "EMAIL");

            // Recipients (lista de objetos con email y name)
            List<Map<String, String>> recipientsList = recipients.stream()
                .map(email -> {
                    Map<String, String> recipient = new HashMap<>();
                    recipient.put("email", email);
                    recipient.put("name", email.split("@")[0]); // Nombre simplificado del email
                    return recipient;
                })
                .toList();
            notificacion.put("recipients", recipientsList);

            // Business Context con TODOS los campos requeridos para EVALUATION_COMPLETED
            Map<String, Object> businessContext = new HashMap<>();
            businessContext.put("proyectoId", proyectoId);
            businessContext.put("projectTitle", projectTitle);
            businessContext.put("documentType", "FORMATO_A");
            businessContext.put("evaluationResult", decision); // APROBADO o RECHAZADO
            businessContext.put("evaluatedBy", evaluatedBy);
            businessContext.put("evaluationDate", LocalDateTime.now().toString());
            businessContext.put("observaciones", observaciones != null ? observaciones : "Sin observaciones");
            notificacion.put("businessContext", businessContext);

            // Campos opcionales
            notificacion.put("message", null); // Usar plantilla por defecto
            notificacion.put("templateId", null); // Usar plantilla por defecto del tipo
            notificacion.put("forceFail", false);

            // Publicar directamente a la cola
            rabbitTemplate.convertAndSend(NOTIFICATIONS_QUEUE, notificacion);

            log.info("✅ Notificación de evaluación enviada exitosamente a {} destinatarios", recipients.size());

        } catch (Exception e) {
            log.error("❌ Error al notificar evaluación de Formato A: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifica a docentes y estudiantes sobre la evaluación de un Anteproyecto.
     * Para uso futuro.
     *
     * @param proyectoId ID del proyecto evaluado
     * @param projectTitle Título del proyecto
     * @param decision Decisión de la evaluación
     * @param evaluatedBy Nombre del evaluador
     * @param observaciones Observaciones de la evaluación
     * @param recipients Lista de emails de destinatarios
     */
    public void notificarEvaluacionAnteproyecto(
            Long proyectoId,
            String projectTitle,
            String decision,
            String evaluatedBy,
            String observaciones,
            List<String> recipients) {

        try {
            log.info("Notificando evaluación de Anteproyecto - ProyectoID: {}, Decisión: {}", proyectoId, decision);

            if (recipients == null || recipients.isEmpty()) {
                log.warn("⚠️ No hay destinatarios para la notificación de evaluación del anteproyecto {}", proyectoId);
                return;
            }

            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("notificationType", "EVALUATION_COMPLETED");
            notificacion.put("channel", "EMAIL");

            List<Map<String, String>> recipientsList = recipients.stream()
                .map(email -> {
                    Map<String, String> recipient = new HashMap<>();
                    recipient.put("email", email);
                    recipient.put("name", email.split("@")[0]);
                    return recipient;
                })
                .toList();
            notificacion.put("recipients", recipientsList);

            Map<String, Object> businessContext = new HashMap<>();
            businessContext.put("proyectoId", proyectoId);
            businessContext.put("projectTitle", projectTitle);
            businessContext.put("documentType", "ANTEPROYECTO");
            businessContext.put("evaluationResult", decision);
            businessContext.put("evaluatedBy", evaluatedBy);
            businessContext.put("evaluationDate", LocalDateTime.now().toString());
            businessContext.put("observaciones", observaciones != null ? observaciones : "Sin observaciones");
            notificacion.put("businessContext", businessContext);

            notificacion.put("message", null);
            notificacion.put("templateId", null);
            notificacion.put("forceFail", false);

            rabbitTemplate.convertAndSend(NOTIFICATIONS_QUEUE, notificacion);

            log.info("✅ Notificación de evaluación de anteproyecto enviada exitosamente");

        } catch (Exception e) {
            log.error("❌ Error al notificar evaluación de Anteproyecto: {}", e.getMessage(), e);
        }
    }
}


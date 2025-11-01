package co.unicauca.comunicacionmicroservicios.service;

import co.unicauca.comunicacionmicroservicios.config.RabbitConfig;
import co.unicauca.comunicacionmicroservicios.dto.NotificationRequest;
import co.unicauca.comunicacionmicroservicios.dto.NotificationType;
import co.unicauca.comunicacionmicroservicios.dto.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para enviar notificaciones asíncronas a través de RabbitMQ.
 *
 * RESPONSABILIDAD:
 * - Construir NotificationRequest con todos los datos requeridos
 * - Publicar a la cola "notifications.q" para que notification-service procese
 * - Manejar errores sin afectar la operación principal
 *
 * REQUISITOS FUNCIONALES:
 * - RF2: Notificar al coordinador cuando se envía Formato A (v1)
 * - RF4: Notificar al coordinador cuando se reenvía Formato A (v2, v3)
 * - RF6: Notificar al jefe de departamento cuando se envía anteproyecto
 *
 * NOTA: RF3 (Notificar evaluación completada) es ahora responsabilidad del review-service
 */
@Service
public class NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * RF2 & RF4: Notifica al coordinador sobre el envío de Formato A.
     * Se usa tanto para la primera versión como para reenvíos.
     *
     * @param proyectoId ID del proyecto
     * @param titulo Título del proyecto
     * @param version Versión del Formato A (1, 2 o 3)
     * @param submittedByName Nombre del docente que envió
     * @param coordinadorEmail Email del coordinador del programa
     */
    public void notificarFormatoAEnviado(
            Integer proyectoId,
            String titulo,
            Integer version,
            String submittedByName,
            String coordinadorEmail
    ) {
        try {
            log.info("Preparando notificación Formato A - Proyecto: {}, Versión: {}, Destinatario: {}",
                    proyectoId, version, coordinadorEmail);

            NotificationRequest request = new NotificationRequest(
                    NotificationType.DOCUMENT_SUBMITTED,
                    "email",
                    List.of(new Recipient(coordinadorEmail, "COORDINATOR", null)),
                    Map.of(
                            "projectTitle", titulo,
                            "documentType", "FORMATO_A",
                            "submittedBy", submittedByName,
                            "submissionDate", LocalDateTime.now().toString(),
                            "documentVersion", version
                    ),
                    null,  // Usa plantilla por defecto
                    null,  // Usa template ID por defecto
                    false  // No forzar fallo
            );

            publishNotification(request, "Formato A enviado (v" + version + ")");

            log.info("✅ Notificación Formato A procesada - Proyecto: {}, Versión: {}, Coordinador: {}",
                    proyectoId, version, coordinadorEmail);

        } catch (Exception e) {
            log.error("❌ Error al publicar notificación de Formato A - Proyecto: {}, Versión: {}",
                    proyectoId, version, e);
            // No lanzamos excepción para no afectar la operación principal
        }
    }

    /**
     * RF6: Notifica al jefe de departamento sobre el envío de anteproyecto.
     *
     * @param proyectoId ID del proyecto
     * @param titulo Título del proyecto
     * @param submittedByName Nombre del director que envió
     * @param jefeDepartamentoEmail Email del jefe de departamento
     */
    public void notificarAnteproyectoEnviado(
            Integer proyectoId,
            String titulo,
            String submittedByName,
            String jefeDepartamentoEmail
    ) {
        try {
            NotificationRequest request = new NotificationRequest(
                    NotificationType.DOCUMENT_SUBMITTED,
                    "email",
                    List.of(new Recipient(jefeDepartamentoEmail, "DEPARTMENT_HEAD", null)),
                    Map.of(
                            "projectTitle", titulo,
                            "documentType", "ANTEPROYECTO",
                            "submittedBy", submittedByName,
                            "submissionDate", LocalDateTime.now().toString(),
                            "documentVersion", 1
                    ),
                    null,
                    null,
                    false
            );

            publishNotification(request, "Anteproyecto enviado");

            log.info("Notificación Anteproyecto enviada - Proyecto: {}, Jefe: {}",
                    proyectoId, jefeDepartamentoEmail);

        } catch (Exception e) {
            log.error("Error al publicar notificación de Anteproyecto - Proyecto: {}",
                    proyectoId, e);
        }
    }
    /**
     * DEPRECADO: Este método ya no se usa en submission-service.
     * La responsabilidad de notificar sobre evaluaciones completadas
     * ahora pertenece al review-service.
     *
     * RF3: Notifica a docentes y estudiantes cuando se completa la evaluación de un Formato A.
     * Se envía tanto cuando se aprueba como cuando se rechaza.
     *
     * @param proyectoId ID del proyecto
     * @param titulo Título del proyecto
     * @param evaluationResult Resultado de la evaluación (APROBADO, RECHAZADO)
     * @param evaluadoPor Nombre del coordinador que evaluó
     * @param observaciones Observaciones de la evaluación
     * @param docenteEmails Lista de emails de docentes (director, codirector)
     * @param estudianteEmails Lista de emails de estudiantes
     * @deprecated Este método ya no se utiliza. La funcionalidad fue movida a review-service.
     */
    @Deprecated
    public void notificarEvaluacionCompletada(
            Integer proyectoId,
            String titulo,
            String evaluationResult,
            String evaluadoPor,
            String observaciones,
            List<String> docenteEmails,
            List<String> estudianteEmails
    ) {
        try {
            // Crear lista de destinatarios combinando docentes y estudiantes
            List<Recipient> recipients = new java.util.ArrayList<>();

            // Agregar docentes con role TEACHER
            docenteEmails.forEach(email ->
                recipients.add(new Recipient(email, "TEACHER", null))
            );

            // Agregar estudiantes con role STUDENT
            estudianteEmails.forEach(email ->
                recipients.add(new Recipient(email, "STUDENT", null))
            );

            if (recipients.isEmpty()) {
                log.warn("No hay destinatarios para notificación de evaluación - Proyecto: {}", proyectoId);
                return;
            }

            NotificationRequest request = new NotificationRequest(
                    NotificationType.EVALUATION_COMPLETED,
                    "email",
                    recipients,
                    Map.of(
                            "projectTitle", titulo,
                            "documentType", "FORMATO_A",
                            "evaluationResult", evaluationResult,
                            "evaluatedBy", evaluadoPor,
                            "evaluationDate", LocalDateTime.now().toString(),
                            "observations", observaciones != null ? observaciones : ""
                    ),
                    null,
                    null,
                    false
            );

            publishNotification(request, "Evaluación completada (" + evaluationResult + ")");

            log.info("RF3: Notificación de evaluación enviada - Proyecto: {}, Resultado: {}, Destinatarios: {} docentes + {} estudiantes",
                    proyectoId, evaluationResult, docenteEmails.size(), estudianteEmails.size());

        } catch (Exception e) {
            log.error("Error al publicar notificación de evaluación - Proyecto: {}",
                    proyectoId, e);
        }
    }


    /**
     * Esto notifica a estudiantes y director.
     * Esto podría notificar a estudiantes, director, etc.
     *
     * @param proyectoId ID del proyecto
     * @param titulo Título del proyecto
     * @param recipientEmails Lista de emails a notificar
     */
    public void notificarRechazoDefinitivo(
            Integer proyectoId,
            String titulo,
            List<String> recipientEmails
    ) {
        try {
            List<Recipient> recipients = recipientEmails.stream()
                    .map(email -> new Recipient(email, "STUDENT", null))
                    .toList();

            NotificationRequest request = new NotificationRequest(
                    NotificationType.STATUS_CHANGED,
                    "email",
                    recipients,
                    Map.of(
                            "projectTitle", titulo,
                            "currentStatus", "RECHAZADO_DEFINITIVO",
                            "previousStatus", "RECHAZADO",
                            "changeDate", LocalDateTime.now().toString()
                    ),
                    null,
                    null,
                    false
            );

            publishNotification(request, "Rechazo definitivo");

            log.info("Notificación de rechazo definitivo enviada - Proyecto: {}",
                    proyectoId);

        } catch (Exception e) {
            log.error("Error al publicar notificación de rechazo definitivo - Proyecto: {}",
                    proyectoId, e);
        }
    }

    /**
     * Método privado para publicar notificaciones con correlation ID.
     */
    private void publishNotification(NotificationRequest request, String eventDescription) {
        // Obtener o generar correlation ID
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;

        // Agregar correlation ID al mensaje
        MessagePostProcessor processor = message -> {
            message.getMessageProperties().setHeader("X-Correlation-Id", finalCorrelationId);
            return message;
        };

        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.NOTIFICATIONS_QUEUE,
                    request,
                    processor
            );

            log.info("✉️ Notificación publicada a RabbitMQ correctamente - Evento: {}, Cola: {}, CorrelationId: {}, Destinatarios: {}",
                    eventDescription, RabbitConfig.NOTIFICATIONS_QUEUE, finalCorrelationId,
                    request.recipients().size());

        } catch (AmqpException e) {
            log.error("❌ Error al enviar mensaje a RabbitMQ - Evento: {}, CorrelationId: {}",
                    eventDescription, finalCorrelationId, e);
            throw e; // Re-lanzar para que el catch del método padre lo maneje
        }
    }
}


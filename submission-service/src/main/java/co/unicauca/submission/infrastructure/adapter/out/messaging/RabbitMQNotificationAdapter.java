package co.unicauca.submission.infrastructure.adapter.out.messaging;

import co.unicauca.submission.application.port.out.INotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador para enviar notificaciones a través de RabbitMQ.
 * Implementa el puerto INotificationPort.
 *
 * Las notificaciones serán consumidas por el notification-service.
 */
@Component
public class RabbitMQNotificationAdapter implements INotificationPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQNotificationAdapter.class);

    // Cola directa para notificaciones (consumida por notification-service)
    private static final String NOTIFICATIONS_QUEUE = "notifications.q";

    private final RabbitTemplate rabbitTemplate;
    private final co.unicauca.submission.application.port.out.IProyectoRepositoryPort repositoryPort;
    private final co.unicauca.submission.application.port.out.IIdentityServicePort identityServicePort;

    public RabbitMQNotificationAdapter(
            RabbitTemplate rabbitTemplate,
            co.unicauca.submission.application.port.out.IProyectoRepositoryPort repositoryPort,
            co.unicauca.submission.application.port.out.IIdentityServicePort identityServicePort) {
        this.rabbitTemplate = rabbitTemplate;
        this.repositoryPort = repositoryPort;
        this.identityServicePort = identityServicePort;
    }

    @Override
    public void enviarNotificacion(String destinatario, String asunto, String mensaje, Map<String, Object> datos) {
        try {
            // Construir mensaje en el formato esperado por notification-service
            Map<String, Object> notificacion = new HashMap<>();

            // NotificationType - Mapear el tipo interno a los valores válidos del enum
            String tipoInterno = (String) datos.getOrDefault("tipo", "DOCUMENT_SUBMITTED");
            String notificationType = mapearTipoNotificacion(tipoInterno);
            notificacion.put("notificationType", notificationType);

            // Channel
            notificacion.put("channel", "EMAIL");

            // Recipients (lista de objetos con email)
            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", destinatario);
            recipient.put("name", destinatario.split("@")[0]); // Nombre simplificado del email
            notificacion.put("recipients", java.util.List.of(recipient));

            // Business Context (datos del dominio)
            Map<String, Object> businessContext = new HashMap<>();
            businessContext.putAll(datos);
            businessContext.put("subject", asunto); // Agregar asunto al contexto
            businessContext.put("customMessage", mensaje); // Agregar mensaje custom al contexto
            notificacion.put("businessContext", businessContext);

            // Campos opcionales
            notificacion.put("message", null); // Usar plantilla por defecto
            notificacion.put("templateId", null); // Usar plantilla por defecto del tipo
            notificacion.put("forceFail", false);

            // Publicar directamente a la cola (sin exchange)
            rabbitTemplate.convertAndSend(NOTIFICATIONS_QUEUE, notificacion);

            log.info("📧 Notificación '{}' enviada a cola '{}' para: {}", notificationType, NOTIFICATIONS_QUEUE, destinatario);

        } catch (Exception e) {
            log.error("❌ Error al enviar notificación: {}", e.getMessage(), e);
        }
    }

    /**
     * Mapea los tipos internos de notificación a los valores válidos del enum NotificationType
     * de notification-service.
     *
     * Valores válidos del enum:
     * - DOCUMENT_SUBMITTED
     * - EVALUATION_COMPLETED
     * - EVALUATOR_ASSIGNED
     * - STATUS_CHANGED
     * - DEADLINE_REMINDER
     */
    private String mapearTipoNotificacion(String tipoInterno) {
        return switch (tipoInterno) {
            case "FORMATO_A_ENVIADO" -> "DOCUMENT_SUBMITTED";
            case "ANTEPROYECTO_ENVIADO" -> "DOCUMENT_SUBMITTED";
            case "EVALUADOR_ASIGNADO" -> "EVALUATOR_ASSIGNED";
            case "EVALUACION_COMPLETADA" -> "EVALUATION_COMPLETED";
            case "ESTADO_CAMBIADO" -> "STATUS_CHANGED";
            default -> "DOCUMENT_SUBMITTED"; // Por defecto
        };
    }

    @Override
    public void notificarCoordinadorFormatoAEnviado(Long proyectoId, int version) {
        try {
            log.info("Notificando coordinador sobre Formato A - ProyectoID: {}, Version: {}",
                    proyectoId, version);

            // Obtener información del proyecto
            co.unicauca.submission.domain.model.Proyecto proyecto = repositoryPort
                .findById(co.unicauca.submission.domain.model.ProyectoId.of(proyectoId))
                .orElse(null);

            if (proyecto == null) {
                log.warn("No se pudo obtener proyecto {} para notificación", proyectoId);
                return;
            }

            // Obtener información del director
            String submittedBy = "Docente Director";
            try {
                co.unicauca.submission.application.port.out.IIdentityServicePort.UsuarioInfo director =
                    identityServicePort.obtenerUsuario(proyecto.getParticipantes().getDirectorId());
                if (director != null) {
                    submittedBy = director.nombreCompleto();
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener nombre del director: {}", e.getMessage());
            }

            // Construir datos con TODOS los campos requeridos
            Map<String, Object> datos = new HashMap<>();
            datos.put("proyectoId", proyectoId);
            datos.put("tipo", "FORMATO_A_ENVIADO");

            // Campos requeridos por DOCUMENT_SUBMITTED
            datos.put("projectTitle", proyecto.getTitulo().getValue());
            datos.put("documentType", "FORMATO_A");
            datos.put("submittedBy", submittedBy);
            datos.put("submissionDate", java.time.LocalDateTime.now().toString());
            datos.put("documentVersion", version);

            // Obtener email del coordinador
            String emailCoordinador = identityServicePort.obtenerEmailCoordinador();

            enviarNotificacion(
                emailCoordinador,
                "Nuevo Formato A para evaluación",
                String.format("Se ha enviado el Formato A (versión %d) del proyecto '%s' para su evaluación.",
                             version, proyecto.getTitulo().getValue()),
                datos
            );

            log.info("✅ Notificación enviada exitosamente al coordinador");

        } catch (Exception e) {
            log.error("❌ Error al notificar coordinador: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notificarJefeDepartamentoAnteproyecto(Long proyectoId) {
        try {
            log.info("Notificando jefe de departamento sobre anteproyecto - ProyectoID: {}", proyectoId);

            // Obtener información del proyecto
            co.unicauca.submission.domain.model.Proyecto proyecto = repositoryPort
                .findById(co.unicauca.submission.domain.model.ProyectoId.of(proyectoId))
                .orElse(null);

            if (proyecto == null) {
                log.warn("No se pudo obtener proyecto {} para notificación", proyectoId);
                return;
            }

            // Obtener información del director
            String submittedBy = "Docente Director";
            try {
                co.unicauca.submission.application.port.out.IIdentityServicePort.UsuarioInfo director =
                    identityServicePort.obtenerUsuario(proyecto.getParticipantes().getDirectorId());
                if (director != null) {
                    submittedBy = director.nombreCompleto();
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener nombre del director: {}", e.getMessage());
            }

            // Construir datos con TODOS los campos requeridos
            Map<String, Object> datos = new HashMap<>();
            datos.put("proyectoId", proyectoId);
            datos.put("tipo", "ANTEPROYECTO_ENVIADO");

            // Campos requeridos por DOCUMENT_SUBMITTED
            datos.put("projectTitle", proyecto.getTitulo().getValue());
            datos.put("documentType", "ANTEPROYECTO");
            datos.put("submittedBy", submittedBy);
            datos.put("submissionDate", java.time.LocalDateTime.now().toString());
            datos.put("documentVersion", 1); // Anteproyecto siempre es versión 1

            // Obtener email del jefe de departamento
            String emailJefe = identityServicePort.obtenerEmailJefeDepartamento();

            enviarNotificacion(
                emailJefe,
                "Nuevo Anteproyecto para asignar evaluadores",
                String.format("Se ha subido el anteproyecto del proyecto '%s'. Por favor asigne evaluadores.",
                             proyecto.getTitulo().getValue()),
                datos
            );

            log.info("✅ Notificación enviada exitosamente al jefe de departamento");

        } catch (Exception e) {
            log.error("❌ Error al notificar jefe departamento: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notificarEvaluadoresAsignados(Long proyectoId, Long evaluador1Id, Long evaluador2Id) {
        try {
            log.info("Notificando evaluadores asignados - ProyectoID: {}, Eval1: {}, Eval2: {}",
                    proyectoId, evaluador1Id, evaluador2Id);

            // Obtener información del proyecto
            co.unicauca.submission.domain.model.Proyecto proyecto = repositoryPort
                .findById(co.unicauca.submission.domain.model.ProyectoId.of(proyectoId))
                .orElse(null);

            if (proyecto == null) {
                log.warn("No se pudo obtener proyecto {} para notificación", proyectoId);
                return;
            }

            // Obtener información del director
            String directorName = "Docente Director";
            try {
                co.unicauca.submission.application.port.out.IIdentityServicePort.UsuarioInfo director =
                    identityServicePort.obtenerUsuario(proyecto.getParticipantes().getDirectorId());
                if (director != null) {
                    directorName = director.nombreCompleto();
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener nombre del director: {}", e.getMessage());
            }

            // Fecha límite (15 días a partir de hoy)
            String dueDate = java.time.LocalDateTime.now().plusDays(15).toString();

            // Construir datos con TODOS los campos requeridos para EVALUATOR_ASSIGNED
            Map<String, Object> datos = new HashMap<>();
            datos.put("proyectoId", proyectoId);
            datos.put("tipo", "EVALUADOR_ASIGNADO");
            datos.put("projectTitle", proyecto.getTitulo().getValue());
            datos.put("documentType", "ANTEPROYECTO");
            datos.put("directorName", directorName);
            datos.put("dueDate", dueDate);

            // Notificar al evaluador 1
            try {
                co.unicauca.submission.application.port.out.IIdentityServicePort.UsuarioInfo eval1 =
                    identityServicePort.obtenerUsuario(evaluador1Id);
                String emailEval1 = eval1 != null ? eval1.email() : ("evaluador" + evaluador1Id + "@unicauca.edu.co");

                enviarNotificacion(
                    emailEval1,
                    "Asignado como evaluador de anteproyecto",
                    String.format("Ha sido asignado como evaluador del anteproyecto del proyecto '%s'.",
                                 proyecto.getTitulo().getValue()),
                    datos
                );
            } catch (Exception e) {
                log.warn("No se pudo notificar al evaluador 1: {}", e.getMessage());
            }

            // Notificar al evaluador 2
            try {
                co.unicauca.submission.application.port.out.IIdentityServicePort.UsuarioInfo eval2 =
                    identityServicePort.obtenerUsuario(evaluador2Id);
                String emailEval2 = eval2 != null ? eval2.email() : ("evaluador" + evaluador2Id + "@unicauca.edu.co");

                enviarNotificacion(
                    emailEval2,
                    "Asignado como evaluador de anteproyecto",
                    String.format("Ha sido asignado como evaluador del anteproyecto del proyecto '%s'.",
                                 proyecto.getTitulo().getValue()),
                    datos
                );
            } catch (Exception e) {
                log.warn("No se pudo notificar al evaluador 2: {}", e.getMessage());
            }

            log.info("✅ Notificaciones enviadas exitosamente a evaluadores");

        } catch (Exception e) {
            log.error("❌ Error al notificar evaluadores: {}", e.getMessage(), e);
        }
    }
}

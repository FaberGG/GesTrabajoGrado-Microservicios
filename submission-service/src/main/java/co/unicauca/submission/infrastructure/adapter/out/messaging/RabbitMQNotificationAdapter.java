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

    private static final String EXCHANGE = "notification.exchange";
    private static final String ROUTING_KEY = "notification.email";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQNotificationAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void enviarNotificacion(String destinatario, String asunto, String mensaje, Map<String, Object> datos) {
        try {
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("destinatario", destinatario);
            notificacion.put("asunto", asunto);
            notificacion.put("mensaje", mensaje);
            notificacion.put("datos", datos);
            notificacion.put("timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, notificacion);

            log.debug("Notificación enviada a: {}", destinatario);

        } catch (Exception e) {
            log.error("Error al enviar notificación: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notificarCoordinadorFormatoAEnviado(Long proyectoId, int version) {
        try {
            log.info("Notificando coordinador sobre Formato A - ProyectoID: {}, Version: {}",
                    proyectoId, version);

            Map<String, Object> datos = new HashMap<>();
            datos.put("proyectoId", proyectoId);
            datos.put("version", version);
            datos.put("tipo", "FORMATO_A_ENVIADO");

            // El coordinador se obtendría del identity-service
            // Por ahora simplificamos enviando a un destinatario genérico
            enviarNotificacion(
                "coordinador@unicauca.edu.co",
                "Nuevo Formato A para evaluación",
                String.format("Se ha enviado el Formato A (versión %d) del proyecto %d para su evaluación.",
                             version, proyectoId),
                datos
            );

        } catch (Exception e) {
            log.error("Error al notificar coordinador: {}", e.getMessage());
        }
    }

    @Override
    public void notificarJefeDepartamentoAnteproyecto(Long proyectoId) {
        try {
            log.info("Notificando jefe de departamento sobre anteproyecto - ProyectoID: {}", proyectoId);

            Map<String, Object> datos = new HashMap<>();
            datos.put("proyectoId", proyectoId);
            datos.put("tipo", "ANTEPROYECTO_ENVIADO");

            enviarNotificacion(
                "jefe.depto@unicauca.edu.co",
                "Nuevo Anteproyecto para asignar evaluadores",
                String.format("Se ha subido el anteproyecto del proyecto %d. Por favor asigne evaluadores.",
                             proyectoId),
                datos
            );

        } catch (Exception e) {
            log.error("Error al notificar jefe departamento: {}", e.getMessage());
        }
    }

    @Override
    public void notificarEvaluadoresAsignados(Long proyectoId, Long evaluador1Id, Long evaluador2Id) {
        try {
            log.info("Notificando evaluadores asignados - ProyectoID: {}, Eval1: {}, Eval2: {}",
                    proyectoId, evaluador1Id, evaluador2Id);

            Map<String, Object> datos = new HashMap<>();
            datos.put("proyectoId", proyectoId);
            datos.put("tipo", "EVALUADOR_ASIGNADO");

            // Notificar al evaluador 1
            enviarNotificacion(
                "evaluador" + evaluador1Id + "@unicauca.edu.co",
                "Asignado como evaluador de anteproyecto",
                String.format("Ha sido asignado como evaluador del anteproyecto del proyecto %d.", proyectoId),
                datos
            );

            // Notificar al evaluador 2
            enviarNotificacion(
                "evaluador" + evaluador2Id + "@unicauca.edu.co",
                "Asignado como evaluador de anteproyecto",
                String.format("Ha sido asignado como evaluador del anteproyecto del proyecto %d.", proyectoId),
                datos
            );

        } catch (Exception e) {
            log.error("Error al notificar evaluadores: {}", e.getMessage());
        }
    }
}


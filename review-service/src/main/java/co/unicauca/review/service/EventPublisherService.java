package co.unicauca.review.service;

import co.unicauca.review.config.RabbitConfig;
import co.unicauca.review.event.AnteproyectoEvaluadoEvent;
import co.unicauca.review.event.EvaluadoresAsignadosEvent;
import co.unicauca.review.event.FormatoAEvaluadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio para publicar eventos de review-service a progress-tracking.
 *
 * Publica eventos de evaluación y asignación con información completa.
 */
@Service
public class EventPublisherService {

    private static final Logger log = LoggerFactory.getLogger(EventPublisherService.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica evento de Formato A evaluado.
     */
    public void publishFormatoAEvaluado(FormatoAEvaluadoEvent evento) {
        try {
            log.info("📤 Publicando evento: formatoa.evaluado → Proyecto: {}, Resultado: {}",
                     evento.getProyectoId(), evento.getResultado());

            rabbitTemplate.convertAndSend(
                RabbitConfig.EVALUACION_EXCHANGE,
                RabbitConfig.FORMATOA_EVALUADO_KEY,
                evento
            );

            log.info("✅ Evento publicado exitosamente: formatoa.evaluado → Proyecto {}",
                     evento.getProyectoId());

        } catch (Exception e) {
            log.error("❌ Error al publicar evento formatoa.evaluado: {}", e.getMessage(), e);
            // No lanzamos excepción para no afectar la transacción principal
        }
    }

    /**
     * Publica evento de evaluadores asignados.
     */
    public void publishEvaluadoresAsignados(EvaluadoresAsignadosEvent evento) {
        try {
            log.info("📤 Publicando evento: evaluadores.asignados → Proyecto: {}, Evaluadores: {}",
                     evento.getProyectoId(), evento.getEvaluadores().size());

            rabbitTemplate.convertAndSend(
                RabbitConfig.EVALUACION_EXCHANGE,
                RabbitConfig.EVALUADORES_ASIGNADOS_KEY,
                evento
            );

            log.info("✅ Evento publicado exitosamente: evaluadores.asignados → Proyecto {}",
                     evento.getProyectoId());

        } catch (Exception e) {
            log.error("❌ Error al publicar evento evaluadores.asignados: {}", e.getMessage(), e);
        }
    }

    /**
     * Publica evento de anteproyecto evaluado.
     */
    public void publishAnteproyectoEvaluado(AnteproyectoEvaluadoEvent evento) {
        try {
            log.info("📤 Publicando evento: anteproyecto.evaluado → Proyecto: {}, Resultado: {}",
                     evento.getProyectoId(), evento.getResultado());

            rabbitTemplate.convertAndSend(
                RabbitConfig.EVALUACION_EXCHANGE,
                RabbitConfig.ANTEPROYECTO_EVALUADO_KEY,
                evento
            );

            log.info("✅ Evento publicado exitosamente: anteproyecto.evaluado → Proyecto {}",
                     evento.getProyectoId());

        } catch (Exception e) {
            log.error("❌ Error al publicar evento anteproyecto.evaluado: {}", e.getMessage(), e);
        }
    }
}


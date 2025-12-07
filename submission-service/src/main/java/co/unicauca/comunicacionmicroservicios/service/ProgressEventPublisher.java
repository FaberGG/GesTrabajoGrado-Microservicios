package co.unicauca.comunicacionmicroservicios.service;

import co.unicauca.comunicacionmicroservicios.config.RabbitConfig;
import co.unicauca.comunicacionmicroservicios.dto.events.AnteproyectoEnviadoEvent;
import co.unicauca.comunicacionmicroservicios.dto.events.FormatoAEnviadoEvent;
import co.unicauca.comunicacionmicroservicios.dto.events.FormatoAReenviadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio para publicar eventos hacia Progress Tracking Service
 *
 * ARQUITECTURA:
 * - Publica a Exchange: "project.events.exchange"
 * - Progress Tracking Service consume de la cola bindeada a ese exchange
 * - Fire-and-forget: Si falla la publicación, NO afecta el flujo de negocio
 */
@Service
public class ProgressEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProgressEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public ProgressEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica evento de Formato A enviado (RF2)
     */
    public void publicarFormatoAEnviado(FormatoAEnviadoEvent event) {
        try {
            log.info("📤 Publicando FormatoAEnviadoEvent: proyectoId={}, version={}",
                    event.getProyectoId(), event.getVersion());

            rabbitTemplate.convertAndSend(
                    RabbitConfig.FORMATOA_EXCHANGE,
                    RabbitConfig.FORMATOA_SUBMITTED_ROUTING_KEY,
                    event
            );

            log.info("✅ Evento publicado exitosamente a exchange: {}, routing-key: {}",
                    RabbitConfig.FORMATOA_EXCHANGE, RabbitConfig.FORMATOA_SUBMITTED_ROUTING_KEY);

        } catch (Exception e) {
            // NO propagar excepción - el negocio debe continuar
            log.error("❌ Error al publicar FormatoAEnviadoEvent para proyecto {}: {}",
                    event.getProyectoId(), e.getMessage(), e);
        }
    }

    /**
     * Publica evento de Formato A reenviado (RF4)
     */
    public void publicarFormatoAReenviado(FormatoAReenviadoEvent event) {
        try {
            log.info("📤 Publicando FormatoAReenviadoEvent: proyectoId={}, version={}",
                    event.getProyectoId(), event.getVersion());

            rabbitTemplate.convertAndSend(
                    RabbitConfig.FORMATOA_EXCHANGE,
                    RabbitConfig.FORMATOA_RESUBMITTED_ROUTING_KEY,
                    event
            );

            log.info("✅ Evento publicado exitosamente a exchange: {}, routing-key: {}",
                    RabbitConfig.FORMATOA_EXCHANGE, RabbitConfig.FORMATOA_RESUBMITTED_ROUTING_KEY);

        } catch (Exception e) {
            log.error("❌ Error al publicar FormatoAReenviadoEvent para proyecto {}: {}",
                    event.getProyectoId(), e.getMessage(), e);
        }
    }

    /**
     * Publica evento de Anteproyecto enviado (RF6)
     */
    public void publicarAnteproyectoEnviado(AnteproyectoEnviadoEvent event) {
        try {
            log.info("📤 Publicando AnteproyectoEnviadoEvent: proyectoId={}",
                    event.getProyectoId());

            rabbitTemplate.convertAndSend(
                    RabbitConfig.ANTEPROYECTO_EXCHANGE,
                    RabbitConfig.ANTEPROYECTO_SUBMITTED_ROUTING_KEY,
                    event
            );

            log.info("✅ Evento publicado exitosamente a exchange: {}, routing-key: {}",
                    RabbitConfig.ANTEPROYECTO_EXCHANGE, RabbitConfig.ANTEPROYECTO_SUBMITTED_ROUTING_KEY);

        } catch (Exception e) {
            log.error("❌ Error al publicar AnteproyectoEnviadoEvent para proyecto {}: {}",
                    event.getProyectoId(), e.getMessage(), e);
        }
    }
}

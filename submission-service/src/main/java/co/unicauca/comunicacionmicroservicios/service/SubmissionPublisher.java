package co.unicauca.comunicacionmicroservicios.service;

import co.unicauca.comunicacionmicroservicios.dto.SubmissionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publicador de eventos para comunicación ASÍNCRONA mediante RabbitMQ
 * Publica mensajes en el exchange configurado para que sean consumidos
 * por el servicio de notificaciones u otros consumidores interesados
 */
@Service
@RequiredArgsConstructor
public class SubmissionPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    @Value("${submission.exchange}")
    private String exchange;

    @Value("${submission.routing-key}")
    private String routingKey;

    /**
     * Publica un mensaje genérico en RabbitMQ
     * @param payload Objeto a publicar
     */
    public void publish(Object payload) {
        logger.info("Publicando mensaje en RabbitMQ - Exchange: {}, RoutingKey: {}", exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
        logger.info("Mensaje publicado exitosamente");
    }

    /**
     * Publica un mensaje de tipo SubmissionMessage en RabbitMQ
     * @param msg Mensaje con información del anteproyecto
     */
    public void publish(SubmissionMessage msg) {
        logger.info("Publicando SubmissionMessage - ProyectoID: {}, Título: {}",
                    msg.getProyectoId(), msg.getTitulo());
        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
        logger.info("SubmissionMessage publicado exitosamente");
    }
}

package co.unicauca.comunicacionmicroservicios.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Publicador de eventos de dominio desde Submission. */
@Service
@RequiredArgsConstructor
public class SubmissionPublisher {

    private static final Logger log = LoggerFactory.getLogger(SubmissionPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    // Exchanges
    @Value("${submission.exchanges.formato-a:formato-a-exchange}")
    private String formatoAExchange;

    @Value("${submission.exchanges.anteproyecto:anteproyecto-exchange}")
    private String anteproyectoExchange;

    @Value("${submission.exchanges.proyecto:proyecto-exchange}")
    private String proyectoExchange;

    // Routing keys
    @Value("${submission.routing.formato-a-enviado:formato-a.enviado}")
    private String rkFormatoAEnviado;

    @Value("${submission.routing.formato-a-reenviado:formato-a.reenviado}")
    private String rkFormatoAReenviado;

    @Value("${submission.routing.anteproyecto-enviado:anteproyecto.enviado}")
    private String rkAnteproyectoEnviado;

    @Value("${submission.routing.proyecto-rechazo-def:proyecto.rechazado-definitivamente}")
    private String rkProyectoRechazoDef;

    public void publicarFormatoAEnviado(Object payload) {
        publish(formatoAExchange, rkFormatoAEnviado, payload);
    }
    public void publicarFormatoAReenviado(Object payload) {
        publish(formatoAExchange, rkFormatoAReenviado, payload);
    }
    public void publicarAnteproyectoEnviado(Object payload) {
        publish(anteproyectoExchange, rkAnteproyectoEnviado, payload);
    }
    public void publicarProyectoRechazoDefinitivo(Object payload) {
        publish(proyectoExchange, rkProyectoRechazoDef, payload);
    }

    private void publish(String exchange, String routingKey, Object payload) {
        log.info("Publicando evento: exchange={}, routingKey={}, payloadClass={}",
                exchange, routingKey, payload != null ? payload.getClass().getSimpleName() : "null");
        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
    }
}

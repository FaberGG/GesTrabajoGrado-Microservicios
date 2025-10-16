
package co.unicauca.comunicacionmicroservicios.service;

import co.unicauca.comunicacionmicroservicios.dto.SubmissionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubmissionPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${submission.exchange}")
    private String exchange;

    @Value("${submission.routing-key}")
    private String routingKey;

    public void publish(Object payload) {
        // IMPORTANTE: en direct exchange, la routing key debe coincidir EXACTO con el binding
        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
    }
    public void publish(SubmissionMessage msg) {
        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
    }

}


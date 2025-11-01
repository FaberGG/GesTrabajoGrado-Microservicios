package edu.unicauca.progresstracking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para Progress Tracking Service
 *
 * Este servicio CONSUME eventos de otros servicios (Submission, Review)
 * y NO publica eventos (es un Read Model CQRS).
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    /**
     * Exchange de tipo Topic para enrutamiento flexible
     *
     * Routing keys esperadas:
     * - project.formatoa.submitted
     * - project.formatoa.resubmitted
     * - project.formatoa.evaluated
     * - project.anteproyecto.submitted
     * - project.evaluators.assigned
     * - project.anteproyecto.evaluated
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    /**
     * Cola exclusiva para Progress Tracking Service
     * Durable = true: la cola sobrevive a reinicios del broker
     */
    @Bean
    public Queue queue() {
        return new Queue(queueName, true);
    }

    /**
     * Binding: conecta la cola con el exchange usando el routing key pattern
     * El pattern "project.#" captura todos los eventos que empiecen con "project."
     */
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(routingKey);
    }

    /**
     * Converter para serializar/deserializar mensajes en JSON
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configurado con el converter JSON
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
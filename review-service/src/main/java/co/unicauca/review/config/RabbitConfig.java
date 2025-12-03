package co.unicauca.review.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EVALUATION_EXCHANGE = "evaluation.exchange";
    public static final String EVALUATION_QUEUE = "evaluation.notifications.queue";
    public static final String EVALUATION_ROUTING_KEY = "evaluation.completed";

    @Bean
    public DirectExchange evaluationExchange() {
        return new DirectExchange(EVALUATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue evaluationQueue() {
        return new Queue(EVALUATION_QUEUE, true);
    }

    @Bean
    public Binding evaluationBinding(Queue evaluationQueue,
                                     DirectExchange evaluationExchange) {
        return BindingBuilder.bind(evaluationQueue)
            .to(evaluationExchange)
            .with(EVALUATION_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}

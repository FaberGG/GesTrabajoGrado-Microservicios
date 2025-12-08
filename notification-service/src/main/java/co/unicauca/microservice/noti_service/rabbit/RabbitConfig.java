package co.unicauca.microservice.noti_service.rabbit;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String NOTIFICATIONS_QUEUE = "notifications.q";
    public static final String NOTIFICATIONS_RETRY_QUEUE = "notifications.retry.q";
    public static final String NOTIFICATIONS_DLQ = "notifications.dlq";

    public static final String HEADER_CORRELATION = "X-Correlation-Id";
    public static final String HEADER_RETRIES = "x-retries";
    public static final int RETRY_TTL_MS = 5000;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_QUEUE).build();
    }

    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", NOTIFICATIONS_QUEUE)
                .withArgument("x-message-ttl", RETRY_TTL_MS)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_DLQ).build();
    }
}

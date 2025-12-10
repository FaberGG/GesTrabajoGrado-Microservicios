package co.unicauca.submission.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para publicación de eventos.
 *
 * Configura exchanges, queues y bindings necesarios para la comunicación
 * con progress-tracking-service.
 */
@Configuration
public class RabbitMQConfig {

    // ==================== EXCHANGES ====================

    public static final String FORMATO_A_EXCHANGE = "formato-a-exchange";
    public static final String ANTEPROYECTO_EXCHANGE = "anteproyecto-exchange";

    // ==================== QUEUES ====================

    public static final String FORMATO_A_QUEUE = "progress.formato-a.queue";
    public static final String ANTEPROYECTO_QUEUE = "progress.anteproyecto.queue";

    // ==================== ROUTING KEYS ====================

    public static final String FORMATO_A_ENVIADO_KEY = "formato-a.enviado";
    public static final String FORMATO_A_REENVIADO_KEY = "formato-a.reenviado";
    public static final String ANTEPROYECTO_ENVIADO_KEY = "anteproyecto.enviado";

    // ==================== EXCHANGE DECLARATIONS ====================

    @Bean
    public DirectExchange formatoAExchange() {
        return new DirectExchange(FORMATO_A_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange anteproyectoExchange() {
        return new DirectExchange(ANTEPROYECTO_EXCHANGE, true, false);
    }

    // ==================== QUEUE DECLARATIONS ====================

    @Bean
    public Queue formatoAQueue() {
        return QueueBuilder.durable(FORMATO_A_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24 horas
                .build();
    }

    @Bean
    public Queue anteproyectoQueue() {
        return QueueBuilder.durable(ANTEPROYECTO_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24 horas
                .build();
    }

    // ==================== BINDINGS ====================

    @Bean
    public Binding formatoAEnviadoBinding() {
        return BindingBuilder
                .bind(formatoAQueue())
                .to(formatoAExchange())
                .with(FORMATO_A_ENVIADO_KEY);
    }

    @Bean
    public Binding formatoAReenviadoBinding() {
        return BindingBuilder
                .bind(formatoAQueue())
                .to(formatoAExchange())
                .with(FORMATO_A_REENVIADO_KEY);
    }

    @Bean
    public Binding anteproyectoEnviadoBinding() {
        return BindingBuilder
                .bind(anteproyectoQueue())
                .to(anteproyectoExchange())
                .with(ANTEPROYECTO_ENVIADO_KEY);
    }

    // ==================== MESSAGE CONVERTER ====================

    @Bean
    public MessageConverter messageConverter() {
        // Configurar ObjectMapper para manejar LocalDateTime correctamente
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        // Registrar módulo JavaTimeModule para soporte de Java 8 date/time types
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        // Configurar para escribir fechas como strings ISO-8601 (no timestamps)
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Ignorar propiedades desconocidas al deserializar
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}


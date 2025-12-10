package co.unicauca.review.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para review-service.
 *
 * Configura exchanges, queues y bindings para:
 * - Notificaciones internas (evaluation.exchange)
 * - Eventos a progress-tracking (evaluacion-exchange)
 */
@Configuration
public class RabbitConfig {

    // ==================== EXCHANGES ====================

    // Exchange para notificaciones internas (legacy - mantener para compatibilidad)
    public static final String EVALUATION_EXCHANGE = "evaluation.exchange";

    // Exchange para eventos a progress-tracking
    public static final String EVALUACION_EXCHANGE = "evaluacion-exchange";

    // ==================== QUEUES ====================

    // Queue para notificaciones internas (legacy)
    public static final String EVALUATION_QUEUE = "evaluation.notifications.queue";

    // Queues para progress-tracking
    public static final String EVALUACION_QUEUE = "progress.evaluacion.queue";

    // ==================== ROUTING KEYS ====================

    // Keys internas (legacy)
    public static final String EVALUATION_ROUTING_KEY = "evaluation.completed";

    // Keys para progress-tracking
    public static final String FORMATOA_EVALUADO_KEY = "formatoa.evaluado";
    public static final String EVALUADORES_ASIGNADOS_KEY = "evaluadores.asignados";
    public static final String ANTEPROYECTO_EVALUADO_KEY = "anteproyecto.evaluado";

    // ==================== EXCHANGE DECLARATIONS ====================

    @Bean
    public DirectExchange evaluationExchange() {
        return new DirectExchange(EVALUATION_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange evaluacionExchange() {
        return new DirectExchange(EVALUACION_EXCHANGE, true, false);
    }

    // ==================== QUEUE DECLARATIONS ====================

    @Bean
    public Queue evaluationQueue() {
        return new Queue(EVALUATION_QUEUE, true);
    }

    @Bean
    public Queue evaluacionQueue() {
        return QueueBuilder.durable(EVALUACION_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24 horas
                .build();
    }

    // ==================== BINDINGS ====================

    // Binding interno (legacy)
    @Bean
    public Binding evaluationBinding(Queue evaluationQueue,
                                     DirectExchange evaluationExchange) {
        return BindingBuilder.bind(evaluationQueue)
            .to(evaluationExchange)
            .with(EVALUATION_ROUTING_KEY);
    }

    // Bindings para progress-tracking
    @Bean
    public Binding formatoAEvaluadoBinding() {
        return BindingBuilder
                .bind(evaluacionQueue())
                .to(evaluacionExchange())
                .with(FORMATOA_EVALUADO_KEY);
    }

    @Bean
    public Binding evaluadoresAsignadosBinding() {
        return BindingBuilder
                .bind(evaluacionQueue())
                .to(evaluacionExchange())
                .with(EVALUADORES_ASIGNADOS_KEY);
    }

    @Bean
    public Binding anteproyectoEvaluadoBinding() {
        return BindingBuilder
                .bind(evaluacionQueue())
                .to(evaluacionExchange())
                .with(ANTEPROYECTO_EVALUADO_KEY);
    }

    // ==================== MESSAGE CONVERTER ====================

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        // Configurar ObjectMapper para manejar LocalDateTime correctamente
        ObjectMapper configuredMapper = objectMapper.copy();

        // Registrar módulo JavaTimeModule para Java 8 date/time types
        configuredMapper.registerModule(new JavaTimeModule());

        // Configurar para escribir fechas como strings ISO-8601 (no timestamps)
        configuredMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Ignorar propiedades desconocidas al deserializar
        configuredMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new Jackson2JsonMessageConverter(configuredMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}

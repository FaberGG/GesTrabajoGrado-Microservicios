package co.unicauca.comunicacionmicroservicios.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para publicar notificaciones asíncronas.
 *
 * ARQUITECTURA:
 * - Submission Service publica mensajes NotificationRequest a la cola "notifications.q"
 * - Notification Service consume de esa cola y procesa las notificaciones
 * - Submission Service también publica eventos de proyectos a "project.events.exchange"
 * - Progress Tracking Service consume eventos de proyectos para actualizar estados
 * - Se usa una ÚNICA instancia de RabbitMQ compartida entre todos los microservicios
 *
 * REQUISITOS FUNCIONALES CUBIERTOS:
 * - RF2: Notificar al coordinador cuando se envía Formato A (v1)
 * - RF4: Notificar al coordinador cuando se reenvía Formato A (v2, v3)
 * - RF6: Notificar al jefe de departamento cuando se envía anteproyecto
 */
@Configuration
public class RabbitConfig {

    // ===== CONFIGURACIÓN EXISTENTE (NOTIFICACIONES) =====

    /**
     * Nombre de la cola compartida para todas las notificaciones.
     * Esta cola es consumida por el notification-service.
     */
    public static final String NOTIFICATIONS_QUEUE = "notifications.q";

    // ===== NUEVA CONFIGURACIÓN PARA PROGRESS TRACKING =====

    /**
     * Exchanges para eventos de proyectos (sincronizados con progress-tracking-service)
     */
    public static final String FORMATOA_EXCHANGE = "formato-a-exchange";
    public static final String ANTEPROYECTO_EXCHANGE = "anteproyecto-exchange";
    public static final String PROYECTO_EXCHANGE = "proyecto-exchange";

    /**
     * Routing keys para los diferentes eventos
     */
    public static final String FORMATOA_SUBMITTED_ROUTING_KEY = "formato-a.enviado";
    public static final String FORMATOA_RESUBMITTED_ROUTING_KEY = "formato-a.reenviado";
    public static final String ANTEPROYECTO_SUBMITTED_ROUTING_KEY = "anteproyecto.enviado";
    public static final String PROYECTO_RECHAZADO_ROUTING_KEY = "proyecto.rechazado-definitivamente";

    /**
     * Converter para serializar/deserializar mensajes como JSON.
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitTemplate configurado con converter JSON.
     * Se usa para publicar mensajes NotificationRequest a la cola.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate rt = new RabbitTemplate(connectionFactory);
        rt.setMessageConverter(converter);
        return rt;
    }

    /**
     * Declaración de la cola de notificaciones.
     * Solo se declara en el productor para asegurar que existe.
     * El notification-service también la declara (operación idempotente).
     */
    @Bean
    public Queue notificationsQueue() {
        return new Queue(NOTIFICATIONS_QUEUE, true); // durable
    }

    /**
     * Declara los Direct Exchanges para eventos de proyectos
     * Progress Tracking Service se bindea a estos exchanges
     */
    @Bean
    public DirectExchange formatoAExchange() {
        return new DirectExchange(FORMATOA_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange anteproyectoExchange() {
        return new DirectExchange(ANTEPROYECTO_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange proyectoExchange() {
        return new DirectExchange(PROYECTO_EXCHANGE, true, false);
    }
}

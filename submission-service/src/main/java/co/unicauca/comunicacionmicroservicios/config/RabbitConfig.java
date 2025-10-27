package co.unicauca.comunicacionmicroservicios.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para publicar eventos de dominio desde Submission:
 * - formato-a-exchange        (routing: formato-a.enviado, formato-a.reenviado)
 * - anteproyecto-exchange     (routing: anteproyecto.enviado)
 * - proyecto-exchange         (routing: proyecto.rechazado-definitivamente)
 *
 * Las colas deben declararse en los servicios consumidores (p.ej. notification-service).
 */
@Configuration
public class RabbitConfig {

    // Exchanges
    @Value("${submission.exchanges.formato-a:formato-a-exchange}")
    private String formatoAExchangeName;

    @Value("${submission.exchanges.anteproyecto:anteproyecto-exchange}")
    private String anteproyectoExchangeName;

    @Value("${submission.exchanges.proyecto:proyecto-exchange}")
    private String proyectoExchangeName;

    // Routing keys
    @Value("${submission.routing.formato-a-enviado:formato-a.enviado}")
    public String rkFormatoAEnviado;

    @Value("${submission.routing.formato-a-reenviado:formato-a.reenviado}")
    public String rkFormatoAReenviado;

    @Value("${submission.routing.anteproyecto-enviado:anteproyecto.enviado}")
    public String rkAnteproyectoEnviado;

    @Value("${submission.routing.proyecto-rechazo-def:proyecto.rechazado-definitivamente}")
    public String rkProyectoRechazoDef;

    // Exchanges como beans (sin colas/bindings aquí)
    @Bean
    public Exchange formatoAExchange() { return new DirectExchange(formatoAExchangeName, true, false); }

    @Bean
    public Exchange anteproyectoExchange() { return new DirectExchange(anteproyectoExchangeName, true, false); }

    @Bean
    public Exchange proyectoExchange() { return new DirectExchange(proyectoExchangeName, true, false); }

    // Converter JSON
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // RabbitTemplate con converter JSON
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate rt = new RabbitTemplate(connectionFactory);
        rt.setMessageConverter(converter);
        return rt;
    }
}

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
 * Configuración de RabbitMQ para Progress Tracking Service (CQRS Read Model)
 *
 * ARQUITECTURA EVENT-DRIVEN:
 * - Submission Service publica eventos de dominio a exchanges específicos
 * - Progress Service consume estos eventos para construir vistas materializadas
 * - Review Service también publicará eventos de evaluación
 *
 * EXCHANGES Y ROUTING KEYS:
 * 1. formato-a-exchange:
 *    - formato-a.enviado (v1)
 *    - formato-a.reenviado (v2, v3)
 * 2. anteproyecto-exchange:
 *    - anteproyecto.enviado
 * 3. proyecto-exchange:
 *    - proyecto.rechazado-definitivamente
 * 4. evaluacion-exchange:
 *    - formatoa.evaluado
 *    - anteproyecto.evaluado
 *    - evaluadores.asignados
 */
@Configuration
public class RabbitMQConfig {

    // ==========================================
    // COLAS DURABLES PARA PROGRESS SERVICE
    // ==========================================

    /**
     * Cola para eventos de Formato A (envío y reenvío)
     */
    @Bean
    public Queue formatoAProgressQueue() {
        return QueueBuilder.durable("progress.formato-a.queue").build();
    }

    /**
     * Cola para eventos de Anteproyecto
     */
    @Bean
    public Queue anteproyectoProgressQueue() {
        return QueueBuilder.durable("progress.anteproyecto.queue").build();
    }

    /**
     * Cola para eventos de Proyecto (rechazo definitivo)
     */
    @Bean
    public Queue proyectoProgressQueue() {
        return QueueBuilder.durable("progress.proyecto.queue").build();
    }

    /**
     * Cola para eventos de Evaluación (desde review-service)
     */
    @Bean
    public Queue evaluacionProgressQueue() {
        return QueueBuilder.durable("progress.evaluacion.queue").build();
    }

    /**
     * Cola para eventos de Evaluadores Asignados (desde review-service)
     */
    @Bean
    public Queue evaluadoresProgressQueue() {
        return QueueBuilder.durable("progress.evaluadores.queue").build();
    }

    // ==========================================
    // EXCHANGES (DECLARADOS POR SUBMISSION)
    // ==========================================

    /**
     * Exchange para eventos de Formato A
     */
    @Bean
    public DirectExchange formatoAExchange() {
        return new DirectExchange("formato-a-exchange", true, false);
    }

    /**
     * Exchange para eventos de Anteproyecto
     */
    @Bean
    public DirectExchange anteproyectoExchange() {
        return new DirectExchange("anteproyecto-exchange", true, false);
    }

    /**
     * Exchange para eventos de Proyecto
     */
    @Bean
    public DirectExchange proyectoExchange() {
        return new DirectExchange("proyecto-exchange", true, false);
    }

    /**
     * Exchange para eventos de Evaluación
     */
    @Bean
    public DirectExchange evaluacionExchange() {
        return new DirectExchange("evaluacion-exchange", true, false);
    }

    // ==========================================
    // BINDINGS: CONECTAR COLAS CON EXCHANGES
    // ==========================================

    /**
     * Binding: formato-a.enviado -> progress.formato-a.queue
     */
    @Bean
    public Binding bindFormatoAEnviado(Queue formatoAProgressQueue, DirectExchange formatoAExchange) {
        return BindingBuilder
                .bind(formatoAProgressQueue)
                .to(formatoAExchange)
                .with("formato-a.enviado");
    }

    /**
     * Binding: formato-a.reenviado -> progress.formato-a.queue
     */
    @Bean
    public Binding bindFormatoAReenviado(Queue formatoAProgressQueue, DirectExchange formatoAExchange) {
        return BindingBuilder
                .bind(formatoAProgressQueue)
                .to(formatoAExchange)
                .with("formato-a.reenviado");
    }

    /**
     * Binding: anteproyecto.enviado -> progress.anteproyecto.queue
     */
    @Bean
    public Binding bindAnteproyectoEnviado(Queue anteproyectoProgressQueue, DirectExchange anteproyectoExchange) {
        return BindingBuilder
                .bind(anteproyectoProgressQueue)
                .to(anteproyectoExchange)
                .with("anteproyecto.enviado");
    }

    /**
     * Binding: proyecto.rechazado-definitivamente -> progress.proyecto.queue
     */
    @Bean
    public Binding bindProyectoRechazado(Queue proyectoProgressQueue, DirectExchange proyectoExchange) {
        return BindingBuilder
                .bind(proyectoProgressQueue)
                .to(proyectoExchange)
                .with("proyecto.rechazado-definitivamente");
    }

    /**
     * Binding: formatoa.evaluado -> progress.evaluacion.queue
     */
    @Bean
    public Binding bindFormatoAEvaluado(Queue evaluacionProgressQueue, DirectExchange evaluacionExchange) {
        return BindingBuilder
                .bind(evaluacionProgressQueue)
                .to(evaluacionExchange)
                .with("formatoa.evaluado");
    }

    /**
     * Binding: anteproyecto.evaluado -> progress.evaluacion.queue
     */
    /**
     * Binding: evaluadores.asignados -> progress.evaluadores.queue
     */
    @Bean
    public Binding bindEvaluadoresAsignados(Queue evaluadoresProgressQueue, DirectExchange evaluacionExchange) {
        return BindingBuilder
                .bind(evaluadoresProgressQueue)
                .to(evaluacionExchange)
                .with("evaluadores.asignados");
    }

    @Bean
    public Binding bindAnteproyectoEvaluado(Queue evaluacionProgressQueue, DirectExchange evaluacionExchange) {
        return BindingBuilder
                .bind(evaluacionProgressQueue)
                .to(evaluacionExchange)
                .with("anteproyecto.evaluado");
    }

    // ==========================================
    // CONVERTER Y TEMPLATE
    // ==========================================

    /**
     * Converter JSON para deserializar eventos
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate (solo para testing, este servicio no publica)
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
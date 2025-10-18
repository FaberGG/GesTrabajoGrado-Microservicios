/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.comunicacionmicroservicios.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configuración de RabbitMQ para comunicación ASÍNCRONA (Escenario 1)
 *
 * Arquitectura:
 * - Exchange: submission.exchange (tipo Direct)
 * - Queue: submission.queue
 * - Routing Key: submission.created
 *
 * Cuando Submission crea un anteproyecto, publica un mensaje en el exchange.
 * El mensaje es enrutado a la cola submission.queue mediante la routing key.
 * El servicio de Notification consume mensajes de esta cola de manera asíncrona.
 */
@Configuration
public class RabbitConfig {

  @Value("${submission.exchange}")
  private String exchangeName;

  @Value("${submission.queue}")
  private String queueName;

  @Value("${submission.routing-key}")
  private String routingKey;

  /**
   * Define el exchange de tipo Direct para publicar eventos de submission
   */
  @Bean
  public Exchange submissionExchange() {
    return ExchangeBuilder.directExchange(exchangeName).durable(true).build();
  }

  /**
   * Define la cola donde se almacenarán los mensajes de submission
   * Es durable para persistir mensajes aunque RabbitMQ se reinicie
   */
  @Bean
  public Queue submissionQueue() {
    return QueueBuilder.durable(queueName).build();
  }

  /**
   * Vincula la cola con el exchange mediante la routing key
   * Los mensajes publicados con esta routing key llegarán a la cola
   */
  @Bean
  public Binding binding(Queue submissionQueue, Exchange submissionExchange) {
    return BindingBuilder.bind(submissionQueue).to(submissionExchange).with(routingKey).noargs();
  }

  /**
   * Conversor JSON para serializar/deserializar mensajes automáticamente
   */
  @Bean
  public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  /**
   * Template para publicar mensajes en RabbitMQ con conversión JSON automática
   */
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
    RabbitTemplate rt = new RabbitTemplate(connectionFactory);
    rt.setMessageConverter(converter);
    return rt;
  }
}
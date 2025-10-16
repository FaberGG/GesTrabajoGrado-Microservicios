/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.comunicacionmicroservicios.config;
/**
 *
 * @author USUARIO
 */

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitConfig {

  @Value("${submission.exchange}")
  private String exchangeName;

  @Value("${submission.queue}")
  private String queueName;

  @Value("${submission.routing-key}")
  private String routingKey;

  @Bean
  public Exchange submissionExchange() {
    return ExchangeBuilder.directExchange(exchangeName).durable(true).build();
  }

  @Bean
  public Queue submissionQueue() {
    return QueueBuilder.durable(queueName).build();
  }

  @Bean
  public Binding binding(Queue submissionQueue, Exchange submissionExchange) {
    return BindingBuilder.bind(submissionQueue).to(submissionExchange).with(routingKey).noargs();
  }

  @Bean
  public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
    RabbitTemplate rt = new RabbitTemplate(connectionFactory);
    rt.setMessageConverter(converter);
    return rt;
  }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.comunicacionmicroservicios.service;

/**
 *
 * @author USUARIO
 */

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.unicauca.comunicacionmicroservicios.infraestructure.repository.*;
import co.unicauca.comunicacionmicroservicios.domain.model.*;
import co.unicauca.comunicacionmicroservicios.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SubmissionService {

  private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

  private final ProyectoGradoRepository proyectoRepo;
  private final FormatoARepository formatoRepo;
  private final RabbitTemplate rabbitTemplate;
  private final NotificationClient notificationClient;

  @Value("${submission.exchange}")
  private String exchange;

  @Value("${submission.routing-key}")
  private String routingKey;

  /**
   * Crea un anteproyecto de grado y notifica usando AMBOS escenarios:
   * 1. Escenario Asíncrono: Publica mensaje en RabbitMQ para notificar a múltiples destinatarios
   * 2. Escenario Síncrono: Llamada HTTP directa al servicio de notificación para el jefe de departamento
   */
  @Transactional
  public SubmissionResponse createSubmission(SubmissionRequest req) {
    logger.info("=== Iniciando creación de anteproyecto: {} ===", req.getTitulo());

    // 1. Validar y persistir el anteproyecto
    ProyectoGrado p = new ProyectoGrado();
    p.setTitulo(req.getTitulo());
    p.setDirectorId(req.getDirectorId());
    p.setEstudiante1Id(req.getEstudiante1Id());
    p.setEstudiante2Id(req.getEstudiante2Id());
    p.setObjetivoGeneral(req.getResumen());
    p = proyectoRepo.save(p);

    FormatoA f = new FormatoA();
    f.setProyecto(p);
    f.setNumeroIntento(1);
    f.setNombreArchivo(req.getTitulo() + ".pdf");
    f.setRutaArchivo("/files/" + f.getNombreArchivo());
    f.setFechaCarga(java.time.LocalDateTime.now());
    formatoRepo.save(f);

    logger.info("Anteproyecto persistido con ID: {}", p.getId());

    // ============================================================================
    // ESCENARIO 1: COMUNICACIÓN ASÍNCRONA CON RABBITMQ
    // ============================================================================
    // Publicar mensaje en cola RabbitMQ para notificar a estudiantes, tutores y jefe
    // El servicio de notificación consumirá este mensaje de manera asíncrona
    logger.info("--- ESCENARIO 1: Publicando mensaje asíncrono a RabbitMQ ---");

    SubmissionMessage msg = new SubmissionMessage();
    msg.setProyectoId(p.getId());
    msg.setTitulo(p.getTitulo());
    msg.setAutoresEmails(req.getAutoresEmails());

    try {
      rabbitTemplate.convertAndSend(exchange, routingKey, msg);
      logger.info("✓ Mensaje publicado en RabbitMQ - Exchange: {}, RoutingKey: {}", exchange, routingKey);
      logger.info("  Destinatarios (asíncrono): {}", req.getAutoresEmails());
    } catch (Exception e) {
      logger.error("✗ Error al publicar mensaje en RabbitMQ", e);
    }

    // ============================================================================
    // ESCENARIO 2: COMUNICACIÓN SÍNCRONA CON HTTP
    // ============================================================================
    // Llamada directa al microservicio de notificación para enviar email al jefe de departamento
    logger.info("--- ESCENARIO 2: Enviando notificación síncrona por HTTP ---");

    Map<String, Object> syncPayload = new HashMap<>();
    syncPayload.put("proyectoId", p.getId());
    syncPayload.put("titulo", p.getTitulo());
    syncPayload.put("tipo", "NOTIFICACION_JEFE_DEPARTAMENTO");
    syncPayload.put("to", "jefe.departamento@unicauca.edu.co");
    syncPayload.put("subject", "Nuevo anteproyecto recibido: " + p.getTitulo());
    syncPayload.put("body", String.format(
        "Se ha recibido un nuevo anteproyecto.\n" +
        "ID: %d\n" +
        "Título: %s\n" +
        "Estudiantes: %s\n" +
        "Por favor, revise el anteproyecto en el sistema.",
        p.getId(), p.getTitulo(), String.join(", ", req.getAutoresEmails())
    ));

    // Realizar llamada HTTP síncrona (no bloqueante gracias a Mono)
    notificationClient.sendNotification(syncPayload)
        .doOnSuccess(v -> logger.info("✓ Notificación síncrona enviada exitosamente al jefe de departamento"))
        .doOnError(e -> logger.error("✗ Error en notificación síncrona: {}", e.getMessage()))
        .subscribe(); // fire-and-forget: no bloquea la respuesta al cliente

    logger.info("=== Anteproyecto creado exitosamente con ID: {} ===", p.getId());

    return new SubmissionResponse(p.getId(), p.getEstado().name());
  }
}

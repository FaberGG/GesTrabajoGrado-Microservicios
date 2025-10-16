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

import java.util.*;

@Service
@RequiredArgsConstructor
public class SubmissionService {

  private final ProyectoGradoRepository proyectoRepo;
  private final FormatoARepository formatoRepo;
  private final RabbitTemplate rabbitTemplate;
  private final NotificationClient notificationClient;

  @Value("${submission.exchange}")
  private String exchange;

  @Value("${submission.routing-key}")
  private String routingKey;

  @Transactional
  public SubmissionResponse createSubmission(SubmissionRequest req) {
    ProyectoGrado p = new ProyectoGrado();
    p.setTitulo(req.getTitulo());
    p.setDirectorId(req.getDirectorId());
    p.setEstudiante1Id(req.getEstudiante1Id());
    p.setEstudiante2Id(req.getEstudiante2Id());
    p.setObjetivoGeneral(req.getResumen()); // mapea resumen a objetivoGeneral
    p = proyectoRepo.save(p);

    FormatoA f = new FormatoA();
    f.setProyecto(p);
    f.setNumeroIntento(1);
    f.setNombreArchivo(req.getTitulo() + ".pdf"); // ejemplo; en real recibirás archivo
    f.setRutaArchivo("/files/" + f.getNombreArchivo());
    f.setFechaCarga(java.time.LocalDateTime.now());
    formatoRepo.save(f);

    // publicar mensaje a RabbitMQ (asincrono)
    SubmissionMessage msg = new SubmissionMessage();
    msg.setProyectoId(p.getId());
    msg.setTitulo(p.getTitulo());
    msg.setAutoresEmails(req.getAutoresEmails());
    rabbitTemplate.convertAndSend(exchange, routingKey, msg);

    // llamada síncrona al Notification (si falla, no rompe)
    Map<String,Object> payload = new HashMap<>();
    payload.put("to", req.getAutoresEmails());
    payload.put("subject", "Anteproyecto recibido: " + p.getTitulo());
    payload.put("body", "Su anteproyecto fue recibido con id: " + p.getId());
    // fire-and-forget: intentamos la llamada pero no bloqueamos la petición principal
    notificationClient.sendNotification(payload).subscribe();

    return new SubmissionResponse(p.getId(), p.getEstado().name());
  }
}


package edu.unicauca.progresstracking.messaging.consumer;

import edu.unicauca.progresstracking.domain.entity.HistorialEvento;
import edu.unicauca.progresstracking.domain.repository.HistorialEventoRepository;
import edu.unicauca.progresstracking.messaging.events.*;
import edu.unicauca.progresstracking.service.ProjectStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer de eventos RabbitMQ para Progress Tracking
 *
 * Este componente escucha la cola de eventos y actualiza:
 * 1. El historial de eventos (tabla historial_eventos)
 * 2. La vista materializada del estado actual (tabla proyecto_estado)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectEventConsumer {

    private final HistorialEventoRepository historialRepository;
    private final ProjectStateService projectStateService;

    /**
     * Consumir evento: Formato A enviado (primera vez)
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void onFormatoAEnviado(FormatoAEnviadoEvent event) {
        log.info("📥 Evento recibido: FORMATO_A_ENVIADO - Proyecto: {}, Versión: {}",
                event.getProyectoId(), event.getVersion());

        try {
            // 1. Guardar en historial
            HistorialEvento historial = HistorialEvento.builder()
                    .proyectoId(event.getProyectoId())
                    .tipoEvento("FORMATO_A_ENVIADO")
                    .fecha(event.getTimestamp())
                    .descripcion(event.getDescripcion())
                    .version(event.getVersion())
                    .usuarioResponsableId(event.getUsuarioResponsableId())
                    .usuarioResponsableNombre(event.getUsuarioResponsableNombre())
                    .usuarioResponsableRol(event.getUsuarioResponsableRol())
                    .build();

            historialRepository.save(historial);
            log.debug("✅ Evento guardado en historial");

            // 2. Actualizar estado materializado
            String nuevoEstado = "FORMATO_A_EN_EVALUACION_" + event.getVersion();
            projectStateService.actualizarEstado(event.getProyectoId(), nuevoEstado, event);
            log.info("✅ Estado actualizado a: {}", nuevoEstado);

        } catch (Exception e) {
            log.error("❌ Error procesando FORMATO_A_ENVIADO: {}", e.getMessage(), e);
            // En producción: enviar a DLQ (Dead Letter Queue)
        }
    }

    /**
     * Consumir evento: Formato A reenviado (correcciones)
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void onFormatoAReenviado(FormatoAReenviadoEvent event) {
        log.info("📥 Evento recibido: FORMATO_A_REENVIADO - Proyecto: {}, Versión: {}",
                event.getProyectoId(), event.getVersion());

        try {
            // Guardar en historial
            HistorialEvento historial = HistorialEvento.builder()
                    .proyectoId(event.getProyectoId())
                    .tipoEvento("FORMATO_A_REENVIADO")
                    .fecha(event.getTimestamp())
                    .descripcion(event.getDescripcion())
                    .version(event.getVersion())
                    .usuarioResponsableId(event.getUsuarioResponsableId())
                    .usuarioResponsableNombre(event.getUsuarioResponsableNombre())
                    .usuarioResponsableRol(event.getUsuarioResponsableRol())
                    .build();

            historialRepository.save(historial);

            // Actualizar estado
            String nuevoEstado = "FORMATO_A_EN_EVALUACION_" + event.getVersion();
            projectStateService.actualizarEstado(event.getProyectoId(), nuevoEstado, event);
            log.info("✅ Estado actualizado a: {}", nuevoEstado);

        } catch (Exception e) {
            log.error("❌ Error procesando FORMATO_A_REENVIADO: {}", e.getMessage(), e);
        }
    }

    /**
     * Consumir evento: Formato A evaluado (aprobado/rechazado)
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void onFormatoAEvaluado(FormatoAEvaluadoEvent event) {
        log.info("📥 Evento recibido: FORMATO_A_EVALUADO - Proyecto: {}, Resultado: {}",
                event.getProyectoId(), event.getResultado());

        try {
            // Guardar en historial
            HistorialEvento historial = HistorialEvento.builder()
                    .proyectoId(event.getProyectoId())
                    .tipoEvento("FORMATO_A_EVALUADO")
                    .fecha(event.getTimestamp())
                    .descripcion("Formato A evaluado: " + event.getResultado())
                    .version(event.getVersion())
                    .resultado(event.getResultado())
                    .observaciones(event.getObservaciones())
                    .usuarioResponsableId(event.getUsuarioResponsableId())
                    .usuarioResponsableNombre(event.getUsuarioResponsableNombre())
                    .usuarioResponsableRol(event.getUsuarioResponsableRol())
                    .build();

            historialRepository.save(historial);

            // Determinar nuevo estado según resultado
            String nuevoEstado;
            if ("APROBADO".equals(event.getResultado())) {
                nuevoEstado = "FORMATO_A_APROBADO";
            } else {
                if (event.getRechazadoDefinitivo() != null && event.getRechazadoDefinitivo()) {
                    nuevoEstado = "FORMATO_A_RECHAZADO_DEFINITIVO";
                } else {
                    nuevoEstado = "FORMATO_A_RECHAZADO_" + event.getVersion();
                }
            }

            projectStateService.actualizarEstado(event.getProyectoId(), nuevoEstado, event);
            log.info("✅ Estado actualizado a: {}", nuevoEstado);

        } catch (Exception e) {
            log.error("❌ Error procesando FORMATO_A_EVALUADO: {}", e.getMessage(), e);
        }
    }

    /**
     * Consumir evento: Anteproyecto enviado
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void onAnteproyectoEnviado(AnteproyectoEnviadoEvent event) {
        log.info("📥 Evento recibido: ANTEPROYECTO_ENVIADO - Proyecto: {}",
                event.getProyectoId());

        try {
            HistorialEvento historial = HistorialEvento.builder()
                    .proyectoId(event.getProyectoId())
                    .tipoEvento("ANTEPROYECTO_ENVIADO")
                    .fecha(event.getTimestamp())
                    .descripcion(event.getDescripcion())
                    .usuarioResponsableId(event.getUsuarioResponsableId())
                    .usuarioResponsableNombre(event.getUsuarioResponsableNombre())
                    .usuarioResponsableRol(event.getUsuarioResponsableRol())
                    .build();

            historialRepository.save(historial);

            projectStateService.actualizarEstado(
                    event.getProyectoId(),
                    "ANTEPROYECTO_ENVIADO",
                    event
            );
            log.info("✅ Estado actualizado a: ANTEPROYECTO_ENVIADO");

        } catch (Exception e) {
            log.error("❌ Error procesando ANTEPROYECTO_ENVIADO: {}", e.getMessage(), e);
        }
    }

    /**
     * Consumir evento: Evaluadores asignados
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void onEvaluadoresAsignados(EvaluadoresAsignadosEvent event) {
        log.info("📥 Evento recibido: EVALUADORES_ASIGNADOS - Proyecto: {}",
                event.getProyectoId());

        try {
            // Convertir lista de evaluadores a string para descripción
            StringBuilder evaluadores = new StringBuilder();
            event.getEvaluadores().forEach(e ->
                    evaluadores.append(e.getNombre()).append(", ")
            );

            HistorialEvento historial = HistorialEvento.builder()
                    .proyectoId(event.getProyectoId())
                    .tipoEvento("EVALUADORES_ASIGNADOS")
                    .fecha(event.getTimestamp())
                    .descripcion("Evaluadores asignados: " + evaluadores.toString())
                    .usuarioResponsableId(event.getUsuarioResponsableId())
                    .usuarioResponsableNombre(event.getUsuarioResponsableNombre())
                    .usuarioResponsableRol(event.getUsuarioResponsableRol())
                    .build();

            historialRepository.save(historial);

            projectStateService.actualizarEstado(
                    event.getProyectoId(),
                    "ANTEPROYECTO_EN_EVALUACION",
                    event
            );
            log.info("✅ Estado actualizado a: ANTEPROYECTO_EN_EVALUACION");

        } catch (Exception e) {
            log.error("❌ Error procesando EVALUADORES_ASIGNADOS: {}", e.getMessage(), e);
        }
    }

    /**
     * Consumir evento: Anteproyecto evaluado
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void onAnteproyectoEvaluado(AnteproyectoEvaluadoEvent event) {
        log.info("📥 Evento recibido: ANTEPROYECTO_EVALUADO - Proyecto: {}, Resultado: {}",
                event.getProyectoId(), event.getResultado());

        try {
            HistorialEvento historial = HistorialEvento.builder()
                    .proyectoId(event.getProyectoId())
                    .tipoEvento("ANTEPROYECTO_EVALUADO")
                    .fecha(event.getTimestamp())
                    .descripcion("Anteproyecto evaluado: " + event.getResultado())
                    .resultado(event.getResultado())
                    .observaciones(event.getObservaciones())
                    .usuarioResponsableId(event.getUsuarioResponsableId())
                    .usuarioResponsableNombre(event.getUsuarioResponsableNombre())
                    .usuarioResponsableRol(event.getUsuarioResponsableRol())
                    .build();

            historialRepository.save(historial);

            String nuevoEstado = "APROBADO".equals(event.getResultado())
                    ? "ANTEPROYECTO_APROBADO"
                    : "ANTEPROYECTO_RECHAZADO";

            projectStateService.actualizarEstado(event.getProyectoId(), nuevoEstado, event);
            log.info("✅ Estado actualizado a: {}", nuevoEstado);

        } catch (Exception e) {
            log.error("❌ Error procesando ANTEPROYECTO_EVALUADO: {}", e.getMessage(), e);
        }
    }
}
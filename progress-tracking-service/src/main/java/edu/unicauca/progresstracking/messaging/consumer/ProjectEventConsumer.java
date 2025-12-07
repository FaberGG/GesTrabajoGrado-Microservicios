package edu.unicauca.progresstracking.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.unicauca.progresstracking.domain.entity.HistorialEvento;
import edu.unicauca.progresstracking.domain.repository.HistorialEventoRepository;
import edu.unicauca.progresstracking.service.ProjectStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Consumer de eventos RabbitMQ para Progress Tracking (CQRS Read Model)
 *
 * RESPONSABILIDADES:
 * 1. Escuchar eventos de dominio desde submission-service y review-service
 * 2. Guardar eventos en historial_eventos (Event Store inmutable)
 * 3. Proyectar eventos en proyecto_estado (Vista Materializada)
 *
 * COLAS CONSUMIDAS:
 * - progress.formato-a.queue: eventos formato-a.enviado, formato-a.reenviado
 * - progress.anteproyecto.queue: eventos anteproyecto.enviado
 * - progress.proyecto.queue: eventos proyecto.rechazado-definitivamente
 * - progress.evaluacion.queue: eventos formatoa.evaluado, anteproyecto.evaluado
 * - progress.evaluadores.queue: eventos evaluadores.asignados
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectEventConsumer {

    private final HistorialEventoRepository historialRepository;
    private final ProjectStateService projectStateService;
    private final ObjectMapper objectMapper;

    // ==========================================
    // EVENTOS DE FORMATO A (ENVÍO Y REENVÍO)
    // ==========================================

    /**
     * Consumir eventos de Formato A desde submission-service
     *
     * Routing keys manejadas:
     * - formato-a.enviado (v1)
     * - formato-a.reenviado (v2, v3)
     */
    @RabbitListener(queues = "progress.formato-a.queue")
    public void onFormatoAEvent(
            @Payload Map<String, Object> payload,
            @Header("amqp_receivedRoutingKey") String routingKey
    ) {
        try {
            log.info("📥 [FORMATO A] Evento recibido: {} - Payload: {}", routingKey, payload);

            // Extraer datos del payload
            Long proyectoId = extractLong(payload, "proyectoId");
            Integer version = extractInteger(payload, "version");
            String titulo = (String) payload.getOrDefault("titulo", "Sin título");

            // Extraer información del director
            Long directorId = extractLong(payload, "directorId");
            String directorNombre = (String) payload.get("directorNombre");

            String timestamp = (String) payload.get("timestamp");

            // Extraer información de estudiantes
            Long estudiante1Id = extractLong(payload, "estudiante1Id");
            String estudiante1Nombre = (String) payload.get("estudiante1Nombre");
            String estudiante1Email = (String) payload.get("estudiante1Email");
            Long estudiante2Id = extractLong(payload, "estudiante2Id");
            String estudiante2Nombre = (String) payload.get("estudiante2Nombre");
            String estudiante2Email = (String) payload.get("estudiante2Email");

            // Determinar tipo de evento y estado
            String tipoEvento = "formato-a.enviado".equals(routingKey)
                    ? "FORMATO_A_ENVIADO"
                    : "FORMATO_A_REENVIADO";

            String nuevoEstado = determinarEstadoFormatoA(version);

            // 1. Guardar en historial (Event Store)
            HistorialEvento historial = HistorialEvento.builder()
                    .proyectoId(proyectoId)
                    .tipoEvento(tipoEvento)
                    .fecha(parseTimestamp(timestamp))
                    .descripcion(String.format("Formato A v%d enviado: %s", version, titulo))
                    .version(version)
                    .metadata(serializeToJson(payload))
                    .build();

            historialRepository.save(historial);
            log.debug("✅ Evento guardado en historial: ID={}", historial.getEventoId());

            // 2. Actualizar vista materializada con información de estudiantes y director
            projectStateService.actualizarEstadoFormatoAConEstudiantes(
                    proyectoId,
                    titulo,
                    version,
                    nuevoEstado,
                    directorId,
                    directorNombre,
                    estudiante1Id,
                    estudiante1Nombre,
                    estudiante1Email,
                    estudiante2Id,
                    estudiante2Nombre,
                    estudiante2Email,
                    payload
            );

            log.info("✅ [FORMATO A] Proyecto {} actualizado a: {} - Director: {} - Estudiantes: [{}, {}]",
                    proyectoId, nuevoEstado, directorNombre, estudiante1Nombre, estudiante2Nombre);

        } catch (Exception e) {
            log.error("❌ Error procesando evento Formato A: routingKey={}, error={}",
                    routingKey, e.getMessage(), e);
            // TODO: Enviar a DLQ (Dead Letter Queue) en producción
        }
    }

    // ==========================================
    // EVENTOS DE ANTEPROYECTO
    // ==========================================

    /**
     * Consumir evento: anteproyecto.enviado
     * Incluye toda la información del proyecto
     */
    @RabbitListener(queues = "progress.anteproyecto.queue")
    public void onAnteproyectoEvent(@Payload Map<String, Object> payload) {
        try {
            log.info("📥 [ANTEPROYECTO] Evento recibido: {}", payload);

            // Extraer datos COMPLETOS del payload
            Long proyectoId = extractLong(payload, "proyectoId");
            String titulo = (String) payload.getOrDefault("titulo", "Sin título");
            String modalidad = (String) payload.get("modalidad");
            String programa = (String) payload.get("programa");

            // Director
            Long directorId = extractLong(payload, "directorId");
            String directorNombre = (String) payload.get("directorNombre");

            // Co-director (opcional)
            Long codirectorId = extractLong(payload, "codirectorId");
            String codirectorNombre = (String) payload.get("codirectorNombre");

            // Estudiantes
            Long estudiante1Id = extractLong(payload, "estudiante1Id");
            String estudiante1Nombre = (String) payload.get("estudiante1Nombre");
            String estudiante1Email = (String) payload.get("estudiante1Email");
            Long estudiante2Id = extractLong(payload, "estudiante2Id");
            String estudiante2Nombre = (String) payload.get("estudiante2Nombre");
            String estudiante2Email = (String) payload.get("estudiante2Email");

            String timestamp = (String) payload.get("timestamp");

            // Guardar en historial (Event Store)
            HistorialEvento historial = HistorialEvento.builder()
                    .proyectoId(proyectoId)
                    .tipoEvento("ANTEPROYECTO_ENVIADO")
                    .fecha(parseTimestamp(timestamp))
                    .descripcion(String.format("Anteproyecto enviado: %s", titulo))
                    .metadata(serializeToJson(payload))
                    .build();

            historialRepository.save(historial);
            log.debug("✅ Evento guardado en historial: ID={}", historial.getEventoId());

            // Actualizar vista materializada con toda la información
            projectStateService.actualizarEstadoAnteproyectoCompleto(
                    proyectoId,
                    titulo,
                    modalidad,
                    programa,
                    "ANTEPROYECTO_ENVIADO",
                    directorId,
                    directorNombre,
                    codirectorId,
                    codirectorNombre,
                    estudiante1Id,
                    estudiante1Nombre,
                    estudiante1Email,
                    estudiante2Id,
                    estudiante2Nombre,
                    estudiante2Email
            );

            log.info("✅ [ANTEPROYECTO] Proyecto {} actualizado a: ANTEPROYECTO_ENVIADO - Modalidad: {}, Estudiantes: [{}, {}]",
                    proyectoId, modalidad, estudiante1Nombre, estudiante2Nombre);

        } catch (Exception e) {
            log.error("❌ Error procesando evento Anteproyecto: {}", e.getMessage(), e);
            // TODO: Enviar a DLQ (Dead Letter Queue) en producción
        }
    }

    // ==========================================
    // EVENTOS DE PROYECTO (RECHAZO DEFINITIVO)
    // ==========================================

    /**
     * Consumir evento: proyecto.rechazado-definitivamente
     * Este evento se publica tras el tercer rechazo de Formato A
     */
    @RabbitListener(queues = "progress.proyecto.queue")
    public void onProyectoEvent(@Payload Map<String, Object> payload) {
        try {
            log.info("📥 [PROYECTO] Evento recibido: {}", payload);

            Long proyectoId = extractLong(payload, "proyectoId");
            String titulo = (String) payload.getOrDefault("titulo", "Sin título");
            String timestamp = (String) payload.get("timestamp");

            // Guardar en historial
            HistorialEvento historial = HistorialEvento.builder()
                    .proyectoId(proyectoId)
                    .tipoEvento("PROYECTO_RECHAZADO_DEFINITIVO")
                    .fecha(parseTimestamp(timestamp))
                    .descripcion("Proyecto rechazado definitivamente tras 3 intentos: " + titulo)
                    .resultado("RECHAZADO_DEFINITIVO")
                    .metadata(serializeToJson(payload))
                    .build();

            historialRepository.save(historial);

            // Actualizar estado a rechazado definitivo
            projectStateService.actualizarEstadoRechazadoDefinitivo(proyectoId);

            log.info("✅ [PROYECTO] Proyecto {} marcado como RECHAZADO_DEFINITIVO", proyectoId);

        } catch (Exception e) {
            log.error("❌ Error procesando evento Proyecto: {}", e.getMessage(), e);
        }
    }

    // ==========================================
    // EVENTOS DE EVALUACIÓN (DESDE REVIEW-SERVICE)
    // ==========================================

    /**
     * Consumir eventos de evaluación desde review-service
     *
     * Routing keys:
     * - formatoa.evaluado
     * - anteproyecto.evaluado
     */
    @RabbitListener(queues = "progress.evaluacion.queue")
    public void onEvaluacionEvent(
            @Payload Map<String, Object> payload,
            @Header("amqp_receivedRoutingKey") String routingKey
    ) {
        try {
            log.info("📥 [EVALUACIÓN] Evento recibido: {} - Payload: {}", routingKey, payload);

            Long proyectoId = extractLong(payload, "proyectoId");
            String resultado = (String) payload.get("resultado"); // APROBADO / RECHAZADO
            String observaciones = (String) payload.getOrDefault("observaciones", "");
            Integer version = extractInteger(payload, "version");
            Boolean rechazadoDefinitivo = (Boolean) payload.getOrDefault("rechazadoDefinitivo", false);
            String timestamp = (String) payload.get("timestamp");

            String tipoEvento = "formatoa.evaluado".equals(routingKey)
                    ? "FORMATO_A_EVALUADO"
                    : "ANTEPROYECTO_EVALUADO";

            // Guardar en historial
            HistorialEvento historial = HistorialEvento.builder()
                    .proyectoId(proyectoId)
                    .tipoEvento(tipoEvento)
                    .fecha(parseTimestamp(timestamp))
                    .descripcion(String.format("Evaluación completada: %s", resultado))
                    .version(version)
                    .resultado(resultado)
                    .observaciones(observaciones)
                    .metadata(serializeToJson(payload))
                    .build();

            historialRepository.save(historial);

            // Determinar nuevo estado según resultado
            if ("formatoa.evaluado".equals(routingKey)) {
                String nuevoEstado = determinarEstadoEvaluacionFormatoA(resultado, version, rechazadoDefinitivo);
                projectStateService.actualizarEstadoEvaluacionFormatoA(
                        proyectoId,
                        nuevoEstado,
                        resultado,
                        rechazadoDefinitivo
                );
                log.info("✅ [EVALUACIÓN FORMATO A] Proyecto {} -> {}", proyectoId, nuevoEstado);
            } else {
                String nuevoEstado = "APROBADO".equals(resultado)
                        ? "ANTEPROYECTO_APROBADO"
                        : "ANTEPROYECTO_RECHAZADO";
                projectStateService.actualizarEstadoEvaluacionAnteproyecto(proyectoId, nuevoEstado);
                log.info("✅ [EVALUACIÓN ANTEPROYECTO] Proyecto {} -> {}", proyectoId, nuevoEstado);
            }

        } catch (Exception e) {
            log.error("❌ Error procesando evento Evaluación: routingKey={}, error={}",
                    routingKey, e.getMessage(), e);
        }
    }

    // ==========================================
    // EVENTOS DE EVALUADORES ASIGNADOS
    // ==========================================

    /**
     * Consumir evento: evaluadores.asignados
     * Se publica cuando el jefe de departamento asigna evaluadores al anteproyecto
     */
    @RabbitListener(queues = "progress.evaluadores.queue")
    public void onEvaluadoresAsignadosEvent(@Payload Map<String, Object> payload) {
        try {
            log.info("📥 [EVALUADORES] Evento recibido: {}", payload);

            Long proyectoId = extractLong(payload, "proyectoId");
            String timestamp = (String) payload.get("timestamp");

            // Extraer lista de evaluadores
            Object evaluadoresObj = payload.get("evaluadores");
            int cantidadEvaluadores = 0;

            if (evaluadoresObj instanceof java.util.List) {
                cantidadEvaluadores = ((java.util.List<?>) evaluadoresObj).size();
            }

            // Guardar en historial
            HistorialEvento historial = HistorialEvento.builder()
                    .proyectoId(proyectoId)
                    .tipoEvento("EVALUADORES_ASIGNADOS")
                    .fecha(parseTimestamp(timestamp))
                    .descripcion(String.format("Se asignaron %d evaluadores al anteproyecto", cantidadEvaluadores))
                    .metadata(serializeToJson(payload))
                    .build();

            historialRepository.save(historial);

            // Actualizar estado del proyecto
            projectStateService.actualizarEstadoEvaluadoresAsignados(proyectoId);

            log.info("✅ [EVALUADORES] Proyecto {} - Evaluadores asignados: {}", proyectoId, cantidadEvaluadores);

        } catch (Exception e) {
            log.error("❌ Error procesando evento Evaluadores Asignados: {}", e.getMessage(), e);
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    private String determinarEstadoFormatoA(Integer version) {
        return switch (version) {
            case 1 -> "EN_PRIMERA_EVALUACION_FORMATO_A";
            case 2 -> "EN_SEGUNDA_EVALUACION_FORMATO_A";
            case 3 -> "EN_TERCERA_EVALUACION_FORMATO_A";
            default -> "FORMATO_A_ENVIADO_V" + version;
        };
    }

    private String determinarEstadoEvaluacionFormatoA(String resultado, Integer version, Boolean rechazadoDefinitivo) {
        if ("APROBADO".equals(resultado)) {
            return "FORMATO_A_APROBADO";
        } else {
            if (rechazadoDefinitivo != null && rechazadoDefinitivo) {
                return "FORMATO_A_RECHAZADO_DEFINITIVO";
            } else {
                return "FORMATO_A_RECHAZADO_" + version;
            }
        }
    }

    private Long extractLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private Integer extractInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Error parseando timestamp: {}, usando fecha actual", timestamp);
            return LocalDateTime.now();
        }
    }

    private String serializeToJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.warn("Error serializando payload a JSON: {}", e.getMessage());
            return payload.toString();
        }
    }
}

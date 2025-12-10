package edu.unicauca.progresstracking.controller;

import edu.unicauca.progresstracking.domain.entity.HistorialEvento;
import edu.unicauca.progresstracking.domain.entity.ProyectoEstado;
import edu.unicauca.progresstracking.domain.repository.HistorialEventoRepository;
import edu.unicauca.progresstracking.domain.repository.ProyectoEstadoRepository;
import edu.unicauca.progresstracking.dto.response.*;
import edu.unicauca.progresstracking.mapper.ProyectoMapper;
import edu.unicauca.progresstracking.service.ProjectStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador REST para Progress Tracking Service - Versión con Base de Datos Real
 *
 * Este servicio es un CQRS Read Model que:
 * - Proporciona vistas materializadas del estado de proyectos
 * - Expone SOLO endpoints de LECTURA (GET)
 * - Recibe eventos vía RabbitMQ (NO mediante POST REST en producción)
 */
@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ProgressController {

    private final ProyectoEstadoRepository proyectoEstadoRepository;
    private final HistorialEventoRepository historialEventoRepository;
    private final ProjectStateService projectStateService;
    private final ProyectoMapper proyectoMapper;

    // ==========================================
    // 🆕 ENDPOINTS PRINCIPALES (MVP)
    // ==========================================

    /**
     * RF5: Estudiante/Docente consulta el estado actual de su proyecto
     *
     * GET /api/progress/proyectos/{id}/estado
     */
    @GetMapping("/proyectos/{id}/estado")
    public ResponseEntity<EstadoProyectoResponseDTO> obtenerEstadoActual(@PathVariable("id") Long id) {
        log.info("📊 Consultando estado del proyecto: {}", id);

        Optional<ProyectoEstado> estadoOpt = proyectoEstadoRepository.findById(id);

        if (estadoOpt.isEmpty()) {
            log.warn("⚠️ Proyecto {} no encontrado", id);
            return ResponseEntity.ok(proyectoMapper.toEstadoProyectoNoEncontradoDTO(id));
        }

        ProyectoEstado estado = estadoOpt.get();
        EstadoProyectoResponseDTO response = proyectoMapper.toEstadoProyectoDTO(estado);

        log.info("✅ Estado consultado exitosamente");
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener el historial completo de eventos de un proyecto
     *
     * GET /api/progress/proyectos/{id}/historial
     *
     * Soporta:
     * - Paginación: ?page=0&size=20
     * - Filtros por tipo: ?tipoEvento=FORMATO_A_ENVIADO,FORMATO_A_EVALUADO
     * - Ordenamiento: siempre por fecha descendente
     */
    @GetMapping("/proyectos/{id}/historial")
    public ResponseEntity<HistorialResponseDTO> obtenerHistorialPorProyecto(
            @PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "tipoEvento", required = false) String tipoEvento
    ) {
        log.info("📜 Consultando historial del proyecto: {} (page={}, size={})", id, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<HistorialEvento> historialPage;

        // Filtrar por tipo de evento si se especifica
        if (tipoEvento != null && !tipoEvento.isEmpty()) {
            List<String> tipos = Arrays.asList(tipoEvento.split(","));
            historialPage = historialEventoRepository.findByProyectoIdAndTipoEventoIn(id, tipos, pageable);
        } else {
            historialPage = historialEventoRepository.findByProyectoIdOrderByFechaDesc(id, pageable);
        }

        // Convertir a DTOs
        List<HistorialEventoDTO> historialList = historialPage.getContent().stream()
                .map(proyectoMapper::toHistorialEventoDTO)
                .collect(Collectors.toList());

        HistorialResponseDTO response = HistorialResponseDTO.builder()
                .proyectoId(id)
                .historial(historialList)
                .paginaActual(historialPage.getNumber())
                .tamanoPagina(historialPage.getSize())
                .totalEventos(historialPage.getTotalElements())
                .totalPaginas(historialPage.getTotalPages())
                .build();

        log.info("✅ Historial consultado: {} eventos", historialList.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 🆕 NUEVO: Obtener proyectos del usuario autenticado
     *
     * GET /api/progress/proyectos/mis-proyectos
     */
    @GetMapping("/proyectos/mis-proyectos")
    public ResponseEntity<MisProyectosResponseDTO> obtenerMisProyectos(
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        // TODO: En producción, extraer userId del token JWT
        if (userId == null) {
            userId = 12L; // Usuario de prueba
        }

        log.info("👤 Consultando proyectos del usuario: {}", userId);

        // Variable final para usar en lambda
        final Long userIdFinal = userId;

        List<ProyectoEstado> proyectos = proyectoEstadoRepository.findProyectosByUsuario(userIdFinal);

        List<ProyectoResumenDTO> proyectosList = proyectos.stream()
                .map(proyecto -> proyectoMapper.toProyectoResumenDTO(proyecto, userIdFinal))
                .collect(Collectors.toList());

        MisProyectosResponseDTO response = MisProyectosResponseDTO.builder()
                .proyectos(proyectosList)
                .total(proyectosList.size())
                .build();

        log.info("✅ Encontrados {} proyectos para el usuario", proyectosList.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 🆕 NUEVO: Obtener historial del proyecto de un estudiante
     *
     * GET /api/progress/estudiantes/{estudianteId}/historial
     *
     * Este endpoint permite que un estudiante consulte el historial
     * de su proyecto sin necesidad de conocer el ID del proyecto
     */
    @GetMapping("/estudiantes/{estudianteId}/historial")
    public ResponseEntity<HistorialResponseDTO> obtenerHistorialPorEstudiante(
            @PathVariable("estudianteId") Long estudianteId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "tipoEvento", required = false) String tipoEvento
    ) {
        log.info("👨‍🎓 Consultando historial del estudiante: {} (page={}, size={})", estudianteId, page, size);

        // Buscar el proyecto más reciente del estudiante
        List<ProyectoEstado> proyectos = proyectoEstadoRepository.findByEstudianteId(estudianteId);

        if (proyectos.isEmpty()) {
            log.warn("⚠️ Estudiante {} no tiene proyectos asignados", estudianteId);
            return ResponseEntity.ok(HistorialResponseDTO.builder()
                    .error(false)
                    .mensaje("El estudiante no tiene proyectos asignados actualmente")
                    .estudianteId(estudianteId)
                    .historial(Collections.emptyList())
                    .totalEventos(0L)
                    .build());
        }

        // Tomar el primer proyecto (el más reciente según ORDER BY)
        ProyectoEstado proyecto = proyectos.get(0);
        Long proyectoId = proyecto.getProyectoId();

        // Log adicional para debugging
        if (proyectos.size() > 1) {
            log.warn("⚠️ Estudiante {} tiene {} proyectos. Retornando el más reciente (ID: {}, última actualización: {})",
                    estudianteId, proyectos.size(), proyectoId, proyecto.getUltimaActualizacion());
        } else {
            log.info("✅ Proyecto encontrado para estudiante {}: ID={}", estudianteId, proyectoId);
        }

        // Obtener historial del proyecto
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<HistorialEvento> historialPage;

        if (tipoEvento != null && !tipoEvento.isEmpty()) {
            List<String> tipos = Arrays.asList(tipoEvento.split(","));
            historialPage = historialEventoRepository.findByProyectoIdAndTipoEventoIn(proyectoId, tipos, pageable);
        } else {
            historialPage = historialEventoRepository.findByProyectoIdOrderByFechaDesc(proyectoId, pageable);
        }

        // Convertir a DTOs
        List<HistorialEventoDTO> historialList = historialPage.getContent().stream()
                .map(proyectoMapper::toHistorialEventoDTO)
                .collect(Collectors.toList());

        // Construir respuesta con información del proyecto
        HistorialResponseDTO response = HistorialResponseDTO.builder()
                .estudianteId(estudianteId)
                .proyectoId(proyectoId)
                .tituloProyecto(proyecto.getTitulo())
                .estadoActual(proyecto.getEstadoActual())
                .estadoLegible(projectStateService.convertirEstadoLegible(proyecto.getEstadoActual()))
                .fase(proyecto.getFase())
                .estudiantes(proyectoMapper.toEstudiantesDTO(proyecto))
                .historial(historialList)
                .paginaActual(historialPage.getNumber())
                .tamanoPagina(historialPage.getSize())
                .totalEventos(historialPage.getTotalElements())
                .totalPaginas(historialPage.getTotalPages())
                .build();

        log.info("✅ Historial consultado para estudiante {}: {} eventos", estudianteId, historialList.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 🆕 NUEVO: Obtener estado del proyecto de un estudiante
     *
     * GET /api/progress/estudiantes/{estudianteId}/estado
     *
     * Este endpoint permite que un estudiante consulte el estado actual
     * de su proyecto sin necesidad de conocer el ID del proyecto
     */
    @GetMapping("/estudiantes/{estudianteId}/estado")
    public ResponseEntity<EstadoProyectoResponseDTO> obtenerEstadoPorEstudiante(
            @PathVariable("estudianteId") Long estudianteId
    ) {
        log.info("👨‍🎓 Consultando estado del proyecto del estudiante: {}", estudianteId);

        // Buscar el proyecto más reciente del estudiante
        List<ProyectoEstado> proyectos = proyectoEstadoRepository.findByEstudianteId(estudianteId);

        if (proyectos.isEmpty()) {
            return ResponseEntity.ok(proyectoMapper.toEstadoSinProyectoDTO(estudianteId));
        }

        // Tomar el primer proyecto (el más reciente según ORDER BY)
        ProyectoEstado estado = proyectos.get(0);

        if (proyectos.size() > 1) {
            log.warn("⚠️ Estudiante {} tiene {} proyectos. Mostrando el más reciente (ID: {})",
                    estudianteId, proyectos.size(), estado.getProyectoId());
        }

        EstadoProyectoResponseDTO response = proyectoMapper.toEstadoProyectoPorEstudianteDTO(estado, estudianteId);

        log.info("✅ Estado consultado exitosamente para estudiante {}", estudianteId);
        return ResponseEntity.ok(response);
    }

    /**
     * 🆕 NUEVO: Buscar y filtrar proyectos
     *
     * GET /api/progress/proyectos/buscar
     */
    @GetMapping("/proyectos/buscar")
    public ResponseEntity<BusquedaProyectosResponseDTO> buscarProyectos(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "fase", required = false) String fase,
            @RequestParam(value = "programa", required = false) String programa,
            @RequestHeader(value = "X-User-Role", required = false) String rol
    ) {
        log.info("🔍 Buscando proyectos - Estado: {}, Fase: {}, Programa: {}", estado, fase, programa);

        List<ProyectoEstado> resultados = proyectoEstadoRepository.buscarProyectos(estado, fase, programa);

        List<ProyectoResumenDTO> proyectosList = resultados.stream()
                .map(proyectoMapper::toProyectoResumenDTO)
                .collect(Collectors.toList());

        BusquedaProyectosResponseDTO response = BusquedaProyectosResponseDTO.builder()
                .resultados(proyectosList)
                .total(proyectosList.size())
                .filtros(Map.of(
                        "estado", estado != null ? estado : "todos",
                        "fase", fase != null ? fase : "todas",
                        "programa", programa != null ? programa : "todos"
                ))
                .build();

        log.info("✅ Búsqueda completada: {} resultados", proyectosList.size());
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // ⚠️ ENDPOINTS TEMPORALES (SOLO PARA TESTING)
    // ==========================================

    /**
     * ⚠️ TEMPORAL: Registrar evento manualmente
     *
     * Este endpoint existe solo para pruebas. En producción,
     * los eventos llegan vía RabbitMQ.
     */
    @PostMapping("/eventos")
    public ResponseEntity<Map<String, Object>> registrarEvento(@RequestBody Map<String, Object> eventoData) {
        log.warn("⚠️ Usando endpoint temporal POST /eventos (solo para testing)");

        try {
            HistorialEvento evento = new HistorialEvento();
            evento.setProyectoId(Long.parseLong(eventoData.get("proyectoId").toString()));
            evento.setTipoEvento(eventoData.get("tipoEvento").toString());
            evento.setDescripcion(eventoData.getOrDefault("descripcion", "").toString());
            evento.setFecha(LocalDateTime.now());

            if (eventoData.containsKey("version")) {
                evento.setVersion(Integer.parseInt(eventoData.get("version").toString()));
            }
            if (eventoData.containsKey("resultado")) {
                evento.setResultado(eventoData.get("resultado").toString());
            }
            if (eventoData.containsKey("observaciones")) {
                evento.setObservaciones(eventoData.get("observaciones").toString());
            }
            if (eventoData.containsKey("usuarioResponsableId")) {
                evento.setUsuarioResponsableId(Long.parseLong(eventoData.get("usuarioResponsableId").toString()));
            }

            historialEventoRepository.save(evento);

            // Simular actualización de estado (en producción esto lo hace el consumer)
            if (eventoData.containsKey("nuevoEstado")) {
                String nuevoEstado = eventoData.get("nuevoEstado").toString();
                // Crear evento mock para actualizar estado
                // En producción esto vendría del evento de RabbitMQ
            }

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Evento registrado (modo testing)",
                    "eventoId", evento.getEventoId(),
                    "advertencia", "Este endpoint será eliminado en producción"
            ));

        } catch (Exception e) {
            log.error("Error registrando evento manual: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "mensaje", "Error registrando evento: " + e.getMessage()
            ));
        }
    }

    /**
     * ⚠️ TEMPORAL: Listar todos los eventos
     */
    @GetMapping("/eventos")
    public ResponseEntity<Map<String, Object>> listarEventos() {
        log.info("📋 Listando todos los eventos (debugging)");

        List<HistorialEvento> eventos = historialEventoRepository.findAll();
        List<HistorialEventoDTO> eventosList = eventos.stream()
                .map(proyectoMapper::toHistorialEventoDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "eventos", eventosList,
                "total", eventosList.size(),
                "advertencia", "Endpoint de debugging - no usar en producción"
        ));
    }

}
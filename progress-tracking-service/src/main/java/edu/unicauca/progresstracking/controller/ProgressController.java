package edu.unicauca.progresstracking.controller;

import edu.unicauca.progresstracking.domain.entity.HistorialEvento;
import edu.unicauca.progresstracking.domain.entity.ProyectoEstado;
import edu.unicauca.progresstracking.domain.repository.HistorialEventoRepository;
import edu.unicauca.progresstracking.domain.repository.ProyectoEstadoRepository;
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

    // ==========================================
    // 🆕 ENDPOINTS PRINCIPALES (MVP)
    // ==========================================

    /**
     * RF5: Estudiante/Docente consulta el estado actual de su proyecto
     *
     * GET /api/progress/proyectos/{id}/estado
     */
    @GetMapping("/proyectos/{id}/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstadoActual(@PathVariable("id") Long id) {
        log.info("📊 Consultando estado del proyecto: {}", id);

        Optional<ProyectoEstado> estadoOpt = proyectoEstadoRepository.findById(id);

        if (estadoOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "error", true,
                    "mensaje", "Proyecto no encontrado",
                    "proyectoId", id
            ));
        }

        ProyectoEstado estado = estadoOpt.get();

        // Construir respuesta enriquecida
        Map<String, Object> response = new HashMap<>();
        response.put("proyectoId", estado.getProyectoId());
        response.put("titulo", estado.getTitulo());
        response.put("modalidad", estado.getModalidad());
        response.put("programa", estado.getPrograma());
        response.put("estadoActual", estado.getEstadoActual());
        response.put("estadoLegible", projectStateService.convertirEstadoLegible(estado.getEstadoActual()));
        response.put("fase", estado.getFase());
        response.put("ultimaActualizacion", estado.getUltimaActualizacion());
        response.put("siguientePaso", projectStateService.determinarSiguientePaso(estado.getEstadoActual()));

        // Información de Formato A
        Map<String, Object> formatoAInfo = new HashMap<>();
        formatoAInfo.put("estado", estado.getFormatoAEstado());
        formatoAInfo.put("versionActual", estado.getFormatoAVersion());
        formatoAInfo.put("intentoActual", estado.getFormatoAIntentoActual());
        formatoAInfo.put("maxIntentos", estado.getFormatoAMaxIntentos());
        formatoAInfo.put("fechaUltimoEnvio", estado.getFormatoAFechaUltimoEnvio());
        formatoAInfo.put("fechaUltimaEvaluacion", estado.getFormatoAFechaUltimaEvaluacion());
        response.put("formatoA", formatoAInfo);

        // Información de Anteproyecto
        Map<String, Object> anteproyectoInfo = new HashMap<>();
        anteproyectoInfo.put("estado", estado.getAnteproyectoEstado());
        anteproyectoInfo.put("fechaEnvio", estado.getAnteproyectoFechaEnvio());
        anteproyectoInfo.put("evaluadoresAsignados", estado.getAnteproyectoEvaluadoresAsignados());
        response.put("anteproyecto", anteproyectoInfo);

        // Participantes
        Map<String, Object> participantes = new HashMap<>();
        if (estado.getDirectorId() != null) {
            participantes.put("director", Map.of(
                    "id", estado.getDirectorId(),
                    "nombre", estado.getDirectorNombre() != null ? estado.getDirectorNombre() : "No asignado"
            ));
        }
        if (estado.getCodirectorId() != null) {
            participantes.put("codirector", Map.of(
                    "id", estado.getCodirectorId(),
                    "nombre", estado.getCodirectorNombre() != null ? estado.getCodirectorNombre() : "No asignado"
            ));
        }
        response.put("participantes", participantes);

        // Estudiantes
        Map<String, Object> estudiantes = new HashMap<>();
        if (estado.getEstudiante1Id() != null) {
            estudiantes.put("estudiante1", Map.of(
                    "id", estado.getEstudiante1Id(),
                    "nombre", estado.getEstudiante1Nombre() != null ? estado.getEstudiante1Nombre() : "Sin nombre"
            ));
        }
        if (estado.getEstudiante2Id() != null) {
            estudiantes.put("estudiante2", Map.of(
                    "id", estado.getEstudiante2Id(),
                    "nombre", estado.getEstudiante2Nombre() != null ? estado.getEstudiante2Nombre() : "Sin nombre"
            ));
        }
        response.put("estudiantes", estudiantes);

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
    public ResponseEntity<Map<String, Object>> obtenerHistorialPorProyecto(
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
        List<Map<String, Object>> historialList = historialPage.getContent().stream()
                .map(this::convertirEventoADTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("proyectoId", id);
        response.put("historial", historialList);
        response.put("paginaActual", historialPage.getNumber());
        response.put("tamanoPagina", historialPage.getSize());
        response.put("totalEventos", historialPage.getTotalElements());
        response.put("totalPaginas", historialPage.getTotalPages());

        log.info("✅ Historial consultado: {} eventos", historialList.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 🆕 NUEVO: Obtener proyectos del usuario autenticado
     *
     * GET /api/progress/proyectos/mis-proyectos
     */
    @GetMapping("/proyectos/mis-proyectos")
    public ResponseEntity<Map<String, Object>> obtenerMisProyectos(
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

        List<Map<String, Object>> proyectosList = proyectos.stream()
                .map(proyecto -> {
                    Map<String, Object> resumen = new HashMap<>();
                    resumen.put("proyectoId", proyecto.getProyectoId());
                    resumen.put("titulo", proyecto.getTitulo());
                    resumen.put("estadoActual", proyecto.getEstadoActual());
                    resumen.put("estadoLegible", projectStateService.convertirEstadoLegible(proyecto.getEstadoActual()));
                    resumen.put("fase", proyecto.getFase());
                    resumen.put("modalidad", proyecto.getModalidad());
                    resumen.put("ultimaActualizacion", proyecto.getUltimaActualizacion());

                    // Determinar rol
                    if (userIdFinal.equals(proyecto.getDirectorId())) {
                        resumen.put("rol", "DIRECTOR");
                    } else if (userIdFinal.equals(proyecto.getCodirectorId())) {
                        resumen.put("rol", "CODIRECTOR");
                    } else {
                        resumen.put("rol", "PARTICIPANTE");
                    }

                    return resumen;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("proyectos", proyectosList);
        response.put("total", proyectosList.size());

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
    public ResponseEntity<Map<String, Object>> obtenerHistorialPorEstudiante(
            @PathVariable("estudianteId") Long estudianteId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "tipoEvento", required = false) String tipoEvento
    ) {
        log.info("👨‍🎓 Consultando historial del estudiante: {} (page={}, size={})", estudianteId, page, size);

        // Buscar el proyecto del estudiante
        Optional<ProyectoEstado> proyectoOpt = proyectoEstadoRepository.findByEstudianteId(estudianteId);

        if (proyectoOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "error", false,
                    "mensaje", "El estudiante no tiene proyectos asignados actualmente",
                    "estudianteId", estudianteId,
                    "historial", Collections.emptyList(),
                    "totalEventos", 0
            ));
        }

        ProyectoEstado proyecto = proyectoOpt.get();
        Long proyectoId = proyecto.getProyectoId();

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
        List<Map<String, Object>> historialList = historialPage.getContent().stream()
                .map(this::convertirEventoADTO)
                .collect(Collectors.toList());

        // Construir respuesta con información del proyecto
        Map<String, Object> response = new HashMap<>();
        response.put("estudianteId", estudianteId);
        response.put("proyectoId", proyectoId);
        response.put("tituloProyecto", proyecto.getTitulo());
        response.put("estadoActual", proyecto.getEstadoActual());
        response.put("estadoLegible", projectStateService.convertirEstadoLegible(proyecto.getEstadoActual()));
        response.put("fase", proyecto.getFase());

        // Información de estudiantes (compañeros)
        Map<String, Object> estudiantes = new HashMap<>();
        if (proyecto.getEstudiante1Id() != null) {
            estudiantes.put("estudiante1", Map.of(
                    "id", proyecto.getEstudiante1Id(),
                    "nombre", proyecto.getEstudiante1Nombre() != null ? proyecto.getEstudiante1Nombre() : "Sin nombre"
            ));
        }
        if (proyecto.getEstudiante2Id() != null) {
            estudiantes.put("estudiante2", Map.of(
                    "id", proyecto.getEstudiante2Id(),
                    "nombre", proyecto.getEstudiante2Nombre() != null ? proyecto.getEstudiante2Nombre() : "Sin nombre"
            ));
        }
        response.put("estudiantes", estudiantes);

        response.put("historial", historialList);
        response.put("paginaActual", historialPage.getNumber());
        response.put("tamanoPagina", historialPage.getSize());
        response.put("totalEventos", historialPage.getTotalElements());
        response.put("totalPaginas", historialPage.getTotalPages());

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
    public ResponseEntity<Map<String, Object>> obtenerEstadoPorEstudiante(
            @PathVariable("estudianteId") Long estudianteId
    ) {
        log.info("👨‍🎓 Consultando estado del proyecto del estudiante: {}", estudianteId);

        // Buscar el proyecto del estudiante
        Optional<ProyectoEstado> proyectoOpt = proyectoEstadoRepository.findByEstudianteId(estudianteId);

        if (proyectoOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "error", false,
                    "mensaje", "El estudiante no tiene proyectos asignados actualmente",
                    "estudianteId", estudianteId,
                    "tieneProyecto", false
            ));
        }

        ProyectoEstado estado = proyectoOpt.get();

        // Construir respuesta enriquecida
        Map<String, Object> response = new HashMap<>();
        response.put("tieneProyecto", true);
        response.put("estudianteId", estudianteId);
        response.put("proyectoId", estado.getProyectoId());
        response.put("titulo", estado.getTitulo());
        response.put("modalidad", estado.getModalidad());
        response.put("programa", estado.getPrograma());
        response.put("estadoActual", estado.getEstadoActual());
        response.put("estadoLegible", projectStateService.convertirEstadoLegible(estado.getEstadoActual()));
        response.put("fase", estado.getFase());
        response.put("ultimaActualizacion", estado.getUltimaActualizacion());
        response.put("siguientePaso", projectStateService.determinarSiguientePaso(estado.getEstadoActual()));

        // Información de Formato A
        Map<String, Object> formatoAInfo = new HashMap<>();
        formatoAInfo.put("estado", estado.getFormatoAEstado());
        formatoAInfo.put("versionActual", estado.getFormatoAVersion());
        formatoAInfo.put("intentoActual", estado.getFormatoAIntentoActual());
        formatoAInfo.put("maxIntentos", estado.getFormatoAMaxIntentos());
        formatoAInfo.put("fechaUltimoEnvio", estado.getFormatoAFechaUltimoEnvio());
        formatoAInfo.put("fechaUltimaEvaluacion", estado.getFormatoAFechaUltimaEvaluacion());
        response.put("formatoA", formatoAInfo);

        // Información de Anteproyecto
        Map<String, Object> anteproyectoInfo = new HashMap<>();
        anteproyectoInfo.put("estado", estado.getAnteproyectoEstado());
        anteproyectoInfo.put("fechaEnvio", estado.getAnteproyectoFechaEnvio());
        anteproyectoInfo.put("evaluadoresAsignados", estado.getAnteproyectoEvaluadoresAsignados());
        response.put("anteproyecto", anteproyectoInfo);

        // Participantes
        Map<String, Object> participantes = new HashMap<>();
        if (estado.getDirectorId() != null) {
            participantes.put("director", Map.of(
                    "id", estado.getDirectorId(),
                    "nombre", estado.getDirectorNombre() != null ? estado.getDirectorNombre() : "No asignado"
            ));
        }
        if (estado.getCodirectorId() != null) {
            participantes.put("codirector", Map.of(
                    "id", estado.getCodirectorId(),
                    "nombre", estado.getCodirectorNombre() != null ? estado.getCodirectorNombre() : "No asignado"
            ));
        }
        response.put("participantes", participantes);

        // Estudiantes
        Map<String, Object> estudiantes = new HashMap<>();
        if (estado.getEstudiante1Id() != null) {
            estudiantes.put("estudiante1", Map.of(
                    "id", estado.getEstudiante1Id(),
                    "nombre", estado.getEstudiante1Nombre() != null ? estado.getEstudiante1Nombre() : "Sin nombre"
            ));
        }
        if (estado.getEstudiante2Id() != null) {
            estudiantes.put("estudiante2", Map.of(
                    "id", estado.getEstudiante2Id(),
                    "nombre", estado.getEstudiante2Nombre() != null ? estado.getEstudiante2Nombre() : "Sin nombre"
            ));
        }
        response.put("estudiantes", estudiantes);

        log.info("✅ Estado consultado exitosamente para estudiante {}", estudianteId);
        return ResponseEntity.ok(response);
    }

    /**
     * 🆕 NUEVO: Buscar y filtrar proyectos
     *
     * GET /api/progress/proyectos/buscar
     */
    @GetMapping("/proyectos/buscar")
    public ResponseEntity<Map<String, Object>> buscarProyectos(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "fase", required = false) String fase,
            @RequestParam(value = "programa", required = false) String programa,
            @RequestHeader(value = "X-User-Role", required = false) String rol
    ) {
        log.info("🔍 Buscando proyectos - Estado: {}, Fase: {}, Programa: {}", estado, fase, programa);

        List<ProyectoEstado> resultados = proyectoEstadoRepository.buscarProyectos(estado, fase, programa);

        List<Map<String, Object>> proyectosList = resultados.stream()
                .map(this::convertirProyectoADTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("resultados", proyectosList);
        response.put("total", proyectosList.size());
        response.put("filtros", Map.of(
                "estado", estado != null ? estado : "todos",
                "fase", fase != null ? fase : "todas",
                "programa", programa != null ? programa : "todos"
        ));

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
        List<Map<String, Object>> eventosList = eventos.stream()
                .map(this::convertirEventoADTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "eventos", eventosList,
                "total", eventosList.size(),
                "advertencia", "Endpoint de debugging - no usar en producción"
        ));
    }

    // ==========================================
    // 🔧 MÉTODOS AUXILIARES
    // ==========================================

    /**
     * Convierte una entidad HistorialEvento a DTO
     */
    private Map<String, Object> convertirEventoADTO(HistorialEvento evento) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("eventoId", evento.getEventoId());
        dto.put("proyectoId", evento.getProyectoId());
        dto.put("tipoEvento", evento.getTipoEvento());
        dto.put("fecha", evento.getFecha());
        dto.put("descripcion", evento.getDescripcion());
        dto.put("version", evento.getVersion());
        dto.put("resultado", evento.getResultado());
        dto.put("observaciones", evento.getObservaciones());

        if (evento.getUsuarioResponsableId() != null) {
            Map<String, Object> responsable = new HashMap<>();
            responsable.put("id", evento.getUsuarioResponsableId());
            responsable.put("nombre", evento.getUsuarioResponsableNombre());
            responsable.put("rol", evento.getUsuarioResponsableRol());
            dto.put("responsable", responsable);
        }

        return dto;
    }

    /**
     * Convierte una entidad ProyectoEstado a DTO
     */
    private Map<String, Object> convertirProyectoADTO(ProyectoEstado proyecto) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("proyectoId", proyecto.getProyectoId());
        dto.put("titulo", proyecto.getTitulo());
        dto.put("modalidad", proyecto.getModalidad());
        dto.put("programa", proyecto.getPrograma());
        dto.put("estadoActual", proyecto.getEstadoActual());
        dto.put("estadoLegible", projectStateService.convertirEstadoLegible(proyecto.getEstadoActual()));
        dto.put("fase", proyecto.getFase());
        dto.put("ultimaActualizacion", proyecto.getUltimaActualizacion());

        if (proyecto.getDirectorId() != null) {
            dto.put("director", Map.of(
                    "id", proyecto.getDirectorId(),
                    "nombre", proyecto.getDirectorNombre() != null ? proyecto.getDirectorNombre() : "No asignado"
            ));
        }

        // Agregar información de estudiantes
        Map<String, Object> estudiantes = new HashMap<>();
        if (proyecto.getEstudiante1Id() != null) {
            estudiantes.put("estudiante1", Map.of(
                    "id", proyecto.getEstudiante1Id(),
                    "nombre", proyecto.getEstudiante1Nombre() != null ? proyecto.getEstudiante1Nombre() : "Sin nombre"
            ));
        }
        if (proyecto.getEstudiante2Id() != null) {
            estudiantes.put("estudiante2", Map.of(
                    "id", proyecto.getEstudiante2Id(),
                    "nombre", proyecto.getEstudiante2Nombre() != null ? proyecto.getEstudiante2Nombre() : "Sin nombre"
            ));
        }
        if (!estudiantes.isEmpty()) {
            dto.put("estudiantes", estudiantes);
        }

        return dto;
    }
}
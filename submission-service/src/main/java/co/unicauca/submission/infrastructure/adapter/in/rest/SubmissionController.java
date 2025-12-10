package co.unicauca.submission.infrastructure.adapter.in.rest;

import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.IObtenerProyectoQuery;
import co.unicauca.submission.domain.model.EstadoProyecto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller para consultas (queries) de proyectos.
 * Implementación con arquitectura hexagonal.
 *
 * Endpoints:
 * - GET /api/submissions/{id}
 * - GET /api/submissions/estudiante/{estudianteId}
 * - GET /api/submissions/director/{directorId}
 * - GET /api/submissions/estado/{estado}
 * - GET /api/submissions
 */
@RestController
@RequestMapping("/api/submissions")
@Tag(name = "Submissions", description = "Gestión de Proyectos de Grado (Arquitectura Hexagonal)")
public class SubmissionController {

    private static final Logger log = LoggerFactory.getLogger(SubmissionController.class);

    private final IObtenerProyectoQuery obtenerProyectoQuery;

    public SubmissionController(IObtenerProyectoQuery obtenerProyectoQuery) {
        this.obtenerProyectoQuery = obtenerProyectoQuery;
    }

    /**
     * Obtener proyecto por ID
     * GET /api/submissions/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener proyecto por ID", description = "Consulta un proyecto específico")
    public ResponseEntity<ProyectoResponse> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("GET /api/submissions/{}", id);

            ProyectoResponse response = obtenerProyectoQuery.obtenerPorId(id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener proyecto {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * RF5: Obtener proyectos de un estudiante
     * GET /api/submissions/estudiante/{estudianteId}
     */
    @GetMapping("/estudiante/{estudianteId}")
    @Operation(summary = "Obtener proyectos de estudiante",
               description = "RF5: El estudiante consulta el estado de sus proyectos")
    public ResponseEntity<List<ProyectoResponse>> obtenerPorEstudiante(@PathVariable Long estudianteId) {
        try {
            log.info("GET /api/submissions/estudiante/{}", estudianteId);

            List<ProyectoResponse> proyectos = obtenerProyectoQuery.obtenerPorEstudiante(estudianteId);

            return ResponseEntity.ok(proyectos);

        } catch (Exception e) {
            log.error("Error al obtener proyectos del estudiante {}: {}", estudianteId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener proyectos de un director
     * GET /api/submissions/director/{directorId}
     */
    @GetMapping("/director/{directorId}")
    @Operation(summary = "Obtener proyectos de director",
               description = "Consulta todos los proyectos donde el usuario es director")
    public ResponseEntity<List<ProyectoResponse>> obtenerPorDirector(@PathVariable Long directorId) {
        try {
            log.info("GET /api/submissions/director/{}", directorId);

            List<ProyectoResponse> proyectos = obtenerProyectoQuery.obtenerPorDirector(directorId);

            return ResponseEntity.ok(proyectos);

        } catch (Exception e) {
            log.error("Error al obtener proyectos del director {}: {}", directorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener proyectos por estado
     * GET /api/submissions/estado/{estado}
     */
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener proyectos por estado",
               description = "Filtra proyectos por su estado actual")
    public ResponseEntity<List<ProyectoResponse>> obtenerPorEstado(@PathVariable EstadoProyecto estado) {
        try {
            log.info("GET /api/submissions/estado/{}", estado);

            List<ProyectoResponse> proyectos = obtenerProyectoQuery.obtenerPorEstado(estado);

            return ResponseEntity.ok(proyectos);

        } catch (Exception e) {
            log.error("Error al obtener proyectos por estado {}: {}", estado, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener todos los proyectos
     * GET /api/submissions
     */
    @GetMapping
    @Operation(summary = "Obtener todos los proyectos",
               description = "Lista todos los proyectos del sistema")
    public ResponseEntity<List<ProyectoResponse>> obtenerTodos() {
        try {
            log.info("GET /api/submissions");

            List<ProyectoResponse> proyectos = obtenerProyectoQuery.obtenerTodos();

            return ResponseEntity.ok(proyectos);

        } catch (Exception e) {
            log.error("Error al obtener todos los proyectos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

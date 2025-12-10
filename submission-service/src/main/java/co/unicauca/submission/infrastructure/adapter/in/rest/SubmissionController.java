package co.unicauca.submission.infrastructure.adapter.in.rest;

import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.IObtenerProyectoQuery;
import co.unicauca.submission.domain.model.EstadoProyecto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Obtener proyecto por ID",
               description = "Consulta la información completa de un proyecto específico por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proyecto encontrado exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProyectoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
                     content = @Content)
    })
    public ResponseEntity<ProyectoResponse> obtenerPorId(
            @Parameter(description = "ID del proyecto", required = true, example = "123")
            @PathVariable Long id) {
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
               description = "RF5: El estudiante consulta el estado de todos sus proyectos de grado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de proyectos obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<List<ProyectoResponse>> obtenerPorEstudiante(
            @Parameter(description = "ID del estudiante", required = true, example = "1001")
            @PathVariable Long estudianteId) {
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
               description = "Consulta todos los proyectos donde el usuario es director o codirector")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de proyectos obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<List<ProyectoResponse>> obtenerPorDirector(
            @Parameter(description = "ID del director", required = true, example = "12")
            @PathVariable Long directorId) {
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
               description = "Filtra proyectos por su estado actual. Estados válidos: FORMATO_A_ENVIADO, EN_EVALUACION_COORDINADOR, FORMATO_A_APROBADO, CORRECCIONES_SOLICITADAS, ANTEPROYECTO_ENVIADO, etc.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de proyectos obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Estado inválido",
                     content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<List<ProyectoResponse>> obtenerPorEstado(
            @Parameter(description = "Estado del proyecto", required = true, example = "FORMATO_A_ENVIADO")
            @PathVariable EstadoProyecto estado) {
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
               description = "Lista todos los proyectos de grado registrados en el sistema. Usar con precaución en producción.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de proyectos obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
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


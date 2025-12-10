package co.unicauca.submission.infrastructure.adapter.in.rest;

import co.unicauca.submission.application.dto.request.SubirAnteproyectoRequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.ISubirAnteproyectoUseCase;
import co.unicauca.submission.application.port.in.IObtenerProyectoQuery;
import co.unicauca.submission.application.port.in.IListarAnteproyectosPendientesQuery;
import co.unicauca.submission.application.usecase.anteproyecto.AsignarEvaluadoresUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST Controller V2 para operaciones de Anteproyecto.
 * Nueva implementación con arquitectura hexagonal.
 *
 * Endpoints:
 * - GET    /api/submissions/anteproyecto/{proyectoId}
 * - POST   /api/submissions/anteproyecto/{proyectoId}
 * - POST   /api/submissions/anteproyecto/{proyectoId}/evaluadores
 */
@RestController
@RequestMapping("/api/submissions/anteproyecto")
@Tag(name = "Anteproyecto", description = "Operaciones de Anteproyecto (Arquitectura Hexagonal)")
public class AnteproyectoController {

    private static final Logger log = LoggerFactory.getLogger(AnteproyectoController.class);

    private final ISubirAnteproyectoUseCase subirUseCase;
    private final AsignarEvaluadoresUseCase asignarEvaluadoresUseCase;
    private final IObtenerProyectoQuery obtenerProyectoQuery;
    private final IListarAnteproyectosPendientesQuery listarPendientesQuery;

    public AnteproyectoController(
            ISubirAnteproyectoUseCase subirUseCase,
            AsignarEvaluadoresUseCase asignarEvaluadoresUseCase,
            IObtenerProyectoQuery obtenerProyectoQuery,
            IListarAnteproyectosPendientesQuery listarPendientesQuery
    ) {
        this.subirUseCase = subirUseCase;
        this.asignarEvaluadoresUseCase = asignarEvaluadoresUseCase;
        this.obtenerProyectoQuery = obtenerProyectoQuery;
        this.listarPendientesQuery = listarPendientesQuery;
    }

    /**
     * Obtener Anteproyecto por ID del proyecto
     * GET /api/submissions/anteproyecto/{proyectoId}
     * Retorna información del proyecto si tiene anteproyecto.
     */
    @GetMapping("/{proyectoId}")
    @Operation(summary = "Obtener Anteproyecto por ID",
               description = "Obtiene información detallada de un Anteproyecto específico. Solo retorna proyectos que tienen anteproyecto subido.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Anteproyecto encontrado exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProyectoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Proyecto no encontrado o no tiene anteproyecto asociado",
                     content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<ProyectoResponse> obtenerAnteproyecto(
            @Parameter(description = "ID del proyecto", required = true, example = "123")
            @PathVariable Long proyectoId) {
        try {
            log.info("GET /api/submissions/anteproyecto/{}", proyectoId);

            ProyectoResponse response = obtenerProyectoQuery.obtenerPorId(proyectoId);

            // Verificar que tenga anteproyecto
            if (response.getRutaPdfAnteproyecto() == null || response.getRutaPdfAnteproyecto().trim().isEmpty()) {
                log.warn("Proyecto {} no tiene anteproyecto asociado, estado: {}", proyectoId, response.getEstado());
                return ResponseEntity.notFound().build();
            }

            // Verificar que sea un proyecto en fase de anteproyecto
            if (!response.getEstado().startsWith("ANTEPROYECTO")) {
                log.warn("Proyecto {} no está en fase de anteproyecto, estado: {}", proyectoId, response.getEstado());
                return ResponseEntity.notFound().build();
            }

            log.info("Anteproyecto {} obtenido correctamente, estado: {}", proyectoId, response.getEstado());
            return ResponseEntity.ok(response);

        } catch (co.unicauca.submission.domain.exception.ProyectoNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al obtener Anteproyecto {}: {}", proyectoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RF8: Listar Anteproyectos Pendientes de Asignación
     * GET /api/submissions/anteproyecto/pendientes
     * Retorna anteproyectos en estado ANTEPROYECTO_ENVIADO (pendientes de asignar evaluadores).
     */
    @GetMapping("/pendientes")
    @Operation(summary = "Listar Anteproyectos Pendientes",
               description = "RF8: El jefe de departamento lista anteproyectos pendientes para asignar evaluadores. Retorna proyectos en estado ANTEPROYECTO_ENVIADO.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de anteproyectos pendientes obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<org.springframework.data.domain.Page<ProyectoResponse>> listarPendientes(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            log.info("GET /api/submissions/anteproyecto/pendientes - page: {}, size: {}", page, size);

            org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);

            org.springframework.data.domain.Page<ProyectoResponse> pendientes =
                listarPendientesQuery.listarPendientes(pageable);

            log.info("Se encontraron {} anteproyectos pendientes de {} totales",
                    pendientes.getNumberOfElements(), pendientes.getTotalElements());

            return ResponseEntity.ok(pendientes);

        } catch (Exception e) {
            log.error("Error al listar anteproyectos pendientes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RF6: Subir Anteproyecto
     * POST /api/submissions/anteproyecto/{proyectoId}
     */
    @PostMapping("/{proyectoId}")
    @Operation(summary = "Subir Anteproyecto",
               description = "RF6: El director sube el documento de anteproyecto para un proyecto cuyo Formato A fue aprobado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Anteproyecto subido exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProyectoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Error al procesar el archivo PDF",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
                     content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<ProyectoResponse> subir(
            @Parameter(description = "ID del proyecto", required = true, example = "123")
            @PathVariable Long proyectoId,
            @Parameter(description = "ID del usuario (docente director)", required = true, example = "12")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Archivo PDF del anteproyecto", required = true)
            @RequestPart("pdf") MultipartFile pdf
    ) {
        try {
            log.info("POST /api/submissions/anteproyecto/{} - Usuario: {}", proyectoId, userId);

            SubirAnteproyectoRequest request = new SubirAnteproyectoRequest();
            request.setPdfStream(pdf.getInputStream());
            request.setPdfNombreArchivo(pdf.getOriginalFilename());

            // Ejecutar use case
            ProyectoResponse response = subirUseCase.subir(proyectoId, request, userId);

            log.info("Anteproyecto subido exitosamente - ProyectoID: {}", proyectoId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            log.error("Error al procesar archivo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error al subir anteproyecto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RF8: Asignar Evaluadores
     * POST /api/submissions/anteproyecto/{proyectoId}/evaluadores
     */
    @PostMapping("/{proyectoId}/evaluadores")
    @Operation(summary = "Asignar Evaluadores",
               description = "RF8: El jefe de departamento asigna dos evaluadores a un anteproyecto para su revisión.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Evaluadores asignados exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProyectoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
                     content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<ProyectoResponse> asignarEvaluadores(
            @Parameter(description = "ID del proyecto", required = true, example = "123")
            @PathVariable Long proyectoId,
            @Parameter(description = "ID del jefe de departamento", required = true, example = "3")
            @RequestHeader("X-User-Id") Long jefeDepartamentoId,
            @Parameter(description = "ID del primer evaluador", required = true, example = "20")
            @RequestParam Long evaluador1Id,
            @Parameter(description = "ID del segundo evaluador", required = true, example = "21")
            @RequestParam Long evaluador2Id
    ) {
        try {
            log.info("POST /api/submissions/anteproyecto/{}/evaluadores - Jefe: {}, Eval1: {}, Eval2: {}",
                    proyectoId, jefeDepartamentoId, evaluador1Id, evaluador2Id);

            // Ejecutar use case
            ProyectoResponse response = asignarEvaluadoresUseCase.asignar(
                proyectoId, evaluador1Id, evaluador2Id, jefeDepartamentoId
            );

            log.info("Evaluadores asignados exitosamente - ProyectoID: {}", proyectoId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al asignar evaluadores: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

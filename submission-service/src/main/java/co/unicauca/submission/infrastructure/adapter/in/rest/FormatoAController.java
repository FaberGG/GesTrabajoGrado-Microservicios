package co.unicauca.submission.infrastructure.adapter.in.rest;

import co.unicauca.submission.application.dto.request.CrearFormatoARequest;
import co.unicauca.submission.application.dto.request.EvaluarFormatoARequest;
import co.unicauca.submission.application.dto.request.ReenviarFormatoARequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.ICrearFormatoAUseCase;
import co.unicauca.submission.application.port.in.IEvaluarFormatoAUseCase;
import co.unicauca.submission.application.port.in.IReenviarFormatoAUseCase;
import co.unicauca.submission.application.port.in.IListarFormatoAPendientesQuery;
import co.unicauca.submission.application.port.in.IObtenerProyectoQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * REST Controller para operaciones de Formato A.
 * Implementación con arquitectura hexagonal.
 *
 * Endpoints:
 * - GET    /api/submissions/formatoA/{id}
 * - POST   /api/submissions/formatoA
 * - POST   /api/submissions/formatoA/{id}/reenviar
 * - PATCH  /api/submissions/formatoA/{id}/evaluar
 * - GET    /api/submissions/formatoA/pendientes
 */
@RestController
@RequestMapping("/api/submissions/formatoA")
@Tag(name = "Formato A", description = "Operaciones de Formato A (Arquitectura Hexagonal)")
public class FormatoAController {

    private static final Logger log = LoggerFactory.getLogger(FormatoAController.class);

    private final ICrearFormatoAUseCase crearUseCase;
    private final IReenviarFormatoAUseCase reenviarUseCase;
    private final IEvaluarFormatoAUseCase evaluarUseCase;
    private final IListarFormatoAPendientesQuery listarPendientesQuery;
    private final IObtenerProyectoQuery obtenerProyectoQuery;
    private final ObjectMapper objectMapper;

    public FormatoAController(
            ICrearFormatoAUseCase crearUseCase,
            IReenviarFormatoAUseCase reenviarUseCase,
            IEvaluarFormatoAUseCase evaluarUseCase,
            @Qualifier("listarFormatoAPendientesQueryEnriched")
            IListarFormatoAPendientesQuery listarPendientesQuery,
            IObtenerProyectoQuery obtenerProyectoQuery,
            ObjectMapper objectMapper
    ) {
        this.crearUseCase = crearUseCase;
        this.reenviarUseCase = reenviarUseCase;
        this.evaluarUseCase = evaluarUseCase;
        this.listarPendientesQuery = listarPendientesQuery;
        this.obtenerProyectoQuery = obtenerProyectoQuery;
        this.objectMapper = objectMapper;
    }

    /**
     * Obtener Formato A por ID
     * GET /api/submissions/formatoA/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Formato A por ID",
               description = "Obtiene información detallada de un Formato A específico por su ID. Solo retorna proyectos en fase de Formato A.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Formato A encontrado exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProyectoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Formato A no encontrado o el proyecto no está en fase de Formato A",
                     content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<ProyectoResponse> obtenerFormatoA(
            @Parameter(description = "ID del proyecto/Formato A", required = true, example = "123")
            @PathVariable Long id) {
        try {
            log.info("GET /api/submissions/formatoA/{}", id);

            ProyectoResponse response = obtenerProyectoQuery.obtenerPorId(id);

            // Verificar que sea un proyecto de tipo Formato A (no Anteproyecto)
            if (!response.getEstado().startsWith("FORMATO_A") &&
                !"EN_EVALUACION_COORDINADOR".equals(response.getEstado()) &&
                !"CORRECCIONES_SOLICITADAS".equals(response.getEstado())) {
                log.warn("Proyecto {} no es un Formato A, estado: {}", id, response.getEstado());
                return ResponseEntity.notFound().build();
            }

            log.info("Formato A {} obtenido correctamente, estado: {}", id, response.getEstado());
            return ResponseEntity.ok(response);

        } catch (co.unicauca.submission.domain.exception.ProyectoNotFoundException e) {
            log.error("Formato A {} no encontrado", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al obtener Formato A {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RF2: Crear Formato A
     * POST /api/submissions/formatoA
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear Formato A",
               description = "RF2: El docente director crea un nuevo proyecto con Formato A. Requiere enviar los datos del proyecto en JSON y el archivo PDF del formato.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Formato A creado exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProyectoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos, archivo vacío o excede el tamaño máximo (10MB)",
                     content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<ProyectoResponse> crear(
            @Parameter(description = "ID del usuario (docente director)", required = true, example = "12")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Datos del Formato A en formato JSON", required = true)
            @RequestPart(value = "data") String dataJson,
            @Parameter(description = "Archivo PDF del Formato A (máximo 10MB)", required = true)
            @RequestPart("pdf") MultipartFile pdf,
            @Parameter(description = "Carta de aceptación (requerida para modalidad PRACTICA_PROFESIONAL)")
            @RequestPart(value = "carta", required = false) MultipartFile carta
    ) {
        try {
            log.info("POST /api/submissions/formatoA - Usuario: {}", userId);
            log.debug("Data JSON recibido: {}", dataJson);
            log.debug("PDF recibido - Nombre: {}, Tamaño: {}, ContentType: {}",
                     pdf.getOriginalFilename(), pdf.getSize(), pdf.getContentType());

            // Parsear el JSON a objeto CrearFormatoARequest
            CrearFormatoARequest request = objectMapper.readValue(dataJson, CrearFormatoARequest.class);

            log.info("Título del proyecto: {}", request.getTitulo());

            // Validar que el archivo no esté vacío
            if (pdf.isEmpty()) {
                log.error("El archivo PDF está vacío");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Validar tamaño del archivo (máximo 10MB)
            if (pdf.getSize() > 10 * 1024 * 1024) {
                log.error("El archivo PDF excede el tamaño máximo permitido (10MB)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Setear streams de archivos en el request
            request.setPdfStream(pdf.getInputStream());
            request.setPdfNombreArchivo(pdf.getOriginalFilename());

            if (carta != null && !carta.isEmpty()) {
                log.debug("Carta recibida - Nombre: {}, Tamaño: {}",
                         carta.getOriginalFilename(), carta.getSize());
                request.setCartaStream(carta.getInputStream());
                request.setCartaNombreArchivo(carta.getOriginalFilename());
            }

            ProyectoResponse response = crearUseCase.crear(request, userId);

            log.info("Formato A creado exitosamente - ProyectoID: {}", response.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            log.error("Error al procesar archivos o JSON: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error al crear Formato A: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RF4: Reenviar Formato A
     * POST /api/submissions/formatoA/{id}/reenviar
     */
    @PostMapping("/{id}/reenviar")
    @Operation(summary = "Reenviar Formato A",
               description = "RF4: El docente director reenvía una nueva versión del Formato A tras realizar correcciones solicitadas. Máximo 3 intentos permitidos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Formato A reenviado exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProyectoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Error al procesar archivos o se excedió el número máximo de intentos",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
                     content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<ProyectoResponse> reenviar(
            @Parameter(description = "ID del proyecto", required = true, example = "123")
            @PathVariable Long id,
            @Parameter(description = "ID del usuario (docente director)", required = true, example = "12")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Nuevo archivo PDF del Formato A corregido")
            @RequestPart(value = "pdf", required = false) MultipartFile pdf,
            @Parameter(description = "Nueva carta de aceptación (si aplica)")
            @RequestPart(value = "carta", required = false) MultipartFile carta
    ) {
        try {
            log.info("POST /api/submissions/formatoA/{}/reenviar - Usuario: {}", id, userId);

            ReenviarFormatoARequest request = new ReenviarFormatoARequest();

            if (pdf != null && !pdf.isEmpty()) {
                request.setPdfStream(pdf.getInputStream());
                request.setPdfNombreArchivo(pdf.getOriginalFilename());
            }

            if (carta != null && !carta.isEmpty()) {
                request.setCartaStream(carta.getInputStream());
                request.setCartaNombreArchivo(carta.getOriginalFilename());
            }

            // Ejecutar use case
            ProyectoResponse response = reenviarUseCase.reenviar(id, request, userId);

            log.info("Formato A reenviado exitosamente - ProyectoID: {}, Intento: {}",
                    id, response.getNumeroIntento());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error al procesar archivos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error al reenviar Formato A: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RF3: Evaluar Formato A
     * PATCH /api/submissions/formatoA/{id}/evaluar
     */
    @PatchMapping("/{id}/evaluar")
    @Operation(summary = "Evaluar Formato A",
               description = "RF3: El coordinador evalúa el Formato A. Puede aprobarlo (pasa a fase de anteproyecto) o solicitar correcciones.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Formato A evaluado exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProyectoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de evaluación inválidos",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
                     content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<ProyectoResponse> evaluar(
            @Parameter(description = "ID del proyecto", required = true, example = "123")
            @PathVariable Long id,
            @Parameter(description = "ID del coordinador evaluador", required = true, example = "5")
            @RequestHeader("X-User-Id") Long evaluadorId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos de la evaluación",
                required = true,
                content = @Content(schema = @Schema(implementation = EvaluarFormatoARequest.class))
            )
            @RequestBody @Valid EvaluarFormatoARequest request
    ) {
        try {
            log.info("PATCH /api/submissions/formatoA/{}/evaluar - Evaluador: {}, Aprobado: {}",
                    id, evaluadorId, request.isAprobado());

            // Ejecutar use case
            ProyectoResponse response = evaluarUseCase.evaluar(id, request, evaluadorId);

            log.info("Formato A evaluado exitosamente - ProyectoID: {}, Estado: {}",
                    id, response.getEstado());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al evaluar Formato A: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RF3: Listar Formatos A Pendientes
     * GET /api/submissions/formatoA/pendientes
     */
    @GetMapping("/pendientes")
    @Operation(summary = "Listar Formatos A pendientes",
               description = "RF3: El coordinador lista los Formatos A pendientes de evaluación. Retorna resultados paginados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de Formatos A pendientes obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                     content = @Content)
    })
    public ResponseEntity<Page<ProyectoResponse>> listarPendientes(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            log.info("GET /api/submissions/formatoA/pendientes - page: {}, size: {}", page, size);

            // Crear Pageable
            Pageable pageable = PageRequest.of(page, size);

            // Ejecutar query
            Page<ProyectoResponse> pendientes = listarPendientesQuery.listarPendientes(pageable);

            log.info("Se encontraron {} Formatos A pendientes", pendientes.getTotalElements());

            return ResponseEntity.ok(pendientes);

        } catch (Exception e) {
            log.error("Error al listar Formatos A pendientes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

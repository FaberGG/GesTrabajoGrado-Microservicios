package co.unicauca.submission.infrastructure.adapter.in.rest;

import co.unicauca.submission.application.dto.request.CrearFormatoARequest;
import co.unicauca.submission.application.dto.request.EvaluarFormatoARequest;
import co.unicauca.submission.application.dto.request.ReenviarFormatoARequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.ICrearFormatoAUseCase;
import co.unicauca.submission.application.port.in.IEvaluarFormatoAUseCase;
import co.unicauca.submission.application.port.in.IReenviarFormatoAUseCase;
import co.unicauca.submission.application.port.in.IListarFormatoAPendientesQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.unicauca.submission.application.port.in.IObtenerProyectoQuery;

import java.io.IOException;

/**
 * REST Controller V2 para operaciones de Formato A.
 * Nueva implementación con arquitectura hexagonal.
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
    private final co.unicauca.submission.application.port.in.IObtenerProyectoQuery obtenerProyectoQuery;
    private final ObjectMapper objectMapper;

    public FormatoAController(
            ICrearFormatoAUseCase crearUseCase,
            IReenviarFormatoAUseCase reenviarUseCase,
            IEvaluarFormatoAUseCase evaluarUseCase,
            @org.springframework.beans.factory.annotation.Qualifier("listarFormatoAPendientesQueryEnriched")
            IListarFormatoAPendientesQuery listarPendientesQuery,
            co.unicauca.submission.application.port.in.IObtenerProyectoQuery obtenerProyectoQuery,
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
     *
     * Endpoint de compatibilidad para review-service.
     * Retorna información del proyecto si es un Formato A.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Formato A por ID", description = "Obtiene información de un Formato A específico")
    public ResponseEntity<ProyectoResponse> obtenerFormatoA(@PathVariable Long id) {
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
     * POST /api/v2/submissions/formatoA
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear Formato A", description = "RF2: El docente crea un nuevo proyecto con Formato A")
    public ResponseEntity<ProyectoResponse> crear(
            @RequestHeader("X-User-Id") Long userId,
            @RequestPart(value = "data") String dataJson,
            @RequestPart("pdf") MultipartFile pdf,
            @RequestPart(value = "carta", required = false) MultipartFile carta
    ) throws IOException {
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
            throw new IllegalArgumentException("El archivo PDF no puede estar vacío");
        }

        // Validar tamaño del archivo (máximo 10MB)
        if (pdf.getSize() > 10 * 1024 * 1024) {
            log.error("El archivo PDF excede el tamaño máximo permitido (10MB)");
            throw new IllegalArgumentException("El archivo PDF excede el tamaño máximo permitido (10MB)");
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

        // Ejecutar use case - las excepciones de dominio se propagan al GlobalExceptionHandler
        ProyectoResponse response = crearUseCase.crear(request, userId);

        log.info("Formato A creado exitosamente - ProyectoID: {}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * RF4: Reenviar Formato A
     * POST /api/v2/submissions/formatoA/{id}/reenviar
     */
    @PostMapping("/{id}/reenviar")
    @Operation(summary = "Reenviar Formato A", description = "RF4: El docente reenvía nueva versión tras correcciones")
    public ResponseEntity<ProyectoResponse> reenviar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestPart(value = "pdf", required = false) MultipartFile pdf,
            @RequestPart(value = "carta", required = false) MultipartFile carta
    ) throws IOException {
        log.info("POST /api/v2/submissions/formatoA/{}/reenviar - Usuario: {}", id, userId);

        ReenviarFormatoARequest request = new ReenviarFormatoARequest();

        if (pdf != null) {
            request.setPdfStream(pdf.getInputStream());
            request.setPdfNombreArchivo(pdf.getOriginalFilename());
        }

        if (carta != null) {
            request.setCartaStream(carta.getInputStream());
            request.setCartaNombreArchivo(carta.getOriginalFilename());
        }

        // Ejecutar use case - las excepciones de dominio se propagan al GlobalExceptionHandler
        ProyectoResponse response = reenviarUseCase.reenviar(id, request, userId);

        log.info("Formato A reenviado exitosamente - ProyectoID: {}, Intento: {}",
                id, response.getNumeroIntento());

        return ResponseEntity.ok(response);
    }

    /**
     * RF3: Evaluar Formato A
     * PATCH /api/v2/submissions/formatoA/{id}/evaluar
     */
    @PatchMapping("/{id}/evaluar")
    @Operation(summary = "Evaluar Formato A", description = "RF3: El coordinador evalúa el Formato A")
    public ResponseEntity<ProyectoResponse> evaluar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long evaluadorId,
            @RequestBody @Valid EvaluarFormatoARequest request
    ) {
        log.info("PATCH /api/v2/submissions/formatoA/{}/evaluar - Evaluador: {}, Aprobado: {}",
                id, evaluadorId, request.isAprobado());

        // Ejecutar use case - las excepciones de dominio se propagan al GlobalExceptionHandler
        ProyectoResponse response = evaluarUseCase.evaluar(id, request, evaluadorId);

        log.info("Formato A evaluado exitosamente - ProyectoID: {}, Estado: {}",
                id, response.getEstado());

        return ResponseEntity.ok(response);
    }

    /**
     * RF3: Listar Formatos A Pendientes
     * GET /api/submissions/formatoA/pendientes
     */
    @GetMapping("/pendientes")
    @Operation(summary = "Listar Formatos A pendientes", description = "RF3: El coordinador lista los Formatos A pendientes de evaluación")
    public ResponseEntity<Page<ProyectoResponse>> listarPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET /api/submissions/formatoA/pendientes - page: {}, size: {}", page, size);

        // Crear Pageable
        Pageable pageable = PageRequest.of(page, size);

        // Ejecutar query - las excepciones se propagan al GlobalExceptionHandler
        Page<ProyectoResponse> pendientes = listarPendientesQuery.listarPendientes(pageable);

        log.info("Se encontraron {} Formatos A pendientes", pendientes.getTotalElements());

        return ResponseEntity.ok(pendientes);
    }
}



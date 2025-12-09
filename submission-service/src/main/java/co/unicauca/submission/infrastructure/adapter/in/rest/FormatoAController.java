package co.unicauca.submission.infrastructure.adapter.in.rest;

import co.unicauca.submission.application.dto.request.CrearFormatoARequest;
import co.unicauca.submission.application.dto.request.EvaluarFormatoARequest;
import co.unicauca.submission.application.dto.request.ReenviarFormatoARequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.ICrearFormatoAUseCase;
import co.unicauca.submission.application.port.in.IEvaluarFormatoAUseCase;
import co.unicauca.submission.application.port.in.IReenviarFormatoAUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * REST Controller V2 para operaciones de Formato A.
 * Nueva implementación con arquitectura hexagonal.
 *
 * Endpoints:
 * - POST   /api/v2/submissions/formatoA
 * - POST   /api/v2/submissions/formatoA/{id}/reenviar
 * - PATCH  /api/v2/submissions/formatoA/{id}/evaluar
 */
@RestController
@RequestMapping("/api/submissions/formatoA")
@Tag(name = "Formato A", description = "Operaciones de Formato A (Arquitectura Hexagonal)")
public class FormatoAController {

    private static final Logger log = LoggerFactory.getLogger(FormatoAController.class);

    private final ICrearFormatoAUseCase crearUseCase;
    private final IReenviarFormatoAUseCase reenviarUseCase;
    private final IEvaluarFormatoAUseCase evaluarUseCase;
    private final ObjectMapper objectMapper;

    public FormatoAController(
            ICrearFormatoAUseCase crearUseCase,
            IReenviarFormatoAUseCase reenviarUseCase,
            IEvaluarFormatoAUseCase evaluarUseCase,
            ObjectMapper objectMapper
    ) {
        this.crearUseCase = crearUseCase;
        this.reenviarUseCase = reenviarUseCase;
        this.evaluarUseCase = evaluarUseCase;
        this.objectMapper = objectMapper;
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

            // Ejecutar use case
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
     * POST /api/v2/submissions/formatoA/{id}/reenviar
     */
    @PostMapping("/{id}/reenviar")
    @Operation(summary = "Reenviar Formato A", description = "RF4: El docente reenvía nueva versión tras correcciones")
    public ResponseEntity<ProyectoResponse> reenviar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestPart(value = "pdf", required = false) MultipartFile pdf,
            @RequestPart(value = "carta", required = false) MultipartFile carta
    ) {
        try {
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
     * PATCH /api/v2/submissions/formatoA/{id}/evaluar
     */
    @PatchMapping("/{id}/evaluar")
    @Operation(summary = "Evaluar Formato A", description = "RF3: El coordinador evalúa el Formato A")
    public ResponseEntity<ProyectoResponse> evaluar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long evaluadorId,
            @RequestBody @Valid EvaluarFormatoARequest request
    ) {
        try {
            log.info("PATCH /api/v2/submissions/formatoA/{}/evaluar - Evaluador: {}, Aprobado: {}",
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
}



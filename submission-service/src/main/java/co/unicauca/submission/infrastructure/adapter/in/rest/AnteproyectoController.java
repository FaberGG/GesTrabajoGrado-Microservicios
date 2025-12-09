package co.unicauca.submission.infrastructure.adapter.in.rest;

import co.unicauca.submission.application.dto.request.SubirAnteproyectoRequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.application.port.in.ISubirAnteproyectoUseCase;
import co.unicauca.submission.application.usecase.anteproyecto.AsignarEvaluadoresUseCase;
import io.swagger.v3.oas.annotations.Operation;
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
 * - POST   /api/v2/submissions/anteproyecto/{proyectoId}
 * - POST   /api/v2/submissions/anteproyecto/{proyectoId}/evaluadores
 */
@RestController
@RequestMapping("/api/submissions/anteproyecto")
@Tag(name = "Anteproyecto", description = "Operaciones de Anteproyecto (Arquitectura Hexagonal)")
public class AnteproyectoController {

    private static final Logger log = LoggerFactory.getLogger(AnteproyectoController.class);

    private final ISubirAnteproyectoUseCase subirUseCase;
    private final AsignarEvaluadoresUseCase asignarEvaluadoresUseCase;

    public AnteproyectoController(
            ISubirAnteproyectoUseCase subirUseCase,
            AsignarEvaluadoresUseCase asignarEvaluadoresUseCase
    ) {
        this.subirUseCase = subirUseCase;
        this.asignarEvaluadoresUseCase = asignarEvaluadoresUseCase;
    }

    /**
     * RF6: Subir Anteproyecto
     * POST /api/v2/submissions/anteproyecto/{proyectoId}
     */
    @PostMapping("/{proyectoId}")
    @Operation(summary = "Subir Anteproyecto", description = "RF6: El director sube el anteproyecto")
    public ResponseEntity<ProyectoResponse> subir(
            @PathVariable Long proyectoId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestPart("pdf") MultipartFile pdf
    ) {
        try {
            log.info("POST /api/v2/submissions/anteproyecto/{} - Usuario: {}", proyectoId, userId);

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
     * POST /api/v2/submissions/anteproyecto/{proyectoId}/evaluadores
     */
    @PostMapping("/{proyectoId}/evaluadores")
    @Operation(summary = "Asignar Evaluadores", description = "RF8: El jefe de departamento asigna evaluadores")
    public ResponseEntity<ProyectoResponse> asignarEvaluadores(
            @PathVariable Long proyectoId,
            @RequestHeader("X-User-Id") Long jefeDepartamentoId,
            @RequestParam Long evaluador1Id,
            @RequestParam Long evaluador2Id
    ) {
        try {
            log.info("POST /api/v2/submissions/anteproyecto/{}/evaluadores - Jefe: {}, Eval1: {}, Eval2: {}",
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


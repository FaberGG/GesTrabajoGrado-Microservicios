package co.unicauca.review.controller;

import co.unicauca.review.dto.request.AsignacionRequestDTO;
import co.unicauca.review.dto.request.EvaluationRequestDTO;
import co.unicauca.review.dto.response.ApiResponse;
import co.unicauca.review.dto.response.AsignacionDTO;
import co.unicauca.review.dto.response.EvaluationResultDTO;
import co.unicauca.review.enums.AsignacionEstado;
import co.unicauca.review.enums.EvaluatorRole;
import co.unicauca.review.exception.InvalidStateException;
import co.unicauca.review.exception.UnauthorizedException;
import co.unicauca.review.service.AsignacionService;
import co.unicauca.review.service.impl.AnteproyectoEvaluationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review/anteproyectos")
public class AnteproyectoReviewController {

    private static final Logger log = LoggerFactory.getLogger(AnteproyectoReviewController.class);

    private final AnteproyectoEvaluationService evaluationService;
    private final AsignacionService asignacionService;

    public AnteproyectoReviewController(
            AnteproyectoEvaluationService evaluationService,
            AsignacionService asignacionService) {
        this.evaluationService = evaluationService;
        this.asignacionService = asignacionService;
    }

    @GetMapping("/asignaciones")
    public ResponseEntity<ApiResponse<Page<AsignacionDTO>>> getAsignaciones(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam(required = false) AsignacionEstado estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Listando asignaciones. UserId: {}, Role: {}", userId, userRole);

        try {
            Page<AsignacionDTO> asignaciones;

            if ("JEFE_DEPARTAMENTO".equals(userRole)) {
                // Jefe ve todas
                asignaciones = asignacionService.findAll(estado, page, size);
            } else if ("EVALUADOR".equals(userRole)) {
                // Evaluador solo ve las suyas
                asignaciones = asignacionService.findByEvaluador(userId, estado, page, size);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Rol no autorizado"));
            }

            return ResponseEntity.ok(ApiResponse.success(asignaciones));

        } catch (Exception e) {
            log.error("Error obteniendo asignaciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al obtener asignaciones"));
        }
    }

    @PostMapping("/asignar")
    public ResponseEntity<ApiResponse<AsignacionDTO>> asignar(
            @Valid @RequestBody AsignacionRequestDTO request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        log.info("Asignando evaluadores: anteproyecto={}, eval1={}, eval2={}",
                request.anteproyectoId(), request.evaluador1Id(), request.evaluador2Id());

        if (!"JEFE_DEPARTAMENTO".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Solo jefes de departamento pueden asignar evaluadores"));
        }

        try {
            AsignacionDTO asignacion = asignacionService.asignar(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(asignacion, "Evaluadores asignados exitosamente"));

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error asignando evaluadores: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al asignar evaluadores"));
        }
    }

    @PostMapping("/{id}/evaluar")
    public ResponseEntity<ApiResponse<EvaluationResultDTO>> evaluar(
            @PathVariable Long id,
            @Valid @RequestBody EvaluationRequestDTO request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        log.info("Evaluando anteproyecto: id={}, userId={}", id, userId);

        if (!"EVALUADOR".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Solo evaluadores pueden evaluar anteproyectos"));
        }

        try {
            EvaluationRequestDTO fullRequest = new EvaluationRequestDTO(
                id,
                request.decision(),
                request.observaciones(),
                userId,
                EvaluatorRole.EVALUADOR
            );

            EvaluationResultDTO result = evaluationService.evaluate(fullRequest);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Evaluación registrada exitosamente"));

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.getMessage()));

        } catch (InvalidStateException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error evaluando anteproyecto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al evaluar anteproyecto"));
        }
    }
}


package co.unicauca.review.controller;

import co.unicauca.review.client.SubmissionServiceClient;
import co.unicauca.review.dto.request.EvaluationRequestDTO;
import co.unicauca.review.dto.response.ApiResponse;
import co.unicauca.review.dto.response.EvaluationResultDTO;
import co.unicauca.review.dto.response.FormatoAReviewDTO;
import co.unicauca.review.enums.EvaluatorRole;
import co.unicauca.review.exception.InvalidStateException;
import co.unicauca.review.exception.UnauthorizedException;
import co.unicauca.review.service.impl.FormatoAEvaluationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review/formatoA")
public class FormatoAReviewController {

    private static final Logger log = LoggerFactory.getLogger(FormatoAReviewController.class);

    private final FormatoAEvaluationService evaluationService;
    private final SubmissionServiceClient submissionClient;

    public FormatoAReviewController(
            FormatoAEvaluationService evaluationService,
            SubmissionServiceClient submissionClient) {
        this.evaluationService = evaluationService;
        this.submissionClient = submissionClient;
    }

    @GetMapping("/pendientes")
    public ResponseEntity<ApiResponse<Page<FormatoAReviewDTO>>> getPendientes(
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Listando Formato A pendientes. UserRole: {}", userRole);

        if (!"COORDINADOR".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Rol no autorizado"));
        }

        try {
            // Obtener de Submission Service
            Page<FormatoAReviewDTO> formatosA =
                submissionClient.getFormatosAPendientes(page, size);

            return ResponseEntity.ok(ApiResponse.success(formatosA));

        } catch (Exception e) {
            log.error("Error obteniendo Formato A pendientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al obtener Formato A pendientes"));
        }
    }

    @PostMapping("/{id}/evaluar")
    public ResponseEntity<ApiResponse<EvaluationResultDTO>> evaluar(
            @PathVariable Long id,
            @Valid @RequestBody EvaluationRequestDTO request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        log.info("Evaluando Formato A: id={}, userId={}, role={}", id, userId, userRole);

        if (!"COORDINADOR".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Solo coordinadores pueden evaluar Formato A"));
        }

        try {
            // Crear request completo con datos del header
            EvaluationRequestDTO fullRequest = new EvaluationRequestDTO(
                id,
                request.decision(),
                request.observaciones(),
                userId,
                EvaluatorRole.COORDINADOR
            );

            EvaluationResultDTO result = evaluationService.evaluate(fullRequest);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Formato A evaluado exitosamente"));

        } catch (UnauthorizedException e) {
            log.warn("Acceso no autorizado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.getMessage()));

        } catch (InvalidStateException e) {
            log.warn("Estado inválido: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error evaluando Formato A: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al evaluar Formato A"));
        }
    }
}


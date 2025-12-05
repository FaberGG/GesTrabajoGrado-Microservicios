package co.unicauca.review.controller;

import co.unicauca.review.client.SubmissionServiceClient;
import co.unicauca.review.dto.request.EvaluateFormatoARequestDTO;
import co.unicauca.review.dto.request.EvaluationRequestDTO;
import co.unicauca.review.dto.response.ApiResponse;
import co.unicauca.review.dto.response.EvaluationResultDTO;
import co.unicauca.review.dto.response.FormatoAReviewDTO;
import co.unicauca.review.dto.response.PageResponse;
import co.unicauca.review.enums.EvaluatorRole;
import co.unicauca.review.exception.InvalidStateException;
import co.unicauca.review.exception.UnauthorizedException;
import co.unicauca.review.service.impl.FormatoAEvaluationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    public ResponseEntity<ApiResponse<PageResponse<FormatoAReviewDTO>>> getPendientes(
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
            Page<FormatoAReviewDTO> formatosAPage =
                submissionClient.getFormatosAPendientes(page, size);

            // Convertir a PageResponse con estructura personalizada
            PageResponse<FormatoAReviewDTO> pageResponse = PageResponse.from(formatosAPage);

            return ResponseEntity.ok(ApiResponse.success(pageResponse));

        } catch (Exception e) {
            log.error("Error obteniendo Formato A pendientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al obtener Formato A pendientes"));
        }
    }

    @PostMapping(value = "/{id}/evaluar", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<EvaluationResultDTO>> evaluar(
            @PathVariable Long id,
            @Valid @RequestBody EvaluateFormatoARequestDTO request,
            @RequestHeader(value = "X-User-Id", required = true) Long userId,
            @RequestHeader(value = "X-User-Role", required = true) String userRole) {

        log.info("Evaluando Formato A: id={}, userId={}, role={}, decision={}",
                id, userId, userRole, request.decision());

        if (!"COORDINADOR".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Solo coordinadores pueden evaluar Formato A"));
        }

        try {
            // Crear request completo con datos del path, headers y body
            EvaluationRequestDTO fullRequest = new EvaluationRequestDTO(
                id,                         // documentId viene del path
                request.decision(),          // decision viene del body
                request.observaciones(),     // observaciones viene del body
                userId,                      // evaluatorId viene del header
                EvaluatorRole.COORDINADOR    // evaluatorRole según validación
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

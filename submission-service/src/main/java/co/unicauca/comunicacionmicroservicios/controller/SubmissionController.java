package co.unicauca.comunicacionmicroservicios.controller;

import co.unicauca.comunicacionmicroservicios.dto.CreateSubmissionDTO;
import co.unicauca.comunicacionmicroservicios.dto.EvaluacionDTO;
import co.unicauca.comunicacionmicroservicios.dto.SubmissionResponseDTO;
import co.unicauca.comunicacionmicroservicios.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestionar las submissions con patrón State
 *
 * Endpoints disponibles:
 * - POST   /api/submissions                          - Crear nuevo proyecto
 * - GET    /api/submissions/{id}                     - Obtener proyecto por ID
 * - GET    /api/submissions                          - Listar todos los proyectos
 * - GET    /api/submissions/estado/{estado}          - Listar por estado
 * - GET    /api/submissions/docente/{docenteId}      - Listar por docente
 * - GET    /api/submissions/en-proceso               - Listar proyectos en proceso
 * - PUT    /api/submissions/{id}/presentar           - Presentar al coordinador
 * - PUT    /api/submissions/{id}/enviar-evaluacion   - Enviar al comité
 * - PUT    /api/submissions/{id}/evaluar             - Evaluar (aprobar/rechazar)
 * - PUT    /api/submissions/{id}/subir-nueva-version - Subir nueva versión
 */
@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
@Tag(name = "Health Check", description = "Endpoints de monitoreo y salud del servicio")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    /**
     * POST /api/submissions
     * Crear un nuevo proyecto submission (estado inicial: FORMATO_A_DILIGENCIADO)
     */
    @PostMapping
    public ResponseEntity<SubmissionResponseDTO> crearSubmission(
            @Valid @RequestBody CreateSubmissionDTO dto) {
        try {
            SubmissionResponseDTO response = submissionService.crearSubmission(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/submissions/{id}
     * Obtener un proyecto por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponseDTO> obtenerSubmission(@PathVariable Long id) {
        try {
            SubmissionResponseDTO response = submissionService.obtenerSubmission(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/submissions
     * Listar todos los proyectos
     */
    @GetMapping
    public ResponseEntity<List<SubmissionResponseDTO>> listarTodos() {
        List<SubmissionResponseDTO> proyectos = submissionService.listarTodos();
        return ResponseEntity.ok(proyectos);
    }

    /**
     * GET /api/submissions/estado/{estado}
     * Listar proyectos por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SubmissionResponseDTO>> listarPorEstado(@PathVariable String estado) {
        List<SubmissionResponseDTO> proyectos = submissionService.listarPorEstado(estado);
        return ResponseEntity.ok(proyectos);
    }

    /**
     * GET /api/submissions/docente/{docenteId}
     * Listar proyectos de un docente específico
     */
    @GetMapping("/docente/{docenteId}")
    public ResponseEntity<List<SubmissionResponseDTO>> listarPorDocente(@PathVariable Long docenteId) {
        List<SubmissionResponseDTO> proyectos = submissionService.listarPorDocente(docenteId);
        return ResponseEntity.ok(proyectos);
    }

    /**
     * GET /api/submissions/en-proceso
     * Listar proyectos que no están en estado final
     */
    @GetMapping("/en-proceso")
    public ResponseEntity<List<SubmissionResponseDTO>> listarEnProceso() {
        List<SubmissionResponseDTO> proyectos = submissionService.listarEnProceso();
        return ResponseEntity.ok(proyectos);
    }

    /**
     * PUT /api/submissions/{id}/presentar
     * Presentar el formato A al coordinador
     * Transición: FORMATO_A_DILIGENCIADO -> PRESENTADO_AL_COORDINADOR
     */
    @PutMapping("/{id}/presentar")
    public ResponseEntity<?> presentarAlCoordinador(@PathVariable Long id) {
        try {
            SubmissionResponseDTO response = submissionService.presentarAlCoordinador(id);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Proyecto no encontrado"));
        }
    }

    /**
     * PUT /api/submissions/{id}/enviar-evaluacion
     * Enviar el formato A al comité para evaluación
     * Transición: PRESENTADO_AL_COORDINADOR -> EN_EVALUACION_COMITE
     */
    @PutMapping("/{id}/enviar-evaluacion")
    public ResponseEntity<?> enviarAComite(@PathVariable Long id) {
        try {
            SubmissionResponseDTO response = submissionService.enviarAComite(id);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Proyecto no encontrado"));
        }
    }

    /**
     * PUT /api/submissions/{id}/evaluar
     * Evaluar el formato A (aprobar o rechazar)
     * Transiciones desde EN_EVALUACION_COMITE:
     * - Si aprueba -> ACEPTADO_POR_COMITE
     * - Si rechaza y intentos < 3 -> CORRECCIONES_COMITE
     * - Si rechaza y intentos >= 3 -> RECHAZADO_POR_COMITE
     */
    @PutMapping("/{id}/evaluar")
    public ResponseEntity<?> evaluar(
            @PathVariable Long id,
            @Valid @RequestBody EvaluacionDTO evaluacion) {
        try {
            SubmissionResponseDTO response = submissionService.evaluar(id, evaluacion);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Proyecto no encontrado"));
        }
    }

    /**
     * PUT /api/submissions/{id}/subir-nueva-version
     * Subir una nueva versión del formato A tras correcciones
     * Transición: CORRECCIONES_COMITE -> EN_EVALUACION_COMITE
     */
    @PutMapping("/{id}/subir-nueva-version")
    public ResponseEntity<?> subirNuevaVersion(@PathVariable Long id) {
        try {
            SubmissionResponseDTO response = submissionService.subirNuevaVersion(id);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Proyecto no encontrado"));
        }
    }

    /**
     * GET /api/submissions/health
     * Health check simple para verificar que el servicio está activo
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check simple",
        description = "Verifica que el servicio esté activo y respondiendo",
        tags = {"Health Check"}
    )
    @ApiResponse(responseCode = "200", description = "Servicio activo")
    public String health() {
        return "ok";
    }

    /**
     * Clase interna para respuestas de error
     */
    private static class ErrorResponse {
        private String mensaje;

        public ErrorResponse(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }
    }
}


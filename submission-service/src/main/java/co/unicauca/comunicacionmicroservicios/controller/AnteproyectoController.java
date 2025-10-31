package co.unicauca.comunicacionmicroservicios.controller;

import co.unicauca.comunicacionmicroservicios.dto.*;
import co.unicauca.comunicacionmicroservicios.util.SecurityRules;
import co.unicauca.comunicacionmicroservicios.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Endpoints de Anteproyecto (RF6 + lectura/estado).
 *
 * - POST  /api/submissions/anteproyecto                  (DOCENTE y director del proyecto)
 * - GET   /api/submissions/anteproyecto                  (listado)
 * - PATCH /api/submissions/anteproyecto/{id}/estado      (lo llama Review/Jefe)
 */
@RestController
@RequestMapping("/api/submissions/anteproyecto")
public class AnteproyectoController {

    private final SubmissionService service;

    // Constructor explícito en lugar de @RequiredArgsConstructor
    public AnteproyectoController(SubmissionService service) {
        this.service = service;
    }

    /**
     * RF6 — Subir anteproyecto.
     *
     * Reglas:
     *  - Solo DOCENTE.
     *  - Debe ser el director del proyecto.
     *  - Formato A debe estar APROBADO.
     *  - No debe existir anteproyecto previo para el proyecto.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IdResponse> subirAnteproyecto(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestPart("data") @Valid AnteproyectoData data,
            @RequestPart("pdf") MultipartFile pdf
    ) {
        SecurityRules.requireDocente(role);
        IdResponse resp = service.subirAnteproyecto(userId, data, pdf);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnteproyectoPage> listarAnteproyectos(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.listarAnteproyectos(page, size));
    }

    /**
     * Cambio de estado de un anteproyecto (invocado por Review/Jefe).
     */
    @PatchMapping(path = "/{id}/estado", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cambiarEstadoAnteproyecto(
            @RequestHeader(value = "X-Service", required = false) String caller,
            @PathVariable Long id,
            @RequestBody @Valid CambioEstadoAnteproyectoRequest req
    ) {
        SecurityRules.requireInternalReviewService(caller);
        service.cambiarEstadoAnteproyecto(id, req);
        return ResponseEntity.ok().build();
    }
}

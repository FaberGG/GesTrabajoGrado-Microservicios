package co.unicauca.comunicacionmicroservicios.controller;

import co.unicauca.comunicacionmicroservicios.dto.*;
import co.unicauca.comunicacionmicroservicios.util.SecurityRules;
import co.unicauca.comunicacionmicroservicios.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Endpoints de Formato A (RF2, RF4 y lecturas).
 *
 * - POST  /api/submissions/formatoA                       (DOCENTE)
 * - GET   /api/submissions/formatoA/{id}
 * - GET   /api/submissions/formatoA?docenteId=...&page=&size=
 * - POST  /api/submissions/formatoA/{proyectoId}/nueva-version (DOCENTE)
 * - PATCH /api/submissions/formatoA/{versionId}/estado   (lo llama Review Service)
 */
@RestController
@RequestMapping("/api/submissions/formatoA")
public class FormatoAController {

    private final SubmissionService service;

    // Constructor explícito en lugar de @RequiredArgsConstructor
    public FormatoAController(SubmissionService service) {
        this.service = service;
    }

    /**
     * RF2 — Crear Formato A inicial.
     *
     * Recibe:
     *  - data: JSON con los datos del proyecto y formato A (ver FormatoAData)
     *  - pdf:  archivo PDF del Formato A
     *  - carta: PDF de carta (OBLIGATORIA si modalidad = PRACTICA_PROFESIONAL)
     *
     * Reglas:
     *  - Solo DOCENTE puede enviar.
     *  - Crea proyecto de grado y versión v1 del Formato A (intentoActual=1).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IdResponse> crearFormatoA(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestPart("data") @Valid FormatoAData data,
            @RequestPart("pdf") MultipartFile pdf,
            @RequestPart(value = "carta", required = false) MultipartFile carta
    ) {
        SecurityRules.requireDocente(role);
        IdResponse resp = service.crearFormatoA(userId, data, pdf, carta);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /**
     * Obtiene detalles de una versión de Formato A (o vista agregada).
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FormatoAView> obtenerFormatoA(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerFormatoA(id));
    }

    /**
     * Lista Formato A (filtrable por docente).
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FormatoAPage> listarFormatoA(
            @RequestParam(name = "docenteId", required = false) String docenteId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.listarFormatoA(Optional.ofNullable(docenteId), page, size));
    }

    /**
     * RF4 — Subir nueva versión de Formato A tras un rechazo.
     *
     * Reglas:
     *  - Solo DOCENTE.
     *  - Proyecto debe existir y no estar RECHAZADO_DEFINITIVO.
     *  - Última evaluación debe ser RECHAZADO.
     *  - Máximo 3 intentos.
     */
    @PostMapping(path = "/{proyectoId}/nueva-version",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IdResponse> nuevaVersion(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long proyectoId,
            @RequestPart("pdf") MultipartFile pdf,
            @RequestPart(value = "carta", required = false) MultipartFile carta
    ) {
        SecurityRules.requireDocente(role);
        IdResponse resp = service.reenviarFormatoA(userId, proyectoId, pdf, carta);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /**
     * Cambiar estado de una versión de Formato A (APROBADO / RECHAZADO).
     * Lo invoca el Review Service (o Coordinador vía Review Service).
     */
    @PatchMapping(path = "/{versionId}/estado", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cambiarEstado(
            @RequestHeader(value = "X-Service", required = false) String caller,
            @PathVariable Long versionId,
            @RequestBody @Valid EvaluacionRequest req
    ) {
        SecurityRules.requireInternalReviewService(caller);
        service.cambiarEstadoFormatoA(versionId, req);
        return ResponseEntity.ok().build();
    }
}

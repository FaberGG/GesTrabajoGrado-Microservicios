package co.unicauca.comunicacionmicroservicios.controller;

import co.unicauca.comunicacionmicroservicios.dto.*;
import co.unicauca.comunicacionmicroservicios.util.SecurityRules;
import co.unicauca.comunicacionmicroservicios.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Anteproyecto", description = "Gestión de anteproyectos (segunda etapa tras aprobación del Formato A)")
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
    @Operation(
        summary = "Subir anteproyecto (RF6)",
        description = """
            Permite al docente director subir el anteproyecto tras la aprobación del Formato A.
            
            **Pre-condiciones obligatorias (RF6)**:
            - Usuario debe tener rol **DOCENTE**
            - Usuario debe ser el **director del proyecto**
            - El **Formato A del proyecto debe estar APROBADO**
            - **No debe existir** un anteproyecto previo para ese proyecto
            - Archivo PDF del anteproyecto es **OBLIGATORIO**
            
            **Datos heredados del proyecto**:
            - Título (del Formato A)
            - Director ID
            - Estudiante1 ID y Estudiante2 ID (si aplica)
            
            **Evento publicado**: `anteproyecto.enviado` a cola `progress.anteproyecto.queue`
            """,
        tags = {"Anteproyecto"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Anteproyecto subido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = IdResponse.class),
                examples = @ExampleObject(
                    name = "Respuesta exitosa",
                    value = "{\"id\": 1}",
                    description = "El ID retornado es el proyectoId, no el anteproyectoId"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Archivo PDF faltante o inválido",
            content = @Content(examples = @ExampleObject(
                value = "{\"error\": \"Bad Request\", \"message\": \"Archivo PDF obligatorio\"}"
            ))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Usuario no autorizado (no es DOCENTE o no es el director)",
            content = @Content(examples = @ExampleObject(
                value = "{\"error\": \"Forbidden\", \"message\": \"Solo el director del proyecto puede subir el anteproyecto\"}"
            ))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Proyecto no encontrado",
            content = @Content(examples = @ExampleObject(
                value = "{\"error\": \"Not Found\", \"message\": \"Proyecto no existe\"}"
            ))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Formato A no aprobado o anteproyecto ya existe",
            content = @Content(examples = @ExampleObject(
                value = "{\"error\": \"Conflict\", \"message\": \"El Formato A debe estar aprobado antes de subir anteproyecto\"}"
            ))
        )
    })
    public ResponseEntity<IdResponse> subirAnteproyecto(
            @Parameter(description = "Rol del usuario (debe ser DOCENTE)", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-User-Role") String role,

            @Parameter(description = "ID del usuario autenticado", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-User-Id") String userId,

            @Parameter(description = "Datos JSON con el proyectoId", required = true)
            @RequestPart("data") @Valid AnteproyectoData data,

            @Parameter(description = "Archivo PDF del anteproyecto", required = true)
            @RequestPart("pdf") MultipartFile pdf
    ) {
        SecurityRules.requireDocente(role);
        IdResponse resp = service.subirAnteproyecto(userId, data, pdf);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Listar anteproyectos",
        description = "Retorna lista paginada de todos los anteproyectos en el sistema",
        tags = {"Anteproyecto"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AnteproyectoPage.class)
            )
        )
    })
    public ResponseEntity<AnteproyectoPage> listarAnteproyectos(
            @Parameter(description = "Número de página (inicia en 0)")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página")
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.listarAnteproyectos(page, size));
    }

    /**
     * Cambio de estado de un anteproyecto (invocado por Review/Jefe).
     */
    @PatchMapping(path = "/{id}/estado", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Cambiar estado del anteproyecto",
        description = """
            Endpoint para que el Review Service o Jefe de Departamento actualice el estado.
            
            **Uso interno**: Invocado por servicios internos, no por usuarios directamente.
            
            **Seguridad**: Requiere header `X-Service: review` para validación.
            """,
        tags = {"Anteproyecto"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado",
            content = @Content(examples = @ExampleObject(
                value = "{\"error\": \"Forbidden\", \"message\": \"Acceso restringido al servicio de revisión\"}"
            ))
        ),
        @ApiResponse(responseCode = "404", description = "Anteproyecto no encontrado")
    })
    public ResponseEntity<Void> cambiarEstadoAnteproyecto(
            @Parameter(description = "Identificador del servicio llamador", required = true, in = ParameterIn.HEADER)
            @RequestHeader(value = "X-Service", required = false) String caller,

            @Parameter(description = "ID del anteproyecto", required = true)
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Nuevo estado y observaciones",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = CambioEstadoAnteproyectoRequest.class),
                    examples = @ExampleObject(
                        value = """
                            {
                              "estado": "APROBADO",
                              "observaciones": "Anteproyecto bien estructurado"
                            }
                            """
                    )
                )
            )
            @RequestBody @Valid CambioEstadoAnteproyectoRequest req
    ) {
        SecurityRules.requireInternalReviewService(caller);
        service.cambiarEstadoAnteproyecto(id, req);
        return ResponseEntity.ok().build();
    }
}

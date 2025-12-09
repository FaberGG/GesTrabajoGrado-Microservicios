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
@Tag(name = "Formato A", description = "Gestión del Formato A (propuesta inicial de trabajo de grado)")
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
    @Operation(
        summary = "Crear Formato A inicial (RF2)",
        description = """
            Crea un nuevo proyecto de grado con su primera versión del Formato A (v1).
            
            **Reglas de Negocio (RF2)**:
            - Solo usuarios con rol **DOCENTE** pueden crear Formato A
            - Se crea el proyecto de grado asociado
            - Se genera la versión 1 del Formato A (intento 1/3)
            - Si modalidad es PRACTICA_PROFESIONAL, la carta de aceptación es **OBLIGATORIA**
            - El archivo PDF del Formato A es **OBLIGATORIO**
            - Estado inicial: PENDIENTE
            
            **Archivos requeridos**:
            - `pdf`: Documento del Formato A (PDF, obligatorio)
            - `carta`: Carta de aceptación de empresa (PDF, solo para PRACTICA_PROFESIONAL)
            
            **Evento publicado**: `formato-a.enviado` a cola `progress.formato-a.queue`
            """,
        tags = {"Formato A"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Formato A creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = IdResponse.class),
                examples = @ExampleObject(
                    name = "Respuesta exitosa",
                    value = "{\"id\": 1}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o archivo PDF faltante",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Bad Request\", \"message\": \"Archivo PDF obligatorio\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Usuario no autorizado (no es DOCENTE)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Forbidden\", \"message\": \"Solo DOCENTE puede realizar esta acción\"}"
                )
            )
        )
    })
    public ResponseEntity<IdResponse> crearFormatoA(
            @Parameter(description = "Rol del usuario (debe ser DOCENTE)", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-User-Role") String role,

            @Parameter(description = "ID del usuario (del Gateway)", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-User-Id") String userId,

            @Parameter(description = "Datos JSON del Formato A", required = true)
            @RequestPart("data") @Valid FormatoAData data,

            @Parameter(description = "Archivo PDF del Formato A", required = true)
            @RequestPart("pdf") MultipartFile pdf,

            @Parameter(description = "Carta de aceptación (obligatoria si modalidad=PRACTICA_PROFESIONAL)")
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
    @Operation(
        summary = "Obtener detalles de una versión de Formato A",
        description = "Retorna información completa de una versión específica del Formato A",
        tags = {"Formato A"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Formato A encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FormatoAView.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Formato A no encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Not Found\", \"message\": \"Formato A no encontrado\"}"
                )
            )
        )
    })
    public ResponseEntity<FormatoAView> obtenerFormatoA(
            @Parameter(description = "ID de la versión del Formato A", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerFormatoA(id));
    }

    /**
     * Lista Formato A (filtrable por docente).
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Listar versiones de Formato A",
        description = "Retorna lista paginada de versiones de Formato A, opcionalmente filtrada por docente",
        tags = {"Formato A"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FormatoAPage.class)
            )
        )
    })
    public ResponseEntity<FormatoAPage> listarFormatoA(
            @Parameter(description = "ID del docente para filtrar (opcional)")
            @RequestParam(name = "docenteId", required = false) String docenteId,

            @Parameter(description = "Número de página (inicia en 0)")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página")
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.listarFormatoA(Optional.ofNullable(docenteId), page, size));
    }

    /**
     * Lista Formato A pendientes de evaluación (para el Review Service).
     * Estados: EN_EVALUACION_COORDINADOR, CORRECCIONES_SOLICITADAS
     */
    @GetMapping(path = "/pendientes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FormatoAPage> listarFormatosAPendientes(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.listarFormatosAPendientes(page, size));
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
    @Operation(
        summary = "Reenviar Formato A tras rechazo (RF4)",
        description = """
            Permite al docente subir una nueva versión del Formato A después de un rechazo.
            
            **Reglas de Negocio (RF4)**:
            - Solo usuarios con rol **DOCENTE** pueden reenviar
            - El proyecto debe existir y NO estar en estado RECHAZADO_DEFINITIVO
            - La última evaluación debe ser RECHAZADO
            - Máximo **3 intentos** permitidos
            - Si es el 3er intento y se rechaza → RECHAZADO_DEFINITIVO
            
            **Evento publicado**: `formato-a.reenviado` a cola `progress.formato-a.queue`
            """,
        tags = {"Formato A"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Nueva versión creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = IdResponse.class),
                examples = @ExampleObject(value = "{\"id\": 1}")
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Usuario no autorizado",
            content = @Content(examples = @ExampleObject(
                value = "{\"error\": \"Forbidden\", \"message\": \"Solo DOCENTE puede realizar esta acción\"}"
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
            description = "No se puede reenviar (máximo intentos o estado inválido)",
            content = @Content(examples = @ExampleObject(
                value = "{\"error\": \"Conflict\", \"message\": \"Se alcanzó el máximo de 3 intentos\"}"
            ))
        )
    })
    public ResponseEntity<IdResponse> nuevaVersion(
            @Parameter(description = "Rol del usuario", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-User-Role") String role,

            @Parameter(description = "ID del usuario", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-User-Id") String userId,

            @Parameter(description = "ID del proyecto de grado", required = true)
            @PathVariable Long proyectoId,

            @Parameter(description = "Archivo PDF del Formato A (nueva versión)", required = true)
            @RequestPart("pdf") MultipartFile pdf,

            @Parameter(description = "Carta de aceptación (si aplica modalidad PRACTICA_PROFESIONAL)")
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
    @Operation(
        summary = "Cambiar estado de una versión de Formato A",
        description = """
            Endpoint para que el Review Service actualice el estado de una versión.
            
            **Uso interno**: Este endpoint es invocado por el **Review Service**, no por usuarios directamente.
            
            **Estados posibles**:
            - APROBADO: El Formato A fue aprobado por el coordinador
            - RECHAZADO: El Formato A fue rechazado (docente puede reenviar)
            
            **Seguridad**: Requiere header `X-Service: review` para validación interna.
            """,
        tags = {"Formato A"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado (no es servicio interno)",
            content = @Content(examples = @ExampleObject(
                value = "{\"error\": \"Forbidden\", \"message\": \"Acceso restringido al servicio de revisión\"}"
            ))
        ),
        @ApiResponse(responseCode = "404", description = "Versión de Formato A no encontrada")
    })
    public ResponseEntity<Void> cambiarEstado(
            @Parameter(description = "Identificador del servicio llamador", required = true, in = ParameterIn.HEADER)
            @RequestHeader(value = "X-Service", required = false) String caller,

            @Parameter(description = "ID de la versión del Formato A", required = true)
            @PathVariable Long versionId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos de la evaluación",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = EvaluacionRequest.class),
                    examples = @ExampleObject(
                        value = """
                            {
                              "estado": "APROBADO",
                              "observaciones": "Cumple con todos los requisitos",
                              "evaluadoPor": 5
                            }
                            """
                    )
                )
            )
            @RequestBody @Valid EvaluacionRequest req
    ) {
        SecurityRules.requireInternalReviewService(caller);
        service.cambiarEstadoFormatoA(versionId, req);
        return ResponseEntity.ok().build();
    }
}

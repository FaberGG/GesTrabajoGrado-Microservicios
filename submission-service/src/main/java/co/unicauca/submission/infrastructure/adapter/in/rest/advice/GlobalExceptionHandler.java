package co.unicauca.submission.infrastructure.adapter.in.rest.advice;

import co.unicauca.submission.domain.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para submission-service.
 * Proporciona respuestas consistentes con formato estándar para el frontend.
 *
 * Formato de respuesta de error:
 * {
 *   "success": false,
 *   "message": "Mensaje descriptivo del error",
 *   "data": null,
 *   "errors": ["Lista de errores específicos"],
 *   "timestamp": "2025-12-10T06:30:00",
 *   "path": "/api/v2/submissions/formatoA"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de dominio: ProyectoNotFoundException
     */
    @ExceptionHandler(ProyectoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProyectoNotFound(ProyectoNotFoundException ex) {
        log.warn("Proyecto no encontrado: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Proyecto no encontrado")
            .errors(java.util.List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja excepciones de dominio: UsuarioNoAutorizadoException
     */
    @ExceptionHandler(UsuarioNoAutorizadoException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioNoAutorizado(UsuarioNoAutorizadoException ex) {
        log.warn("Usuario no autorizado: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("No tiene permisos para realizar esta operación")
            .errors(java.util.List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Maneja excepciones de dominio: EstadoProyectoInvalidoException
     */
    @ExceptionHandler(EstadoProyectoInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleEstadoInvalido(EstadoProyectoInvalidoException ex) {
        log.warn("Estado del proyecto inválido: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("El proyecto no está en el estado correcto para esta operación")
            .errors(java.util.List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja excepciones: EstudianteYaTieneProyectoException
     */
    @ExceptionHandler(EstudianteYaTieneProyectoException.class)
    public ResponseEntity<ErrorResponse> handleEstudianteYaTieneProyecto(EstudianteYaTieneProyectoException ex) {
        log.warn("Estudiante ya tiene proyecto: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("El estudiante ya tiene un proyecto activo")
            .errors(java.util.List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja excepciones: FormatoAYaEvaluadoException
     */
    @ExceptionHandler(FormatoAYaEvaluadoException.class)
    public ResponseEntity<ErrorResponse> handleFormatoAYaEvaluado(FormatoAYaEvaluadoException ex) {
        log.warn("Formato A ya evaluado: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("El Formato A ya ha sido evaluado")
            .errors(java.util.List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja excepciones: CartaAceptacionRequeridaException
     */
    @ExceptionHandler(CartaAceptacionRequeridaException.class)
    public ResponseEntity<ErrorResponse> handleCartaAceptacionRequerida(CartaAceptacionRequeridaException ex) {
        log.warn("Carta de aceptación requerida: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Carta de aceptación requerida")
            .errors(java.util.List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones: MaximosIntentosExcedidosException
     */
    @ExceptionHandler(MaximosIntentosExcedidosException.class)
    public ResponseEntity<ErrorResponse> handleMaximosIntentosExcedidos(MaximosIntentosExcedidosException ex) {
        log.warn("Máximos intentos excedidos: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Se han agotado los intentos permitidos")
            .errors(java.util.List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja excepciones de dominio: ReglaNegocioException
     */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> handleReglaNegocio(ReglaNegocioException ex) {
        log.warn("Violación de regla de negocio: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("No se puede completar la operación")
            .errors(java.util.List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones de validación (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Errores de validación: {}", ex.getBindingResult().getErrorCount());

        java.util.List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Error de validación en los datos enviados")
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja IllegalArgumentException (validaciones de Value Objects)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Datos inválidos")
            .errors(java.util.List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja IllegalStateException (transiciones de estado inválidas)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Estado inválido: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Operación no permitida en el estado actual")
            .errors(java.util.List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja excepciones genéricas no esperadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Ha ocurrido un error interno en el servidor")
            .errors(java.util.List.of("Por favor, contacte al administrador del sistema"))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * DTO para respuestas de error consistentes
     */
    public static class ErrorResponse {
        private boolean success;
        private String message;
        private Object data;
        private java.util.List<String> errors;
        private LocalDateTime timestamp;

        // Constructor privado para usar el builder
        private ErrorResponse() {
            this.data = null; // Siempre null en respuestas de error
        }

        // Builder pattern
        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }

        public java.util.List<String> getErrors() {
            return errors;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        // Builder class
        public static class ErrorResponseBuilder {
            private final ErrorResponse response;

            private ErrorResponseBuilder() {
                this.response = new ErrorResponse();
            }

            public ErrorResponseBuilder success(boolean success) {
                response.success = success;
                return this;
            }

            public ErrorResponseBuilder message(String message) {
                response.message = message;
                return this;
            }

            public ErrorResponseBuilder errors(java.util.List<String> errors) {
                response.errors = errors;
                return this;
            }

            public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
                response.timestamp = timestamp;
                return this;
            }

            public ErrorResponse build() {
                return response;
            }
        }
    }
}


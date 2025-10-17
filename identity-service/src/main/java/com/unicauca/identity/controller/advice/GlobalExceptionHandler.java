package com.unicauca.identity.controller.advice;

import com.unicauca.identity.dto.response.ApiResponse;
import com.unicauca.identity.exception.BusinessException;
import com.unicauca.identity.exception.EmailAlreadyExistsException;
import com.unicauca.identity.exception.InvalidCredentialsException;
import com.unicauca.identity.exception.InvalidTokenException;
import com.unicauca.identity.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para APIs REST
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Logger estático para esta clase
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja errores de validación de los campos en las solicitudes
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        List<String> errorMessages = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toList());

        log.warn("Error de validación: {}", errorMessages);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Error de validación", errorMessages));
    }

    /**
     * Maneja la excepción cuando el email ya existe
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Intento de registro con email existente: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Maneja la excepción de credenciales inválidas
     */
    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(Exception ex) {
        log.warn("Intento de autenticación fallido: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Credenciales inválidas"));
    }

    /**
     * Maneja la excepción de token inválido
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Token inválido: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Maneja la excepción cuando no se encuentra un usuario
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("Usuario no encontrado: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Maneja la excepción de acceso denegado
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Acceso denegado: {} - ruta: {}", ex.getMessage(), request.getDescription(false));

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("No tienes permisos para acceder a este recurso"));
    }

    /**
     * Maneja excepciones genéricas de negocio
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Error de negocio: {}", ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Maneja errores de deserialización JSON (valores de enum inválidos, formato JSON incorrecto)
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.warn("Error de deserialización JSON: {}", ex.getMessage());

        String message = "Error en el formato de los datos enviados";

        // Extraer mensaje más específico si es posible
        String exMessage = ex.getMessage();
        if (exMessage != null) {
            if (exMessage.contains("Programa")) {
                message = "Valor inválido para 'programa'. Valores válidos: INGENIERIA_DE_SISTEMAS, INGENIERIA_ELECTRONICA_Y_TELECOMUNICACIONES, AUTOMATICA_INDUSTRIAL, TECNOLOGIA_EN_TELEMATICA";
            } else if (exMessage.contains("Rol")) {
                message = "Valor inválido para 'rol'. Valores válidos: ESTUDIANTE, PROFESOR, COORDINADOR, ADMIN";
            } else if (exMessage.contains("JSON parse error")) {
                message = "Error en el formato JSON. Verifica que los datos estén correctamente formateados";
            }
        }

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(message));
    }

    /**
     * Maneja cualquier excepción no controlada
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex, WebRequest request) {
        // SIEMPRE loguear el error completo con stack trace para diagnóstico
        log.error("Error no controlado en ruta {}: {} - Tipo: {}",
                  request.getDescription(false),
                  ex.getMessage(),
                  ex.getClass().getName(),
                  ex);

        // En desarrollo/debug, mostrar más detalles del error en la respuesta
        String detailedMessage = "Ha ocurrido un error inesperado. Por favor, inténtalo más tarde.";
        if (log.isDebugEnabled()) {
            detailedMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(detailedMessage));
    }
}

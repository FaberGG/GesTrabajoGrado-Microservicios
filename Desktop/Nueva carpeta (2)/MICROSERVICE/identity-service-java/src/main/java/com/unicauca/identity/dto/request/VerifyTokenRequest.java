package com.unicauca.identity.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de verificación de token (Java 21 Record)
 */
public record VerifyTokenRequest(
    @NotBlank(message = "El token es obligatorio")
    String token
) {
    // Los records automáticamente generan:
    // - Constructor con todos los parámetros
    // - Métodos de acceso (token())
    // - equals(), hashCode(), toString()
}

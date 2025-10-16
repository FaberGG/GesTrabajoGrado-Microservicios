package com.unicauca.identity.exception;

/**
 * Excepción lanzada cuando un token JWT es inválido o ha expirado
 */
public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super("Token inválido o expirado");
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}

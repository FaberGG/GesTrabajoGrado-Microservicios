package com.unicauca.identity.exception;

/**
 * Excepción lanzada cuando las credenciales de login son inválidas
 */
public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}

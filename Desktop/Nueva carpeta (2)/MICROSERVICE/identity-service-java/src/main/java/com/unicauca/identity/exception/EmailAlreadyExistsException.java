package com.unicauca.identity.exception;

/**
 * Excepción lanzada cuando se intenta registrar un usuario con un email ya existente
 */
public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException() {
        super("El email ya está registrado");
    }
}

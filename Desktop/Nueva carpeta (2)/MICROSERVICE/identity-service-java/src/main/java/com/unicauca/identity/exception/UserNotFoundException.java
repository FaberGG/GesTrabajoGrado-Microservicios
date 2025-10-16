package com.unicauca.identity.exception;

/**
 * Excepción lanzada cuando no se encuentra un usuario
 */
public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super("Usuario no encontrado");
    }

    public UserNotFoundException(Long userId) {
        super("Usuario con ID " + userId + " no encontrado");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}

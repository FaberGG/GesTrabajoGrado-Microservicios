package com.unicauca.identity.exception;

/**
 * Excepción base para errores de negocio en el microservicio de identidad
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

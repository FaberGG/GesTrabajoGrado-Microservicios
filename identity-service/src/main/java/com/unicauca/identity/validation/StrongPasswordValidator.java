package com.unicauca.identity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementación del validador para contraseñas fuertes:
 * - Al menos 6 caracteres
 * - Al menos 1 letra mayúscula (A-Z)
 * - Al menos 1 número (0-9)
 * - Al menos 1 carácter especial (!@#$%^&*()_+-=[]{}|;:,.<>?)
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.matches(PASSWORD_PATTERN);
    }
}

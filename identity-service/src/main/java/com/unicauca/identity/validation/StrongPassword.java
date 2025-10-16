package com.unicauca.identity.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotación de validación personalizada para verificar que la contraseña sea fuerte
 * - Al menos 6 caracteres
 * - Al menos 1 letra mayúscula (A-Z)
 * - Al menos 1 número (0-9)
 * - Al menos 1 carácter especial (!@#$%^&*()_+-=[]{}|;:,.<>?)
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "La contraseña debe tener al menos 6 caracteres, " +
            "1 mayúscula, 1 número y 1 carácter especial";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

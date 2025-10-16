package com.unicauca.identity.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotación de validación personalizada para verificar que el email sea institucional (@unicauca.edu.co)
 */
@Documented
@Constraint(validatedBy = InstitutionalEmailValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InstitutionalEmail {
    String message() default "El email debe ser institucional (@unicauca.edu.co)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

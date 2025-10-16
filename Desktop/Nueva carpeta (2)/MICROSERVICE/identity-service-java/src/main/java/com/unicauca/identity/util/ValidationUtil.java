package com.unicauca.identity.util;

import com.unicauca.identity.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Clase de utilidad para realizar validaciones comunes
 */
@Component
@Slf4j
public class ValidationUtil {

    private static final String EMAIL_REGEX = "^[^\\s@]+@unicauca\\.edu\\.co$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    private static final String NAME_REGEX = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{2,}$";
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    private static final String PHONE_REGEX = "^[0-9]{10}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    /**
     * Valida que un email sea institucional (@unicauca.edu.co)
     *
     * @param email Email a validar
     * @return true si el email es válido
     */
    public boolean isValidInstitutionalEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valida que una contraseña cumpla con los requisitos de seguridad
     *
     * @param password Contraseña a validar
     * @return true si la contraseña es válida
     */
    public boolean isValidStrongPassword(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Valida que un nombre o apellido contenga solo letras y espacios
     *
     * @param name Nombre o apellido a validar
     * @return true si el nombre es válido
     */
    public boolean isValidName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Valida que un número de teléfono contenga 10 dígitos numéricos
     *
     * @param phone Número de teléfono a validar
     * @return true si el teléfono es válido
     */
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return true; // El teléfono es opcional
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Valida que una cadena no sea null o vacía
     *
     * @param value Valor a validar
     * @param fieldName Nombre del campo para el mensaje de error
     * @throws BusinessException si la validación falla
     */
    public void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("El campo " + fieldName + " no puede estar vacío");
        }
    }

    /**
     * Valida que un objeto no sea null
     *
     * @param value Valor a validar
     * @param fieldName Nombre del campo para el mensaje de error
     * @throws BusinessException si la validación falla
     */
    public void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessException("El campo " + fieldName + " no puede ser nulo");
        }
    }
}

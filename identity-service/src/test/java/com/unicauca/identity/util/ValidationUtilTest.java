package com.unicauca.identity.util;

import com.unicauca.identity.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    private ValidationUtil validationUtil;

    @BeforeEach
    void setUp() {
        validationUtil = new ValidationUtil();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "usuario@unicauca.edu.co",
            "estudiante@unicauca.edu.co",
            "profesor@unicauca.edu.co",
            "admin@unicauca.edu.co"
    })
    void isValidInstitutionalEmail_ShouldReturnTrue_ForValidEmails(String email) {
        assertTrue(validationUtil.isValidInstitutionalEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "usuario@gmail.com",
            "usuario@hotmail.com",
            "usuario@unicauca.com",
            "usuario@unicauca",
            "usuario@",
            "usuario",
            "usuario@unicauca.edu.co.extra"
    })
    @NullAndEmptySource
    void isValidInstitutionalEmail_ShouldReturnFalse_ForInvalidEmails(String email) {
        assertFalse(validationUtil.isValidInstitutionalEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Password1!",
            "Secure2@",
            "StrongP4$$word",
            "C0mpl3x!P4ssw0rd",
            "Abcde1!"
    })
    void isValidStrongPassword_ShouldReturnTrue_ForValidPasswords(String password) {
        assertTrue(validationUtil.isValidStrongPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "password", // sin mayúscula, número o especial
            "Password", // sin número o especial
            "password1", // sin mayúscula o especial
            "password!", // sin mayúscula o número
            "Pass1", // muy corta
            "PASSWORD1!" // sin minúscula
    })
    @NullAndEmptySource
    void isValidStrongPassword_ShouldReturnFalse_ForInvalidPasswords(String password) {
        assertFalse(validationUtil.isValidStrongPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Juan",
            "María",
            "José Luis",
            "Ángel",
            "Núñez",
            "García Pérez"
    })
    void isValidName_ShouldReturnTrue_ForValidNames(String name) {
        assertTrue(validationUtil.isValidName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Juan123",
            "María!",
            "José_Luis",
            "1234",
            "J",
            "@#$%"
    })
    @NullAndEmptySource
    void isValidName_ShouldReturnFalse_ForInvalidNames(String name) {
        assertFalse(validationUtil.isValidName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "3201234567",
            "1234567890",
            "0000000000"
    })
    void isValidPhone_ShouldReturnTrue_ForValidPhones(String phone) {
        assertTrue(validationUtil.isValidPhone(phone));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "320123456", // muy corto
            "32012345678", // muy largo
            "3201234A67", // contiene letras
            "320-123-4567" // contiene caracteres no numéricos
    })
    void isValidPhone_ShouldReturnFalse_ForInvalidPhones(String phone) {
        assertFalse(validationUtil.isValidPhone(phone));
    }

    @Test
    void isValidPhone_ShouldReturnTrue_ForEmptyPhone() {
        assertTrue(validationUtil.isValidPhone(""));
        assertTrue(validationUtil.isValidPhone(null));
    }

    @Test
    void validateNotEmpty_ShouldNotThrow_WhenValueIsPresent() {
        assertDoesNotThrow(() -> validationUtil.validateNotEmpty("value", "field"));
    }

    @Test
    void validateNotEmpty_ShouldThrow_WhenValueIsEmpty() {
        assertThrows(BusinessException.class, () -> validationUtil.validateNotEmpty("", "field"));
    }

    @Test
    void validateNotEmpty_ShouldThrow_WhenValueIsNull() {
        assertThrows(BusinessException.class, () -> validationUtil.validateNotEmpty(null, "field"));
    }

    @Test
    void validateNotNull_ShouldNotThrow_WhenValueIsPresent() {
        assertDoesNotThrow(() -> validationUtil.validateNotNull(new Object(), "field"));
    }

    @Test
    void validateNotNull_ShouldThrow_WhenValueIsNull() {
        assertThrows(BusinessException.class, () -> validationUtil.validateNotNull(null, "field"));
    }
}

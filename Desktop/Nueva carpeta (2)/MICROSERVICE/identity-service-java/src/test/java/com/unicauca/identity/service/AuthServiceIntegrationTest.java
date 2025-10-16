package com.unicauca.identity.service;

import com.unicauca.identity.dto.request.LoginRequest;
import com.unicauca.identity.dto.request.RegisterRequest;
import com.unicauca.identity.dto.response.LoginResponse;
import com.unicauca.identity.dto.response.UserResponse;
import com.unicauca.identity.entity.User;
import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;
import com.unicauca.identity.exception.EmailAlreadyExistsException;
import com.unicauca.identity.exception.InvalidCredentialsException;
import com.unicauca.identity.exception.UserNotFoundException;
import com.unicauca.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void register_ShouldCreateUser_WhenDataIsValid() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .nombres("Integration")
                .apellidos("Test")
                .celular("3201234567")
                .programa(Programa.INGENIERIA_DE_SISTEMAS)
                .rol(Rol.ESTUDIANTE)
                .email("integration.test@unicauca.edu.co")
                .password("Test123!")
                .build();

        // When
        UserResponse response = authService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("integration.test@unicauca.edu.co");

        // Verify that user exists in database
        assertThat(userRepository.existsByEmail("integration.test@unicauca.edu.co")).isTrue();
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        // Given
        // Crear un usuario primero
        User existingUser = User.builder()
                .nombres("Existing")
                .apellidos("User")
                .celular("3201234567")
                .programa(Programa.INGENIERIA_DE_SISTEMAS)
                .rol(Rol.ESTUDIANTE)
                .email("existing@unicauca.edu.co")
                .passwordHash("hashedPassword")
                .build();

        userRepository.save(existingUser);

        RegisterRequest request = RegisterRequest.builder()
                .nombres("New")
                .apellidos("User")
                .celular("3207654321")
                .programa(Programa.INGENIERIA_DE_SISTEMAS)
                .rol(Rol.ESTUDIANTE)
                .email("existing@unicauca.edu.co") // Mismo email
                .password("Test123!")
                .build();

        // When/Then
        assertThrows(EmailAlreadyExistsException.class, () -> {
            authService.register(request);
        });
    }

    @Test
    void login_ShouldReturnUser_WhenCredentialsAreValid() {
        // Given
        // Registrar un usuario para luego hacer login
        RegisterRequest registerRequest = RegisterRequest.builder()
                .nombres("Login")
                .apellidos("Test")
                .celular("3201234567")
                .programa(Programa.INGENIERIA_DE_SISTEMAS)
                .rol(Rol.ESTUDIANTE)
                .email("login.test@unicauca.edu.co")
                .password("Login123!")
                .build();

        authService.register(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("login.test@unicauca.edu.co")
                .password("Login123!")
                .build();

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.user()).isNotNull();
        assertThat(response.user().email()).isEqualTo("login.test@unicauca.edu.co");
        assertThat(response.token()).isNotNull();
        assertThat(response.token()).isNotEmpty();
    }

    @Test
    void login_ShouldThrowException_WhenEmailNotFound() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent@unicauca.edu.co")
                .password("Password123!")
                .build();

        // When/Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsWrong() {
        // Given
        // Registrar un usuario para luego hacer login
        RegisterRequest registerRequest = RegisterRequest.builder()
                .nombres("Wrong")
                .apellidos("Password")
                .celular("3201234567")
                .programa(Programa.INGENIERIA_DE_SISTEMAS)
                .rol(Rol.ESTUDIANTE)
                .email("wrong.password@unicauca.edu.co")
                .password("Correct123!")
                .build();

        authService.register(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("wrong.password@unicauca.edu.co")
                .password("Wrong123!") // Contraseña incorrecta
                .build();

        // When/Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void getProfile_ShouldReturnUser_WhenUserExists() {
        // Given
        // Registrar un usuario primero
        RegisterRequest registerRequest = RegisterRequest.builder()
                .nombres("Profile")
                .apellidos("Test")
                .celular("3201234567")
                .programa(Programa.INGENIERIA_DE_SISTEMAS)
                .rol(Rol.ESTUDIANTE)
                .email("profile.test@unicauca.edu.co")
                .password("Profile123!")
                .build();

        UserResponse registeredUser = authService.register(registerRequest);
        Long userId = registeredUser.id();

        // When
        UserResponse profile = authService.getProfile(userId);

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.id()).isEqualTo(userId);
        assertThat(profile.email()).isEqualTo("profile.test@unicauca.edu.co");
    }

    @Test
    void getProfile_ShouldThrowException_WhenUserDoesNotExist() {
        // Given
        Long nonExistentUserId = 999999L;

        // When/Then
        assertThrows(UserNotFoundException.class, () -> {
            authService.getProfile(nonExistentUserId);
        });
    }
}

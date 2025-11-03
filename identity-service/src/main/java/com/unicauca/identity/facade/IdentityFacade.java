package com.unicauca.identity.facade;

import com.unicauca.identity.dto.request.LoginRequest;
import com.unicauca.identity.dto.request.RegisterRequest;
import com.unicauca.identity.dto.request.VerifyTokenRequest;
import com.unicauca.identity.dto.response.LoginResponse;
import com.unicauca.identity.dto.response.RolesResponse;
import com.unicauca.identity.dto.response.TokenVerificationResponse;
import com.unicauca.identity.dto.response.UserResponse;
import com.unicauca.identity.entity.User;
import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;
import com.unicauca.identity.repository.UserRepository;
import com.unicauca.identity.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Facade que centraliza la lógica de operaciones de identidad y autenticación.
 * Este patrón proporciona una interfaz simplificada para el controlador,
 * ocultando la complejidad de las interacciones entre servicios y repositorios.
 */
@Component
@Slf4j
public class IdentityFacade {

    private final AuthService authService;
    private final UserRepository userRepository;

    public IdentityFacade(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    /**
     * Registra un nuevo usuario en el sistema
     */
    public UserResponse registerUser(RegisterRequest request) {
        log.debug("Facade: Registrando nuevo usuario con email: {}", request.email());
        return authService.register(request);
    }

    /**
     * Autentica un usuario y genera un token JWT
     */
    public LoginResponse authenticateUser(LoginRequest request) {
        log.debug("Facade: Autenticando usuario: {}", request.email());
        return authService.login(request);
    }

    /**
     * Obtiene el perfil del usuario autenticado
     */
    public UserResponse getUserProfile(Long userId) {
        log.debug("Facade: Obteniendo perfil de usuario con ID: {}", userId);
        return authService.getProfile(userId);
    }

    /**
     * Obtiene los roles y programas disponibles en el sistema
     */
    public RolesResponse getRolesAndPrograms() {
        log.debug("Facade: Obteniendo roles y programas disponibles");
        return authService.getRolesAndPrograms();
    }

    /**
     * Verifica la validez de un token JWT
     */
    public TokenVerificationResponse verifyToken(VerifyTokenRequest request) {
        log.debug("Facade: Verificando token JWT");
        return authService.verifyToken(request);
    }

    /**
     * Obtiene el ID de un usuario por su email
     */
    public Long getUserIdByEmail(String email) {
        log.debug("Facade: Obteniendo ID de usuario por email: {}", email);
        return authService.getUserIdByEmail(email);
    }

    /**
     * Busca usuarios según criterios específicos con paginación
     */
    public Page<UserResponse> searchUsers(String query, Rol rol, Programa programa, int page, int size) {
        log.debug("Facade: Buscando usuarios con query: {}, rol: {}, programa: {}", query, rol, programa);
        return authService.searchUsers(query, rol, programa, page, size);
    }

    /**
     * Obtiene el email del primer usuario encontrado con el rol especificado
     */
    public Optional<String> getEmailByRole(Rol rol) {
        log.debug("Facade: Buscando email de usuario con rol: {}", rol);
        Optional<User> userOpt = userRepository.findFirstByRol(rol);
        return userOpt.map(User::getEmail);
    }
}


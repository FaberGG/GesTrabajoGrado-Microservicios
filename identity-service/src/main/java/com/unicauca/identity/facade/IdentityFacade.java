package com.unicauca.identity.facade;

import com.unicauca.identity.dto.request.LoginRequest;
import com.unicauca.identity.dto.request.RegisterRequest;
import com.unicauca.identity.dto.request.VerifyTokenRequest;
import com.unicauca.identity.dto.response.*;
import com.unicauca.identity.entity.User;
import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;
import com.unicauca.identity.repository.UserRepository;
import com.unicauca.identity.security.JwtTokenProvider;
import com.unicauca.identity.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Facade que centraliza la lógica de operaciones de identidad y autenticación.
 * Este patrón proporciona una interfaz simplificada para el controlador,
 * ocultando la complejidad de las interacciones entre servicios y repositorios.
 *
 * Encapsula completamente las operaciones de seguridad (hashing BCrypt y tokens JWT),
 * actuando como único punto de acceso para estas funcionalidades.
 */
@Component
@Slf4j
public class IdentityFacade {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public IdentityFacade(AuthService authService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
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

    // ========================================
    // MÉTODOS DE HASHING (BCrypt)
    // ========================================

    /**
     * Encripta una contraseña usando BCrypt con el PasswordEncoder configurado.
     *
     * @param rawPassword Contraseña en texto plano
     * @return Hash BCrypt de la contraseña
     */
    public String hashPassword(String rawPassword) {
        log.debug("Facade: Encriptando contraseña");
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifica si una contraseña en texto plano coincide con su hash BCrypt
     *
     * @param rawPassword     Contraseña en texto plano
     * @param encodedPassword Hash BCrypt almacenado
     * @return true si las contraseñas coinciden, false en caso contrario
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        log.debug("Facade: Verificando contraseña");
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // ========================================
    // MÉTODOS DE JWT
    // ========================================

    /**
     * Genera un token JWT firmado para un usuario.
     * El token incluye: email (subject), userId, rol y programa
     *
     * @param user Usuario para el cual generar el token
     * @return Token JWT firmado con tiempo de expiración configurado
     */
    public String generateToken(User user) {
        log.debug("Facade: Generando token JWT para usuario: {}", user.getEmail());
        return jwtTokenProvider.generateToken(user);
    }

    /**
     * Valida un token JWT verificando firma, formato y expiración.
     *
     * @param token Token JWT a validar
     * @return true si el token es válido
     * @throws com.unicauca.identity.exception.InvalidTokenException si el token es inválido, expirado o malformado
     */
    public boolean validateToken(String token) {
        log.debug("Facade: Validando token JWT");
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * Extrae todos los claims (payload) de un token JWT
     *
     * @param token Token JWT
     * @return Claims del token (incluye userId, email, rol, programa, iat, exp)
     * @throws com.unicauca.identity.exception.InvalidTokenException si el token es inválido
     */
    public Claims extractAllClaims(String token) {
        log.debug("Facade: Extrayendo claims del token JWT");
        return jwtTokenProvider.getAllClaimsFromToken(token);
    }

    /**
     * Extrae el ID del usuario de un token JWT
     *
     * @param token Token JWT
     * @return ID del usuario
     * @throws com.unicauca.identity.exception.InvalidTokenException si el token es inválido
     */
    public Long getUserIdFromToken(String token) {
        log.debug("Facade: Extrayendo userId del token JWT");
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    /**
     * Extrae el email del usuario de un token JWT
     *
     * @param token Token JWT
     * @return Email del usuario
     * @throws com.unicauca.identity.exception.InvalidTokenException si el token es inválido
     */
    public String getUserEmailFromToken(String token) {
        log.debug("Facade: Extrayendo email del token JWT");
        return jwtTokenProvider.getUserEmailFromToken(token);
    }

    public UserBasicInfoDTO getUserBasicInfo(Long userId) {
        return authService.getUserBasicInfo(userId);
    }

    public UserBasicInfoDTO getCoordinador() {
        return authService.getCoordinador();
    }

    public UserBasicInfoDTO getJefeDepartamento() {
        return authService.getJefeDepartamento();
    }
}


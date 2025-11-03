package com.unicauca.identity.controller;

import com.unicauca.identity.dto.request.LoginRequest;
import com.unicauca.identity.dto.request.RegisterRequest;
import com.unicauca.identity.dto.request.VerifyTokenRequest;
import com.unicauca.identity.dto.response.*;
import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;
import com.unicauca.identity.facade.IdentityFacade;
import com.unicauca.identity.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para operaciones de autenticación y gestión de identidad
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Operaciones de autenticación y gestión de identidad")
@Slf4j
public class AuthController {

    private final IdentityFacade identityFacade;

    public AuthController(IdentityFacade identityFacade) {
        this.identityFacade = identityFacade;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario",
            description = "Registra un nuevo usuario en el sistema con sus datos personales")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse registeredUser = identityFacade.registerUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(registeredUser, "Usuario registrado exitosamente"));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Autentica al usuario y devuelve un token JWT para acceder a recursos protegidos")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = identityFacade.authenticateUser(request);
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login exitoso"));
    }

    @GetMapping("/profile")
    @Operation(summary = "Obtener perfil de usuario",
            description = "Obtiene el perfil del usuario autenticado (requiere token JWT)")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        Long userId = identityFacade.getUserIdByEmail(userEmail);
        UserResponse userProfile = identityFacade.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(userProfile));
    }

    @GetMapping("/roles")
    @Operation(summary = "Obtener roles y programas disponibles",
            description = "Obtiene la lista de roles y programas académicos disponibles (requiere token JWT)")
    public ResponseEntity<ApiResponse<RolesResponse>> getRoles() {
        RolesResponse rolesAndPrograms = identityFacade.getRolesAndPrograms();
        return ResponseEntity.ok(ApiResponse.success(rolesAndPrograms));
    }

    @PostMapping("/verify-token")
    @Operation(summary = "Verificar token JWT",
            description = "Verifica si un token JWT es válido y devuelve los datos asociados")
    public ResponseEntity<TokenVerificationResponse> verifyToken(@Valid @RequestBody VerifyTokenRequest request) {
        TokenVerificationResponse response = identityFacade.verifyToken(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/search")
    @Operation(summary = "Buscar usuarios",
            description = "Busca usuarios según criterios y devuelve resultados paginados (requiere token JWT)")
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Programa programa,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserResponse> userPage = identityFacade.searchUsers(query, rol, programa, page, size);
        return PaginationUtil.createPaginatedResponse(userPage);
    }

    @GetMapping("/users/role/{role}/email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmailByRole(@PathVariable("role") String role) {
        try {
            log.info("Buscando usuario con rol: {}", role);

            Rol rolEnum = Rol.valueOf(role.toUpperCase());

            Optional<String> emailOpt = identityFacade.getEmailByRole(rolEnum);

            if (emailOpt.isPresent()) {
                Map<String, Object> data = new HashMap<>();
                data.put("email", emailOpt.get());
                return ResponseEntity.ok(ApiResponse.success(data, "Email obtenido correctamente"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No se encontró usuario con rol " + role));
            }
        } catch (IllegalArgumentException e) {
            log.error("Rol inválido: {}", role);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Rol inválido: " + role));
        } catch (Exception e) {
            log.error("Error obteniendo email por rol: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error interno al buscar el email"));
        }
    }


    /**
     * Obtiene información básica de un usuario por ID
     * Endpoint interno para otros microservicios
     */
    @GetMapping("/users/{userId}/basic")
    @Operation(summary = "Obtener información básica de usuario por ID",
            description = "Endpoint interno para comunicación entre microservicios")
    public ResponseEntity<ApiResponse<UserBasicInfoDTO>> getUserBasicInfo(
            @PathVariable Long userId,
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {

        // Solo servicios internos pueden acceder
        if (!isValidServiceToken(serviceToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token de servicio inválido o ausente"));
        }

        UserBasicInfoDTO userInfo = identityFacade.getUserBasicInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(userInfo, "Usuario encontrado"));
    }

    /**
     * Obtiene información del coordinador
     */
    @GetMapping("/users/coordinador")
    @Operation(summary = "Obtener coordinador del sistema")
    public ResponseEntity<ApiResponse<UserBasicInfoDTO>> getCoordinador(
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {

        if (!isValidServiceToken(serviceToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token de servicio inválido o ausente"));
        }

        UserBasicInfoDTO coordinador = identityFacade.getCoordinador();
        return ResponseEntity.ok(ApiResponse.success(coordinador, "Coordinador encontrado"));
    }

    /**
     * Obtiene información del jefe de departamento
     */
    @GetMapping("/users/jefe-departamento")
    @Operation(summary = "Obtener jefe de departamento")
    public ResponseEntity<ApiResponse<UserBasicInfoDTO>> getJefeDepartamento(
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {

        if (!isValidServiceToken(serviceToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token de servicio inválido o ausente"));
        }

        UserBasicInfoDTO jefe = identityFacade.getJefeDepartamento();
        return ResponseEntity.ok(ApiResponse.success(jefe, "Jefe de departamento encontrado"));
    }

    // =====================================================
    // MÉTODO PRIVADO: Validación de Token de Servicio
    // =====================================================

    /**
     * Valida que el token de servicio sea correcto
     * (Solo para endpoints internos service-to-service)
     */
    private boolean isValidServiceToken(String receivedToken) {
        if (receivedToken == null || receivedToken.isBlank()) {
            log.warn("Intento de acceso a endpoint interno sin token de servicio");
            return false;
        }

        // Obtener el token esperado desde variables de entorno
        String expectedToken = System.getenv("SERVICE_INTERNAL_TOKEN");

        // Si no está configurado, usar valor por defecto solo en desarrollo
        if (expectedToken == null || expectedToken.isBlank()) {
            log.warn("SERVICE_INTERNAL_TOKEN no configurado, usando valor por defecto (SOLO DESARROLLO)");
            expectedToken = "default-token-only-for-dev";
        }

        boolean isValid = receivedToken.equals(expectedToken);

        if (!isValid) {
            log.warn("Token de servicio inválido recibido");
        }

        return isValid;
    }
}

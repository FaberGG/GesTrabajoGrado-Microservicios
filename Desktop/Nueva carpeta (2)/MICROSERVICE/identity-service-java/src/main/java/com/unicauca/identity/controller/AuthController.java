package com.unicauca.identity.controller;

import com.unicauca.identity.dto.request.LoginRequest;
import com.unicauca.identity.dto.request.RegisterRequest;
import com.unicauca.identity.dto.request.VerifyTokenRequest;
import com.unicauca.identity.dto.response.ApiResponse;
import com.unicauca.identity.dto.response.LoginResponse;
import com.unicauca.identity.dto.response.RolesResponse;
import com.unicauca.identity.dto.response.TokenVerificationResponse;
import com.unicauca.identity.dto.response.UserResponse;
import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;
import com.unicauca.identity.service.AuthService;
import com.unicauca.identity.util.PaginationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de autenticación
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Operaciones de autenticación y gestión de identidad")
public class AuthController {

    private final AuthService authService;

    // Constructor explícito para la inyección de dependencias
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario",
               description = "Registra un nuevo usuario en el sistema con sus datos personales")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse registeredUser = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(registeredUser, "Usuario registrado exitosamente"));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
               description = "Autentica al usuario y devuelve un token JWT para acceder a recursos protegidos")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login exitoso"));
    }

    @GetMapping("/profile")
    @Operation(summary = "Obtener perfil de usuario",
               description = "Obtiene el perfil del usuario autenticado (requiere token JWT)")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        // Obtener el email del usuario autenticado
        String userEmail = userDetails.getUsername();

        // Buscar el usuario por email para obtener su ID
        Long userId = authService.getUserIdByEmail(userEmail);

        // Obtener el perfil completo
        UserResponse userProfile = authService.getProfile(userId);

        return ResponseEntity.ok(ApiResponse.success(userProfile));
    }

    @GetMapping("/roles")
    @Operation(summary = "Obtener roles y programas disponibles",
               description = "Obtiene la lista de roles y programas académicos disponibles (requiere token JWT)")
    public ResponseEntity<ApiResponse<RolesResponse>> getRoles() {
        RolesResponse rolesAndPrograms = authService.getRolesAndPrograms();
        return ResponseEntity.ok(ApiResponse.success(rolesAndPrograms));
    }

    @PostMapping("/verify-token")
    @Operation(summary = "Verificar token JWT",
               description = "Verifica si un token JWT es válido y devuelve los datos asociados")
    public ResponseEntity<TokenVerificationResponse> verifyToken(@Valid @RequestBody VerifyTokenRequest request) {
        TokenVerificationResponse response = authService.verifyToken(request);
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

        Page<UserResponse> userPage = authService.searchUsers(query, rol, programa, page, size);
        return PaginationUtil.createPaginatedResponse(userPage);
    }
}

package com.unicauca.identity.service;

import com.unicauca.identity.dto.request.LoginRequest;
import com.unicauca.identity.dto.request.RegisterRequest;
import com.unicauca.identity.dto.request.VerifyTokenRequest;
import com.unicauca.identity.dto.response.LoginResponse;
import com.unicauca.identity.dto.response.RolesResponse;
import com.unicauca.identity.dto.response.TokenVerificationResponse;
import com.unicauca.identity.dto.response.UserResponse;
import com.unicauca.identity.entity.User;
import com.unicauca.identity.enums.Rol;
import com.unicauca.identity.enums.Programa;
import org.springframework.data.domain.Page;

import java.util.Arrays;
import java.util.List;

/**
 * Interfaz para el servicio de autenticación
 */
public interface AuthService {

    /**
     * Registra un nuevo usuario en el sistema
     *
     * @param request Datos del usuario a registrar
     * @return DTO con los datos del usuario registrado
     */
    UserResponse register(RegisterRequest request);

    /**
     * Autentica un usuario y genera un token JWT
     *
     * @param request Credenciales de login
     * @return DTO con el usuario y token JWT
     */
    LoginResponse login(LoginRequest request);

    /**
     * Obtiene el perfil del usuario autenticado
     *
     * @param userId ID del usuario
     * @return DTO con los datos del usuario
     */
    UserResponse getProfile(Long userId);

    /**
     * Obtiene los roles y programas disponibles
     *
     * @return DTO con los roles y programas disponibles
     */
    RolesResponse getRolesAndPrograms();

    /**
     * Verifica la validez de un token JWT
     *
     * @param request Token JWT a verificar
     * @return DTO con la respuesta de verificación
     */
    TokenVerificationResponse verifyToken(VerifyTokenRequest request);

    /**
     * Obtiene el ID de un usuario por su email
     *
     * @param email Email del usuario
     * @return ID del usuario
     */
    Long getUserIdByEmail(String email);

    /**
     * Convierte una entidad User a un DTO UserResponse
     *
     * @param user Entidad de usuario
     * @return DTO con los datos del usuario
     */
    UserResponse mapUserToUserResponse(User user);

    /**
     * Busca usuarios según criterios específicos
     *
     * @param query Texto para buscar en nombres, apellidos o email
     * @param rol Filtro opcional por rol
     * @param programa Filtro opcional por programa
     * @param page Número de página (inicia en 0)
     * @param size Tamaño de página
     * @return Página de usuarios que coinciden con los criterios
     */
    Page<UserResponse> searchUsers(String query, Rol rol, Programa programa, int page, int size);
}

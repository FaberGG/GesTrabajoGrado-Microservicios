package com.unicauca.identity.service;

import com.unicauca.identity.dto.request.LoginRequest;
import com.unicauca.identity.dto.request.RegisterRequest;
import com.unicauca.identity.dto.request.VerifyTokenRequest;
import com.unicauca.identity.dto.response.*;
import com.unicauca.identity.entity.User;
import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;
import com.unicauca.identity.exception.UserNotFoundException;
import org.springframework.data.domain.Page;

import java.util.Optional;

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

    /**
     * Obtiene información básica de un usuario por su ID
     * (Para comunicación entre microservicios)
     *
     * @param userId ID del usuario
     * @return Información básica del usuario
     * @throws UserNotFoundException si el usuario no existe
     */
    UserBasicInfoDTO getUserBasicInfo(Long userId);

    /**
     * Obtiene el primer coordinador registrado
     * (Para envío de notificaciones)
     *
     * @return Información básica del coordinador
     * @throws UserNotFoundException si no hay coordinador registrado
     */
    UserBasicInfoDTO getCoordinador();

    /**
     * Obtiene el primer jefe de departamento registrado
     * (Para envío de notificaciones)
     *
     * @return Información básica del jefe de departamento
     * @throws UserNotFoundException si no hay jefe de departamento registrado
     */
    UserBasicInfoDTO getJefeDepartamento();

    /**
     * Obtiene el email de un usuario por rol
     * (Fallback para compatibilidad con código existente)
     *
     * @param rol Rol del usuario
     * @return Email del primer usuario con ese rol
     */
    Optional<String> getEmailByRole(Rol rol);
}

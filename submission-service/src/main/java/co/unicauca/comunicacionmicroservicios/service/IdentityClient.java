package co.unicauca.comunicacionmicroservicios.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Cliente MEJORADO para comunicarse con Identity Service
 *
 * CAMBIOS:
 * - Usa endpoints específicos (/users/{id}/basic) en lugar de /search
 * - Incluye token de servicio en todas las llamadas (X-Service-Token)
 * - Manejo robusto de errores con fallback
 */
@Service
public class IdentityClient {

    private static final Logger log = LoggerFactory.getLogger(IdentityClient.class);
    private final WebClient webClient;

    @Value("${service.internal.token}")
    private String serviceToken;

    @Value("${notification.default.coordinador-email:coordinador@unicauca.edu.co}")
    private String defaultCoordinadorEmail;

    @Value("${notification.default.jefe-departamento-email:jefe.departamento@unicauca.edu.co}")
    private String defaultJefeDepartamentoEmail;

    @Value("${identity.timeout-ms:3000}")
    private long timeoutMs;

    public IdentityClient(
            @Value("${identity.base-url:http://localhost:8081}") String baseUrl,
            WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
        log.info("IdentityClient configurado con baseUrl: {}", baseUrl);
    }

    /**
     *Obtiene información completa de un usuario por ID
     * Usa el endpoint GET /api/auth/users/{id}/basic
     */
    public UserBasicInfo getUserById(Long userId) {
        try {
            log.debug("Obteniendo usuario {} desde identity-service...", userId);

            ApiResponse<UserBasicInfo> response = webClient.get()
                    .uri("/api/auth/users/{id}/basic", userId)
                    .header("X-Service-Token", serviceToken)  // ✅ Token de servicio
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserBasicInfo>>() {})
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response != null && response.success && response.data != null) {
                log.info("Usuario {} encontrado: {}", userId, response.data.email);
                return response.data;
            }

            log.warn("Usuario {} no encontrado, usando datos por defecto", userId);
            return createDefaultUserInfo(userId);

        } catch (WebClientResponseException e) {
            log.error("Error HTTP al obtener usuario {}: {} - {}",
                    userId, e.getStatusCode(), e.getMessage());
            return createDefaultUserInfo(userId);
        } catch (Exception e) {
            log.error("Error al obtener usuario {} desde identity-service", userId, e);
            return createDefaultUserInfo(userId);
        }
    }

    /**
     * Obtiene el coordinador usando endpoint específico
     */
    public String getCoordinadorEmail() {
        try {
            log.debug("Obteniendo coordinador desde identity-service...");

            ApiResponse<UserBasicInfo> response = webClient.get()
                    .uri("/api/auth/users/coordinador")
                    .header("X-Service-Token", serviceToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserBasicInfo>>() {})
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response != null && response.success && response.data != null) {
                log.info("Coordinador encontrado: {}", response.data.email);
                return response.data.email;
            }

            log.warn("No se encontró coordinador, usando email por defecto");
            return defaultCoordinadorEmail;

        } catch (Exception e) {
            log.error("Error al obtener coordinador desde identity-service", e);
            return defaultCoordinadorEmail;
        }
    }

    /**
     * Obtiene el jefe de departamento usando endpoint específico
     */
    public String getJefeDepartamentoEmail() {
        try {
            log.debug("Obteniendo jefe de departamento desde identity-service...");

            ApiResponse<UserBasicInfo> response = webClient.get()
                    .uri("/api/auth/users/jefe-departamento")
                    .header("X-Service-Token", serviceToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserBasicInfo>>() {})
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response != null && response.success && response.data != null) {
                log.info("Jefe de departamento encontrado: {}", response.data.email);
                return response.data.email;
            }

            log.warn("No se encontró jefe de departamento, usando email por defecto");
            return defaultJefeDepartamentoEmail;

        } catch (Exception e) {
            log.error("Error al obtener jefe de departamento desde identity-service", e);
            return defaultJefeDepartamentoEmail;
        }
    }

    /**
     * Obtiene solo el email de un usuario
     */
    public String getUserEmail(String userId) {
        if (userId == null) {
            return "desconocido@unicauca.edu.co";
        }
        try {
            Long id = Long.parseLong(userId);
            UserBasicInfo user = getUserById(id);
            return user.email;
        } catch (NumberFormatException e) {
            log.warn("ID de usuario inválido: {}", userId);
            return "usuario." + userId + "@unicauca.edu.co";
        }
    }

    /**
     * Obtiene el nombre completo de un usuario
     */
    public String getUserName(String userId) {
        if (userId == null) {
            return "Usuario Desconocido";
        }
        try {
            Long id = Long.parseLong(userId);
            UserBasicInfo user = getUserById(id);
            return user.getNombreCompleto();
        } catch (NumberFormatException e) {
            log.warn("ID de usuario inválido: {}", userId);
            return "Usuario " + userId;
        }
    }

    /**
     * Crea información de usuario por defecto (fallback)
     */
    private UserBasicInfo createDefaultUserInfo(Long userId) {
        return new UserBasicInfo(
                userId,
                "Usuario",
                String.valueOf(userId),
                "usuario." + userId + "@unicauca.edu.co",
                "DESCONOCIDO",
                null
        );
    }

    /**
     * DTO para información básica de usuario
     */
    public record UserBasicInfo(
            Long id,
            String nombres,
            String apellidos,
            String email,
            String rol,
            String programa
    ) {
        public String getNombreCompleto() {
            return nombres + " " + apellidos;
        }
    }

    /**
     * DTO para respuestas de Identity Service
     */
    private static class ApiResponse<T> {
        public boolean success;
        public String message;
        public T data;
        public Object errors;
    }
}
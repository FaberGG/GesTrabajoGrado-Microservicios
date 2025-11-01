package co.unicauca.comunicacionmicroservicios.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Cliente para comunicarse con el Identity Service y obtener información de usuarios.
 *
 * ENDPOINTS DISPONIBLES (según README de identity-service):
 * - GET  /api/auth/profile - Obtener perfil del usuario autenticado
 * - GET  /api/auth/users/search?rol={ROL} - Buscar usuarios por rol
 *
 * PROPÓSITO:
 * - Obtener emails de coordinadores, jefes de departamento, docentes, estudiantes
 * - Obtener información completa de usuarios para notificaciones
 *
 * ROLES DISPONIBLES (según identity-service):
 * - ESTUDIANTE: Estudiante que realiza proyecto de grado
 * - DOCENTE: Director o codirector de proyecto de grado
 * - COORDINADOR: Coordinador de programa que evalúa Formato A (RF2, RF4)
 * - JEFE_DEPARTAMENTO: Jefe de departamento que recibe anteproyectos (RF6)
 * - ADMIN: Administrador del sistema
 */
@Service
public class IdentityClient {

    private static final Logger log = LoggerFactory.getLogger(IdentityClient.class);
    private final WebClient webClient;

    // Emails por defecto (FALLBACK - se usan si identity-service falla)
    @Value("${notification.default.coordinador-email:coordinador@unicauca.edu.co}")
    private String defaultCoordinadorEmail;

    @Value("${notification.default.jefe-departamento-email:jefe.departamento@unicauca.edu.co}")
    private String defaultJefeDepartamentoEmail;

    // Timeout para llamadas al identity-service
    @Value("${identity.timeout-ms:3000}")
    private long timeoutMs;

    public IdentityClient(
            @Value("${identity.base-url:http://identity:8081}") String baseUrl,
            WebClient.Builder builder
    ) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
        log.info("IdentityClient configurado con baseUrl: {}", baseUrl);
    }

    /**
     * Obtiene el email del coordinador del programa.
     * Busca el primer usuario con rol COORDINADOR.
     *
     * Endpoint: GET /api/auth/users/search?rol=COORDINADOR
     *
     * @return Email del coordinador
     */
    public String getCoordinadorEmail() {
        try {
            log.debug("Buscando coordinador en identity-service...");

            ApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/auth/users/search")
                            .queryParam("rol", "COORDINADOR")
                            .queryParam("page", 0)
                            .queryParam("size", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response != null && response.success && response.data != null) {
                Map<String, Object> data = (Map<String, Object>) response.data;
                List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

                if (content != null && !content.isEmpty()) {
                    String email = (String) content.get(0).get("email");
                    log.info("Coordinador encontrado: {}", email);
                    return email;
                }
            }

            log.warn("No se encontró coordinador en identity-service, usando email por defecto");
            return defaultCoordinadorEmail;

        } catch (WebClientResponseException e) {
            log.error("Error HTTP al obtener coordinador: {} - {}", e.getStatusCode(), e.getMessage());
            return defaultCoordinadorEmail;
        } catch (Exception e) {
            log.error("Error al obtener coordinador desde identity-service", e);
            return defaultCoordinadorEmail;
        }
    }

    /**
     * Obtiene el email del jefe de departamento.
     * Busca el primer usuario con rol JEFE_DEPARTAMENTO.
     *
     * Endpoint: GET /api/auth/users/search?rol=JEFE_DEPARTAMENTO
     *
     * @return Email del jefe de departamento
     */
    public String getJefeDepartamentoEmail() {
        try {
            log.debug("Buscando jefe de departamento en identity-service...");

            ApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/auth/users/search")
                            .queryParam("rol", "JEFE_DEPARTAMENTO")
                            .queryParam("page", 0)
                            .queryParam("size", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response != null && response.success && response.data != null) {
                Map<String, Object> data = (Map<String, Object>) response.data;
                List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

                if (content != null && !content.isEmpty()) {
                    String email = (String) content.get(0).get("email");
                    log.info("Jefe de departamento encontrado: {}", email);
                    return email;
                }
            }

            log.warn("No se encontró jefe de departamento, usando email por defecto");
            return defaultJefeDepartamentoEmail;

        } catch (WebClientResponseException e) {
            log.error("Error HTTP al obtener jefe de departamento: {} - {}", e.getStatusCode(), e.getMessage());
            return defaultJefeDepartamentoEmail;
        } catch (Exception e) {
            log.error("Error al obtener jefe de departamento desde identity-service", e);
            return defaultJefeDepartamentoEmail;
        }
    }

    /**
     * Busca un usuario por su ID.
     *
     * Endpoint: GET /api/auth/users/search?query={userId}
     *
     * @param userId ID del usuario
     * @return Map con datos del usuario (email, nombres, apellidos, etc.)
     */
    public Map<String, Object> getUserInfo(String userId) {
        try {
            log.debug("Buscando usuario {} en identity-service...", userId);

            ApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/auth/users/search")
                            .queryParam("query", userId)
                            .queryParam("page", 0)
                            .queryParam("size", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response != null && response.success && response.data != null) {
                Map<String, Object> data = (Map<String, Object>) response.data;
                List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

                if (content != null && !content.isEmpty()) {
                    Map<String, Object> userInfo = content.get(0);
                    log.debug("Usuario encontrado: {}", userInfo.get("email"));
                    return userInfo;
                }
            }

            log.warn("Usuario {} no encontrado, usando datos por defecto", userId);
            return createDefaultUserInfo(userId);

        } catch (WebClientResponseException e) {
            log.error("Error HTTP al obtener usuario {}: {} - {}", userId, e.getStatusCode(), e.getMessage());
            return createDefaultUserInfo(userId);
        } catch (Exception e) {
            log.error("Error al obtener usuario {} desde identity-service", userId, e);
            return createDefaultUserInfo(userId);
        }
    }

    /**
     * Obtiene el email de un usuario por su ID.
     *
     * @param userId ID del usuario
     * @return Email del usuario
     */
    public String getUserEmail(String userId) {
        if (userId == null) {
            return "desconocido@unicauca.edu.co";
        }
        Map<String, Object> userInfo = getUserInfo(userId);
        return (String) userInfo.getOrDefault("email", "usuario." + userId + "@unicauca.edu.co");
    }

    /**
     * Obtiene el nombre completo de un usuario por su ID.
     *
     * @param userId ID del usuario
     * @return Nombre completo del usuario (nombres + apellidos)
     */
    public String getUserName(String userId) {
        if (userId == null) {
            return "Usuario Desconocido";
        }
        Map<String, Object> userInfo = getUserInfo(userId);

        String nombres = (String) userInfo.get("nombres");
        String apellidos = (String) userInfo.get("apellidos");

        if (nombres != null && apellidos != null) {
            return nombres + " " + apellidos;
        } else if (nombres != null) {
            return nombres;
        } else if (apellidos != null) {
            return apellidos;
        }

        return "Usuario " + userId;
    }

    /**
     * Crea información de usuario por defecto cuando no se puede obtener del identity-service.
     */
    private Map<String, Object> createDefaultUserInfo(String userId) {
        return Map.of(
                "id", userId,
                "email", "usuario." + userId + "@unicauca.edu.co",
                "nombres", "Usuario",
                "apellidos", userId,
                "rol", "DESCONOCIDO"
        );
    }

    /**
     * DTO interno para deserializar respuestas del identity-service.
     * Coincide con ApiResponse del identity-service.
     */
    private static class ApiResponse {
        public boolean success;
        public String message;
        public Object data;
        public Object errors;
    }
}


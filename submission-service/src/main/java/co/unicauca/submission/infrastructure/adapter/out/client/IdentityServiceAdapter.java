package co.unicauca.submission.infrastructure.adapter.out.client;

import co.unicauca.submission.application.port.out.IIdentityServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Adaptador para comunicación con el Identity Service.
 * Implementa el puerto IIdentityServicePort.
 *
 * Usa RestTemplate para hacer llamadas HTTP al identity-service.
 */
@Component
public class IdentityServiceAdapter implements IIdentityServicePort {

    private static final Logger log = LoggerFactory.getLogger(IdentityServiceAdapter.class);

    @Value("${services.identity.url:http://identity:8081}")
    private String identityServiceUrl;

    @Value("${services.identity.service-token:default-token-only-for-dev}")
    private String serviceToken;

    private final RestTemplate restTemplate;

    public IdentityServiceAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public UsuarioInfo obtenerUsuario(Long userId) {
        try {
            log.debug("Obteniendo usuario desde identity-service: {}", userId);

            String url = identityServiceUrl + "/api/auth/users/" + userId + "/basic";

            // Configurar headers con el token de servicio
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Token", serviceToken);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Hacer la llamada HTTP
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                // Extraer datos del wrapper ApiResponse
                Map<String, Object> data = (Map<String, Object>) body.get("data");

                if (data != null) {
                    Long id = ((Number) data.get("id")).longValue();
                    String nombreCompleto = (String) data.get("nombreCompleto");
                    String email = (String) data.get("email");
                    String programa = (String) data.get("programa");
                    String rol = (String) data.get("rol");

                    log.info("✅ Usuario obtenido de identity-service: {} - {}", id, nombreCompleto);

                    return new UsuarioInfo(id, nombreCompleto, email, programa, rol);
                } else {
                    log.warn("⚠️ Identity-service no devolvió datos para usuario {}", userId);
                }
            }

            // Fallback a mock si falla
            log.warn("⚠️ Usando datos mock para usuario {} (identity-service no disponible)", userId);
            return new UsuarioInfo(
                userId,
                "Usuario Mock",
                "usuario" + userId + "@unicauca.edu.co",
                "Ingeniería de Sistemas",
                "DOCENTE"
            );

        } catch (Exception e) {
            log.error("❌ Error al obtener usuario {} de identity-service: {}", userId, e.getMessage());

            // Fallback a mock en caso de error
            log.warn("⚠️ Usando datos mock para usuario {} debido a error", userId);
            return new UsuarioInfo(
                userId,
                "Usuario Mock",
                "usuario" + userId + "@unicauca.edu.co",
                "Ingeniería de Sistemas",
                "DOCENTE"
            );
        }
    }

    @Override
    public String obtenerEmailCoordinador() {
        try {
            log.debug("Obteniendo email del coordinador");

            // TODO: Implementar endpoint en identity-service para obtener usuarios por rol
            // Por ahora usamos un email conocido
            String url = identityServiceUrl + "/api/auth/users/by-role/COORDINADOR";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Token", serviceToken);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    Map<String, Object> data = (Map<String, Object>) body.get("data");

                    if (data != null) {
                        String email = (String) data.get("email");
                        log.info("✅ Email coordinador obtenido: {}", email);
                        return email;
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ Endpoint de coordinador no disponible, usando fallback");
            }

            // Fallback
            return "coordinador@unicauca.edu.co";

        } catch (Exception e) {
            log.error("Error al obtener email coordinador: {}", e.getMessage());
            return "coordinador@unicauca.edu.co"; // Fallback
        }
    }

    @Override
    public String obtenerEmailJefeDepartamento() {
        try {
            log.debug("Obteniendo email del jefe de departamento");

            // TODO: Implementar endpoint en identity-service para obtener usuarios por rol
            String url = identityServiceUrl + "/api/auth/users/by-role/JEFE_DEPARTAMENTO";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Token", serviceToken);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    Map<String, Object> data = (Map<String, Object>) body.get("data");

                    if (data != null) {
                        String email = (String) data.get("email");
                        log.info("✅ Email jefe departamento obtenido: {}", email);
                        return email;
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ Endpoint de jefe departamento no disponible, usando fallback");
            }

            // Fallback
            return "jefe.depto@unicauca.edu.co";

        } catch (Exception e) {
            log.error("Error al obtener email jefe departamento: {}", e.getMessage());
            return "jefe.depto@unicauca.edu.co"; // Fallback
        }
    }

    @Override
    public boolean tieneRol(Long userId, String rol) {
        try {
            log.debug("Verificando si usuario {} tiene rol {}", userId, rol);

            // Obtener información del usuario y verificar su rol
            UsuarioInfo usuario = obtenerUsuario(userId);

            if (usuario != null && usuario.rol() != null) {
                boolean tieneRol = usuario.rol().equalsIgnoreCase(rol);
                log.debug("Usuario {} {} el rol {}", userId, tieneRol ? "tiene" : "no tiene", rol);
                return tieneRol;
            }

            log.warn("⚠️ No se pudo verificar rol para usuario {}, permitiendo por defecto", userId);
            return true; // Fallback permisivo para no bloquear

        } catch (Exception e) {
            log.error("❌ Error al verificar rol para usuario {}: {}", userId, e.getMessage());
            // Por seguridad en caso de error de comunicación, permitimos la operación
            return true;
        }
    }
}


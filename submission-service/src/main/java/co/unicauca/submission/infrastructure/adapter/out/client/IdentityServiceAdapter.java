package co.unicauca.submission.infrastructure.adapter.out.client;

import co.unicauca.submission.application.port.out.IIdentityServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Adaptador para comunicación con el Identity Service.
 * Implementa el puerto IIdentityServicePort.
 *
 * Usa RestTemplate para hacer llamadas HTTP al identity-service.
 */
@Component
public class IdentityServiceAdapter implements IIdentityServicePort {

    private static final Logger log = LoggerFactory.getLogger(IdentityServiceAdapter.class);

    @Value("${services.identity.url:http://localhost:8081}")
    private String identityServiceUrl;

    private final RestTemplate restTemplate;

    public IdentityServiceAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public UsuarioInfo obtenerUsuario(Long userId) {
        try {
            log.debug("Obteniendo usuario desde identity-service: {}", userId);

            String url = identityServiceUrl + "/api/users/" + userId;

            // TODO: Implementar llamada real al identity-service
            // Por ahora retornamos mock para no bloquear el desarrollo

            log.warn("MOCK: Retornando usuario mockeado (implementar integración real)");

            return new UsuarioInfo(
                userId,
                "Usuario Mock",
                "usuario" + userId + "@unicauca.edu.co",
                "Ingeniería de Sistemas",
                "DOCENTE"
            );

        } catch (Exception e) {
            log.error("Error al obtener usuario {}: {}", userId, e.getMessage());
            throw new RuntimeException("No se pudo obtener información del usuario", e);
        }
    }

    @Override
    public String obtenerEmailCoordinador() {
        try {
            log.debug("Obteniendo email del coordinador");

            // TODO: Implementar llamada real al identity-service
            log.warn("MOCK: Retornando email mockeado del coordinador");

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

            // TODO: Implementar llamada real al identity-service
            log.warn("MOCK: Retornando email mockeado del jefe");

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

            // TODO: Implementar llamada real al identity-service
            String url = identityServiceUrl + "/api/users/" + userId + "/roles";

            log.warn("MOCK: Simulando validación de rol (implementar integración real)");

            // Por ahora retornamos true para no bloquear el desarrollo
            // En producción, hacer la llamada HTTP real
            return true;

        } catch (Exception e) {
            log.error("Error al verificar rol para usuario {}: {}", userId, e.getMessage());
            // Por seguridad, retornamos false en caso de error
            return false;
        }
    }
}


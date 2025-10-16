package co.unicauca.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * Clase principal de la aplicación Gateway.
 *
 * Este gateway actúa como único punto de entrada al sistema de gestión
 * de trabajos de grado, enrutando peticiones a los microservicios:
 * - identity-service: autenticación y gestión de usuarios
 * - submission-service: gestión de entregas y documentos
 * - notification-service: envío de notificaciones
 *
 * Funcionalidades principales:
 * - Validación JWT en todas las rutas protegidas
 * - Inyección de claims de usuario como headers (X-User-*)
 * - Logging de peticiones y respuestas
 * - Circuit breaker para resiliencia
 * - Autorización basada en roles (opcional)
 *
 * @author Gateway Team
 * @version 1.0.0
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

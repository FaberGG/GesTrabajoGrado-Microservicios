package co.unicauca.comunicacionmicroservicios.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI submissionServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Submission Service API")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestión de entregas de trabajos de grado en la Universidad del Cauca.
                                
                                Este servicio gestiona:
                                - **Formato A**: Propuesta inicial del trabajo de grado (hasta 3 intentos)
                                - **Anteproyecto**: Documento detallado tras aprobación del Formato A
                                
                                ## Flujo de Trabajo
                                1. **Docente** sube Formato A (RF2)
                                2. **Coordinador** evalúa Formato A (aprueba/rechaza)
                                3. Si rechazado: Docente puede reenviar hasta 3 veces (RF4)
                                4. Si aprobado: Docente sube Anteproyecto (RF6)
                                5. Sistema publica eventos a RabbitMQ para tracking
                                
                                ## Autenticación
                                Las peticiones requieren headers del API Gateway:
                                - `X-User-Id`: ID del usuario autenticado
                                - `X-User-Role`: Rol del usuario (DOCENTE, COORDINADOR, ESTUDIANTE)
                                - `X-User-Email`: Email institucional del usuario
                                - `Authorization`: Bearer token JWT (opcional para algunos endpoints)
                                """)
                        .contact(new Contact()
                                .name("Universidad del Cauca - Ingeniería de Sistemas")
                                .email("sistemas@unicauca.edu.co")
                                .url("https://www.unicauca.edu.co"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de Desarrollo Local"),
                        new Server()
                                .url("http://localhost:8080/api/submissions")
                                .description("A través del API Gateway (Producción)")
                ))
                .tags(Arrays.asList(
                        new Tag()
                                .name("Formato A")
                                .description("Gestión del Formato A (propuesta inicial de trabajo de grado)"),
                        new Tag()
                                .name("Anteproyecto")
                                .description("Gestión de anteproyectos (tras aprobación del Formato A)"),
                        new Tag()
                                .name("Health Check")
                                .description("Endpoints de monitoreo y salud del servicio")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Gateway Headers"))
                .components(new Components()
                        .addSecuritySchemes("Gateway Headers", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Id")
                                .description("Headers de autenticación proporcionados por el API Gateway"))
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT para autenticación (opcional, manejado por Gateway)"))
                );
    }
}


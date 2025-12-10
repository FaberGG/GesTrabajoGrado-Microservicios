package co.unicauca.submission.infrastructure.config;

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
import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentar la API REST del Submission Service.
 *
 * Acceder a la documentación:
 * - Swagger UI: http://localhost:8082/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8082/v3/api-docs
 * - OpenAPI YAML: http://localhost:8082/v3/api-docs.yaml
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:submission-service}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .tags(tags())
                .components(new Components()
                        .addSecuritySchemes("X-User-Id", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Id")
                                .description("ID del usuario autenticado (proporcionado por el Gateway)")))
                .addSecurityItem(new SecurityRequirement().addList("X-User-Id"));
    }

    private Info apiInfo() {
        return new Info()
                .title("📄 Submission Service API")
                .description("""
                        ## Sistema de Gestión de Trabajos de Grado - Microservicio de Submissions
                        
                        Este microservicio gestiona el ciclo de vida de los proyectos de grado, incluyendo:
                        
                        ### 📋 Formato A (Propuesta inicial)
                        - **RF2**: Creación de Formato A por el docente director
                        - **RF3**: Evaluación por el coordinador
                        - **RF4**: Reenvío tras correcciones (máximo 3 intentos)
                        
                        ### 📑 Anteproyecto
                        - **RF6**: Subida del anteproyecto por el director
                        - **RF8**: Asignación de evaluadores por el jefe de departamento
                        
                        ### 🔍 Consultas
                        - **RF5**: Consulta del estado del proyecto por estudiantes
                        - Consulta de proyectos por director, estado, etc.
                        
                        ---
                        
                        ### 🏗️ Arquitectura
                        Este servicio implementa **Arquitectura Hexagonal** con:
                        - **Puertos de entrada (Use Cases)**: Definen las operaciones del dominio
                        - **Adaptadores REST**: Exponen la API HTTP
                        - **Eventos de dominio**: Publican a RabbitMQ para comunicación asíncrona
                        
                        ### 🔐 Autenticación
                        Las operaciones requieren el header `X-User-Id` con el ID del usuario autenticado.
                        Este header es inyectado por el API Gateway tras validar el token JWT.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Equipo de Desarrollo - Unicauca")
                        .email("desarrollo@unicauca.edu.co")
                        .url("https://github.com/unicauca"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> servers() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:8082")
                        .description("🖥️ Servidor de Desarrollo Local"),
                new Server()
                        .url("http://localhost:8080")
                        .description("🌐 API Gateway (Producción)")
        );
    }

    private List<Tag> tags() {
        return Arrays.asList(
                new Tag()
                        .name("Formato A")
                        .description("📄 Operaciones para gestión de Formato A (propuesta inicial del proyecto)"),
                new Tag()
                        .name("Anteproyecto")
                        .description("📑 Operaciones para gestión de Anteproyecto"),
                new Tag()
                        .name("Submissions")
                        .description("🔍 Consultas generales de proyectos de grado")
        );
    }
}


package com.unicauca.identity.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración mejorada para la documentación OpenAPI (Swagger)
 */
@Configuration
public class OpenApiConfig {

    @Value("${springdoc.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${springdoc.server-description:API Server}")
    private String serverDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Identity Service API")
                .version("1.0.0")
                .description("Microservicio de identidad y autenticación para la Universidad del Cauca")
                .contact(new Contact()
                    .name("Universidad del Cauca")
                    .email("soporte@unicauca.edu.co")
                    .url("https://www.unicauca.edu.co"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addServersItem(new Server()
                .url(serverUrl)
                .description(serverDescription))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Ingrese el token JWT con el formato: Bearer {token}"))
                .addResponses("UnauthorizedError", createUnauthorizedResponse())
                .addResponses("ForbiddenError", createForbiddenResponse())
                .addResponses("ValidationError", createValidationErrorResponse())
                .addResponses("NotFoundError", createNotFoundResponse())
                .addResponses("InternalError", createInternalServerErrorResponse())
            );
    }

    private ApiResponse createUnauthorizedResponse() {
        return new ApiResponse()
                .description("No autenticado - Se requiere autenticación")
                .content(new Content()
                        .addMediaType("application/json", createMediaType(
                                "Error de autenticación",
                                "{\n  \"success\": false,\n  \"message\": \"No autenticado o token inválido\"\n}"
                        )));
    }

    private ApiResponse createForbiddenResponse() {
        return new ApiResponse()
                .description("Acceso prohibido - No tiene permisos para acceder a este recurso")
                .content(new Content()
                        .addMediaType("application/json", createMediaType(
                                "Error de permisos",
                                "{\n  \"success\": false,\n  \"message\": \"No tiene permisos para acceder a este recurso\"\n}"
                        )));
    }

    private ApiResponse createValidationErrorResponse() {
        return new ApiResponse()
                .description("Error de validación - Los datos enviados no cumplen con las validaciones")
                .content(new Content()
                        .addMediaType("application/json", createMediaType(
                                "Error de validación",
                                "{\n  \"success\": false,\n  \"message\": \"Error de validación\",\n  \"errors\": [\"El email debe ser institucional\", \"La contraseña debe contener al menos una mayúscula\"]\n}"
                        )));
    }

    private ApiResponse createNotFoundResponse() {
        return new ApiResponse()
                .description("Recurso no encontrado")
                .content(new Content()
                        .addMediaType("application/json", createMediaType(
                                "Recurso no encontrado",
                                "{\n  \"success\": false,\n  \"message\": \"Usuario no encontrado\"\n}"
                        )));
    }

    private ApiResponse createInternalServerErrorResponse() {
        return new ApiResponse()
                .description("Error interno del servidor")
                .content(new Content()
                        .addMediaType("application/json", createMediaType(
                                "Error interno",
                                "{\n  \"success\": false,\n  \"message\": \"Ha ocurrido un error inesperado. Por favor, inténtalo más tarde.\"\n}"
                        )));
    }

    private MediaType createMediaType(String exampleName, String exampleValue) {
        Map<String, Example> examples = new HashMap<>();
        examples.put(exampleName, new Example().value(exampleValue));

        return new MediaType().examples(examples);
    }
}

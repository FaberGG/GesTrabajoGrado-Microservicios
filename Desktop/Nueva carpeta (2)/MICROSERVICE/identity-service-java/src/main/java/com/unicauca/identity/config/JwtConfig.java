package com.unicauca.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuración para JWT
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {

    /**
     * Clave secreta para firmar los tokens JWT
     */
    private String secret;

    /**
     * Tiempo de expiración del token en milisegundos
     */
    private long expiration;
}

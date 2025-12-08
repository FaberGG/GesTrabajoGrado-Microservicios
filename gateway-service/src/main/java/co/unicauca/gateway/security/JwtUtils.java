package co.unicauca.gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad para validar y extraer información de tokens JWT.
 *
 * Responsabilidades:
 * - Validar la firma del token usando el secret configurado
 * - Verificar que el token no haya expirado
 * - Extraer claims del payload: userId, rol (sin 'e'), email
 *
 * Usa la librería io.jsonwebtoken (JJWT) - LA MISMA que identity-service.
 *
 * NOTA DE SEGURIDAD:
 * - No loguear el token completo en producción
 * - El secret debe ser de al menos 256 bits y almacenarse en variable de entorno
 *
 * @author Gateway Team
 */
@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Obtiene la clave de firma para JWT
     *
     * @return SecretKey para firma de tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Valida un token JWT verificando firma y expiración.
     *
     * @param token El token JWT (sin el prefijo "Bearer ")
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            log.debug("Token validado correctamente");
            return true;
        } catch (SignatureException e) {
            log.error("Firma JWT inválida: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformado: {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expirado: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT no soportado: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("Claims vacías en el token JWT: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae los claims principales del token JWT.
     *
     * Claims esperados:
     * - userId: identificador único del usuario (Long)
     * - rol: rol del usuario (DOCENTE, ESTUDIANTE, COORDINADOR) - SIN 'e'
     * - email: correo electrónico del usuario (subject del JWT)
     * - programa: programa académico del usuario
     *
     * @param token El token JWT
     * @return Map con los claims extraídos, o Map vacío si el token es inválido
     */
    public Map<String, String> extractClaims(String token) {
        Map<String, String> claims = new HashMap<>();

        try {
            Claims jwtClaims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Subject (email del usuario)
            String email = jwtClaims.getSubject();
            if (email != null) {
                claims.put("email", email);
            }

            // Claim personalizado: userId
            Object userIdObj = jwtClaims.get("userId");
            if (userIdObj != null) {
                claims.put("userId", String.valueOf(userIdObj));
            }

            // Claim personalizado: rol (SIN 'e' - importante!)
            Object rolObj = jwtClaims.get("rol");
            if (rolObj != null) {
                claims.put("role", String.valueOf(rolObj)); // Guardamos como "role" para compatibilidad interna
            }

            // Claim personalizado: programa
            Object programaObj = jwtClaims.get("programa");
            if (programaObj != null) {
                claims.put("programa", String.valueOf(programaObj));
            }

            log.debug("Claims extraídos del token: userId={}, rol={}",
                    claims.get("userId"), claims.get("role"));

        } catch (JwtException e) {
            log.error("Error al extraer claims del token: {}", e.getMessage());
        }

        return claims;
    }

    /**
     * Extrae el token del header Authorization.
     *
     * Formato esperado: "Bearer <token>"
     *
     * @param authorizationHeader El valor del header Authorization
     * @return El token sin el prefijo "Bearer ", o null si el formato es inválido
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.debug("Header Authorization inválido o ausente");
            return null;
        }

        return authorizationHeader.substring(7); // Eliminar "Bearer "
    }

    /**
     * Extrae el userId del token JWT.
     *
     * @param token El token JWT
     * @return El userId, o null si no se encuentra
     */
    public String getUserId(String token) {
        Map<String, String> claims = extractClaims(token);
        return claims.get("userId");
    }

    /**
     * Extrae el rol del token JWT.
     *
     * @param token El token JWT
     * @return El rol del usuario, o null si no se encuentra
     */
    public String getRole(String token) {
        Map<String, String> claims = extractClaims(token);
        return claims.get("role");
    }

    /**
     * Extrae el email del token JWT.
     *
     * @param token El token JWT
     * @return El email del usuario, o null si no se encuentra
     */
    public String getEmail(String token) {
        Map<String, String> claims = extractClaims(token);
        return claims.get("email");
    }
}
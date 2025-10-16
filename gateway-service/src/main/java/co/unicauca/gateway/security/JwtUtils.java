package co.unicauca.gateway.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad para validar y extraer información de tokens JWT.
 *
 * Responsabilidades:
 * - Validar la firma del token usando el secret configurado
 * - Verificar que el token no haya expirado
 * - Extraer claims del payload: userId, role, email
 *
 * Usa la librería Auth0 java-jwt para el procesamiento de tokens.
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

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtUtils(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
        this.algorithm = Algorithm.HMAC256(jwtSecret);
        this.verifier = JWT.require(algorithm).build();
    }

    /**
     * Valida un token JWT verificando firma y expiración.
     *
     * @param token El token JWT (sin el prefijo "Bearer ")
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);

            // Verificar expiración explícitamente
            Date expiresAt = jwt.getExpiresAt();
            if (expiresAt != null && expiresAt.before(new Date())) {
                log.warn("Token expirado. Expira en: {}", expiresAt);
                return false;
            }

            log.debug("Token validado correctamente para subject: {}", jwt.getSubject());
            return true;
        } catch (JWTVerificationException e) {
            log.error("Error al validar token JWT: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae los claims principales del token JWT.
     *
     * Claims esperados:
     * - userId: identificador único del usuario (UUID o string)
     * - role: rol del usuario (DOCENTE, ESTUDIANTE, COORDINADOR, JEFE_DEPARTAMENTO)
     * - email: correo electrónico del usuario (opcional)
     * - sub: subject del JWT (típicamente el userId)
     *
     * @param token El token JWT
     * @return Map con los claims extraídos, o Map vacío si el token es inválido
     */
    public Map<String, String> extractClaims(String token) {
        Map<String, String> claims = new HashMap<>();

        try {
            DecodedJWT jwt = verifier.verify(token);

            // Subject (típicamente el userId)
            String subject = jwt.getSubject();
            if (subject != null) {
                claims.put("userId", subject);
            }

            // Claim personalizado: userId (puede ser diferente al subject)
            String userId = jwt.getClaim("userId").asString();
            if (userId != null) {
                claims.put("userId", userId);
            }

            // Claim personalizado: role
            String role = jwt.getClaim("role").asString();
            if (role != null) {
                claims.put("role", role);
            }

            // Claim personalizado: email
            String email = jwt.getClaim("email").asString();
            if (email != null) {
                claims.put("email", email);
            }

            log.debug("Claims extraídos del token: userId={}, role={}",
                    claims.get("userId"), claims.get("role"));

        } catch (JWTVerificationException e) {
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
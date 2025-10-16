package co.unicauca.gateway.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para JwtUtils.
 *
 * Verifica la funcionalidad de:
 * - Validación de tokens JWT
 * - Extracción de claims
 * - Manejo de tokens expirados
 * - Manejo de tokens con firma inválida
 *
 * @author Gateway Team
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private String secretKey;
    private Algorithm algorithm;

    @BeforeEach
    void setUp() {
        secretKey = "test-secret-key-for-jwt-validation-minimum-256-bits";
        algorithm = Algorithm.HMAC256(secretKey);
        jwtUtils = new JwtUtils(secretKey);
    }

    /**
     * Test: Validar token válido debe retornar true.
     */
    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Crear un token válido con expiración futura
        String token = JWT.create()
                .withSubject("test-user-id")
                .withClaim("userId", "12345678-90ab-cdef-1234-567890abcdef")
                .withClaim("role", "DOCENTE")
                .withClaim("email", "test@universidad.com")
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000)) // +24h
                .sign(algorithm);

        boolean isValid = jwtUtils.validateToken(token);

        assertTrue(isValid, "El token válido debería ser validado correctamente");
    }

    /**
     * Test: Validar token expirado debe retornar false.
     */
    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Crear un token expirado
        String token = JWT.create()
                .withSubject("test-user-id")
                .withClaim("userId", "12345678-90ab-cdef-1234-567890abcdef")
                .withClaim("role", "DOCENTE")
                .withExpiresAt(new Date(System.currentTimeMillis() - 1000)) // Expirado hace 1 segundo
                .sign(algorithm);

        boolean isValid = jwtUtils.validateToken(token);

        assertFalse(isValid, "El token expirado debería ser rechazado");
    }

    /**
     * Test: Validar token con firma inválida debe retornar false.
     */
    @Test
    void testValidateToken_InvalidSignature_ReturnsFalse() {
        // Crear un token con una clave diferente
        Algorithm wrongAlgorithm = Algorithm.HMAC256("wrong-secret-key");
        String token = JWT.create()
                .withSubject("test-user-id")
                .withClaim("userId", "12345678-90ab-cdef-1234-567890abcdef")
                .withClaim("role", "DOCENTE")
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
                .sign(wrongAlgorithm);

        boolean isValid = jwtUtils.validateToken(token);

        assertFalse(isValid, "El token con firma inválida debería ser rechazado");
    }

    /**
     * Test: Extraer claims de un token válido.
     */
    @Test
    void testExtractClaims_ValidToken_ReturnsClaims() {
        String userId = "12345678-90ab-cdef-1234-567890abcdef";
        String role = "DOCENTE";
        String email = "docente@universidad.com";

        String token = JWT.create()
                .withSubject(userId)
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withClaim("email", email)
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
                .sign(algorithm);

        Map<String, String> claims = jwtUtils.extractClaims(token);

        assertNotNull(claims, "Los claims no deberían ser null");
        assertEquals(userId, claims.get("userId"), "El userId debería coincidir");
        assertEquals(role, claims.get("role"), "El role debería coincidir");
        assertEquals(email, claims.get("email"), "El email debería coincidir");
    }

    /**
     * Test: Extraer claims de un token inválido debe retornar mapa vacío.
     */
    @Test
    void testExtractClaims_InvalidToken_ReturnsEmptyMap() {
        String invalidToken = "invalid.jwt.token";

        Map<String, String> claims = jwtUtils.extractClaims(invalidToken);

        assertNotNull(claims, "Los claims no deberían ser null");
        assertTrue(claims.isEmpty(), "Los claims deberían estar vacíos para token inválido");
    }

    /**
     * Test: Extraer token del header Authorization con formato correcto.
     */
    @Test
    void testExtractTokenFromHeader_ValidFormat_ReturnsToken() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";
        String authHeader = "Bearer " + token;

        String extractedToken = jwtUtils.extractTokenFromHeader(authHeader);

        assertEquals(token, extractedToken, "El token extraído debería coincidir");
    }

    /**
     * Test: Extraer token sin prefijo "Bearer " debe retornar null.
     */
    @Test
    void testExtractTokenFromHeader_MissingBearer_ReturnsNull() {
        String authHeader = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";

        String extractedToken = jwtUtils.extractTokenFromHeader(authHeader);

        assertNull(extractedToken, "Debería retornar null si falta el prefijo Bearer");
    }

    /**
     * Test: Extraer token de header null debe retornar null.
     */
    @Test
    void testExtractTokenFromHeader_NullHeader_ReturnsNull() {
        String extractedToken = jwtUtils.extractTokenFromHeader(null);

        assertNull(extractedToken, "Debería retornar null si el header es null");
    }

    /**
     * Test: Extraer userId de token válido.
     */
    @Test
    void testGetUserId_ValidToken_ReturnsUserId() {
        String userId = "12345678-90ab-cdef-1234-567890abcdef";

        String token = JWT.create()
                .withSubject(userId)
                .withClaim("userId", userId)
                .withClaim("role", "DOCENTE")
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
                .sign(algorithm);

        String extractedUserId = jwtUtils.getUserId(token);

        assertEquals(userId, extractedUserId, "El userId extraído debería coincidir");
    }

    /**
     * Test: Extraer role de token válido.
     */
    @Test
    void testGetRole_ValidToken_ReturnsRole() {
        String role = "DOCENTE";

        String token = JWT.create()
                .withSubject("test-user")
                .withClaim("userId", "12345678-90ab-cdef-1234-567890abcdef")
                .withClaim("role", role)
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
                .sign(algorithm);

        String extractedRole = jwtUtils.getRole(token);

        assertEquals(role, extractedRole, "El role extraído debería coincidir");
    }

    /**
     * Test: Extraer email de token válido.
     */
    @Test
    void testGetEmail_ValidToken_ReturnsEmail() {
        String email = "docente@universidad.com";

        String token = JWT.create()
                .withSubject("test-user")
                .withClaim("userId", "12345678-90ab-cdef-1234-567890abcdef")
                .withClaim("role", "DOCENTE")
                .withClaim("email", email)
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
                .sign(algorithm);

        String extractedEmail = jwtUtils.getEmail(token);

        assertEquals(email, extractedEmail, "El email extraído debería coincidir");
    }
}
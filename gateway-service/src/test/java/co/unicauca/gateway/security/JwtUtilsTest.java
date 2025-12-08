package co.unicauca.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        secretKey = "test-secret-key-for-jwt-validation-minimum-256-bits";
        signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // Crear instancia de JwtUtils e inyectar el secret usando ReflectionTestUtils
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secretKey);
    }

    /**
     * Test: Validar token válido debe retornar true.
     */
    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Crear un token válido con expiración futura
        String token = Jwts.builder()
                .subject("test@universidad.com")
                .claim("userId", 123)
                .claim("rol", "DOCENTE")
                .claim("programa", "INGENIERIA_SISTEMAS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // +24h
                .signWith(signingKey)
                .compact();

        boolean isValid = jwtUtils.validateToken(token);

        assertTrue(isValid, "El token válido debería ser validado correctamente");
    }

    /**
     * Test: Validar token expirado debe retornar false.
     */
    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Crear un token expirado
        String token = Jwts.builder()
                .subject("test@universidad.com")
                .claim("userId", 123)
                .claim("rol", "DOCENTE")
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Expirado hace 1 segundo
                .signWith(signingKey)
                .compact();

        boolean isValid = jwtUtils.validateToken(token);

        assertFalse(isValid, "El token expirado debería ser rechazado");
    }

    /**
     * Test: Validar token con firma inválida debe retornar false.
     */
    @Test
    void testValidateToken_InvalidSignature_ReturnsFalse() {
        // Crear un token con una clave diferente
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong-secret-key-that-is-long-enough-for-hmac-sha256".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("test@universidad.com")
                .claim("userId", 123)
                .claim("rol", "DOCENTE")
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(wrongKey)
                .compact();

        boolean isValid = jwtUtils.validateToken(token);

        assertFalse(isValid, "El token con firma inválida debería ser rechazado");
    }

    /**
     * Test: Extraer claims de un token válido.
     */
    @Test
    void testExtractClaims_ValidToken_ReturnsClaims() {
        String email = "docente@universidad.com";
        Integer userId = 123;
        String rol = "DOCENTE";
        String programa = "INGENIERIA_SISTEMAS";

        String token = Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("rol", rol)
                .claim("programa", programa)
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(signingKey)
                .compact();

        Map<String, String> claims = jwtUtils.extractClaims(token);

        assertNotNull(claims, "Los claims no deberían ser null");
        assertEquals(String.valueOf(userId), claims.get("userId"), "El userId debería coincidir");
        assertEquals(rol, claims.get("role"), "El role debería coincidir");
        assertEquals(email, claims.get("email"), "El email debería coincidir");
        assertEquals(programa, claims.get("programa"), "El programa debería coincidir");
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
        Integer userId = 123;

        String token = Jwts.builder()
                .subject("test@universidad.com")
                .claim("userId", userId)
                .claim("rol", "DOCENTE")
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(signingKey)
                .compact();

        String extractedUserId = jwtUtils.getUserId(token);

        assertEquals(String.valueOf(userId), extractedUserId, "El userId extraído debería coincidir");
    }

    /**
     * Test: Extraer role de token válido.
     */
    @Test
    void testGetRole_ValidToken_ReturnsRole() {
        String rol = "DOCENTE";

        String token = Jwts.builder()
                .subject("test@universidad.com")
                .claim("userId", 123)
                .claim("rol", rol)
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(signingKey)
                .compact();

        String extractedRole = jwtUtils.getRole(token);

        assertEquals(rol, extractedRole, "El role extraído debería coincidir");
    }

    /**
     * Test: Extraer email de token válido.
     */
    @Test
    void testGetEmail_ValidToken_ReturnsEmail() {
        String email = "test@universidad.com";

        String token = Jwts.builder()
                .subject(email)
                .claim("userId", 123)
                .claim("rol", "DOCENTE")
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(signingKey)
                .compact();

        String extractedEmail = jwtUtils.getEmail(token);

        assertEquals(email, extractedEmail, "El email extraído debería coincidir");
    }
}
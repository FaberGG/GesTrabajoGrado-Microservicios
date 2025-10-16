package com.unicauca.identity.security;

import com.unicauca.identity.entity.User;
import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;
import com.unicauca.identity.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;
    private String testSecret = "testSecretKeyThatIsLongEnoughToBeValidForHs256Algorithm";
    private long testExpiration = 3600000; // 1 hora en milisegundos

    @BeforeEach
    void setUp() {
        // Crear y configurar el proveedor de tokens
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", testExpiration);

        // Configurar usuario de prueba
        testUser = User.builder()
                .id(1L)
                .nombres("Juan")
                .apellidos("Perez")
                .email("jperez@unicauca.edu.co")
                .programa(Programa.INGENIERIA_DE_SISTEMAS)
                .rol(Rol.ESTUDIANTE)
                .passwordHash("hashedPassword")
                .celular("3201234567")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Generar token
        String token = jwtTokenProvider.generateToken(testUser);

        // Verificaciones
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void getUserEmailFromToken_ShouldReturnCorrectEmail() {
        // Generar token
        String token = jwtTokenProvider.generateToken(testUser);

        // Verificar que el email extraído es correcto
        String email = jwtTokenProvider.getUserEmailFromToken(token);
        assertEquals(testUser.getEmail(), email);
    }

    @Test
    void getUserIdFromToken_ShouldReturnCorrectUserId() {
        // Generar token
        String token = jwtTokenProvider.generateToken(testUser);

        // Verificar que el ID de usuario extraído es correcto
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(testUser.getId(), userId);
    }

    @Test
    void validateToken_ShouldThrowException_WhenTokenIsInvalid() {
        // Token inválido
        String invalidToken = "invalid.token.here";

        // Verificar que se lanza excepción
        assertThrows(InvalidTokenException.class, () -> {
            jwtTokenProvider.validateToken(invalidToken);
        });
    }

    @Test
    void getAllClaimsFromToken_ShouldReturnAllClaims() {
        // Generar token
        String token = jwtTokenProvider.generateToken(testUser);

        // Obtener claims
        Claims claims = jwtTokenProvider.getAllClaimsFromToken(token);

        // Verificaciones
        assertNotNull(claims);
        assertEquals(testUser.getEmail(), claims.getSubject());
        assertEquals(testUser.getId().toString(), claims.get("userId").toString());
        assertEquals(testUser.getRol().toString(), claims.get("rol"));
        assertEquals(testUser.getPrograma().toString(), claims.get("programa"));
    }
}

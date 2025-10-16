package com.unicauca.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicauca.identity.dto.request.LoginRequest;
import com.unicauca.identity.dto.request.RegisterRequest;
import com.unicauca.identity.dto.request.VerifyTokenRequest;
import com.unicauca.identity.dto.response.ApiResponse;
import com.unicauca.identity.dto.response.LoginResponse;
import com.unicauca.identity.dto.response.RolesResponse;
import com.unicauca.identity.dto.response.TokenVerificationResponse;
import com.unicauca.identity.dto.response.UserResponse;
import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;
import com.unicauca.identity.exception.EmailAlreadyExistsException;
import com.unicauca.identity.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        // Configurar RegisterRequest para pruebas
        registerRequest = RegisterRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .celular("3201234567")
                .programa(Programa.INGENIERIA_DE_SISTEMAS)
                .rol(Rol.ESTUDIANTE)
                .email("jperez@unicauca.edu.co")
                .password("Pass123!")
                .build();

        // Configurar LoginRequest para pruebas
        loginRequest = LoginRequest.builder()
                .email("jperez@unicauca.edu.co")
                .password("Pass123!")
                .build();

        // Configurar UserResponse para pruebas
        userResponse = UserResponse.builder()
                .id(1L)
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .celular("3201234567")
                .programa(Programa.INGENIERIA_DE_SISTEMAS)
                .rol(Rol.ESTUDIANTE)
                .email("jperez@unicauca.edu.co")
                .build();

        // Configurar LoginResponse para pruebas
        loginResponse = LoginResponse.builder()
                .user(userResponse)
                .token("jwt.token.here")
                .build();
    }

    @Test
    void register_ShouldReturnCreated_WhenDataValid() throws Exception {
        // Given
        given(authService.register(any(RegisterRequest.class))).willReturn(userResponse);

        // When
        ResultActions response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Usuario registrado exitosamente")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.email", is("jperez@unicauca.edu.co")));
    }

    @Test
    void register_ShouldReturnConflict_WhenEmailExists() throws Exception {
        // Given
        willThrow(new EmailAlreadyExistsException()).given(authService).register(any(RegisterRequest.class));

        // When
        ResultActions response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("El email ya está registrado")));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsValid() throws Exception {
        // Given
        given(authService.login(any(LoginRequest.class))).willReturn(loginResponse);

        // When
        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Login exitoso")))
                .andExpect(jsonPath("$.data.user.email", is("jperez@unicauca.edu.co")))
                .andExpect(jsonPath("$.data.token", is("jwt.token.here")));
    }

    @Test
    @WithMockUser(username = "jperez@unicauca.edu.co", roles = "ESTUDIANTE")
    void getProfile_ShouldReturnUserProfile_WhenAuthenticated() throws Exception {
        // Given
        given(authService.getUserIdByEmail(anyString())).willReturn(1L);
        given(authService.getProfile(anyLong())).willReturn(userResponse);

        // When
        ResultActions response = mockMvc.perform(get("/api/auth/profile")
                .with(SecurityMockMvcRequestPostProcessors.user(
                        new User("jperez@unicauca.edu.co", "password",
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))))));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.email", is("jperez@unicauca.edu.co")));
    }

    @Test
    @WithMockUser
    void getRoles_ShouldReturnRolesAndPrograms_WhenAuthenticated() throws Exception {
        // Given
        RolesResponse rolesResponse = RolesResponse.builder()
                .roles(Arrays.asList(Rol.values()))
                .programas(Arrays.asList(Programa.values()))
                .build();

        given(authService.getRolesAndPrograms()).willReturn(rolesResponse);

        // When
        ResultActions response = mockMvc.perform(get("/api/auth/roles"));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.programas").isArray());
    }

    @Test
    void verifyToken_ShouldReturnValidToken_WhenTokenIsValid() throws Exception {
        // Given
        VerifyTokenRequest tokenRequest = new VerifyTokenRequest("valid.jwt.token");
        TokenVerificationResponse tokenResponse = TokenVerificationResponse.valid(
                TokenVerificationResponse.TokenData.builder()
                        .userId(1L)
                        .email("jperez@unicauca.edu.co")
                        .rol(Rol.ESTUDIANTE)
                        .programa(Programa.INGENIERIA_DE_SISTEMAS)
                        .build()
        );

        given(authService.verifyToken(any(VerifyTokenRequest.class))).willReturn(tokenResponse);

        // When
        ResultActions response = mockMvc.perform(post("/api/auth/verify-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.data.userId", is(1)))
                .andExpect(jsonPath("$.data.email", is("jperez@unicauca.edu.co")));
    }

    @Test
    void verifyToken_ShouldReturnInvalidToken_WhenTokenIsInvalid() throws Exception {
        // Given
        VerifyTokenRequest tokenRequest = new VerifyTokenRequest("invalid.jwt.token");
        TokenVerificationResponse tokenResponse = TokenVerificationResponse.invalid("Token inválido o expirado");

        given(authService.verifyToken(any(VerifyTokenRequest.class))).willReturn(tokenResponse);

        // When
        ResultActions response = mockMvc.perform(post("/api/auth/verify-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)));

        // Then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.valid", is(false)))
                .andExpect(jsonPath("$.message", is("Token inválido o expirado")));
    }
}

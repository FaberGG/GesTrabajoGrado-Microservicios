package com.unicauca.identity.dto.request;

import com.unicauca.identity.validation.InstitutionalEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de login como Record (Java 21)
 */
public record LoginRequest(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @InstitutionalEmail
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {
    public static Builder builder() {
        return new Builder();
    }

    // Builder para mantener compatibilidad con código existente
    public static class Builder {
        private String email;
        private String password;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public LoginRequest build() {
            return new LoginRequest(email, password);
        }
    }
}

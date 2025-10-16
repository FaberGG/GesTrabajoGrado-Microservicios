package com.unicauca.identity.dto.response;

/**
 * DTO para la respuesta de login exitoso como Record (Java 21)
 */
public record LoginResponse(
    UserResponse user,
    String token
) {
    // Builder para mantener compatibilidad con código existente
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UserResponse user;
        private String token;

        public Builder user(UserResponse user) {
            this.user = user;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public LoginResponse build() {
            return new LoginResponse(user, token);
        }
    }
}

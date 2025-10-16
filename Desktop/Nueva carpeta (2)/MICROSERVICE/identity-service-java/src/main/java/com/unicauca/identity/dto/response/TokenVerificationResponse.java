package com.unicauca.identity.dto.response;

import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;

/**
 * DTO para la respuesta de verificación de token
 */
public class TokenVerificationResponse {

    private boolean success;
    private boolean valid;
    private String message;
    private TokenData data;

    // Constructores
    public TokenVerificationResponse() {
    }

    public TokenVerificationResponse(boolean success, boolean valid, String message, TokenData data) {
        this.success = success;
        this.valid = valid;
        this.message = message;
        this.data = data;
    }

    // Getters y setters manuales
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TokenData getData() {
        return data;
    }

    public void setData(TokenData data) {
        this.data = data;
    }

    // Implementación manual del patrón builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final TokenVerificationResponse response = new TokenVerificationResponse();

        public Builder success(boolean success) {
            response.setSuccess(success);
            return this;
        }

        public Builder valid(boolean valid) {
            response.setValid(valid);
            return this;
        }

        public Builder message(String message) {
            response.setMessage(message);
            return this;
        }

        public Builder data(TokenData data) {
            response.setData(data);
            return this;
        }

        public TokenVerificationResponse build() {
            return response;
        }
    }

    public static class TokenData {
        private Long userId;
        private String email;
        private Rol rol;
        private Programa programa;

        // Constructores
        public TokenData() {
        }

        public TokenData(Long userId, String email, Rol rol, Programa programa) {
            this.userId = userId;
            this.email = email;
            this.rol = rol;
            this.programa = programa;
        }

        // Getters y setters manuales
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Rol getRol() {
            return rol;
        }

        public void setRol(Rol rol) {
            this.rol = rol;
        }

        public Programa getPrograma() {
            return programa;
        }

        public void setPrograma(Programa programa) {
            this.programa = programa;
        }

        // Implementación manual del patrón builder
        public static TokenDataBuilder builder() {
            return new TokenDataBuilder();
        }

        public static class TokenDataBuilder {
            private final TokenData data = new TokenData();

            public TokenDataBuilder userId(Long userId) {
                data.setUserId(userId);
                return this;
            }

            public TokenDataBuilder email(String email) {
                data.setEmail(email);
                return this;
            }

            public TokenDataBuilder rol(Rol rol) {
                data.setRol(rol);
                return this;
            }

            public TokenDataBuilder programa(Programa programa) {
                data.setPrograma(programa);
                return this;
            }

            public TokenData build() {
                return data;
            }
        }
    }

    public static TokenVerificationResponse valid(TokenData data) {
        return TokenVerificationResponse.builder()
                .success(true)
                .valid(true)
                .data(data)
                .build();
    }

    public static TokenVerificationResponse invalid(String message) {
        return TokenVerificationResponse.builder()
                .success(false)
                .valid(false)
                .message(message)
                .build();
    }
}

package com.unicauca.identity.dto.response;

import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta con datos del usuario como Record (Java 21)
 */
public record UserResponse(
    Long id,
    String nombres,
    String apellidos,
    String celular,
    Programa programa,
    Rol rol,
    String email,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    // Builder para mantener compatibilidad con código existente
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String nombres;
        private String apellidos;
        private String celular;
        private Programa programa;
        private Rol rol;
        private String email;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder nombres(String nombres) {
            this.nombres = nombres;
            return this;
        }

        public Builder apellidos(String apellidos) {
            this.apellidos = apellidos;
            return this;
        }

        public Builder celular(String celular) {
            this.celular = celular;
            return this;
        }

        public Builder programa(Programa programa) {
            this.programa = programa;
            return this;
        }

        public Builder rol(Rol rol) {
            this.rol = rol;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserResponse build() {
            return new UserResponse(id, nombres, apellidos, celular, programa, rol, email, createdAt, updatedAt);
        }
    }
}

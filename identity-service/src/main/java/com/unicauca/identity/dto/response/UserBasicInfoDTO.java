package com.unicauca.identity.dto.response;

import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;

/**
 * DTO simplificado con información básica de usuario
 * Para comunicación service-to-service
 */
public record UserBasicInfoDTO(
        Long id,
        String nombres,
        String apellidos,
        String email,
        Rol rol,
        Programa programa
) {
    // Builder para mantener compatibilidad
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Obtiene el nombre completo del usuario
     */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public static class Builder {
        private Long id;
        private String nombres;
        private String apellidos;
        private String email;
        private Rol rol;
        private Programa programa;

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

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder rol(Rol rol) {
            this.rol = rol;
            return this;
        }

        public Builder programa(Programa programa) {
            this.programa = programa;
            return this;
        }

        public UserBasicInfoDTO build() {
            return new UserBasicInfoDTO(id, nombres, apellidos, email, rol, programa);
        }
    }
}
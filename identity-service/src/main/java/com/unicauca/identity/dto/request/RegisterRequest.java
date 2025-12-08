package com.unicauca.identity.dto.request;

import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;
import com.unicauca.identity.validation.InstitutionalEmail;
import com.unicauca.identity.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para la solicitud de registro de un nuevo usuario como Record (Java 21)
 */
public record RegisterRequest(
    @NotBlank(message = "Los nombres son obligatorios")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{2,}$",
             message = "Nombres debe contener solo letras y tener al menos 2 caracteres")
    String nombres,

    @NotBlank(message = "Los apellidos son obligatorios")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{2,}$",
             message = "Apellidos debe contener solo letras y tener al menos 2 caracteres")
    String apellidos,

    @Pattern(regexp = "^[0-9]{10}$",
             message = "Celular debe tener 10 dígitos numéricos")
    String celular,

    @NotNull(message = "El programa es obligatorio")
    Programa programa,

    @NotNull(message = "El rol es obligatorio")
    Rol rol,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @InstitutionalEmail
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @StrongPassword
    String password
) {
    // Builder para mantener compatibilidad con código existente
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String nombres;
        private String apellidos;
        private String celular;
        private Programa programa;
        private Rol rol;
        private String email;
        private String password;

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

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public RegisterRequest build() {
            return new RegisterRequest(nombres, apellidos, celular, programa, rol, email, password);
        }
    }
}

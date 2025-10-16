package com.unicauca.identity.dto.response;

import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la respuesta con roles y programas disponibles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolesResponse {

    private List<Rol> roles;
    private List<Programa> programas;

    // Métodos getter y setter manuales
    public List<Rol> getRoles() {
        return roles;
    }

    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }

    public List<Programa> getProgramas() {
        return programas;
    }

    public void setProgramas(List<Programa> programas) {
        this.programas = programas;
    }

    // Implementación manual de patrón builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RolesResponse response = new RolesResponse();

        public Builder roles(List<Rol> roles) {
            response.setRoles(roles);
            return this;
        }

        public Builder programas(List<Programa> programas) {
            response.setProgramas(programas);
            return this;
        }

        public RolesResponse build() {
            return response;
        }
    }
}

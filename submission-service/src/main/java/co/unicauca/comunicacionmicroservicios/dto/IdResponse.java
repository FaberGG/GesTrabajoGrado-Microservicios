package co.unicauca.comunicacionmicroservicios.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Respuesta simple con ID de la entidad creada. */
@Schema(description = "Respuesta con ID del recurso creado")
public class IdResponse {

    @Schema(description = "ID del recurso creado", example = "1")
    private Long id;

    public IdResponse() {}

    public IdResponse(Long id) {
        this.id = id;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}


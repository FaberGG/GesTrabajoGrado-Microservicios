package co.unicauca.comunicacionmicroservicios.dto;

/** Respuesta simple con ID de la entidad creada. */
public class IdResponse {
    private Long id;

    public IdResponse() {}

    public IdResponse(Long id) {
        this.id = id;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}


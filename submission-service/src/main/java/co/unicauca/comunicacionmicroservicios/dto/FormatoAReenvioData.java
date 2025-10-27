package co.unicauca.comunicacionmicroservicios.dto;

import jakarta.validation.constraints.Size;

/** Datos opcionales para un reenvío del Formato A (RF4). */
public class FormatoAReenvioData {
    @Size(max = 1000)
    private String comentarioDocente;

    public String getComentarioDocente() { return comentarioDocente; }
    public void setComentarioDocente(String comentarioDocente) { this.comentarioDocente = comentarioDocente; }
}

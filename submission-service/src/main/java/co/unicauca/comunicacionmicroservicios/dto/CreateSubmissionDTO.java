package co.unicauca.comunicacionmicroservicios.dto;

import co.unicauca.comunicacionmicroservicios.domain.model.enumModalidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para crear un nuevo proyecto submission
 */
public class CreateSubmissionDTO {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "La modalidad es obligatoria")
    private enumModalidad modalidad;

    @NotNull(message = "El ID del docente director es obligatorio")
    private Long docenteDirectorId;

    private Long docenteCodirectorId;
    private Long estudianteId;

    private String objetivoGeneral;
    private String objetivosEspecificos;

    private String rutaFormatoA;
    private String rutaCarta;

    // Getters y Setters

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public enumModalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(enumModalidad modalidad) {
        this.modalidad = modalidad;
    }

    public Long getDocenteDirectorId() {
        return docenteDirectorId;
    }

    public void setDocenteDirectorId(Long docenteDirectorId) {
        this.docenteDirectorId = docenteDirectorId;
    }

    public Long getDocenteCodirectorId() {
        return docenteCodirectorId;
    }

    public void setDocenteCodirectorId(Long docenteCodirectorId) {
        this.docenteCodirectorId = docenteCodirectorId;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getObjetivoGeneral() {
        return objetivoGeneral;
    }

    public void setObjetivoGeneral(String objetivoGeneral) {
        this.objetivoGeneral = objetivoGeneral;
    }

    public String getObjetivosEspecificos() {
        return objetivosEspecificos;
    }

    public void setObjetivosEspecificos(String objetivosEspecificos) {
        this.objetivosEspecificos = objetivosEspecificos;
    }

    public String getRutaFormatoA() {
        return rutaFormatoA;
    }

    public void setRutaFormatoA(String rutaFormatoA) {
        this.rutaFormatoA = rutaFormatoA;
    }

    public String getRutaCarta() {
        return rutaCarta;
    }

    public void setRutaCarta(String rutaCarta) {
        this.rutaCarta = rutaCarta;
    }
}


package co.unicauca.gestiontrabajogrado.dto;

import co.unicauca.gestiontrabajogrado.domain.model.enumModalidad;
import co.unicauca.gestiontrabajogrado.domain.model.enumProgram;

/**
 * DTO para solicitud de creación de proyecto de grado
 */
public class ProyectoGradoRequestDTO {
    private String titulo;
    private String descripcion;
    private enumModalidad modalidad;
    private enumProgram programa;
    private Long docenteId;
    private String rutaArchivo;
    private String objetivoGeneral;
    private String objetivosEspecificos;

    public ProyectoGradoRequestDTO() {
    }

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

    public enumProgram getPrograma() {
        return programa;
    }

    public void setPrograma(enumProgram programa) {
        this.programa = programa;
    }

    public Long getDocenteId() {
        return docenteId;
    }

    public void setDocenteId(Long docenteId) {
        this.docenteId = docenteId;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
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
}


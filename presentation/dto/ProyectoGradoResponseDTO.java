package co.unicauca.gestiontrabajogrado.dto;

import co.unicauca.gestiontrabajogrado.domain.model.enumEstadoProyecto;
import co.unicauca.gestiontrabajogrado.domain.model.enumModalidad;

import java.util.Date;
import java.util.List;

/**
 * DTO para respuesta de proyecto de grado
 */
public class ProyectoGradoResponseDTO {
    public Long id;
    public String titulo;
    public String descripcion;
    public enumModalidad modalidad;
    public enumEstadoProyecto estado;
    public Date fechaCreacion;
    public String mensaje;
    public List<FormatoADetalleDTO> formatosA;
    public Integer numeroIntentos;
    public Long directorId;
    public Long codirectorId;
    public Long estudiante1Id;
    public Long estudiante2Id;
    public String objetivoGeneral;
    public String objetivosEspecificos;

    public ProyectoGradoResponseDTO() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public enumEstadoProyecto getEstado() {
        return estado;
    }

    public void setEstado(enumEstadoProyecto estado) {
        this.estado = estado;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<FormatoADetalleDTO> getFormatosA() {
        return formatosA;
    }

    public void setFormatosA(List<FormatoADetalleDTO> formatosA) {
        this.formatosA = formatosA;
    }

    public Integer getNumeroIntentos() {
        return numeroIntentos;
    }

    public void setNumeroIntentos(Integer numeroIntentos) {
        this.numeroIntentos = numeroIntentos;
    }

    public Long getDirectorId() {
        return directorId;
    }

    public void setDirectorId(Long directorId) {
        this.directorId = directorId;
    }

    public Long getCodirectorId() {
        return codirectorId;
    }

    public void setCodirectorId(Long codirectorId) {
        this.codirectorId = codirectorId;
    }

    public Long getEstudiante1Id() {
        return estudiante1Id;
    }

    public void setEstudiante1Id(Long estudiante1Id) {
        this.estudiante1Id = estudiante1Id;
    }

    public Long getEstudiante2Id() {
        return estudiante2Id;
    }

    public void setEstudiante2Id(Long estudiante2Id) {
        this.estudiante2Id = estudiante2Id;
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


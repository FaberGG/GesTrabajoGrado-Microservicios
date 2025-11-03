package co.unicauca.gestiontrabajogrado.dto;

import co.unicauca.gestiontrabajogrado.domain.model.enumEstadoFormato;

import java.util.Date;

/**
 * DTO para detalles de Formato A
 */
public class FormatoADetalleDTO {
    public Long id;
    public String titulo;
    public String descripcion;
    public Date fechaEnvio;
    public enumEstadoFormato estado;
    public String comentarios;
    public String rutaArchivo;
    public String observaciones;

    public FormatoADetalleDTO() {
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

    public Date getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Date fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public enumEstadoFormato getEstado() {
        return estado;
    }

    public void setEstado(enumEstadoFormato estado) {
        this.estado = estado;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}


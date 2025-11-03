package co.unicauca.gestiontrabajogrado.presentation.dashboard.coordinadorview;

import co.unicauca.gestiontrabajogrado.domain.model.enumEstadoFormato;
import co.unicauca.gestiontrabajogrado.domain.model.enumEstadoProyecto;

public class PropuestaRow {
    private Integer id;
    private Integer formatoId;
    private String titulo;
    private enumEstadoProyecto estadoProyecto;
    private enumEstadoFormato estadoFormato;
    private Integer version;

    public PropuestaRow() {
    }

    public PropuestaRow(Integer id, Integer formatoId, String titulo,
                       enumEstadoProyecto estadoProyecto, enumEstadoFormato estadoFormato, Integer version) {
        this.id = id;
        this.formatoId = formatoId;
        this.titulo = titulo;
        this.estadoProyecto = estadoProyecto;
        this.estadoFormato = estadoFormato;
        this.version = version;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFormatoId() {
        return formatoId;
    }

    public void setFormatoId(Integer formatoId) {
        this.formatoId = formatoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public enumEstadoProyecto getEstadoProyecto() {
        return estadoProyecto;
    }

    public void setEstadoProyecto(enumEstadoProyecto estadoProyecto) {
        this.estadoProyecto = estadoProyecto;
    }

    public enumEstadoFormato getEstadoFormato() {
        return estadoFormato;
    }

    public void setEstadoFormato(enumEstadoFormato estadoFormato) {
        this.estadoFormato = estadoFormato;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}


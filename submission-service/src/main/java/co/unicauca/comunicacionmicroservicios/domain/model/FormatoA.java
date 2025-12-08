/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.comunicacionmicroservicios.domain.model;

/**
 *
 * @author USUARIO
 */

import jakarta.persistence.*;
import java.time.LocalDateTime;
import co.unicauca.comunicacionmicroservicios.domain.model.*;

@Entity
@Table(name = "formatos_a")
public class FormatoA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // FK a proyecto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private ProyectoGrado proyecto;

    private Integer numeroIntento;
    private String rutaArchivo;
    private String nombreArchivo;
    private String rutaCartaAceptacion;
    private String nombreCartaAceptacion;
    private LocalDateTime fechaCarga = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private enumEstadoFormato estado = enumEstadoFormato.PENDIENTE;

    @Column(columnDefinition = "text")
    private String observaciones;

    private Integer evaluadoPor;
    private LocalDateTime fechaEvaluacion;

    public void aprobar(Integer evaluadorId, String observaciones) {
        this.estado = enumEstadoFormato.APROBADO;
        this.evaluadoPor = evaluadorId;
        this.observaciones = observaciones;
        this.fechaEvaluacion = LocalDateTime.now();
    }

    public void rechazar(Integer evaluadorId, String observaciones) {
        this.estado = enumEstadoFormato.RECHAZADO;
        this.evaluadoPor = evaluadorId;
        this.observaciones = observaciones;
        this.fechaEvaluacion = LocalDateTime.now();
    }

    public boolean esUltimoIntento() {
        return this.numeroIntento != null && this.numeroIntento >= 3;
    }

    public boolean tieneCartaAceptacion() {
        return rutaCartaAceptacion != null && !rutaCartaAceptacion.trim().isEmpty();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ProyectoGrado getProyecto() {
        return proyecto;
    }

    public void setProyecto(ProyectoGrado proyecto) {
        this.proyecto = proyecto;
    }

    public Integer getNumeroIntento() {
        return numeroIntento;
    }

    public void setNumeroIntento(Integer numeroIntento) {
        this.numeroIntento = numeroIntento;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getRutaCartaAceptacion() {
        return rutaCartaAceptacion;
    }

    public void setRutaCartaAceptacion(String rutaCartaAceptacion) {
        this.rutaCartaAceptacion = rutaCartaAceptacion;
    }

    public String getNombreCartaAceptacion() {
        return nombreCartaAceptacion;
    }

    public void setNombreCartaAceptacion(String nombreCartaAceptacion) {
        this.nombreCartaAceptacion = nombreCartaAceptacion;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public enumEstadoFormato getEstado() {
        return estado;
    }

    public void setEstado(enumEstadoFormato estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getEvaluadoPor() {
        return evaluadoPor;
    }

    public void setEvaluadoPor(Integer evaluadoPor) {
        this.evaluadoPor = evaluadoPor;
    }

    public LocalDateTime getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(LocalDateTime fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    
}

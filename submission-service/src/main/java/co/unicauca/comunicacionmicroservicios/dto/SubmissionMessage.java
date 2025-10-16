/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.comunicacionmicroservicios.dto;

/**
 *
 * @author USUARIO
 */

import java.util.List;

public class SubmissionMessage {
    private Integer proyectoId;
    private String titulo;
    private List<String> autoresEmails;

    public SubmissionMessage() {
    }

    public SubmissionMessage(Integer proyectoId, String titulo, List<String> autoresEmails) {
        this.proyectoId = proyectoId;
        this.titulo = titulo;
        this.autoresEmails = autoresEmails;
    }

    public Integer getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(Integer proyectoId) {
        this.proyectoId = proyectoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<String> getAutoresEmails() {
        return autoresEmails;
    }

    public void setAutoresEmails(List<String> autoresEmails) {
        this.autoresEmails = autoresEmails;
    }
    
    
}
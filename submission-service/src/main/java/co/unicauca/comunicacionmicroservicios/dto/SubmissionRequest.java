/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.comunicacionmicroservicios.dto;

/**
 *
 * @author USUARIO
 */

import jakarta.validation.constraints.*;
import java.util.List;

public class SubmissionRequest {
    @NotBlank
    private String titulo;
    @NotBlank
    private String resumen; // lo mapeamos a objetivoGeneral u objetivo breve
    @NotEmpty
    private List<@Email String> autoresEmails;

    private Integer directorId;
    private Integer estudiante1Id;
    private Integer estudiante2Id;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public List<String> getAutoresEmails() {
        return autoresEmails;
    }

    public void setAutoresEmails(List<String> autoresEmails) {
        this.autoresEmails = autoresEmails;
    }

    public Integer getDirectorId() {
        return directorId;
    }

    public void setDirectorId(Integer directorId) {
        this.directorId = directorId;
    }

    public Integer getEstudiante1Id() {
        return estudiante1Id;
    }

    public void setEstudiante1Id(Integer estudiante1Id) {
        this.estudiante1Id = estudiante1Id;
    }

    public Integer getEstudiante2Id() {
        return estudiante2Id;
    }

    public void setEstudiante2Id(Integer estudiante2Id) {
        this.estudiante2Id = estudiante2Id;
    }

    
}


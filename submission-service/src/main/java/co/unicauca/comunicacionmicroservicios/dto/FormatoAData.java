package co.unicauca.comunicacionmicroservicios.dto;

import co.unicauca.comunicacionmicroservicios.domain.model.enumModalidad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Datos JSON para crear el Formato A inicial (RF2).
 * Los archivos van en otras partes del multipart: pdf (obligatorio), carta (si PRACTICA_PROFESIONAL).
 */
@Schema(description = "Datos para crear un Formato A inicial")
public class FormatoAData {

    @Schema(description = "Título del trabajo de grado", example = "Sistema de Gestión de Inventarios con IoT", required = true)
    @NotBlank
    @Size(max = 300)
    private String titulo;

    @Schema(description = "Modalidad del trabajo", example = "INVESTIGACION", required = true,
            allowableValues = {"INVESTIGACION", "PRACTICA_PROFESIONAL"})
    @NotNull
    private enumModalidad modalidad; // INVESTIGACION | PRACTICA_PROFESIONAL

    @Schema(description = "Objetivo general del trabajo", example = "Desarrollar un sistema IoT para gestión de inventarios", required = true)
    @NotBlank
    @Size(max = 1000)
    private String objetivoGeneral;

    @Schema(description = "Lista de objetivos específicos", required = true, minLength = 1)
    @NotEmpty
    private List<@NotBlank String> objetivosEspecificos;

    @Schema(description = "ID del docente director", example = "101", required = true)
    @NotNull
    private Integer directorId;

    @Schema(description = "ID del codirector (opcional)", example = "102", nullable = true)
    private Integer codirectorId; // opcional

    @Schema(description = "ID del primer estudiante", example = "201", required = true)
    @NotNull
    private Integer estudiante1Id;

    @Schema(description = "ID del segundo estudiante (opcional, solo para INVESTIGACION)", example = "202", nullable = true)
    private Integer estudiante2Id; // opcional

    // getters/setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public enumModalidad getModalidad() { return modalidad; }
    public void setModalidad(enumModalidad modalidad) { this.modalidad = modalidad; }

    public String getObjetivoGeneral() { return objetivoGeneral; }
    public void setObjetivoGeneral(String objetivoGeneral) { this.objetivoGeneral = objetivoGeneral; }

    public List<String> getObjetivosEspecificos() { return objetivosEspecificos; }
    public void setObjetivosEspecificos(List<String> objetivosEspecificos) { this.objetivosEspecificos = objetivosEspecificos; }

    public Integer getDirectorId() { return directorId; }
    public void setDirectorId(Integer directorId) { this.directorId = directorId; }

    public Integer getCodirectorId() { return codirectorId; }
    public void setCodirectorId(Integer codirectorId) { this.codirectorId = codirectorId; }

    public Integer getEstudiante1Id() { return estudiante1Id; }
    public void setEstudiante1Id(Integer estudiante1Id) { this.estudiante1Id = estudiante1Id; }

    public Integer getEstudiante2Id() { return estudiante2Id; }
    public void setEstudiante2Id(Integer estudiante2Id) { this.estudiante2Id = estudiante2Id; }
}

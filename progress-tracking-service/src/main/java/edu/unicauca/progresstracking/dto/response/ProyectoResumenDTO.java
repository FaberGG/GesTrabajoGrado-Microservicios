package edu.unicauca.progresstracking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resumen de un proyecto en listados
 * Usado en: GET /api/progress/proyectos/mis-proyectos
 * Usado en: GET /api/progress/proyectos/buscar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProyectoResumenDTO {

    private Long proyectoId;
    private String titulo;
    private String estadoActual;
    private String estadoLegible;
    private String fase;
    private String modalidad;
    private String programa;
    private LocalDateTime ultimaActualizacion;
    private String rol; // ROL del usuario en este proyecto (DIRECTOR, CODIRECTOR, etc.)
    private PersonaDTO director;
    private PersonaDTO codirector;
    private EstudiantesDTO estudiantes;
}


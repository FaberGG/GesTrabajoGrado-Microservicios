package edu.unicauca.progresstracking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de respuesta para el estado completo de un proyecto
 * Usado en: GET /api/progress/proyectos/{id}/estado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EstadoProyectoResponseDTO {

    private Long proyectoId;
    private String titulo;
    private String modalidad;
    private String programa;
    private String estadoActual;
    private String estadoLegible;
    private String fase;
    private LocalDateTime ultimaActualizacion;
    private String siguientePaso;

    private FormatoAInfoDTO formatoA;
    private AnteproyectoInfoDTO anteproyecto;
    private ParticipantesDTO participantes;
    private EstudiantesDTO estudiantes;

    private Boolean tieneProyecto;
    private Boolean error;
    private String mensaje;
    private Long estudianteId;
}


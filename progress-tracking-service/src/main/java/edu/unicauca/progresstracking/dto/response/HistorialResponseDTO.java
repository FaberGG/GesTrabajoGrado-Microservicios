package edu.unicauca.progresstracking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para el historial de eventos con paginación
 * Usado en: GET /api/progress/proyectos/{id}/historial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistorialResponseDTO {

    private Long proyectoId;
    private Long estudianteId;
    private String tituloProyecto;
    private String estadoActual;
    private String estadoLegible;
    private String fase;
    private EstudiantesDTO estudiantes;
    private List<HistorialEventoDTO> historial;
    private Integer paginaActual;
    private Integer tamanoPagina;
    private Long totalEventos;
    private Integer totalPaginas;

    private Boolean error;
    private String mensaje;
}


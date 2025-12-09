package edu.unicauca.progresstracking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para un evento del historial
 * Usado en: GET /api/progress/proyectos/{id}/historial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistorialEventoDTO {

    private Long eventoId;
    private Long proyectoId;
    private String tipoEvento;
    private LocalDateTime fecha;
    private String descripcion;
    private Integer version;
    private String resultado;
    private String observaciones;
    private PersonaDTO responsable;
}


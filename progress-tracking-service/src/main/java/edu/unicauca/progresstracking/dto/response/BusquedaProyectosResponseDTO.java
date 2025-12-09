package edu.unicauca.progresstracking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO de respuesta para búsqueda de proyectos
 * Usado en: GET /api/progress/proyectos/buscar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusquedaProyectosResponseDTO {

    private List<ProyectoResumenDTO> resultados;
    private Integer total;
    private Map<String, String> filtros;
}


package edu.unicauca.progresstracking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO con información del estado del Anteproyecto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnteproyectoInfoDTO {

    private String estado;
    private LocalDateTime fechaEnvio;
    private Boolean evaluadoresAsignados;
}


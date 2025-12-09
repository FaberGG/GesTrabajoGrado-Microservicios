package edu.unicauca.progresstracking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO con información del estado del Formato A
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormatoAInfoDTO {

    private String estado;
    private Integer versionActual;
    private Integer intentoActual;
    private Integer maxIntentos;
    private LocalDateTime fechaUltimoEnvio;
    private LocalDateTime fechaUltimaEvaluacion;
}


package edu.unicauca.progresstracking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para información de estudiantes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EstudiantesDTO {

    private EstudianteDTO estudiante1;
    private EstudianteDTO estudiante2;
}


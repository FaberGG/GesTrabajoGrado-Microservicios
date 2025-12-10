package edu.unicauca.progresstracking.messaging.events;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento base - todos los eventos heredan de este
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ProjectEvent implements Serializable {
    private Long proyectoId;
    private LocalDateTime timestamp;
    private Long usuarioResponsableId;
    private String usuarioResponsableNombre;
    private String usuarioResponsableRol;

    // Información de estudiantes (uno o dos)
    private List<EstudianteInfo> estudiantes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstudianteInfo {
        private Long id;
        private String nombre;
        private String email;
    }
}
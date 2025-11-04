package co.unicauca.comunicacionmicroservicios.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando se envía el anteproyecto (RF6)
 * Consumido por: Progress Tracking Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnteproyectoEnviadoEvent {

    /**
     * ID del proyecto en Submission Service
     */
    private Long proyectoId;

    /**
     * Descripción del evento
     */
    private String descripcion;

    /**
     * Timestamp del evento
     */
    private LocalDateTime timestamp;

    /**
     * ID del usuario responsable (docente director)
     */
    private Long usuarioResponsableId;

    /**
     * Nombre completo del usuario responsable
     */
    private String usuarioResponsableNombre;

    /**
     * Rol del usuario responsable
     */
    private String usuarioResponsableRol;
}


package co.unicauca.comunicacionmicroservicios.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando se envía el Formato A por primera vez (RF2)
 * Consumido por: Progress Tracking Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormatoAEnviadoEvent {

    /**
     * ID del proyecto en Submission Service
     */
    private Long proyectoId;

    /**
     * Título del proyecto
     */
    private String titulo;

    /**
     * Modalidad: INVESTIGACION | PRACTICA_PROFESIONAL
     */
    private String modalidad;

    /**
     * Programa académico del estudiante
     */
    private String programa;

    /**
     * Número de versión (siempre 1 en el primer envío)
     */
    private Integer version;

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
     * Rol del usuario responsable (siempre "DOCENTE" en RF2)
     */
    private String usuarioResponsableRol;
}


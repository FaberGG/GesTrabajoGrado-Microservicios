package co.unicauca.comunicacionmicroservicios.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando se envía el anteproyecto (RF6)
 * Consumido por: Progress Tracking Service
 *
 * ACTUALIZADO: Incluye información completa del proyecto según guía de integración
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

    // ✨ INFORMACIÓN COMPLETA DEL PROYECTO

    /**
     * ID del director del proyecto
     */
    private Long directorId;

    /**
     * Nombre completo del director
     */
    private String directorNombre;

    /**
     * ID del co-director (opcional)
     */
    private Long codirectorId;

    /**
     * Nombre completo del co-director (opcional)
     */
    private String codirectorNombre;

    /**
     * ID del estudiante 1
     */
    private Long estudiante1Id;

    /**
     * Nombre completo del estudiante 1
     */
    private String estudiante1Nombre;

    /**
     * ID del estudiante 2 (opcional)
     */
    private Long estudiante2Id;

    /**
     * Nombre completo del estudiante 2 (opcional)
     */
    private String estudiante2Nombre;
}


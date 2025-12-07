package co.unicauca.comunicacionmicroservicios.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando se reenvía el Formato A tras correcciones (RF4)
 * Consumido por: Progress Tracking Service
 *
 * ACTUALIZADO: Incluye información completa de director y estudiantes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormatoAReenviadoEvent {

    /**
     * ID del proyecto en Submission Service
     */
    private Long proyectoId;

    /**
     * Título del proyecto
     */
    private String titulo;

    /**
     * Número de versión (2 o 3 según intento)
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
     * Rol del usuario responsable
     */
    private String usuarioResponsableRol;

    // ========== INFORMACIÓN COMPLETA DEL PROYECTO ==========

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
     * Email del estudiante 1
     */
    private String estudiante1Email;

    /**
     * ID del estudiante 2 (opcional)
     */
    private Long estudiante2Id;

    /**
     * Nombre completo del estudiante 2 (opcional)
     */
    private String estudiante2Nombre;

    /**
     * Email del estudiante 2 (opcional)
     */
    private String estudiante2Email;
}


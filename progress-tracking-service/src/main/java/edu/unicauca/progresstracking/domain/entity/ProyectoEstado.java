package edu.unicauca.progresstracking.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Vista Materializada del Estado Actual de cada Proyecto
 *
 * Esta tabla se actualiza cada vez que llega un evento nuevo.
 * Permite consultas rápidas sin necesidad de reconstruir el estado
 * desde todo el historial de eventos.
 */
@Entity
@Table(name = "proyecto_estado", indexes = {
        @Index(name = "idx_estado_actual", columnList = "estado_actual"),
        @Index(name = "idx_fase", columnList = "fase"),
        @Index(name = "idx_director", columnList = "director_id"),
        @Index(name = "idx_estudiante1", columnList = "estudiante1_id"),
        @Index(name = "idx_estudiante2", columnList = "estudiante2_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProyectoEstado {

    @Id
    @Column(name = "proyecto_id")
    private Long proyectoId;

    // ========== INFORMACIÓN BÁSICA ==========
    @Column(length = 500)
    private String titulo;

    @Column(length = 50)
    private String modalidad; // INVESTIGACION, EMPRENDIMIENTO, etc.

    @Column(name = "programa", length = 100)
    private String programa; // INGENIERIA_SISTEMAS, etc.

    // ========== ESTADO Y FASE ==========
    @Column(name = "estado_actual", length = 100, nullable = false)
    private String estadoActual;

    @Column(length = 50)
    private String fase; // FORMATO_A, ANTEPROYECTO, DEFENSA

    // ========== FORMATO A ==========
    @Column(name = "formato_a_version")
    private Integer formatoAVersion = 0;

    @Column(name = "formato_a_intento_actual")
    private Integer formatoAIntentoActual = 0;

    @Column(name = "formato_a_max_intentos")
    private Integer formatoAMaxIntentos = 3;

    @Column(name = "formato_a_estado", length = 50)
    private String formatoAEstado;

    @Column(name = "formato_a_fecha_ultimo_envio")
    private LocalDateTime formatoAFechaUltimoEnvio;

    @Column(name = "formato_a_fecha_ultima_evaluacion")
    private LocalDateTime formatoAFechaUltimaEvaluacion;

    // ========== ANTEPROYECTO ==========
    @Column(name = "anteproyecto_estado", length = 50)
    private String anteproyectoEstado;

    @Column(name = "anteproyecto_fecha_envio")
    private LocalDateTime anteproyectoFechaEnvio;

    @Column(name = "anteproyecto_evaluadores_asignados")
    private Boolean anteproyectoEvaluadoresAsignados = false;

    // ========== PARTICIPANTES ==========
    @Column(name = "director_id")
    private Long directorId;

    @Column(name = "director_nombre", length = 200)
    private String directorNombre;

    @Column(name = "codirector_id")
    private Long codirectorId;

    @Column(name = "codirector_nombre", length = 200)
    private String codirectorNombre;

    // ========== ESTUDIANTES ==========
    @Column(name = "estudiante1_id")
    private Long estudiante1Id;

    @Column(name = "estudiante1_nombre", length = 200)
    private String estudiante1Nombre;

    @Column(name = "estudiante1_email", length = 200)
    private String estudiante1Email;

    @Column(name = "estudiante2_id")
    private Long estudiante2Id;

    @Column(name = "estudiante2_nombre", length = 200)
    private String estudiante2Nombre;

    @Column(name = "estudiante2_email", length = 200)
    private String estudiante2Email;

    // ========== AUDITORÍA ==========
    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.ultimaActualizacion = LocalDateTime.now();
    }
}
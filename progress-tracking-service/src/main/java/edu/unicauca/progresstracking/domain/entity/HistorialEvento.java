package edu.unicauca.progresstracking.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Historial completo de eventos del proyecto
 *
 * Cada evento es INMUTABLE - nunca se modifica ni elimina.
 * Esto garantiza la trazabilidad completa del proyecto.
 *
 * Tabla en BD: historial_eventos
 */
@Entity
@Table(name = "historial_eventos", indexes = {
        @Index(name = "idx_proyecto", columnList = "proyecto_id"),
        @Index(name = "idx_fecha", columnList = "fecha"),
        @Index(name = "idx_tipo_evento", columnList = "tipo_evento"),
        @Index(name = "idx_proyecto_fecha", columnList = "proyecto_id, fecha")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEvento {

    // ========== ID AUTOGENERADO ==========
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evento_id")
    private Long eventoId;

    // ========== PROYECTO AL QUE PERTENECE ==========
    @Column(name = "proyecto_id", nullable = false)
    private Long proyectoId;

    // ========== TIPO Y DESCRIPCIÓN DEL EVENTO ==========
    @Column(name = "tipo_evento", length = 100, nullable = false)
    private String tipoEvento;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // ========== INFORMACIÓN ESPECÍFICA DEL EVENTO ==========

    /**
     * Versión del documento (para FORMATO_A_ENVIADO, FORMATO_A_REENVIADO)
     * Ejemplo: 1, 2, 3
     */
    @Column(name = "version")
    private Integer version;

    /**
     * Resultado de una evaluación
     * Valores: "APROBADO", "RECHAZADO"
     */
    @Column(name = "resultado", length = 50)
    private String resultado;

    /**
     * Observaciones del evaluador
     */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // ========== USUARIO RESPONSABLE ==========

    @Column(name = "usuario_responsable_id")
    private Long usuarioResponsableId;

    @Column(name = "usuario_responsable_nombre", length = 200)
    private String usuarioResponsableNombre;

    @Column(name = "usuario_responsable_rol", length = 50)
    private String usuarioResponsableRol;

    // ========== METADATA ADICIONAL (JSON) ==========

    /**
     * Campo para guardar información adicional en formato JSON
     * Ejemplo: evaluadores asignados, archivos adjuntos, etc.
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // ========== AUDITORÍA ==========

    /**
     * Método que se ejecuta automáticamente antes de guardar
     * Asegura que siempre haya una fecha
     */
    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}
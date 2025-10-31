package co.unicauca.review.entity;

import co.unicauca.review.enums.AsignacionEstado;
import co.unicauca.review.enums.Decision;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones_evaluadores", indexes = {
    @Index(name = "idx_asig_anteproyecto", columnList = "anteproyecto_id", unique = true),
    @Index(name = "idx_asig_eval1", columnList = "evaluador1_id"),
    @Index(name = "idx_asig_eval2", columnList = "evaluador2_id")
})
@EntityListeners(AuditingEntityListener.class)
public class AsignacionEvaluadores {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anteproyecto_id", nullable = false, unique = true)
    private Long anteproyectoId;

    @Column(name = "evaluador1_id", nullable = false)
    private Long evaluador1Id;

    @Column(name = "evaluador2_id", nullable = false)
    private Long evaluador2Id;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluador1_decision", length = 20)
    private Decision evaluador1Decision;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluador2_decision", length = 20)
    private Decision evaluador2Decision;

    @Column(name = "evaluador1_observaciones", columnDefinition = "TEXT")
    private String evaluador1Observaciones;

    @Column(name = "evaluador2_observaciones", columnDefinition = "TEXT")
    private String evaluador2Observaciones;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_completado")
    private LocalDateTime fechaCompletado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AsignacionEstado estado;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor vacío
    public AsignacionEvaluadores() {}

    // Métodos de negocio
    public boolean isCompletada() {
        return evaluador1Decision != null && evaluador2Decision != null;
    }

    public Decision getFinalDecision() {
        if (!isCompletada()) {
            return null;
        }
        return (evaluador1Decision == Decision.APROBADO &&
                evaluador2Decision == Decision.APROBADO)
                ? Decision.APROBADO
                : Decision.RECHAZADO;
    }

    // Getters y setters manuales
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAnteproyectoId() {
        return anteproyectoId;
    }

    public void setAnteproyectoId(Long anteproyectoId) {
        this.anteproyectoId = anteproyectoId;
    }

    public Long getEvaluador1Id() {
        return evaluador1Id;
    }

    public void setEvaluador1Id(Long evaluador1Id) {
        this.evaluador1Id = evaluador1Id;
    }

    public Long getEvaluador2Id() {
        return evaluador2Id;
    }

    public void setEvaluador2Id(Long evaluador2Id) {
        this.evaluador2Id = evaluador2Id;
    }

    public Decision getEvaluador1Decision() {
        return evaluador1Decision;
    }

    public void setEvaluador1Decision(Decision evaluador1Decision) {
        this.evaluador1Decision = evaluador1Decision;
    }

    public Decision getEvaluador2Decision() {
        return evaluador2Decision;
    }

    public void setEvaluador2Decision(Decision evaluador2Decision) {
        this.evaluador2Decision = evaluador2Decision;
    }

    public String getEvaluador1Observaciones() {
        return evaluador1Observaciones;
    }

    public void setEvaluador1Observaciones(String evaluador1Observaciones) {
        this.evaluador1Observaciones = evaluador1Observaciones;
    }

    public String getEvaluador2Observaciones() {
        return evaluador2Observaciones;
    }

    public void setEvaluador2Observaciones(String evaluador2Observaciones) {
        this.evaluador2Observaciones = evaluador2Observaciones;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public LocalDateTime getFechaCompletado() {
        return fechaCompletado;
    }

    public void setFechaCompletado(LocalDateTime fechaCompletado) {
        this.fechaCompletado = fechaCompletado;
    }

    public AsignacionEstado getEstado() {
        return estado;
    }

    public void setEstado(AsignacionEstado estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

package edu.unicauca.progresstracking.messaging.events;

import lombok.*;

/**
 * Evento: Anteproyecto ha sido evaluado
 * Origen: Review Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AnteproyectoEvaluadoEvent extends ProjectEvent {
    private String resultado; // APROBADO, RECHAZADO
    private String observaciones;
}
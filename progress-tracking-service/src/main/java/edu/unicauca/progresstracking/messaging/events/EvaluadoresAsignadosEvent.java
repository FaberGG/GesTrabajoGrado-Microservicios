package edu.unicauca.progresstracking.messaging.events;

import lombok.*;
import java.util.List;

/**
 * Evento: Evaluadores han sido asignados al anteproyecto
 * Origen: Review Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EvaluadoresAsignadosEvent extends ProjectEvent {
    private List<EvaluadorInfo> evaluadores;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluadorInfo {
        private Long id;
        private String nombre;
    }
}
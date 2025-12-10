package edu.unicauca.progresstracking.messaging.events;

import lombok.*;

/**
 * Evento: Anteproyecto ha sido enviado
 * Origen: Submission Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AnteproyectoEnviadoEvent extends ProjectEvent {
    private String descripcion;
}
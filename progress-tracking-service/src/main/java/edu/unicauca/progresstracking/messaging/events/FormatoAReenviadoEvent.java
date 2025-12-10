package edu.unicauca.progresstracking.messaging.events;

import lombok.*;

/**
 * Evento: Formato A ha sido reenviado (correcciones)
 * Origen: Submission Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FormatoAReenviadoEvent extends ProjectEvent {
    private Integer version; // 2 o 3
    private String descripcion;
}
package edu.unicauca.progresstracking.messaging.events;

import lombok.*;

/**
 * Evento: Formato A ha sido enviado
 * Origen: Submission Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FormatoAEnviadoEvent extends ProjectEvent {
    private Integer version; // 1, 2, 3
    private String descripcion;
}
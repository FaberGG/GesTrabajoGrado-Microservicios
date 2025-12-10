package edu.unicauca.progresstracking.messaging.events;

import lombok.*;

/**
 * Evento: Formato A ha sido evaluado
 * Origen: Review Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FormatoAEvaluadoEvent extends ProjectEvent {
    private String resultado; // APROBADO, RECHAZADO
    private String observaciones;
    private Integer version;
    private Boolean rechazadoDefinitivo; // true si es el 3er rechazo
}
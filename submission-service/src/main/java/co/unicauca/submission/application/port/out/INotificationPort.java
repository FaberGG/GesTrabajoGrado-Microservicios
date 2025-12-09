package co.unicauca.submission.application.port.out;

import java.util.Map;

/**
 * Puerto de salida para envío de notificaciones.
 */
public interface INotificationPort {

    /**
     * Envía una notificación genérica por email.
     */
    void enviarNotificacion(
        String destinatario,
        String asunto,
        String mensaje,
        Map<String, Object> datos
    );

    /**
     * Notifica al coordinador sobre nuevo Formato A (RF2).
     */
    void notificarCoordinadorFormatoAEnviado(Long proyectoId, int version);

    /**
     * Notifica al jefe de departamento sobre anteproyecto (RF6).
     */
    void notificarJefeDepartamentoAnteproyecto(Long proyectoId);

    /**
     * Notifica a evaluadores asignados (RF8).
     */
    void notificarEvaluadoresAsignados(Long proyectoId, Long evaluador1Id, Long evaluador2Id);
}


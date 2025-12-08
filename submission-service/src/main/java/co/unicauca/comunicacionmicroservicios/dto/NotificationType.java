package co.unicauca.comunicacionmicroservicios.dto;

/**
 * Tipos de notificaciones soportadas por el sistema.
 * DEBE coincidir con el enum del notification-service.
 */
public enum NotificationType {
    /**
     * Notificación cuando se envía un Formato A (inicial o nueva versión)
     * Destinatario: Coordinador
     */
    DOCUMENT_SUBMITTED,

    /**
     * Notificación cuando se completa la evaluación de un documento
     * Destinatarios: Docente(s) y Estudiante(s)
     */
    EVALUATION_COMPLETED,

    /**
     * Notificación cuando cambia el estado de un proyecto
     * Destinatarios: Involucrados en el proyecto
     */
    STATUS_CHANGED,

    /**
     * Notificación cuando se asignan evaluadores
     * Destinatarios: Evaluadores asignados
     */
    EVALUATOR_ASSIGNED,

    /**
     * Recordatorio de fecha límite
     * Destinatarios: Responsables
     */
    DEADLINE_REMINDER
}


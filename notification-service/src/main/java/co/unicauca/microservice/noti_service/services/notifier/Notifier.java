package co.unicauca.microservice.noti_service.services.notifier;

import co.unicauca.microservice.noti_service.model.NotificationRequest;
import co.unicauca.microservice.noti_service.model.NotificationResponse;

/**
 * Interfaz base para el servicio de notificaciones.
 * Permite aplicar el patrón Decorator para agregar comportamientos de forma dinámica.
 */
public interface Notifier {

    /**
     * Envía una notificación de forma síncrona
     * @param request Datos de la notificación
     * @return Respuesta con el resultado del envío
     */
    NotificationResponse sendSync(NotificationRequest request);

    /**
     * Publica una notificación de forma asíncrona en la cola
     * @param request Datos de la notificación
     * @param correlationId ID de correlación para trazabilidad
     */
    void publishAsync(NotificationRequest request, String correlationId);

    /**
     * Envía una notificación (usado por el consumidor de la cola)
     * @param request Datos de la notificación
     * @param correlationId ID de correlación para trazabilidad
     */
    void send(NotificationRequest request, String correlationId);
}
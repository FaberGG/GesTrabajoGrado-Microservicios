package co.unicauca.microservice.noti_service.services.notifier.decorators;

import co.unicauca.microservice.noti_service.model.NotificationRequest;
import co.unicauca.microservice.noti_service.model.NotificationResponse;
import co.unicauca.microservice.noti_service.services.notifier.Notifier;
import co.unicauca.microservice.noti_service.services.validation.NotificationValidator;

/**
 * Decorator que agrega validación de notificaciones.
 * Valida que la notificación tenga todos los campos requeridos antes de enviarla.
 */
public class ValidationNotifierDecorator implements Notifier {

    private final Notifier wrapped;
    private final NotificationValidator validator;

    public ValidationNotifierDecorator(Notifier wrapped, NotificationValidator validator) {
        this.wrapped = wrapped;
        this.validator = validator;
    }

    @Override
    public NotificationResponse sendSync(NotificationRequest request) {
        // Validar antes de delegar
        validator.validate(request);
        return wrapped.sendSync(request);
    }

    @Override
    public void publishAsync(NotificationRequest request, String correlationId) {
        // Validar antes de publicar en la cola
        validator.validate(request);
        wrapped.publishAsync(request, correlationId);
    }

    @Override
    public void send(NotificationRequest request, String correlationId) {
        // Validar antes de enviar
        validator.validate(request);
        wrapped.send(request, correlationId);
    }
}
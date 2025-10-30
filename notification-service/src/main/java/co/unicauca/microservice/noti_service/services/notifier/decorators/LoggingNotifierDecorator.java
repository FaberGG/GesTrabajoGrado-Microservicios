package co.unicauca.microservice.noti_service.services.notifier.decorators;

import co.unicauca.microservice.noti_service.model.NotificationRequest;
import co.unicauca.microservice.noti_service.model.NotificationResponse;
import co.unicauca.microservice.noti_service.services.NotificationLogger;
import co.unicauca.microservice.noti_service.services.notifier.Notifier;
import org.slf4j.MDC;

/**
 * Decorator que agrega logging estructurado a las notificaciones.
 * Registra inicio, éxito y fallos de las notificaciones.
 */
public class LoggingNotifierDecorator implements Notifier {

    private final Notifier wrapped;
    private final NotificationLogger logger;

    public LoggingNotifierDecorator(Notifier wrapped, NotificationLogger logger) {
        this.wrapped = wrapped;
        this.logger = logger;
    }

    @Override
    public NotificationResponse sendSync(NotificationRequest request) {
        String correlationId = MDC.get("correlationId");

        // Log inicio del procesamiento
        logger.logNotificationProcessing(
                correlationId,
                request.channel(),
                getRecipientsString(request),
                false
        );

        try {
            NotificationResponse response = wrapped.sendSync(request);

            // Log de éxito
            logger.logNotificationSent(
                    request.channel(),
                    getRecipientsString(request),
                    getMessagePreview(request),
                    correlationId,
                    false,
                    response.status()
            );

            return response;

        } catch (Exception e) {
            // Log de error
            logger.logNotificationError(
                    correlationId,
                    e.getMessage(),
                    false,
                    request.channel()
            );
            throw e;
        }
    }

    @Override
    public void publishAsync(NotificationRequest request, String correlationId) {
        try {
            wrapped.publishAsync(request, correlationId);

            // Log de publicación en la cola
            logger.logNotificationPublished(
                    correlationId,
                    request.channel(),
                    getRecipientsString(request)
            );

        } catch (Exception e) {
            logger.logNotificationError(
                    correlationId,
                    e.getMessage(),
                    true,
                    request.channel()
            );
            throw e;
        }
    }

    @Override
    public void send(NotificationRequest request, String correlationId) {
        // Log inicio del procesamiento asíncrono
        logger.logNotificationProcessing(
                correlationId,
                request.channel(),
                getRecipientsString(request),
                true
        );

        try {
            wrapped.send(request, correlationId);

            // Log de éxito
            logger.logNotificationSent(
                    request.channel(),
                    getRecipientsString(request),
                    getMessagePreview(request),
                    correlationId,
                    true,
                    "SENT"
            );

        } catch (Exception e) {
            logger.logNotificationError(
                    correlationId,
                    e.getMessage(),
                    true,
                    request.channel()
            );
            throw e;
        }
    }

    /**
     * Obtiene una representación de string de los destinatarios
     */
    private String getRecipientsString(NotificationRequest request) {
        if (request.recipients().size() == 1) {
            return request.recipients().get(0).email();
        }
        return request.recipients().size() + " recipients";
    }

    /**
     * Obtiene preview del mensaje para logging
     */
    private String getMessagePreview(NotificationRequest request) {
        String projectTitle = (String) request.businessContext().get("projectTitle");
        String notifType = request.notificationType().toString();
        return notifType + (projectTitle != null ? " - " + projectTitle : "");
    }
}
package co.unicauca.microservice.noti_service.http;

import co.unicauca.microservice.noti_service.model.NotificationRequest;
import co.unicauca.microservice.noti_service.model.NotificationResponse;
import co.unicauca.microservice.noti_service.services.notifier.Notifier;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.MDC;

/**
 * Controller REST para envío de notificaciones.
 * Soporta envío síncrono y asíncrono.
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final Notifier notifier;

    public NotificationController(Notifier notifier) {
        this.notifier = notifier;
    }

    /**
     * Envía una notificación de forma síncrona.
     * Espera a que la notificación se envíe y retorna el resultado.
     *
     * @param request Datos de la notificación
     * @return Respuesta con el resultado del envío
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> sendSync(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notifier.sendSync(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Envía una notificación de forma asíncrona.
     * Publica la notificación en una cola y retorna inmediatamente.
     *
     * @param request Datos de la notificación
     * @return 202 Accepted (procesamiento asíncrono)
     */
    @PostMapping("/async")
    public ResponseEntity<Void> sendAsync(@Valid @RequestBody NotificationRequest request) {
        String correlationId = MDC.get("correlationId");
        notifier.publishAsync(request, correlationId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
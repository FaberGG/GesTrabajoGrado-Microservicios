package co.unicauca.microservice.noti_service.http;

import co.unicauca.microservice.noti_service.model.NotificationRequest;
import co.unicauca.microservice.noti_service.model.NotificationResponse;
import co.unicauca.microservice.noti_service.services.NotifierService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.MDC;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotifierService notifier;

    public NotificationController(NotifierService notifier) {
        this.notifier = notifier;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendSync(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notifier.sendSync(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/async")
    public ResponseEntity<Void> sendAsync(@Valid @RequestBody NotificationRequest request) {
        String correlationId = MDC.get("correlationId");
        notifier.publishAsync(request, correlationId);
        return ResponseEntity.accepted().build();
    }
}

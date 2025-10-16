package co.unicauca.comunicacionmicroservicios.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class NotificationClient {

    private final WebClient webClient;

    @Autowired
    public NotificationClient(
            @Value("${notification.base-url}") String baseUrl,   // p.ej. http://localhost:8082
            WebClient.Builder builder
    ) {
        // Spring SIEMPRE tiene un WebClient.Builder listo; aquí construimos el WebClient final
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<Void> sendNotification(Map<String, Object> payload) {
        // Si tu base-url YA incluye /api/notifications, deja uri("")
        // Si tu base-url es solo http://localhost:8082, usa uri("/api/notifications")
        return webClient.post()
                .uri("")                // o .uri("/api/notifications")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> Mono.empty()); // fallback: no tumbes la petición si notificaciones falla
    }
}

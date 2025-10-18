package co.unicauca.comunicacionmicroservicios.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Cliente para comunicación SÍNCRONA con el microservicio de notificaciones
 * Utiliza HTTP/REST para enviar solicitudes directas
 */
@Service
public class NotificationClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificationClient.class);
    private final WebClient webClient;

    @Autowired
    public NotificationClient(
            @Value("${notification.base-url}") String baseUrl,
            WebClient.Builder builder
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        logger.info("NotificationClient configurado con baseUrl: {}", baseUrl);
    }

    /**
     * Envía una notificación de forma síncrona al servicio de notificaciones
     * @param payload Datos de la notificación (to, subject, body, etc.)
     * @return Mono<Void> para manejo reactivo
     */
    public Mono<Void> sendNotification(Map<String, Object> payload) {
        logger.info("Enviando notificación síncrona HTTP...");
        logger.debug("Payload: {}", payload);

        return webClient.post()
                .uri("/send")  // endpoint en el servicio de notificación
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> logger.info("Notificación HTTP enviada correctamente"))
                .onErrorResume(e -> {
                    logger.warn("Error al enviar notificación síncrona (no crítico): {}", e.getMessage());
                    return Mono.empty(); // fallback: no tumbes la petición si notificaciones falla
                });
    }
}

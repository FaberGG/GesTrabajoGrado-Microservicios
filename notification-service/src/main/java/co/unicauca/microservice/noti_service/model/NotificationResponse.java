package co.unicauca.microservice.noti_service.model;

import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String status,
        String correlationId
) {}

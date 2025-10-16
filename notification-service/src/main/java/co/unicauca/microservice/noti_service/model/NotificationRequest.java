package co.unicauca.microservice.noti_service.model;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
    @NotBlank String channel,    // "email" | "sms"
    @NotBlank String to,
    @NotBlank String message,
    String templateId,           // opcional
    boolean forceFail            // para simular fallos
) {}

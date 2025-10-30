package co.unicauca.microservice.noti_service.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Representa un destinatario de notificación con su rol en el sistema.
 */
public record Recipient(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        String role,  // COORDINATOR, TEACHER, STUDENT, DEPARTMENT_HEAD, EVALUATOR

        String name   // Opcional, para personalización de mensajes
) {
    /**
     * Constructor convenience para casos simples (sin rol ni nombre)
     */
    public Recipient(String email) {
        this(email, null, null);
    }

    /**
     * Constructor convenience con email y nombre
     */
    public Recipient(String email, String name) {
        this(email, null, name);
    }
}
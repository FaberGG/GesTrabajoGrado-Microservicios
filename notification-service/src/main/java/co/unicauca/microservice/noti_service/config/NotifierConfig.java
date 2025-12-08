package co.unicauca.microservice.noti_service.config;

import co.unicauca.microservice.noti_service.services.NotificationLogger;
import co.unicauca.microservice.noti_service.services.notifier.BaseNotifierService;
import co.unicauca.microservice.noti_service.services.notifier.Notifier;
import co.unicauca.microservice.noti_service.services.notifier.decorators.LoggingNotifierDecorator;
import co.unicauca.microservice.noti_service.services.notifier.decorators.ValidationNotifierDecorator;
import co.unicauca.microservice.noti_service.services.validation.NotificationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuración del servicio de notificaciones usando el patrón Decorator.
 * Permite habilitar/deshabilitar features mediante properties.
 */
@Configuration
public class NotifierConfig {
    private static final Logger log = LoggerFactory.getLogger(NotifierConfig.class);

    @Bean
    @Primary
    public Notifier notifier(
            BaseNotifierService baseService,
            NotificationValidator validator,
            NotificationLogger notificationLogger,
            @Value("${notifications.features.validation:true}") boolean validationEnabled,
            @Value("${notifications.features.logging:true}") boolean loggingEnabled
    ) {
        log.info("Configuring Notifier with features: validation={}, logging={}",
                validationEnabled, loggingEnabled);

        Notifier notifier = baseService;

        // Aplicar decoradores en orden específico (innermost to outermost)

        // 1. Validación (primero - innermost)
        // Debe fallar rápido antes de cualquier procesamiento
        if (validationEnabled) {
            log.info("✓ Validation decorator enabled");
            notifier = new ValidationNotifierDecorator(notifier, validator);
        }

        // 2. Logging (último - outermost)
        // Logging al final para capturar todo el flujo
        if (loggingEnabled) {
            log.info("✓ Logging decorator enabled");
            notifier = new LoggingNotifierDecorator(notifier, notificationLogger);
        }

        log.info("Notifier configured successfully with {} active decorators",
                (validationEnabled ? 1 : 0) + (loggingEnabled ? 1 : 0));

        return notifier;
    }
}
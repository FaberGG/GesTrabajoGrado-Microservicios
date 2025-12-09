package co.unicauca.submission.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para feature flags de arquitectura hexagonal.
 * Permite activar/desactivar la nueva implementación gradualmente.
 */
@Configuration
@ConfigurationProperties(prefix = "feature.hexagonal")
public class HexagonalFeatureConfig {

    /**
     * Flag para activar la nueva arquitectura hexagonal.
     * true = usa nuevos Use Cases
     * false = usa código legacy (SubmissionService)
     */
    private boolean enabled = false;

    /**
     * Flag para logging detallado de la migración.
     */
    private boolean debugMode = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}


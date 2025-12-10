package co.unicauca.submission.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de Beans para la aplicación.
 * Define beans que no tienen anotaciones propias.
 */
@Configuration
public class BeanConfiguration {

    /**
     * Bean de RestTemplate para hacer llamadas HTTP a otros servicios.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}


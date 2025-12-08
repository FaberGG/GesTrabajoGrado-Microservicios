package co.unicauca.gateway.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuración de WebClient para comunicación con servicios externos.
 *
 * Configura timeouts y otras opciones de conexión HTTP para las llamadas
 * que el gateway podría hacer directamente a servicios (health checks, etc.).
 *
 * NOTA: Spring Cloud Gateway usa su propio cliente HTTP interno para el proxy,
 * pero este WebClient puede usarse para operaciones adicionales del gateway
 * como verificación de salud de servicios o llamadas auxiliares.
 *
 * @author Gateway Team
 */
@Configuration
public class WebClientConfig {

    @Value("${spring.cloud.gateway.httpclient.connect-timeout:3000}")
    private int connectTimeout;

    @Value("${spring.cloud.gateway.httpclient.response-timeout:10000}")
    private int responseTimeout;

    /**
     * Configura un WebClient.Builder con timeouts apropiados.
     *
     * Timeouts configurados:
     * - Connection timeout: tiempo máximo para establecer conexión
     * - Read timeout: tiempo máximo esperando respuesta
     * - Write timeout: tiempo máximo para enviar datos
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(responseTimeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(responseTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(responseTimeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    /**
     * WebClient pre-configurado para uso general.
     */
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
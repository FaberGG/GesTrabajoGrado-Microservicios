package co.unicauca.comunicacionmicroservicios.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
public class IdentityClient {

    private final WebClient webClient;
    private final long timeoutMs;

    public IdentityClient(@Value("${identity.base-url}") String baseUrl,
                          @Value("${identity.timeout-ms:3000}") long timeoutMs) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.timeoutMs = timeoutMs;
    }

    public String getEmailByRole(String role) {
        try {
            var resp = webClient.get()
                    .uri("/api/auth/users/role/{role}/email", role)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Map<String,String>>>() {})
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();
            return resp != null && resp.data != null ? resp.data.getOrDefault("email", "") : "";
        } catch (Exception e) {
            log.error("Error llamando identity para rol {}: {}", role, e.getMessage());
            return "";
        }
    }

    public String getCoordinadorEmail() { return getEmailByRole("COORDINADOR"); }
    public String getJefeDepartamentoEmail() { return getEmailByRole("JEFE_DEPARTAMENTO"); }

    // DTO para la respuesta de identity
    public static class ApiResponse<T> {
        public boolean success;
        public String message;
        public T data;
        public Object errors;
    }
}

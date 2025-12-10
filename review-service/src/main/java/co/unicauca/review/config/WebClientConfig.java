package co.unicauca.review.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.submission.base-url}")
    private String submissionBaseUrl;

    @Value("${services.submission.timeout:5000}")
    private int submissionTimeout;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Bean("submissionWebClient")
    public WebClient submissionWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(submissionBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

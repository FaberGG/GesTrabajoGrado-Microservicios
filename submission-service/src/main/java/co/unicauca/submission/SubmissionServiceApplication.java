package co.unicauca.submission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de Spring Boot para Submission Service.
 * Arquitectura Hexagonal + Domain-Driven Design.
 *
 * @version 2.0.0
 * @since 2025-12-09
 */
@SpringBootApplication
public class SubmissionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubmissionServiceApplication.class, args);
    }
}


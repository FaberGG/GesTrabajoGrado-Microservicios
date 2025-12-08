package co.unicauca.review.client;

import co.unicauca.review.dto.response.FormatoAReviewDTO;
import co.unicauca.review.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class SubmissionServiceClient {

    private static final Logger log = LoggerFactory.getLogger(SubmissionServiceClient.class);

    private final WebClient webClient;

    public SubmissionServiceClient(@Qualifier("submissionWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public FormatoADTO getFormatoA(Long formatoAId) {
        log.debug("Obteniendo Formato A con id: {}", formatoAId);

        try {
            return webClient.get()
                    .uri("/api/submissions/formatoA/{id}", formatoAId)
                    .retrieve()
                    .bodyToMono(FormatoADTO.class)
                    .block();
        } catch (Exception e) {
            log.error("Error obteniendo Formato A {}: {}", formatoAId, e.getMessage());
            throw new ResourceNotFoundException("Formato A no encontrado: " + formatoAId);
        }
    }

    public AnteproyectoDTO getAnteproyecto(Long anteproyectoId) {
        log.debug("Obteniendo Anteproyecto con id: {}", anteproyectoId);

        try {
            return webClient.get()
                    .uri("/api/submissions/anteproyectos/{id}", anteproyectoId)
                    .retrieve()
                    .bodyToMono(AnteproyectoDTO.class)
                    .block();
        } catch (Exception e) {
            log.error("Error obteniendo Anteproyecto {}: {}", anteproyectoId, e.getMessage());
            throw new ResourceNotFoundException("Anteproyecto no encontrado: " + anteproyectoId);
        }
    }

    public Page<FormatoAReviewDTO> getFormatosAPendientes(int page, int size) {
        log.debug("Obteniendo Formatos A pendientes - page: {}, size: {}", page, size);

        try {
            // Simular respuesta paginada
            List<FormatoAReviewDTO> content = List.of(
                new FormatoAReviewDTO(
                    1L,
                    "Implementación de IA en agricultura",
                    "Dr. Juan Pérez",
                    "juan.perez@unicauca.edu.co",
                    List.of("estudiante1@unicauca.edu.co", "estudiante2@unicauca.edu.co"),
                    LocalDateTime.now().minusDays(2),
                    "EN_REVISION"
                )
            );
            return new PageImpl<>(content);
        } catch (Exception e) {
            log.error("Error obteniendo Formatos A pendientes: {}", e.getMessage());
            throw new RuntimeException("Error al obtener Formatos A pendientes", e);
        }
    }

    public void updateFormatoAEstado(Long formatoAId, Map<String, String> body) {
        log.debug("Actualizando estado de Formato A {}: {}", formatoAId, body);

        try {
            webClient.patch()
                    .uri("/api/submissions/formatoA/{id}/estado", formatoAId)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Estado de Formato A {} actualizado exitosamente", formatoAId);
        } catch (Exception e) {
            log.error("Error actualizando estado de Formato A {}: {}", formatoAId, e.getMessage());
            throw new RuntimeException("Error al actualizar estado de Formato A", e);
        }
    }

    public void updateAnteproyectoEstado(Long anteproyectoId, Map<String, String> body) {
        log.debug("Actualizando estado de Anteproyecto {}: {}", anteproyectoId, body);

        try {
            webClient.patch()
                    .uri("/api/submissions/anteproyectos/{id}/estado", anteproyectoId)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Estado de Anteproyecto {} actualizado exitosamente", anteproyectoId);
        } catch (Exception e) {
            log.error("Error actualizando estado de Anteproyecto {}: {}", anteproyectoId, e.getMessage());
            throw new RuntimeException("Error al actualizar estado de Anteproyecto", e);
        }
    }

    // DTOs internos para comunicación
    public static class FormatoADTO {
        private Long id;
        private String titulo;
        private String estado;
        private String docenteDirectorNombre;
        private String docenteDirectorEmail;
        private List<String> estudiantesEmails;

        public FormatoADTO() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public String getDocenteDirectorNombre() { return docenteDirectorNombre; }
        public void setDocenteDirectorNombre(String nombre) { this.docenteDirectorNombre = nombre; }

        public String getDocenteDirectorEmail() { return docenteDirectorEmail; }
        public void setDocenteDirectorEmail(String email) { this.docenteDirectorEmail = email; }

        public List<String> getEstudiantesEmails() { return estudiantesEmails; }
        public void setEstudiantesEmails(List<String> emails) { this.estudiantesEmails = emails; }
    }

    public static class AnteproyectoDTO {
        private Long id;
        private String titulo;
        private String estado;
        private String docenteDirectorNombre;
        private String docenteDirectorEmail;
        private List<String> estudiantesEmails;

        public AnteproyectoDTO() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public String getDocenteDirectorNombre() { return docenteDirectorNombre; }
        public void setDocenteDirectorNombre(String nombre) { this.docenteDirectorNombre = nombre; }

        public String getDocenteDirectorEmail() { return docenteDirectorEmail; }
        public void setDocenteDirectorEmail(String email) { this.docenteDirectorEmail = email; }

        public List<String> getEstudiantesEmails() { return estudiantesEmails; }
        public void setEstudiantesEmails(List<String> emails) { this.estudiantesEmails = emails; }
    }
}


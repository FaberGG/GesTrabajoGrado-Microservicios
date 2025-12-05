package co.unicauca.review.client;

import co.unicauca.review.dto.EvaluacionRequest;
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
            // Usar Map para manejar la deserialización de forma flexible
            Map<String, Object> response = webClient.get()
                    .uri("/api/submissions/formatoA/{id}", formatoAId)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null) {
                throw new ResourceNotFoundException("Formato A no encontrado: " + formatoAId);
            }

            log.debug("Formato A obtenido del submission-service: {}", response);

            // Mapear a FormatoADTO
            FormatoADTO dto = new FormatoADTO();
            dto.setId(((Number) response.get("id")).longValue());
            dto.setTitulo((String) response.get("titulo"));

            // El estado viene como String del enum
            Object estadoObj = response.get("estado");
            dto.setEstado(estadoObj != null ? estadoObj.toString() : "PENDIENTE");

            dto.setDocenteDirectorNombre((String) response.get("docenteDirectorNombre"));
            dto.setDocenteDirectorEmail((String) response.get("docenteDirectorEmail"));
            dto.setEstudiantesEmails((List<String>) response.get("estudiantesEmails"));

            log.info("✅ Formato A mapeado correctamente: id={}, titulo={}, estado={}",
                    dto.getId(), dto.getTitulo(), dto.getEstado());

            return dto;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error obteniendo Formato A {}: {}", formatoAId, e.getMessage(), e);
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
            // El submission-service devuelve directamente un Map con la estructura de paginación
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/submissions/formatoA/pendientes")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null) {
                log.warn("No se recibió respuesta del submission-service");
                return Page.empty();
            }

            log.debug("Respuesta recibida del submission-service: {}", response);

            // Extraer el contenido directamente (no hay wrapper de ApiResponse)
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

            if (content == null || content.isEmpty()) {
                log.warn("La respuesta no contiene formatos A pendientes");
                return Page.empty();
            }

            // Convertir los mapas a FormatoAReviewDTO
            List<FormatoAReviewDTO> formatosA = content.stream()
                    .map(this::mapToFormatoAReviewDTO)
                    .toList();

            // Crear Page con los datos de paginación
            int pageNumber = ((Number) response.getOrDefault("page", 0)).intValue();
            int pageSize = ((Number) response.getOrDefault("size", size)).intValue();
            long totalElements = ((Number) response.getOrDefault("totalElements", 0L)).longValue();

            log.info("✅ Se recibieron {} formatos A de {} totales", formatosA.size(), totalElements);

            return new PageImpl<>(formatosA,
                    org.springframework.data.domain.PageRequest.of(pageNumber, pageSize),
                    totalElements);

        } catch (Exception e) {
            log.error("Error obteniendo Formatos A pendientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener Formatos A pendientes", e);
        }
    }

    private FormatoAReviewDTO mapToFormatoAReviewDTO(Map<String, Object> map) {
        // Mapeo correcto: submission-service usa "id" pero necesitamos "formatoAId"
        return new FormatoAReviewDTO(
                ((Number) map.get("id")).longValue(),  // id -> formatoAId
                (String) map.get("titulo"),
                (String) map.get("docenteDirectorNombre"),
                (String) map.get("docenteDirectorEmail"),
                (List<String>) map.get("estudiantesEmails"),
                map.get("fechaEnvio") != null ?  // fechaEnvio -> fechaCarga
                        LocalDateTime.parse((String) map.get("fechaEnvio")) : null,
                map.get("estado") != null ? map.get("estado").toString() : "PENDIENTE"
        );
    }

    public void updateFormatoAEstado(Long formatoAId, EvaluacionRequest request) {
        log.debug("Actualizando estado de Formato A {}: {}", formatoAId, request);

        try {
            webClient.patch()
                    .uri("/api/submissions/formatoA/{id}/estado", formatoAId)
                    .header("X-Service", "review")

                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Estado de Formato A {} actualizado exitosamente", formatoAId);
        } catch (Exception e) {
            log.error("Error actualizando estado de Formato A {}: {}", formatoAId, e.getMessage());
            throw new RuntimeException("Error al actualizar estado de Formato A", e);
        }
    }

    public void updateAnteproyectoEstado(Long anteproyectoId, Map<String, Object> body) {
        log.debug("Actualizando estado de Anteproyecto {}: {}", anteproyectoId, body);

        try {
            webClient.patch()
                    .uri("/api/submissions/anteproyectos/{id}/estado", anteproyectoId)
                    .header("X-Service", "review")

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

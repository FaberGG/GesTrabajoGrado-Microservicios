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
            // Usar el endpoint correcto: /api/submissions/{id} (no /formatoA/{id})
            Map<String, Object> response = webClient.get()
                    .uri("/api/submissions/{id}", formatoAId)
                    .retrieve()
                    .onStatus(
                        status -> status.value() == 404,
                        clientResponse -> {
                            log.warn("Formato A {} no encontrado en submission-service (404)", formatoAId);
                            return clientResponse.bodyToMono(String.class)
                                .map(body -> new ResourceNotFoundException("Formato A no encontrado: " + formatoAId));
                        }
                    )
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null) {
                throw new ResourceNotFoundException("Formato A no encontrado: " + formatoAId);
            }

            log.debug("Proyecto obtenido del submission-service: {}", response);

            // El estado viene como String del enum
            Object estadoObj = response.get("estado");
            String estado = estadoObj != null ? estadoObj.toString() : "PENDIENTE";

            // Verificar si es un estado final (ya fue evaluado)
            if ("FORMATO_A_APROBADO".equals(estado)) {
                log.warn("Intento de obtener Formato A {} que ya fue APROBADO", formatoAId);
                throw new co.unicauca.review.exception.InvalidStateException(
                    "El Formato A ya ha sido APROBADO anteriormente. No se puede evaluar nuevamente."
                );
            }

            if ("FORMATO_A_RECHAZADO".equals(estado)) {
                log.warn("Intento de obtener Formato A {} que ya fue RECHAZADO definitivamente", formatoAId);
                throw new co.unicauca.review.exception.InvalidStateException(
                    "El Formato A ya ha sido RECHAZADO definitivamente. No se puede evaluar nuevamente."
                );
            }

            // Mapear a FormatoADTO
            FormatoADTO dto = new FormatoADTO();
            dto.setId(((Number) response.get("id")).longValue());
            dto.setTitulo((String) response.get("titulo"));
            dto.setEstado(estado);

            dto.setDocenteDirectorNombre((String) response.get("docenteDirectorNombre"));
            dto.setDocenteDirectorEmail((String) response.get("docenteDirectorEmail"));
            dto.setEstudiantesEmails((List<String>) response.get("estudiantesEmails"));

            log.info("✅ Formato A mapeado correctamente: id={}, titulo={}, estado={}",
                    dto.getId(), dto.getTitulo(), dto.getEstado());

            return dto;
        } catch (ResourceNotFoundException | co.unicauca.review.exception.InvalidStateException e) {
            throw e;
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException.NotFound e) {
            log.error("Formato A {} no encontrado (404): {}", formatoAId, e.getMessage());
            throw new ResourceNotFoundException("Formato A no encontrado: " + formatoAId);
        } catch (Exception e) {
            log.error("Error obteniendo Formato A {}: {}", formatoAId, e.getMessage(), e);
            throw new ResourceNotFoundException("Formato A no encontrado: " + formatoAId);
        }
    }

    public AnteproyectoDTO getAnteproyecto(Long anteproyectoId) {
        log.debug("Obteniendo Anteproyecto con id: {}", anteproyectoId);

        try {
            // Usar el endpoint correcto: /api/submissions/{id} (no /anteproyectos/{id})
            Map<String, Object> response = webClient.get()
                    .uri("/api/submissions/{id}", anteproyectoId)
                    .retrieve()
                    .onStatus(
                        status -> status.value() == 404,
                        clientResponse -> {
                            log.warn("Anteproyecto {} no encontrado en submission-service (404)", anteproyectoId);
                            return clientResponse.bodyToMono(String.class)
                                .map(body -> new ResourceNotFoundException("Anteproyecto no encontrado: " + anteproyectoId));
                        }
                    )
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null) {
                throw new ResourceNotFoundException("Anteproyecto no encontrado: " + anteproyectoId);
            }

            log.debug("Proyecto obtenido del submission-service: {}", response);

            // Verificar que sea un Anteproyecto (no solo Formato A)
            String estado = response.get("estado") != null ? response.get("estado").toString() : null;
            String rutaPdfAnteproyecto = (String) response.get("rutaPdfAnteproyecto");

            if (rutaPdfAnteproyecto == null || rutaPdfAnteproyecto.trim().isEmpty()) {
                log.warn("Proyecto {} no tiene anteproyecto asociado. Estado: {}", anteproyectoId, estado);
                throw new ResourceNotFoundException("El proyecto no tiene un anteproyecto asociado: " + anteproyectoId);
            }

            // Mapear a AnteproyectoDTO
            AnteproyectoDTO dto = new AnteproyectoDTO();
            dto.setId(((Number) response.get("id")).longValue());
            dto.setTitulo((String) response.get("titulo"));
            dto.setEstado(estado);
            dto.setDocenteDirectorNombre((String) response.get("docenteDirectorNombre"));
            dto.setDocenteDirectorEmail((String) response.get("docenteDirectorEmail"));
            dto.setEstudiantesEmails((List<String>) response.get("estudiantesEmails"));

            log.info("✅ Anteproyecto mapeado correctamente: id={}, titulo={}, estado={}",
                    dto.getId(), dto.getTitulo(), dto.getEstado());

            return dto;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException.NotFound e) {
            log.error("Anteproyecto {} no encontrado (404): {}", anteproyectoId, e.getMessage());
            throw new ResourceNotFoundException("Anteproyecto no encontrado: " + anteproyectoId);
        } catch (Exception e) {
            log.error("Error obteniendo Anteproyecto {}: {}", anteproyectoId, e.getMessage(), e);
            throw new ResourceNotFoundException("Anteproyecto no encontrado: " + anteproyectoId);
        }
    }

    public Page<co.unicauca.review.dto.response.AsignacionDTO> getAnteproyectosPendientes(int page, int size) {
        log.info("Obteniendo Anteproyectos pendientes desde submission - page: {}, size: {}", page, size);

        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/submissions/anteproyecto/pendientes")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null || !response.containsKey("content")) {
                log.warn("La respuesta no contiene anteproyectos pendientes");
                return Page.empty();
            }

            log.debug("Respuesta recibida: {}", response);

            // Extraer el contenido (lista de Anteproyectos)
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

            // Convertir a AsignacionDTO (anteproyectos pendientes = asignaciones pendientes)
            List<co.unicauca.review.dto.response.AsignacionDTO> asignaciones = content.stream()
                    .map(item -> new co.unicauca.review.dto.response.AsignacionDTO(
                        null, // asignacionId - no hay asignación aún
                        ((Number) item.get("id")).longValue(), // anteproyectoId
                        (String) item.get("titulo"), // tituloAnteproyecto
                        null, // evaluador1 - no asignado aún
                        null, // evaluador2 - no asignado aún
                        co.unicauca.review.enums.AsignacionEstado.PENDIENTE, // estado
                        null, // fechaAsignacion - no asignado aún
                        null, // fechaCompletado - no completado aún
                        null  // finalDecision - no evaluado aún
                    ))
                    .toList();

            // Construir Page
            Map<String, Object> pageable = (Map<String, Object>) response.get("pageable");
            int totalElements = ((Number) response.get("totalElements")).intValue();
            int pageNumber = ((Number) pageable.get("pageNumber")).intValue();
            int pageSize = ((Number) pageable.get("pageSize")).intValue();

            log.info("✅ Se recibieron {} anteproyectos pendientes de {} totales",
                    asignaciones.size(), totalElements);

            return new org.springframework.data.domain.PageImpl<>(
                    asignaciones,
                    org.springframework.data.domain.PageRequest.of(pageNumber, pageSize),
                    totalElements
            );

        } catch (Exception e) {
            log.error("Error obteniendo anteproyectos pendientes: {}", e.getMessage(), e);
            return Page.empty();
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

    /**
     * Actualiza el estado del Formato A en submission-service.
     * Compatible con la nueva arquitectura hexagonal de submission-service.
     *
     * @param formatoAId ID del formato A
     * @param aprobado true si fue aprobado, false si fue rechazado
     * @param comentarios Comentarios de la evaluación
     * @param evaluadorId ID del evaluador (se envía en header X-User-Id)
     */
    public void updateFormatoAEstado(Long formatoAId, Boolean aprobado, String comentarios, Long evaluadorId) {
        log.debug("Actualizando estado de Formato A {}: aprobado={}, evaluador={}", formatoAId, aprobado, evaluadorId);

        try {
            // Crear el DTO compatible con EvaluarFormatoARequest de submission-service
            EvaluarFormatoARequestDTO request = new EvaluarFormatoARequestDTO(aprobado, comentarios);

            log.debug("Request DTO: {}", request);

            webClient.patch()
                    .uri("/api/submissions/formatoA/{id}/evaluar", formatoAId)
                    .header("X-Service", "review")
                    .header("X-User-Id", String.valueOf(evaluadorId))
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("✅ Estado de Formato A {} actualizado exitosamente: aprobado={}", formatoAId, aprobado);
        } catch (Exception e) {
            log.error("❌ Error actualizando estado de Formato A {}: {}", formatoAId, e.getMessage());
            throw new RuntimeException("Error al actualizar estado de Formato A", e);
        }
    }

    /**
     * Actualiza el estado del Anteproyecto en submission-service.
     *
     * NOTA: Este método usa el endpoint legacy. Submission-service (arquitectura hexagonal)
     * aún no tiene implementado el endpoint de evaluación de anteproyecto.
     * Cuando se implemente, este método deberá actualizarse similar a updateFormatoAEstado.
     *
     * @param anteproyectoId ID del anteproyecto
     * @param body Map con estado, observaciones y evaluadoPor
     */
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

    /**
     * DTO compatible con EvaluarFormatoARequest de submission-service.
     * Arquitectura Hexagonal - Diciembre 2025
     *
     * Estructura esperada por submission-service:
     * {
     *   "aprobado": true/false,
     *   "comentarios": "..."
     * }
     */
    public static class EvaluarFormatoARequestDTO {
        private Boolean aprobado;
        private String comentarios;

        public EvaluarFormatoARequestDTO() {}

        public EvaluarFormatoARequestDTO(Boolean aprobado, String comentarios) {
            this.aprobado = aprobado;
            this.comentarios = comentarios;
        }

        public Boolean getAprobado() { return aprobado; }
        public void setAprobado(Boolean aprobado) { this.aprobado = aprobado; }

        public String getComentarios() { return comentarios; }
        public void setComentarios(String comentarios) { this.comentarios = comentarios; }

        @Override
        public String toString() {
            return "EvaluarFormatoARequestDTO{aprobado=" + aprobado + ", comentarios='" + comentarios + "'}";
        }
    }
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

package co.unicauca.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record FormatoAReviewDTO(
    Long formatoAId,
    String titulo,
    String docenteDirectorNombre,
    String docenteDirectorEmail,
    List<String> estudiantesEmails,
    LocalDateTime fechaCarga,
    String estado
) {}


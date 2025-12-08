package com.unicauca.identity.util;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilidad para manejar la paginación en las respuestas de la API
 */
public class PaginationUtil {

    private PaginationUtil() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Genera encabezados HTTP para información de paginación
     *
     * @param page La página de resultados
     * @return HttpHeaders con información de paginación
     */
    public static HttpHeaders generatePaginationHeaders(Page<?> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Long.toString(page.getTotalElements()));
        headers.add("X-Total-Pages", Integer.toString(page.getTotalPages()));
        headers.add("X-Page-Number", Integer.toString(page.getNumber()));
        headers.add("X-Page-Size", Integer.toString(page.getSize()));
        return headers;
    }

    /**
     * Crea un ResponseEntity con datos paginados y encabezados de paginación
     *
     * @param page La página de resultados
     * @param <T> Tipo de los elementos en la página
     * @return ResponseEntity con los datos y encabezados
     */
    public static <T> ResponseEntity<Map<String, Object>> createPaginatedResponse(Page<T> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", page.getContent());
        response.put("pagination", Map.of(
                "totalItems", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "currentPage", page.getNumber(),
                "pageSize", page.getSize(),
                "hasNext", page.hasNext(),
                "hasPrevious", page.hasPrevious()
        ));

        return ResponseEntity.ok()
                .headers(generatePaginationHeaders(page))
                .body(response);
    }
}

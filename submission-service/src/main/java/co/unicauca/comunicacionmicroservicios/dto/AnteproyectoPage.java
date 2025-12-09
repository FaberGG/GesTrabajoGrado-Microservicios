package co.unicauca.comunicacionmicroservicios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * DTO contenedor para respuestas paginadas de anteproyectos.
 * Utilizado en el endpoint GET /api/submissions/anteproyecto para retornar listas paginadas.
 * 
 * @see AnteproyectoView DTO individual contenido en esta página
 */
@Schema(description = "Respuesta paginada de anteproyectos")
public class AnteproyectoPage {
    
    @Schema(description = "Lista de anteproyectos en la página actual")
    private List<AnteproyectoView> content;
    
    @Schema(description = "Número de página actual (inicia en 0)", example = "0")
    private int page;
    
    @Schema(description = "Tamaño de la página (cantidad de elementos)", example = "20")
    private int size;
    
    @Schema(description = "Total de elementos en todas las páginas", example = "150")
    private long totalElements;

    // Getters and Setters
    
    public List<AnteproyectoView> getContent() { return content; }
    public void setContent(List<AnteproyectoView> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
}

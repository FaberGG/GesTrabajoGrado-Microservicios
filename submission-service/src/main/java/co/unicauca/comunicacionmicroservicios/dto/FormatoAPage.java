package co.unicauca.comunicacionmicroservicios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * DTO contenedor para respuestas paginadas de versiones de Formato A.
 * Utilizado en el endpoint GET /api/submissions/formatoA para retornar listas paginadas.
 * 
 * @see FormatoAView DTO individual contenido en esta página
 */
@Schema(description = "Respuesta paginada de versiones de Formato A")
public class FormatoAPage {
    
    @Schema(description = "Lista de versiones de Formato A en la página actual")
    private List<FormatoAView> content;
    
    @Schema(description = "Número de página actual (inicia en 0)", example = "0")
    private int page;
    
    @Schema(description = "Tamaño de la página (cantidad de elementos)", example = "20")
    private int size;
    
    @Schema(description = "Total de elementos en todas las páginas", example = "100")
    private long totalElements;

    // Getters and Setters
    
    public List<FormatoAView> getContent() { return content; }
    public void setContent(List<FormatoAView> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
}

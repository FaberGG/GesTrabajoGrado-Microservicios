package co.unicauca.comunicacionmicroservicios.dto;

import java.util.List;

public class AnteproyectoPage {
    private List<AnteproyectoView> content;
    private int page;
    private int size;
    private long totalElements;

    public List<AnteproyectoView> getContent() { return content; }
    public void setContent(List<AnteproyectoView> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
}

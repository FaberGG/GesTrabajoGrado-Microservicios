package co.unicauca.comunicacionmicroservicios.dto;

import java.util.List;

public class FormatoAPage {
    private List<FormatoAView> content;
    private int page;
    private int size;
    private long totalElements;

    public List<FormatoAView> getContent() { return content; }
    public void setContent(List<FormatoAView> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
}

package co.unicauca.submission.application.dto.request;

import co.unicauca.submission.domain.model.Modalidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.InputStream;
import java.util.List;

/**
 * DTO de request para crear un Formato A inicial.
 * RF2: Docente crea Formato A.
 */
public class CrearFormatoARequest {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotNull(message = "La modalidad es obligatoria")
    private Modalidad modalidad;

    @NotBlank(message = "El objetivo general es obligatorio")
    private String objetivoGeneral;

    @NotEmpty(message = "Debe haber al menos un objetivo específico")
    private List<String> objetivosEspecificos;

    @NotNull(message = "El estudiante 1 es obligatorio")
    private Long estudiante1Id;

    private Long estudiante2Id; // Opcional

    private Long codirectorId; // Opcional

    // Streams de archivos (se setean desde el controller)
    private InputStream pdfStream;
    private String pdfNombreArchivo;

    private InputStream cartaStream; // Obligatorio si modalidad = PRACTICA_PROFESIONAL
    private String cartaNombreArchivo;

    // Constructores
    public CrearFormatoARequest() {}

    // Getters y Setters

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public String getObjetivoGeneral() {
        return objetivoGeneral;
    }

    public void setObjetivoGeneral(String objetivoGeneral) {
        this.objetivoGeneral = objetivoGeneral;
    }

    public List<String> getObjetivosEspecificos() {
        return objetivosEspecificos;
    }

    public void setObjetivosEspecificos(List<String> objetivosEspecificos) {
        this.objetivosEspecificos = objetivosEspecificos;
    }

    public Long getEstudiante1Id() {
        return estudiante1Id;
    }

    public void setEstudiante1Id(Long estudiante1Id) {
        this.estudiante1Id = estudiante1Id;
    }

    public Long getEstudiante2Id() {
        return estudiante2Id;
    }

    public void setEstudiante2Id(Long estudiante2Id) {
        this.estudiante2Id = estudiante2Id;
    }

    public Long getCodirectorId() {
        return codirectorId;
    }

    public void setCodirectorId(Long codirectorId) {
        this.codirectorId = codirectorId;
    }

    public InputStream getPdfStream() {
        return pdfStream;
    }

    public void setPdfStream(InputStream pdfStream) {
        this.pdfStream = pdfStream;
    }

    public String getPdfNombreArchivo() {
        return pdfNombreArchivo;
    }

    public void setPdfNombreArchivo(String pdfNombreArchivo) {
        this.pdfNombreArchivo = pdfNombreArchivo;
    }

    public InputStream getCartaStream() {
        return cartaStream;
    }

    public void setCartaStream(InputStream cartaStream) {
        this.cartaStream = cartaStream;
    }

    public String getCartaNombreArchivo() {
        return cartaNombreArchivo;
    }

    public void setCartaNombreArchivo(String cartaNombreArchivo) {
        this.cartaNombreArchivo = cartaNombreArchivo;
    }
}


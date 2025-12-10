package co.unicauca.submission.application.dto.request;

import java.io.InputStream;

/**
 * DTO de request para subir el anteproyecto.
 * RF6: Director sube anteproyecto.
 */
public class SubirAnteproyectoRequest {

    // Stream del archivo PDF del anteproyecto
    private InputStream pdfStream;
    private String pdfNombreArchivo;

    // Constructores
    public SubirAnteproyectoRequest() {}

    // Getters y Setters

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
}


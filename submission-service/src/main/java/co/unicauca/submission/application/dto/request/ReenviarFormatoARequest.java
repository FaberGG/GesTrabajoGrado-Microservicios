package co.unicauca.submission.application.dto.request;

import java.io.InputStream;

/**
 * DTO de request para reenviar una nueva versión del Formato A.
 * RF4: Docente reenvía Formato A tras correcciones.
 */
public class ReenviarFormatoARequest {

    // Streams de archivos (se setean desde el controller)
    private InputStream pdfStream;
    private String pdfNombreArchivo;

    private InputStream cartaStream; // Opcional
    private String cartaNombreArchivo;

    // Constructores
    public ReenviarFormatoARequest() {}

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


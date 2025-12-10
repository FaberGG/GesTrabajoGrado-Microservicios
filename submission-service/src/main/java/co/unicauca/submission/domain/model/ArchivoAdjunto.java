package co.unicauca.submission.domain.model;

import java.util.Objects;

/**
 * Value Object que representa un archivo adjunto (PDF, carta, etc.).
 */
public class ArchivoAdjunto {

    private final String ruta;
    private final String nombreOriginal;
    private final TipoArchivo tipo;

    private ArchivoAdjunto(String ruta, String nombreOriginal, TipoArchivo tipo) {
        if (ruta == null || ruta.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacía");
        }
        if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de archivo es obligatorio");
        }

        this.ruta = ruta;
        this.nombreOriginal = nombreOriginal;
        this.tipo = tipo;
    }

    public static ArchivoAdjunto of(String ruta, String nombreOriginal, TipoArchivo tipo) {
        return new ArchivoAdjunto(ruta, nombreOriginal, tipo);
    }

    public static ArchivoAdjunto pdf(String ruta, String nombreOriginal) {
        return new ArchivoAdjunto(ruta, nombreOriginal, TipoArchivo.PDF);
    }

    public boolean esPDF() {
        return this.tipo == TipoArchivo.PDF;
    }

    // Getters
    public String getRuta() {
        return ruta;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public TipoArchivo getTipo() {
        return tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArchivoAdjunto that = (ArchivoAdjunto) o;
        return Objects.equals(ruta, that.ruta) &&
               Objects.equals(nombreOriginal, that.nombreOriginal) &&
               tipo == that.tipo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruta, nombreOriginal, tipo);
    }

    @Override
    public String toString() {
        return "ArchivoAdjunto{" +
                "nombre='" + nombreOriginal + '\'' +
                ", tipo=" + tipo +
                '}';
    }

    /**
     * Enum para tipos de archivo soportados
     */
    public enum TipoArchivo {
        PDF, WORD, EXCEL, IMAGEN
    }
}


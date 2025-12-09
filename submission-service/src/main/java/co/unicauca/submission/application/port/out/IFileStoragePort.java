package co.unicauca.submission.application.port.out;

import java.io.InputStream;

/**
 * Puerto de salida para almacenamiento de archivos.
 */
public interface IFileStoragePort {

    /**
     * Guarda un archivo y retorna su ruta.
     *
     * @param contenido Stream del contenido del archivo
     * @param nombreArchivo Nombre del archivo
     * @param directorio Directorio donde guardar
     * @return Ruta donde se guardó el archivo
     */
    String guardarArchivo(InputStream contenido, String nombreArchivo, String directorio);

    /**
     * Obtiene un archivo por su ruta.
     *
     * @param ruta Ruta del archivo
     * @return Stream del contenido del archivo
     */
    InputStream obtenerArchivo(String ruta);

    /**
     * Elimina un archivo.
     *
     * @param ruta Ruta del archivo a eliminar
     */
    void eliminarArchivo(String ruta);

    /**
     * Valida que un archivo sea PDF.
     *
     * @param contenido Stream del contenido
     * @return true si es PDF, false en caso contrario
     */
    boolean esPDF(InputStream contenido);
}


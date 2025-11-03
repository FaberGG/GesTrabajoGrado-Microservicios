package co.unicauca.gestiontrabajogrado.domain.service;

/**
 * Interfaz para servicios de archivos
 */
public interface IArchivoService {
    byte[] descargarArchivo(String ruta);
    String guardarArchivo(byte[] contenido, String nombre);
}


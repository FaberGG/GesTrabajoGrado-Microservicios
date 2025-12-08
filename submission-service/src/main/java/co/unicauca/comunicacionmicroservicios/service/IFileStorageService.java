package co.unicauca.comunicacionmicroservicios.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interfaz para el servicio de almacenamiento de archivos.
 */
public interface IFileStorageService {

    /**
     * Guarda un archivo en el sistema de archivos.
     * @param relativePath Ruta relativa desde el directorio base
     * @param fileName Nombre del archivo
     * @param file Archivo multipart a guardar
     * @return Ruta completa donde se guardó el archivo
     */
    String guardarArchivo(String relativePath, String fileName, MultipartFile file);

    /**
     * Elimina un archivo del sistema de archivos.
     * @param filePath Ruta completa del archivo a eliminar
     * @return true si se eliminó correctamente
     */
    boolean eliminarArchivo(String filePath);
}


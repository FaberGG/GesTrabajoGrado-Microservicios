package co.unicauca.submission.infrastructure.adapter.out.filesystem;

import co.unicauca.submission.application.port.out.IFileStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Adaptador para almacenamiento local de archivos.
 * Implementa el puerto IFileStoragePort.
 *
 * Guarda archivos en el sistema de archivos local.
 * En producción podría reemplazarse por un adaptador de S3, MinIO, etc.
 */
@Component
public class LocalFileStorageAdapter implements IFileStoragePort {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageAdapter.class);

    @Value("${file.storage.base-path:./uploads}")
    private String basePath;

    @Override
    public String guardarArchivo(InputStream contenido, String nombreArchivo, String directorio) {
        try {
            // Crear directorio si no existe
            Path dirPath = Paths.get(basePath, directorio);
            Files.createDirectories(dirPath);

            // Generar nombre único para evitar colisiones
            String nombreUnico = generarNombreUnico(nombreArchivo);
            Path filePath = dirPath.resolve(nombreUnico);

            // Copiar archivo
            Files.copy(contenido, filePath, StandardCopyOption.REPLACE_EXISTING);

            // Retornar ruta relativa
            String rutaRelativa = directorio + "/" + nombreUnico;

            log.info("Archivo guardado: {}", rutaRelativa);

            return rutaRelativa;

        } catch (IOException e) {
            log.error("Error al guardar archivo {}: {}", nombreArchivo, e.getMessage(), e);
            throw new RuntimeException("No se pudo guardar el archivo: " + nombreArchivo, e);
        }
    }

    @Override
    public InputStream obtenerArchivo(String ruta) {
        try {
            Path filePath = Paths.get(basePath, ruta);

            if (!Files.exists(filePath)) {
                throw new RuntimeException("Archivo no encontrado: " + ruta);
            }

            log.debug("Obteniendo archivo: {}", ruta);

            return Files.newInputStream(filePath);

        } catch (IOException e) {
            log.error("Error al obtener archivo {}: {}", ruta, e.getMessage(), e);
            throw new RuntimeException("No se pudo obtener el archivo: " + ruta, e);
        }
    }

    @Override
    public void eliminarArchivo(String ruta) {
        try {
            Path filePath = Paths.get(basePath, ruta);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Archivo eliminado: {}", ruta);
            } else {
                log.warn("Archivo no encontrado para eliminar: {}", ruta);
            }

        } catch (IOException e) {
            log.error("Error al eliminar archivo {}: {}", ruta, e.getMessage(), e);
            throw new RuntimeException("No se pudo eliminar el archivo: " + ruta, e);
        }
    }

    @Override
    public boolean esPDF(InputStream contenido) {
        try {
            // Leer los primeros 4 bytes para verificar el magic number de PDF
            byte[] header = new byte[4];
            contenido.read(header);

            // PDF magic number: %PDF
            String headerString = new String(header);
            boolean isPdf = headerString.equals("%PDF");

            log.debug("Verificación de PDF: {}", isPdf);

            return isPdf;

        } catch (IOException e) {
            log.error("Error al verificar PDF: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Genera un nombre único para el archivo agregando UUID.
     */
    private String generarNombreUnico(String nombreOriginal) {
        String extension = "";
        int dotIndex = nombreOriginal.lastIndexOf('.');

        if (dotIndex > 0) {
            extension = nombreOriginal.substring(dotIndex);
            nombreOriginal = nombreOriginal.substring(0, dotIndex);
        }

        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return nombreOriginal + "_" + uuid + extension;
    }
}


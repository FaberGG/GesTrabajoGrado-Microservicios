package co.unicauca.comunicacionmicroservicios.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;

/**
 * Servicio para almacenamiento de archivos en el sistema de archivos local.
 * Estructura:
 * /app/uploads/
 * ├── formato-a/{proyectoId}/v{version}/documento.pdf
 * └── anteproyectos/{proyectoId}/documento.pdf
 */
@Service
public class FileStorageService implements IFileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.storage.path:/app/uploads}")
    private String basePath;

    @Override
    public String guardarArchivo(String relativePath, String fileName, MultipartFile file) {
        try {
            Path baseDir = Paths.get(basePath);
            Path targetDir = baseDir.resolve(relativePath).normalize();

            // Crear directorios si no existen
            Files.createDirectories(targetDir);

            // Ruta completa del archivo
            Path targetFile = targetDir.resolve(fileName).normalize();

            // Validar que la ruta esté dentro del directorio base (seguridad)
            if (!targetFile.startsWith(baseDir)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ruta de archivo inválida");
            }

            // Guardar el archivo
            Files.write(targetFile, file.getBytes(),
                       StandardOpenOption.CREATE,
                       StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Archivo guardado exitosamente: {}", targetFile);
            return targetFile.toString();

        } catch (IOException e) {
            log.error("Error al guardar archivo: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                            "No se pudo guardar el archivo");
        }
    }

    @Override
    public boolean eliminarArchivo(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Error al eliminar archivo: {}", e.getMessage(), e);
            return false;
        }
    }
}

package co.unicauca.gestiontrabajogrado.infrastructure.repository;

import co.unicauca.gestiontrabajogrado.dto.ProyectoGradoResponseDTO;

import java.util.List;

/**
 * Interfaz para repositorio de proyectos de grado
 */
public interface IProyectoGradoRepository {
    List<ProyectoGradoResponseDTO> findAll();
    ProyectoGradoResponseDTO findById(Long id);
    ProyectoGradoResponseDTO save(ProyectoGradoResponseDTO proyecto);
}


package co.unicauca.gestiontrabajogrado.domain.service;

import co.unicauca.gestiontrabajogrado.dto.ProyectoGradoRequestDTO;
import co.unicauca.gestiontrabajogrado.dto.ProyectoGradoResponseDTO;

import java.util.List;

/**
 * Servicio para gestión de proyectos de grado
 */
public interface IProyectoGradoService {

    /**
     * Crear un nuevo proyecto de grado
     */
    ProyectoGradoResponseDTO crearProyecto(ProyectoGradoRequestDTO request);

    /**
     * Obtener un proyecto por su ID
     */
    ProyectoGradoResponseDTO obtenerProyectoPorId(Long id);

    /**
     * Listar todos los proyectos de un docente
     */
    List<ProyectoGradoResponseDTO> listarProyectosPorDocente(Long docenteId);

    /**
     * Listar todos los proyectos
     */
    List<ProyectoGradoResponseDTO> listarTodosProyectos();

    /**
     * Actualizar un proyecto existente
     */
    ProyectoGradoResponseDTO actualizarProyecto(Long id, ProyectoGradoRequestDTO request);

    /**
     * Eliminar un proyecto
     */
    void eliminarProyecto(Long id);
}


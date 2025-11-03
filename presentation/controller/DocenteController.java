package co.unicauca.gestiontrabajogrado.controller;

import co.unicauca.gestiontrabajogrado.dto.ProyectoGradoRequestDTO;
import co.unicauca.gestiontrabajogrado.dto.ProyectoGradoResponseDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para funcionalidades del docente
 */
public class DocenteController {

    public DocenteController() {
    }

    /**
     * Crea un nuevo proyecto de grado
     */
    public ProyectoGradoResponseDTO handleCrearProyecto(ProyectoGradoRequestDTO request) {
        // TODO: Implementar llamada al microservicio de submission
        ProyectoGradoResponseDTO response = new ProyectoGradoResponseDTO();
        response.setId(1L);
        response.setTitulo(request.getTitulo());
        response.setMensaje("Proyecto creado exitosamente");
        return response;
    }

    /**
     * Crea un nuevo proyecto de grado con archivos
     */
    public boolean handleCrearProyecto(ProyectoGradoRequestDTO request, java.io.File formatoA, java.io.File carta) {
        try {
            // TODO: Implementar llamada al microservicio con archivos
            ProyectoGradoResponseDTO response = handleCrearProyecto(request);
            return response != null && response.getId() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sube una nueva versión del formato A
     */
    public boolean handleSubirNuevaVersion(Long proyectoId, java.io.File formatoA, java.io.File carta,
                                          String objetivoGeneral, String objetivosEspecificos) {
        try {
            // TODO: Implementar llamada al microservicio
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene los proyectos del docente
     */
    public List<ProyectoGradoResponseDTO> obtenerProyectosDocente(Long docenteId) {
        // TODO: Implementar llamada al microservicio
        return new ArrayList<>();
    }

    /**
     * Descarga la plantilla del formato
     */
    public byte[] descargarPlantilla(String tipoFormato) {
        // TODO: Implementar descarga de plantilla
        return new byte[0];
    }

    /**
     * Obtiene un proyecto por ID
     */
    public ProyectoGradoResponseDTO obtenerProyectoPorId(Integer id) {
        // TODO: Implementar llamada al microservicio
        return new ProyectoGradoResponseDTO();
    }

    /**
     * Obtiene el último formato A de un proyecto
     */
    public co.unicauca.gestiontrabajogrado.dto.FormatoADetalleDTO obtenerUltimoFormatoA(Integer proyectoId) {
        // TODO: Implementar llamada al microservicio
        return new co.unicauca.gestiontrabajogrado.dto.FormatoADetalleDTO();
    }


    /**
     * Cierra la sesión del usuario
     */
    public void handleCerrarSesion() {
        // TODO: Implementar cierre de sesión
        System.out.println("Cerrando sesión...");
    }
}


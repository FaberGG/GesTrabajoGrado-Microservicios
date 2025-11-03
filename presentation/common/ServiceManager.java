package co.unicauca.gestiontrabajogrado.presentation.common;

import co.unicauca.gestiontrabajogrado.domain.service.*;
import co.unicauca.gestiontrabajogrado.infrastructure.repository.*;

/**
 * Gestor singleton para servicios de la aplicación
 * Permite acceso global a servicios importantes sin duplicar instancias
 */
public class ServiceManager {

    private static ServiceManager instance;

    // Servicios
    private IAutenticacionService autenticacionService;
    private IProyectoGradoService proyectoGradoService;
    private IArchivoService archivoService;

    // Repositorios
    private IProyectoGradoRepository proyectoGradoRepository;
    private IFormatoARepository formatoARepository;
    private IUserRepository userRepository;

    private ServiceManager() {
        // Constructor privado para patrón singleton
        inicializarServicios();
    }

    /**
     * Obtiene la instancia única del ServiceManager
     */
    public static ServiceManager getInstance() {
        if (instance == null) {
            synchronized (ServiceManager.class) {
                if (instance == null) {
                    instance = new ServiceManager();
                }
            }
        }
        return instance;
    }

    /**
     * Inicializa todos los servicios y repositorios necesarios
     */
    private void inicializarServicios() {
        // TODO: Implementar servicios reales cuando se conecten con los microservicios
        // Por ahora, los servicios se configurarán como null y se inicializarán bajo demanda
        System.out.println("ServiceManager inicializado. Los servicios se crearán bajo demanda.");
    }

    /**
     * Crea una instancia de una clase usando reflexión (solo constructores sin parámetros)
     */
    @SuppressWarnings("unchecked")
    private <T> T crearInstancia(String nombreClase, Class<T> interfaz) {
        try {
            Class<?> clase = Class.forName(nombreClase);
            return (T) clase.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.err.println("No se pudo crear instancia de " + nombreClase + ": " + e.getMessage());
            return null;
        }
    }

    // ========== GETTERS ==========

    public IAutenticacionService getAutenticacionService() {
        return autenticacionService;
    }

    public IProyectoGradoService getProyectoGradoService() {
        return proyectoGradoService;
    }

    // Alias para el método del main
    public IProyectoGradoService getProyectoService() {
        return proyectoGradoService;
    }

    public IArchivoService getArchivoService() {
        return archivoService;
    }

    public IProyectoGradoRepository getProyectoGradoRepository() {
        return proyectoGradoRepository;
    }

    // Alias para el método del main
    public IProyectoGradoRepository getProyectoRepository() {
        return proyectoGradoRepository;
    }

    public IFormatoARepository getFormatoARepository() {
        return formatoARepository;
    }

    // Alias para el método del main
    public IFormatoARepository getFormatoRepository() {
        return formatoARepository;
    }

    public IUserRepository getUserRepository() {
        return userRepository;
    }

    // ========== SETTERS ==========

    public void setAutenticacionService(IAutenticacionService service) {
        this.autenticacionService = service;
    }

    public void setProyectoGradoService(IProyectoGradoService service) {
        this.proyectoGradoService = service;
    }

    public void setArchivoService(IArchivoService service) {
        this.archivoService = service;
    }

    public void setProyectoGradoRepository(IProyectoGradoRepository repository) {
        this.proyectoGradoRepository = repository;
    }

    public void setFormatoARepository(IFormatoARepository repository) {
        this.formatoARepository = repository;
    }

    public void setUserRepository(IUserRepository repository) {
        this.userRepository = repository;
    }
}
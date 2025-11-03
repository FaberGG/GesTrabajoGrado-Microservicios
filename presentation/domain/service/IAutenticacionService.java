package co.unicauca.gestiontrabajogrado.domain.service;

/**
 * Interfaz para servicios de autenticación
 */
public interface IAutenticacionService {
    boolean autenticar(String email, String password);
    void cerrarSesion();
}


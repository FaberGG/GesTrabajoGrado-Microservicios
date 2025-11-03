package co.unicauca.gestiontrabajogrado.infrastructure.repository;

import co.unicauca.gestiontrabajogrado.domain.model.User;

/**
 * Interfaz para repositorio de usuarios
 */
public interface IUserRepository {
    User findById(Long id);
    User findByEmail(String email);
    User save(User user);
}


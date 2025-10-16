package com.unicauca.identity.repository;

import com.unicauca.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de la entidad User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Busca un usuario por su dirección de email
     *
     * @param email Email del usuario
     * @return Usuario encontrado (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email proporcionado
     *
     * @param email Email a verificar
     * @return true si existe un usuario con ese email
     */
    boolean existsByEmail(String email);
}

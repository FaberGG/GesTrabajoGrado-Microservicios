package com.unicauca.identity.repository;

import com.unicauca.identity.entity.User;
import com.unicauca.identity.enums.Programa;
import com.unicauca.identity.enums.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // Given
        User user = User.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .email("jperez@unicauca.edu.co")
                .programa(Programa.INGENIERIA_DE_SISTEMAS)
                .rol(Rol.ESTUDIANTE)
                .passwordHash("hashedPassword")
                .celular("3201234567")
                .build();

        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByEmail("jperez@unicauca.edu.co");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNombres()).isEqualTo("Juan");
        assertThat(found.get().getEmail()).isEqualTo("jperez@unicauca.edu.co");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        // When
        Optional<User> found = userRepository.findByEmail("noexiste@unicauca.edu.co");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Given
        User user = User.builder()
                .nombres("Maria")
                .apellidos("Lopez")
                .email("mlopez@unicauca.edu.co")
                .programa(Programa.AUTOMATICA_INDUSTRIAL)
                .rol(Rol.ESTUDIANTE)
                .passwordHash("hashedPassword")
                .celular("3109876543")
                .build();

        entityManager.persist(user);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByEmail("mlopez@unicauca.edu.co");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // When
        boolean exists = userRepository.existsByEmail("noexiste@unicauca.edu.co");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistUser() {
        // Given
        User user = User.builder()
                .nombres("Carlos")
                .apellidos("Gomez")
                .email("cgomez@unicauca.edu.co")
                .programa(Programa.TECNOLOGIA_EN_TELEMATICA)
                .rol(Rol.DOCENTE)
                .passwordHash("hashedPassword")
                .celular("3001234567")
                .build();

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(entityManager.find(User.class, saved.getId())).isEqualTo(saved);
    }
}

package co.unicauca.comunicacionmicroservicios.infraestructure.repository;

import co.unicauca.comunicacionmicroservicios.domain.model.Anteproyecto;
import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoGrado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IAnteproyectoRepository extends JpaRepository<Anteproyecto, Integer> {
    Optional<Anteproyecto> findByProyecto(ProyectoGrado proyecto);
}

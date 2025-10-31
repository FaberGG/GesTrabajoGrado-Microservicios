/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.comunicacionmicroservicios.infraestructure.repository;

/**
 *
 * @author USUARIO
 */

import co.unicauca.comunicacionmicroservicios.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IFormatoARepository extends JpaRepository<FormatoA, Integer> {
    // Última versión subida para un proyecto (por número de intento)
    Optional<FormatoA> findTopByProyectoOrderByNumeroIntentoDesc(ProyectoGrado proyecto);

    // Conteo de versiones para un proyecto
    long countByProyecto(ProyectoGrado proyecto);
}

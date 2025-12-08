/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.comunicacionmicroservicios.infraestructure.repository;
/**
 *
 * @author USUARIO
 */
import org.springframework.data.jpa.repository.JpaRepository;
import co.unicauca.comunicacionmicroservicios.domain.model.*;

public interface IProyectoGradoRepository extends JpaRepository<ProyectoGrado, Integer> {
    long countByDirectorId(Integer directorId);
 }

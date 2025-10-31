/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.comunicacionmicroservicios.controller;

/**
 * Controlador básico de salud (health check).
 * Los endpoints principales están en FormatoAController y AnteproyectoController.
 */

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @GetMapping("/health")
    public String health() {
        return "Submission Service OK";
    }
}

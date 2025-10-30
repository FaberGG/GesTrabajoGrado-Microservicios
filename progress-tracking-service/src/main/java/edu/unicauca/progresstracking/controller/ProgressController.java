package edu.unicauca.progresstracking.controller;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    // Lista temporal (simula base de datos)
    private final List<Map<String, Object>> eventos = new ArrayList<>();

    // Contador para IDs ascendentes
    private int contadorId = 1;

    // POST /api/progress/eventos
    @PostMapping("/eventos")
    public Map<String, Object> registrarEvento(@RequestBody Map<String, Object> evento) {
        evento.put("id", contadorId++); // 🔹 ID numérico y ascendente
        evento.put("fechaRegistro", LocalDateTime.now().toString());
        eventos.add(evento);

        return Map.of(
                "mensaje", "Evento registrado exitosamente",
                "evento", evento
        );
    }

    // GET /api/progress/eventos
    @GetMapping("/eventos")
    public List<Map<String, Object>> listarEventos() {
        return eventos;
    }

    // GET /api/progress/proyectos/{id}/historial
    @GetMapping("/proyectos/{id}/historial")
    public List<Map<String, Object>> obtenerHistorialPorProyecto(@PathVariable("id") int id) {
        List<Map<String, Object>> historial = new ArrayList<>();
        for (Map<String, Object> evento : eventos) {
            Object proyectoId = evento.get("proyectoId");
            if (proyectoId != null && Integer.parseInt(proyectoId.toString()) == id) {
                historial.add(evento);
            }
        }
        return historial;
    }

    // GET /api/progress/proyectos/{id}/estado
    @GetMapping("/proyectos/{id}/estado")
    public Map<String, Object> obtenerEstadoActual(@PathVariable("id") int id) {
        Map<String, Object> ultimoEvento = null;
        for (Map<String, Object> evento : eventos) {
            Object proyectoId = evento.get("proyectoId");
            if (proyectoId != null && Integer.parseInt(proyectoId.toString()) == id) {
                ultimoEvento = evento;
            }
        }
        if (ultimoEvento == null) {
            return Map.of("mensaje", "No se encontraron eventos para este proyecto");
        } else {
            return Map.of("estadoActual", ultimoEvento);
        }
    }

    // DELETE /api/progress/eventos/{id}
    @DeleteMapping("/eventos/{id}")
    public Map<String, Object> eliminarEventoPorId(@PathVariable("id") int id) {
        Map<String, Object> eventoEliminado = null;
        for (Map<String, Object> evento : new ArrayList<>(eventos)) {
            Object idEvento = evento.get("id");
            if (idEvento != null && Integer.parseInt(idEvento.toString()) == id) {
                eventoEliminado = evento;
                eventos.remove(evento);
                break;
            }
        }

        if (eventoEliminado == null) {
            return Map.of("mensaje", "No se encontró ningún evento con el id especificado");
        }

        return Map.of(
                "mensaje", "Evento eliminado correctamente",
                "eventoEliminado", eventoEliminado
        );
    }
}

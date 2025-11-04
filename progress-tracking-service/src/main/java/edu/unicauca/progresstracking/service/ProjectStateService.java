package edu.unicauca.progresstracking.service;

import edu.unicauca.progresstracking.domain.entity.ProyectoEstado;
import edu.unicauca.progresstracking.domain.repository.ProyectoEstadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para proyectar eventos en la vista materializada (CQRS Read Model)
 *
 * RESPONSABILIDADES:
 * 1. Crear o actualizar ProyectoEstado según eventos recibidos
 * 2. Aplicar lógica de transición de estados
 * 3. Mantener datos desnormalizados para queries rápidas
 *
 * IMPORTANTE: Este servicio NO valida reglas de negocio, solo proyecta eventos
 * ya validados por el Write Model (submission-service, review-service)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectStateService {

    private final ProyectoEstadoRepository proyectoEstadoRepository;

    // ==========================================
    // MÉTODOS PARA FORMATO A
    // ==========================================

    /**
     * Actualiza estado tras envío/reenvío de Formato A
     */
    @Transactional
    public void actualizarEstadoFormatoA(
            Long proyectoId,
            String titulo,
            Integer version,
            String nuevoEstado,
            Long directorId,
            Map<String, Object> payload
    ) {
        log.debug("📝 Actualizando Formato A - Proyecto: {}, Versión: {}, Estado: {}",
                proyectoId, version, nuevoEstado);

        // Obtener o crear estado
        ProyectoEstado estado = proyectoEstadoRepository.findById(proyectoId)
                .orElseGet(() -> crearEstadoInicial(proyectoId, titulo, directorId));

        // Actualizar datos básicos
        estado.setTitulo(titulo);
        estado.setEstadoActual(nuevoEstado);
        estado.setFase("FORMATO_A");

        // Actualizar campos específicos de Formato A
        estado.setFormatoAVersion(version);
        estado.setFormatoAIntentoActual(version);
        estado.setFormatoAEstado("EN_EVALUACION");
        estado.setFormatoAFechaUltimoEnvio(LocalDateTime.now());

        // Extraer datos adicionales del payload
        if (payload.containsKey("estudiante1Id")) {
            // Aquí podrías guardar IDs de estudiantes si los necesitas
        }

        estado.setUltimaActualizacion(LocalDateTime.now());
        proyectoEstadoRepository.save(estado);

        log.info("✅ Estado Formato A actualizado - Proyecto: {} -> {}", proyectoId, nuevoEstado);
    }

    /**
     * Actualiza estado tras evaluación de Formato A
     */
    @Transactional
    public void actualizarEstadoEvaluacionFormatoA(
            Long proyectoId,
            String nuevoEstado,
            String resultado,
            Boolean rechazadoDefinitivo
    ) {
        log.debug("📊 Actualizando evaluación Formato A - Proyecto: {}, Estado: {}",
                proyectoId, nuevoEstado);

        ProyectoEstado estado = proyectoEstadoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado: " + proyectoId));

        estado.setEstadoActual(nuevoEstado);
        estado.setFormatoAFechaUltimaEvaluacion(LocalDateTime.now());

        if ("APROBADO".equals(resultado)) {
            estado.setFormatoAEstado("APROBADO");
        } else {
            if (rechazadoDefinitivo) {
                estado.setFormatoAEstado("RECHAZADO_DEFINITIVO");
            } else {
                estado.setFormatoAEstado("RECHAZADO");
            }
        }

        estado.setUltimaActualizacion(LocalDateTime.now());
        proyectoEstadoRepository.save(estado);

        log.info("✅ Evaluación Formato A actualizada - Proyecto: {} -> {}", proyectoId, nuevoEstado);
    }

    /**
     * Marca proyecto como rechazado definitivamente
     */
    @Transactional
    public void actualizarEstadoRechazadoDefinitivo(Long proyectoId) {
        log.debug("🚫 Marcando proyecto como rechazado definitivo: {}", proyectoId);

        ProyectoEstado estado = proyectoEstadoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado: " + proyectoId));

        estado.setEstadoActual("FORMATO_A_RECHAZADO_DEFINITIVO");
        estado.setFormatoAEstado("RECHAZADO_DEFINITIVO");
        estado.setFase("FORMATO_A");
        estado.setUltimaActualizacion(LocalDateTime.now());

        proyectoEstadoRepository.save(estado);

        log.info("✅ Proyecto {} marcado como RECHAZADO_DEFINITIVO", proyectoId);
    }

    // ==========================================
    // MÉTODOS PARA ANTEPROYECTO
    // ==========================================

    /**
     * Actualiza estado tras envío de Anteproyecto
     */
    @Transactional
    public void actualizarEstadoAnteproyecto(Long proyectoId, String nuevoEstado, Map<String, Object> payload) {
        log.debug("📄 Actualizando Anteproyecto - Proyecto: {}, Estado: {}", proyectoId, nuevoEstado);

        ProyectoEstado estado = proyectoEstadoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado: " + proyectoId));

        estado.setEstadoActual(nuevoEstado);
        estado.setFase("ANTEPROYECTO");
        estado.setAnteproyectoEstado("ENVIADO");
        estado.setAnteproyectoFechaEnvio(LocalDateTime.now());
        estado.setUltimaActualizacion(LocalDateTime.now());

        proyectoEstadoRepository.save(estado);

        log.info("✅ Estado Anteproyecto actualizado - Proyecto: {} -> {}", proyectoId, nuevoEstado);
    }

    /**
     * Actualiza estado tras evaluación de Anteproyecto
     */
    @Transactional
    public void actualizarEstadoEvaluacionAnteproyecto(Long proyectoId, String nuevoEstado) {
        log.debug("📊 Actualizando evaluación Anteproyecto - Proyecto: {}, Estado: {}",
                proyectoId, nuevoEstado);

        ProyectoEstado estado = proyectoEstadoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado: " + proyectoId));

        estado.setEstadoActual(nuevoEstado);
        estado.setAnteproyectoEstado(nuevoEstado.contains("APROBADO") ? "APROBADO" : "RECHAZADO");
        estado.setUltimaActualizacion(LocalDateTime.now());

        proyectoEstadoRepository.save(estado);

        log.info("✅ Evaluación Anteproyecto actualizada - Proyecto: {} -> {}", proyectoId, nuevoEstado);
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    /**
     * Crea el estado inicial de un proyecto
     */
    private ProyectoEstado crearEstadoInicial(Long proyectoId, String titulo, Long directorId) {
        log.info("🆕 Creando estado inicial para proyecto: {}", proyectoId);

        return ProyectoEstado.builder()
                .proyectoId(proyectoId)
                .titulo(titulo != null ? titulo : "Proyecto " + proyectoId)
                .estadoActual("CREADO")
                .fase("INICIAL")
                .formatoAVersion(0)
                .formatoAIntentoActual(0)
                .formatoAMaxIntentos(3)
                .anteproyectoEvaluadoresAsignados(false)
                .directorId(directorId)
                .ultimaActualizacion(LocalDateTime.now())
                .build();
    }

    // ==========================================
    // MÉTODOS DE CONSULTA (PARA CONTROLADORES)
    // ==========================================

    /**
     * Convierte el estado técnico a formato legible para el usuario
     */
    public String convertirEstadoLegible(String estado) {
        Map<String, String> traducciones = new HashMap<>();

        // Estados de Formato A
        traducciones.put("EN_PRIMERA_EVALUACION_FORMATO_A", "En primera evaluación - Formato A");
        traducciones.put("FORMATO_A_RECHAZADO_1", "Rechazado - Primera evaluación (puede reenviar)");
        traducciones.put("EN_SEGUNDA_EVALUACION_FORMATO_A", "En segunda evaluación - Formato A");
        traducciones.put("FORMATO_A_RECHAZADO_2", "Rechazado - Segunda evaluación (puede reenviar)");
        traducciones.put("EN_TERCERA_EVALUACION_FORMATO_A", "En tercera evaluación - Formato A (última oportunidad)");
        traducciones.put("FORMATO_A_RECHAZADO_3", "Rechazado - Tercera evaluación");
        traducciones.put("FORMATO_A_APROBADO", "Formato A Aprobado ✅");
        traducciones.put("FORMATO_A_RECHAZADO_DEFINITIVO", "Formato A Rechazado Definitivamente ❌");

        // Estados de Anteproyecto
        traducciones.put("ANTEPROYECTO_ENVIADO", "Anteproyecto enviado - Pendiente asignación");
        traducciones.put("ANTEPROYECTO_EN_EVALUACION", "Anteproyecto en evaluación");
        traducciones.put("ANTEPROYECTO_APROBADO", "Anteproyecto Aprobado ✅");
        traducciones.put("ANTEPROYECTO_RECHAZADO", "Anteproyecto Rechazado");

        return traducciones.getOrDefault(estado, estado);
    }

    /**
     * Determina el siguiente paso que debe realizar el usuario
     */
    public String determinarSiguientePaso(String estadoActual) {
        Map<String, String> siguientesPasos = new HashMap<>();

        siguientesPasos.put("EN_PRIMERA_EVALUACION_FORMATO_A", "Esperar evaluación del coordinador");
        siguientesPasos.put("EN_SEGUNDA_EVALUACION_FORMATO_A", "Esperar evaluación del coordinador");
        siguientesPasos.put("EN_TERCERA_EVALUACION_FORMATO_A", "Esperar evaluación del coordinador (última oportunidad)");
        siguientesPasos.put("FORMATO_A_RECHAZADO_1", "Corregir y reenviar Formato A");
        siguientesPasos.put("FORMATO_A_RECHAZADO_2", "Corregir y reenviar Formato A (última oportunidad)");
        siguientesPasos.put("FORMATO_A_RECHAZADO_3", "Revisar observaciones y consultar con el coordinador");
        siguientesPasos.put("FORMATO_A_APROBADO", "Subir anteproyecto");
        siguientesPasos.put("FORMATO_A_RECHAZADO_DEFINITIVO", "Proyecto rechazado - Consultar con coordinación");
        siguientesPasos.put("ANTEPROYECTO_ENVIADO", "Esperar asignación de evaluadores");
        siguientesPasos.put("ANTEPROYECTO_EN_EVALUACION", "Esperar evaluación de evaluadores");
        siguientesPasos.put("ANTEPROYECTO_APROBADO", "Preparar defensa del proyecto");

        return siguientesPasos.getOrDefault(estadoActual, "Consultar con el director del proyecto");
    }
}

    /**
     * Actualiza el estado materializado de un proyecto
     *
     * @param proyectoId ID del proyecto
     * @param nuevoEstado Nuevo estado del proyecto
     * @param event Evento que originó el cambio
     */
    @Transactional
    public void actualizarEstado(Long proyectoId, String nuevoEstado, ProjectEvent event) {
        log.debug("Actualizando estado del proyecto {} a: {}", proyectoId, nuevoEstado);

        // Obtener o crear el estado del proyecto
        ProyectoEstado estado = proyectoEstadoRepository.findById(proyectoId)
                .orElseGet(() -> crearEstadoInicial(proyectoId, event));

        // Actualizar estado general
        estado.setEstadoActual(nuevoEstado);
        estado.setFase(determinarFase(nuevoEstado));
        estado.setUltimaActualizacion(LocalDateTime.now());

        // Actualizar campos específicos según el estado
        actualizarCamposEspecificos(estado, nuevoEstado, event);

        // Guardar cambios
        proyectoEstadoRepository.save(estado);
        log.debug("Estado actualizado exitosamente");
    }

    /**
     * Crea el estado inicial de un proyecto
     */
    private ProyectoEstado crearEstadoInicial(Long proyectoId, ProjectEvent event) {
        log.info("Creando estado inicial para proyecto: {}", proyectoId);

        return ProyectoEstado.builder()
                .proyectoId(proyectoId)
                .titulo("Proyecto " + proyectoId)
                .estadoActual("CREADO")
                .fase("INICIAL")
                .formatoAVersion(0)
                .formatoAIntentoActual(0)
                .formatoAMaxIntentos(3)
                .anteproyectoEvaluadoresAsignados(false)
                .directorId(event.getUsuarioResponsableId())
                .directorNombre(event.getUsuarioResponsableNombre())
                .ultimaActualizacion(LocalDateTime.now())
                .build();
    }

    /**
     * Determina la fase del proyecto según su estado
     */
    private String determinarFase(String estado) {
        if (estado.contains("FORMATO_A")) {
            return "FORMATO_A";
        } else if (estado.contains("ANTEPROYECTO")) {
            return "ANTEPROYECTO";
        } else if (estado.contains("DEFENSA")) {
            return "DEFENSA";
        }
        return "INICIAL";
    }

    /**
     * Actualiza campos específicos según el tipo de estado
     */
    private void actualizarCamposEspecificos(ProyectoEstado estado, String nuevoEstado, ProjectEvent event) {
        // Estados de Formato A
        if (nuevoEstado.startsWith("FORMATO_A_EN_EVALUACION_")) {
            int version = Integer.parseInt(nuevoEstado.substring(nuevoEstado.length() - 1));
            estado.setFormatoAVersion(version);
            estado.setFormatoAIntentoActual(version);
            estado.setFormatoAEstado("EN_EVALUACION");
            estado.setFormatoAFechaUltimoEnvio(LocalDateTime.now());

        } else if ("FORMATO_A_APROBADO".equals(nuevoEstado)) {
            estado.setFormatoAEstado("APROBADO");
            estado.setFormatoAFechaUltimaEvaluacion(LocalDateTime.now());

        } else if (nuevoEstado.startsWith("FORMATO_A_RECHAZADO_")) {
            estado.setFormatoAEstado("RECHAZADO");
            estado.setFormatoAFechaUltimaEvaluacion(LocalDateTime.now());

            if ("FORMATO_A_RECHAZADO_DEFINITIVO".equals(nuevoEstado)) {
                estado.setFormatoAEstado("RECHAZADO_DEFINITIVO");
            }
        }

        // Estados de Anteproyecto
        else if ("ANTEPROYECTO_ENVIADO".equals(nuevoEstado)) {
            estado.setAnteproyectoEstado("ENVIADO");
            estado.setAnteproyectoFechaEnvio(LocalDateTime.now());

        } else if ("ANTEPROYECTO_EN_EVALUACION".equals(nuevoEstado)) {
            estado.setAnteproyectoEstado("EN_EVALUACION");
            estado.setAnteproyectoEvaluadoresAsignados(true);

        } else if ("ANTEPROYECTO_APROBADO".equals(nuevoEstado)) {
            estado.setAnteproyectoEstado("APROBADO");
        }
    }

    /**
     * Convierte el estado técnico a formato legible para el usuario
     */
    public String convertirEstadoLegible(String estado) {
        Map<String, String> traducciones = new HashMap<>();

        // Estados de Formato A
        traducciones.put("FORMATO_A_EN_EVALUACION_1", "En primera evaluación - Formato A");
        traducciones.put("FORMATO_A_RECHAZADO_1", "Rechazado - Primera evaluación (puede reenviar)");
        traducciones.put("FORMATO_A_EN_EVALUACION_2", "En segunda evaluación - Formato A");
        traducciones.put("FORMATO_A_RECHAZADO_2", "Rechazado - Segunda evaluación (puede reenviar)");
        traducciones.put("FORMATO_A_EN_EVALUACION_3", "En tercera evaluación - Formato A (última oportunidad)");
        traducciones.put("FORMATO_A_RECHAZADO_3", "Rechazado - Tercera evaluación");
        traducciones.put("FORMATO_A_APROBADO", "Formato A Aprobado ✅");
        traducciones.put("FORMATO_A_RECHAZADO_DEFINITIVO", "Formato A Rechazado Definitivamente ❌");

        // Estados de Anteproyecto
        traducciones.put("ANTEPROYECTO_ENVIADO", "Anteproyecto enviado - Pendiente asignación");
        traducciones.put("ANTEPROYECTO_EN_EVALUACION", "Anteproyecto en evaluación");
        traducciones.put("ANTEPROYECTO_APROBADO", "Anteproyecto Aprobado ✅");

        return traducciones.getOrDefault(estado, estado);
    }

    /**
     * Determina el siguiente paso que debe realizar el usuario
     */
    public String determinarSiguientePaso(String estadoActual) {
        Map<String, String> siguientesPasos = new HashMap<>();

        siguientesPasos.put("FORMATO_A_EN_EVALUACION_1", "Esperar evaluación del coordinador");
        siguientesPasos.put("FORMATO_A_EN_EVALUACION_2", "Esperar evaluación del coordinador");
        siguientesPasos.put("FORMATO_A_EN_EVALUACION_3", "Esperar evaluación del coordinador (última oportunidad)");
        siguientesPasos.put("FORMATO_A_RECHAZADO_1", "Corregir y reenviar Formato A");
        siguientesPasos.put("FORMATO_A_RECHAZADO_2", "Corregir y reenviar Formato A (última oportunidad)");
        siguientesPasos.put("FORMATO_A_RECHAZADO_3", "Revisar observaciones y consultar con el coordinador");
        siguientesPasos.put("FORMATO_A_APROBADO", "Subir anteproyecto");
        siguientesPasos.put("FORMATO_A_RECHAZADO_DEFINITIVO", "Proyecto rechazado - Consultar con coordinación");
        siguientesPasos.put("ANTEPROYECTO_ENVIADO", "Esperar asignación de evaluadores");
        siguientesPasos.put("ANTEPROYECTO_EN_EVALUACION", "Esperar evaluación de evaluadores");
        siguientesPasos.put("ANTEPROYECTO_APROBADO", "Preparar defensa del proyecto");

        return siguientesPasos.getOrDefault(estadoActual, "Consultar con el director del proyecto");
    }
}
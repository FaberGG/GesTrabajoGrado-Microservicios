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
     * Actualiza estado tras envío/reenvío de Formato A con información completa de estudiantes y director
     */
    @Transactional
    public void actualizarEstadoFormatoAConEstudiantes(
            Long proyectoId,
            String titulo,
            Integer version,
            String nuevoEstado,
            Long directorId,
            String directorNombre,
            Long estudiante1Id,
            String estudiante1Nombre,
            String estudiante1Email,
            Long estudiante2Id,
            String estudiante2Nombre,
            String estudiante2Email,
            Map<String, Object> payload
    ) {
        log.debug("📝 Actualizando Formato A con estudiantes y director - Proyecto: {}, Versión: {}, Estado: {}",
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

        // Actualizar información del director
        if (directorId != null) {
            estado.setDirectorId(directorId);
            estado.setDirectorNombre(directorNombre);
        }

        // Actualizar información de estudiantes
        if (estudiante1Id != null) {
            estado.setEstudiante1Id(estudiante1Id);
            estado.setEstudiante1Nombre(estudiante1Nombre);
            estado.setEstudiante1Email(estudiante1Email);
        }
        if (estudiante2Id != null) {
            estado.setEstudiante2Id(estudiante2Id);
            estado.setEstudiante2Nombre(estudiante2Nombre);
            estado.setEstudiante2Email(estudiante2Email);
        }

        // Extraer información adicional del proyecto si está disponible
        if (payload.containsKey("modalidad")) {
            estado.setModalidad((String) payload.get("modalidad"));
        }
        if (payload.containsKey("programa")) {
            estado.setPrograma((String) payload.get("programa"));
        }

        estado.setUltimaActualizacion(LocalDateTime.now());
        proyectoEstadoRepository.save(estado);

        log.info("✅ Estado Formato A actualizado - Proyecto: {} -> {} - Director: {} - Estudiantes: [{}, {}]",
                proyectoId, nuevoEstado, directorNombre, estudiante1Nombre, estudiante2Nombre);
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
     * Actualiza estado tras envío de Anteproyecto (VERSIÓN ANTIGUA - DEPRECADA)
     * @deprecated Use actualizarEstadoAnteproyectoCompleto en su lugar
     */
    @Deprecated
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
     * Actualiza estado tras envío de Anteproyecto con TODOS los campos del proyecto
     * Esta es la versión completa que debe usarse para asegurar que todos los datos se persistan
     */
    @Transactional
    public void actualizarEstadoAnteproyectoCompleto(
            Long proyectoId,
            String titulo,
            String modalidad,
            String programa,
            String nuevoEstado,
            Long directorId,
            String directorNombre,
            Long codirectorId,
            String codirectorNombre,
            Long estudiante1Id,
            String estudiante1Nombre,
            String estudiante1Email,
            Long estudiante2Id,
            String estudiante2Nombre,
            String estudiante2Email
    ) {
        log.info("🔄 Actualizando estado COMPLETO Anteproyecto - Proyecto: {}, Estado: {}",
                proyectoId, nuevoEstado);

        // Obtener estado existente (debe existir porque ya pasó por Formato A)
        ProyectoEstado estado = proyectoEstadoRepository.findById(proyectoId)
                .orElseThrow(() -> {
                    log.error("❌ Proyecto no encontrado: {}. El Formato A debe ser aprobado primero.", proyectoId);
                    return new RuntimeException("Proyecto no encontrado: " + proyectoId);
                });

        // Actualizar información básica del proyecto (puede haber cambiado)
        estado.setTitulo(titulo);
        estado.setModalidad(modalidad);
        estado.setPrograma(programa);
        estado.setEstadoActual(nuevoEstado);
        estado.setFase("ANTEPROYECTO");

        // Actualizar campos específicos de Anteproyecto
        estado.setAnteproyectoEstado("EN_EVALUACION");
        estado.setAnteproyectoFechaEnvio(LocalDateTime.now());

        // Actualizar Director (puede haber cambiado)
        if (directorId != null) {
            estado.setDirectorId(directorId);
            estado.setDirectorNombre(directorNombre);
        }

        // Actualizar Co-director (opcional)
        if (codirectorId != null) {
            estado.setCodirectorId(codirectorId);
            estado.setCodirectorNombre(codirectorNombre);
        }

        // Actualizar Estudiantes con email
        if (estudiante1Id != null) {
            estado.setEstudiante1Id(estudiante1Id);
            estado.setEstudiante1Nombre(estudiante1Nombre);
            estado.setEstudiante1Email(estudiante1Email);
        }
        if (estudiante2Id != null) {
            estado.setEstudiante2Id(estudiante2Id);
            estado.setEstudiante2Nombre(estudiante2Nombre);
            estado.setEstudiante2Email(estudiante2Email);
        }

        estado.setUltimaActualizacion(LocalDateTime.now());
        proyectoEstadoRepository.save(estado);

        log.info("✅ Estado COMPLETO Anteproyecto actualizado - Proyecto: {}, Modalidad: {}, Programa: {}, Director: {}, Estudiantes: [{}, {}]",
                proyectoId, modalidad, programa, directorNombre, estudiante1Nombre, estudiante2Nombre);
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

    /**
     * Actualiza estado cuando se asignan evaluadores al anteproyecto
     */
    @Transactional
    public void actualizarEstadoEvaluadoresAsignados(Long proyectoId) {
        log.debug("👥 Asignando evaluadores al Anteproyecto - Proyecto: {}", proyectoId);

        ProyectoEstado estado = proyectoEstadoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado: " + proyectoId));

        estado.setEstadoActual("ANTEPROYECTO_EN_EVALUACION");
        estado.setAnteproyectoEstado("EN_EVALUACION");
        estado.setAnteproyectoEvaluadoresAsignados(true);
        estado.setUltimaActualizacion(LocalDateTime.now());

        proyectoEstadoRepository.save(estado);

        log.info("✅ Evaluadores asignados - Proyecto: {} -> ANTEPROYECTO_EN_EVALUACION", proyectoId);
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

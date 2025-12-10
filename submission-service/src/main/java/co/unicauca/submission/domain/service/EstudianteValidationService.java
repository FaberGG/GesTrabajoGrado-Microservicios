package co.unicauca.submission.domain.service;

import co.unicauca.submission.application.port.out.IProyectoRepositoryPort;
import co.unicauca.submission.domain.exception.EstudianteYaTieneProyectoException;
import co.unicauca.submission.domain.model.EstadoProyecto;
import co.unicauca.submission.domain.model.Proyecto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de dominio para validar reglas de negocio relacionadas con estudiantes.
 *
 * En DDD, los Domain Services encapsulan lógica de negocio que:
 * - No pertenece naturalmente a ninguna entidad
 * - Requiere coordinar múltiples agregados
 * - Implementa reglas de negocio complejas
 */
@Service
public class EstudianteValidationService {

    private static final Logger log = LoggerFactory.getLogger(EstudianteValidationService.class);

    private final IProyectoRepositoryPort repositoryPort;

    public EstudianteValidationService(IProyectoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    /**
     * Valida que un estudiante no tenga un proyecto activo.
     *
     * Regla de negocio: Un estudiante solo puede participar en UN proyecto activo a la vez.
     *
     * Estados considerados "activos" (no finales):
     * - FORMATO_A_DILIGENCIADO
     * - EN_EVALUACION_COORDINADOR
     * - CORRECCIONES_SOLICITADAS
     * - FORMATO_A_APROBADO
     * - ANTEPROYECTO_ENVIADO
     * - EN_EVALUACION_ANTEPROYECTO
     *
     * Estados finales (no se consideran activos):
     * - FORMATO_A_RECHAZADO (rechazado definitivamente)
     * - ANTEPROYECTO_RECHAZADO
     * - ANTEPROYECTO_APROBADO (proyecto completado)
     *
     * @param estudianteId ID del estudiante a validar
     * @param proyectoIdExcluir ID del proyecto actual (para excluir en la búsqueda, null si es nuevo)
     * @throws EstudianteYaTieneProyectoException si el estudiante ya tiene un proyecto activo
     */
    public void validarEstudianteNoTieneProyectoActivo(Long estudianteId, Long proyectoIdExcluir) {
        log.debug("Validando que estudiante {} no tenga proyecto activo (excluir: {})",
                 estudianteId, proyectoIdExcluir);

        // Buscar proyectos donde el estudiante participe
        List<Proyecto> proyectosEstudiante = repositoryPort.findByEstudianteId(estudianteId);

        for (Proyecto proyecto : proyectosEstudiante) {
            // Excluir el proyecto actual si se está actualizando
            if (proyectoIdExcluir != null && proyecto.getId().getValue().equals(proyectoIdExcluir)) {
                continue;
            }

            // Verificar si el proyecto está en estado activo (no final)
            if (esEstadoActivo(proyecto.getEstado())) {
                log.warn("Estudiante {} ya tiene proyecto activo: ID={}, Estado={}",
                        estudianteId, proyecto.getId().getValue(), proyecto.getEstado());

                throw new EstudianteYaTieneProyectoException(
                    estudianteId,
                    proyecto.getId().getValue()
                );
            }
        }

        log.debug("✅ Validación exitosa: Estudiante {} no tiene proyectos activos", estudianteId);
    }

    /**
     * Determina si un estado es considerado "activo" (el proyecto aún está en curso).
     *
     * @param estado Estado del proyecto
     * @return true si el estado es activo (no final)
     */
    private boolean esEstadoActivo(EstadoProyecto estado) {
        return switch (estado) {
            // Estados activos
            case FORMATO_A_DILIGENCIADO,
                 EN_EVALUACION_COORDINADOR,
                 CORRECCIONES_SOLICITADAS,
                 FORMATO_A_APROBADO,
                 ANTEPROYECTO_ENVIADO,
                 ANTEPROYECTO_EN_EVALUACION -> true;

            // Estados finales (proyecto terminado o rechazado definitivamente)
            case FORMATO_A_RECHAZADO,
                 ANTEPROYECTO_RECHAZADO,
                 ANTEPROYECTO_APROBADO -> false;
        };
    }
}


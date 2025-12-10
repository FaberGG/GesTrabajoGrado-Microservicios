package co.unicauca.submission.application.port.in;

import co.unicauca.submission.application.dto.response.ProyectoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Puerto de entrada (Query) para listar Formatos A pendientes de evaluación.
 * RF3: Yo como coordinador necesito ver los Formatos A pendientes por evaluar.
 */
public interface IListarFormatoAPendientesQuery {

    /**
     * Obtiene todos los Formatos A que están pendientes de evaluación por el coordinador.
     * Filtra proyectos en estado EN_EVALUACION_COORDINADOR.
     *
     * @param pageable Configuración de paginación
     * @return Página con los proyectos pendientes
     */
    Page<ProyectoResponse> listarPendientes(Pageable pageable);
}


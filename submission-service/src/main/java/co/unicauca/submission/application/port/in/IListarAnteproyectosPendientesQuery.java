package co.unicauca.submission.application.port.in;

import co.unicauca.submission.application.dto.response.ProyectoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Query para listar Anteproyectos Pendientes de Asignación.
 * RF8: El jefe de departamento necesita ver los anteproyectos pendientes para asignar evaluadores.
 *
 * Este query filtra proyectos en estado ANTEPROYECTO_ENVIADO (pendientes de asignación de evaluadores).
 */
public interface IListarAnteproyectosPendientesQuery {

    /**
     * Lista anteproyectos que están pendientes de asignación de evaluadores.
     * Filtra proyectos en estado ANTEPROYECTO_ENVIADO.
     *
     * @param pageable Configuración de paginación
     * @return Página de proyectos con anteproyecto pendiente de asignación
     */
    Page<ProyectoResponse> listarPendientes(Pageable pageable);
}


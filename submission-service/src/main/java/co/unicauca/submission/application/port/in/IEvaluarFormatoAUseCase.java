package co.unicauca.submission.application.port.in;

import co.unicauca.submission.application.dto.request.EvaluarFormatoARequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;

/**
 * Puerto de entrada (Use Case) para evaluar el Formato A.
 * RF3: Yo como coordinador de programa necesito evaluar un formato A para aprobar, rechazar y dejar observaciones.
 */
public interface IEvaluarFormatoAUseCase {

    /**
     * Evalúa el Formato A (aprobar o rechazar).
     *
     * @param proyectoId ID del proyecto
     * @param request Resultado de la evaluación
     * @param evaluadorId ID del coordinador que evalúa
     * @return Proyecto actualizado con nuevo estado
     * @throws EstadoInvalidoException si no está en evaluación
     * @throws UsuarioNoAutorizadoException si no es coordinador
     * @throws ProyectoNotFoundException si el proyecto no existe
     */
    ProyectoResponse evaluar(Long proyectoId, EvaluarFormatoARequest request, Long evaluadorId);
}


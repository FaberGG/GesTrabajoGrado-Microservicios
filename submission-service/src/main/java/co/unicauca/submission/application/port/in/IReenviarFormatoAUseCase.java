package co.unicauca.submission.application.port.in;

import co.unicauca.submission.application.dto.request.ReenviarFormatoARequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;

/**
 * Puerto de entrada (Use Case) para reenviar una nueva versión del Formato A.
 * RF4: Yo como docente necesito subir una nueva versión del formato A cuando hubo una evaluación de rechazado.
 */
public interface IReenviarFormatoAUseCase {

    /**
     * Reenvía una nueva versión del Formato A tras correcciones.
     *
     * @param proyectoId ID del proyecto
     * @param request Datos de la nueva versión
     * @param userId ID del usuario (debe ser director)
     * @return Proyecto actualizado
     * @throws MaximosIntentosExcedidosException si ya tiene 3 intentos
     * @throws UsuarioNoAutorizadoException si no es el director
     * @throws ProyectoNotFoundException si el proyecto no existe
     */
    ProyectoResponse reenviar(Long proyectoId, ReenviarFormatoARequest request, Long userId);
}


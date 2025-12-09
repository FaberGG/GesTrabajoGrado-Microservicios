package co.unicauca.submission.application.port.in;

import co.unicauca.submission.application.dto.request.SubirAnteproyectoRequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;

/**
 * Puerto de entrada (Use Case) para subir el anteproyecto.
 * RF6: Yo como docente necesito subir el anteproyecto para continuar con el proceso de proyecto de grado.
 */
public interface ISubirAnteproyectoUseCase {

    /**
     * Sube el anteproyecto tras aprobación del Formato A.
     *
     * @param proyectoId ID del proyecto
     * @param request Datos del anteproyecto
     * @param userId ID del usuario (debe ser director)
     * @return Proyecto actualizado
     * @throws FormatoANoAprobadoException si el Formato A no está aprobado
     * @throws UsuarioNoAutorizadoException si no es el director
     * @throws DomainException si ya existe anteproyecto
     * @throws ProyectoNotFoundException si el proyecto no existe
     */
    ProyectoResponse subir(Long proyectoId, SubirAnteproyectoRequest request, Long userId);
}


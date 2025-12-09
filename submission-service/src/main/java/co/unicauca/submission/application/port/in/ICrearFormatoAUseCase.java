package co.unicauca.submission.application.port.in;

import co.unicauca.submission.application.dto.request.CrearFormatoARequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;

/**
 * Puerto de entrada (Use Case) para crear un Formato A inicial.
 * RF2: Yo como docente necesito subir un el formato A para comenzar el proceso de proyecto de grado.
 */
public interface ICrearFormatoAUseCase {

    /**
     * Crea un nuevo proyecto con su Formato A inicial.
     *
     * @param request Datos del formato A
     * @param userId ID del usuario que crea (debe ser docente director)
     * @return Proyecto creado con su ID
     * @throws UsuarioNoAutorizadoException si no es docente
     * @throws IllegalArgumentException si faltan datos obligatorios
     */
    ProyectoResponse crear(CrearFormatoARequest request, Long userId);
}


package co.unicauca.submission.application.port.out;

/**
 * Puerto de salida para comunicación con el servicio de identidad.
 */
public interface IIdentityServicePort {

    /**
     * Obtiene información básica de un usuario.
     */
    UsuarioInfo obtenerUsuario(Long userId);

    /**
     * Obtiene el email del coordinador del programa.
     */
    String obtenerEmailCoordinador();

    /**
     * Obtiene el email del jefe de departamento.
     */
    String obtenerEmailJefeDepartamento();

    /**
     * Valida si un usuario tiene un rol específico.
     */
    boolean tieneRol(Long userId, String rol);

    /**
     * DTO con información básica de un usuario.
     */
    record UsuarioInfo(
        Long id,
        String nombreCompleto,
        String email,
        String programa,
        String rol
    ) {}
}


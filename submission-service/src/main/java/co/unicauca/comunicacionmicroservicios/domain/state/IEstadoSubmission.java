package co.unicauca.comunicacionmicroservicios.domain.state;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;

/**
 * Interfaz para el patrón State
 * Define las operaciones que cada estado puede realizar
 */
public interface IEstadoSubmission {
    
    /**
     * Presenta el formato A al coordinador
     */
    void presentarAlCoordinador(ProyectoSubmission proyecto);
    
    /**
     * El coordinador envía el formato A al comité
     */
    void enviarAComite(ProyectoSubmission proyecto);
    
    /**
     * El comité evalúa el formato A (aprobar o rechazar)
     */
    void evaluar(ProyectoSubmission proyecto, boolean aprobado, String comentarios);
    
    /**
     * El docente sube una nueva versión del formato A tras correcciones
     */
    void subirNuevaVersion(ProyectoSubmission proyecto);
    
    /**
     * Obtiene el nombre del estado actual
     */
    String getNombreEstado();
    
    /**
     * Verifica si este es un estado final (no permite más transiciones)
     */
    boolean esEstadoFinal();
}


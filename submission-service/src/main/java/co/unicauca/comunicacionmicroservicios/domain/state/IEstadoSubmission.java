package co.unicauca.comunicacionmicroservicios.domain.state;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;

/**
 * Interfaz para el patron State
 * Define las operaciones que cada estado puede realizar
 *
 * Flujo de estados:
 *
 * FORMATO A (RF-3):
 * FORMATO_A_DILIGENCIADO -> EN_EVALUACION_COORDINADOR -> FORMATO_A_APROBADO/RECHAZADO
 *
 * ANTEPROYECTO (RF-6, RF-7, RF-8):
 * FORMATO_A_APROBADO -> ANTEPROYECTO_ENVIADO -> ANTEPROYECTO_EN_EVALUACION -> ANTEPROYECTO_APROBADO/RECHAZADO
 */
public interface IEstadoSubmission {

    // ==========================================
    // OPERACIONES PARA FORMATO A
    // ==========================================

    /**
     * Presenta el formato A al coordinador para evaluacion
     * Transicion: FORMATO_A_DILIGENCIADO -> EN_EVALUACION_COORDINADOR
     */
    void presentarAlCoordinador(ProyectoSubmission proyecto);

    /**
     * El coordinador evalua el formato A (aprobar o rechazar)
     * Transiciones posibles desde EN_EVALUACION_COORDINADOR:
     * - Si aprueba -> FORMATO_A_APROBADO
     * - Si rechaza y intentos menor a 3 -> CORRECCIONES_SOLICITADAS
     * - Si rechaza y intentos >= 3 -> FORMATO_A_RECHAZADO
     */
    void evaluar(ProyectoSubmission proyecto, boolean aprobado, String comentarios);

    /**
     * El docente sube una nueva version del formato A tras correcciones
     * Transicion: CORRECCIONES_SOLICITADAS -> EN_EVALUACION_COORDINADOR
     */
    void subirNuevaVersion(ProyectoSubmission proyecto);

    // ==========================================
    // OPERACIONES PARA ANTEPROYECTO
    // ==========================================

    /**
     * El docente sube el anteproyecto tras aprobacion del Formato A (RF-6)
     * Transicion: FORMATO_A_APROBADO -> ANTEPROYECTO_ENVIADO
     */
    void subirAnteproyecto(ProyectoSubmission proyecto, String rutaAnteproyecto);

    /**
     * El jefe de departamento asigna 2 evaluadores al anteproyecto (RF-8)
     * Transicion: ANTEPROYECTO_ENVIADO -> ANTEPROYECTO_EN_EVALUACION
     */
    void asignarEvaluadores(ProyectoSubmission proyecto, Long evaluador1Id, Long evaluador2Id);

    /**
     * Los evaluadores evaluan el anteproyecto (aprobar o rechazar)
     * Transiciones desde ANTEPROYECTO_EN_EVALUACION:
     * - Si aprueba -> ANTEPROYECTO_APROBADO
     * - Si rechaza -> ANTEPROYECTO_RECHAZADO
     */
    void evaluarAnteproyecto(ProyectoSubmission proyecto, boolean aprobado, String comentarios);

    // ==========================================
    // METODOS DE INFORMACION
    // ==========================================

    /**
     * Obtiene el nombre del estado actual
     */
    String getNombreEstado();

    /**
     * Verifica si este es un estado final (no permite mas transiciones)
     */
    boolean esEstadoFinal();
}


package co.unicauca.submission.domain.model;

import co.unicauca.submission.domain.event.DomainEvent;
import co.unicauca.submission.domain.event.FormatoACreado;
import co.unicauca.submission.domain.event.FormatoAEvaluado;
import co.unicauca.submission.domain.event.FormatoAReenviado;
import co.unicauca.submission.domain.event.AnteproyectoSubido;
import co.unicauca.submission.domain.event.EvaluadoresAsignados;
import co.unicauca.submission.domain.event.AnteproyectoEvaluado;
import co.unicauca.submission.domain.exception.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root: Proyecto
 *
 * Representa el ciclo de vida completo de un proyecto de grado,
 * desde el Formato A hasta el Anteproyecto.
 *
 * Encapsula toda la lógica de negocio relacionada con:
 * - Creación y gestión del Formato A
 * - Evaluaciones y reenvíos
 * - Gestión del Anteproyecto
 * - Transiciones de estado
 * - Validaciones de reglas de negocio
 */
public class Proyecto {

    // Identidad
    private ProyectoId id;

    // Información básica
    private Titulo titulo;
    private Modalidad modalidad;
    private ObjetivosProyecto objetivos;
    private Participantes participantes;

    // Estado actual
    private EstadoProyecto estado;

    // Formato A
    private FormatoAInfo formatoA;

    // Anteproyecto (opcional, solo si Formato A aprobado)
    private AnteproyectoInfo anteproyecto;

    // Auditoría
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    // Domain Events pendientes de publicar
    private List<DomainEvent> domainEvents = new ArrayList<>();

    // Constructor privado (usar factory methods)
    private Proyecto() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // ==========================================
    // FACTORY METHODS
    // ==========================================

    /**
     * Factory Method: Crea un proyecto con su Formato A inicial.
     * Estado inicial: FORMATO_A_DILIGENCIADO
     *
     * @param titulo Título del proyecto (validado)
     * @param modalidad Modalidad del proyecto
     * @param objetivos Objetivos del proyecto
     * @param participantes Participantes del proyecto
     * @param pdfFormatoA PDF del Formato A
     * @param cartaAceptacion Carta de aceptación (si aplica)
     * @return Nuevo proyecto creado
     */
    public static Proyecto crearConFormatoA(
            Titulo titulo,
            Modalidad modalidad,
            ObjetivosProyecto objetivos,
            Participantes participantes,
            ArchivoAdjunto pdfFormatoA,
            ArchivoAdjunto cartaAceptacion
    ) {
        // Validar carta si es práctica profesional
        if (modalidad.requiereCarta() && cartaAceptacion == null) {
            throw new IllegalArgumentException(
                "La carta de aceptación es obligatoria para modalidad " + modalidad
            );
        }

        Proyecto proyecto = new Proyecto();
        proyecto.titulo = titulo;
        proyecto.modalidad = modalidad;
        proyecto.objetivos = objetivos;
        proyecto.participantes = participantes;
        proyecto.estado = EstadoProyecto.FORMATO_A_DILIGENCIADO;
        proyecto.formatoA = new FormatoAInfo(1, pdfFormatoA, cartaAceptacion);

        // Registrar evento de dominio
        proyecto.registrarEvento(new FormatoACreado(
            null, // El ID se asignará después del save
            proyecto.titulo.getValue(),
            proyecto.modalidad.name(),
            proyecto.participantes.getDirectorId(),
            1
        ));

        return proyecto;
    }

    // ==========================================
    // COMPORTAMIENTO DE NEGOCIO - FORMATO A
    // ==========================================

    /**
     * Presenta el Formato A al coordinador para evaluación.
     * Transición: FORMATO_A_DILIGENCIADO → EN_EVALUACION_COORDINADOR
     */
    public void presentarAlCoordinador() {
        validarTransicion(EstadoProyecto.FORMATO_A_DILIGENCIADO);

        this.estado = EstadoProyecto.EN_EVALUACION_COORDINADOR;
        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Evalúa el Formato A (aprobar o rechazar).
     * RF3: El coordinador evalúa el Formato A.
     *
     * Transiciones posibles:
     * - Aprobado → FORMATO_A_APROBADO
     * - Rechazado (intentos < 3) → CORRECCIONES_SOLICITADAS
     * - Rechazado (intentos >= 3) → FORMATO_A_RECHAZADO
     *
     * @param aprobado true si aprueba, false si rechaza
     * @param comentarios Observaciones del coordinador
     * @param evaluadorId ID del coordinador que evalúa
     */
    public void evaluarFormatoA(boolean aprobado, String comentarios, Long evaluadorId) {
        validarTransicion(EstadoProyecto.EN_EVALUACION_COORDINADOR);

        Evaluacion evaluacion = new Evaluacion(aprobado, comentarios, evaluadorId);
        this.formatoA.agregarEvaluacion(evaluacion);

        if (aprobado) {
            this.estado = EstadoProyecto.FORMATO_A_APROBADO;
            registrarEvento(new FormatoAEvaluado(
                this.id != null ? this.id.getValue() : null,
                true,
                comentarios,
                evaluadorId,
                this.formatoA.getNumeroIntento()
            ));
        } else {
            // ⚠️ NO incrementar aquí - se incrementa al REENVIAR
            // Verificar si ya alcanzó 3 intentos (está rechazando la tercera versión)
            if (this.formatoA.getNumeroIntento() >= 3) {
                // Tercer rechazo → Estado final
                this.estado = EstadoProyecto.FORMATO_A_RECHAZADO;
                registrarEvento(new FormatoAEvaluado(
                    this.id != null ? this.id.getValue() : null,
                    false,
                    "Rechazado definitivamente tras 3 intentos: " + comentarios,
                    evaluadorId,
                    this.formatoA.getNumeroIntento()
                ));
            } else {
                // Primer o segundo rechazo → Puede reenviar
                this.estado = EstadoProyecto.CORRECCIONES_SOLICITADAS;
                registrarEvento(new FormatoAEvaluado(
                    this.id != null ? this.id.getValue() : null,
                    false,
                    comentarios,
                    evaluadorId,
                    this.formatoA.getNumeroIntento()
                ));
            }
        }

        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Reenvía una nueva versión del Formato A tras correcciones.
     * RF4: El docente sube nueva versión del Formato A.
     *
     * Transición: CORRECCIONES_SOLICITADAS → EN_EVALUACION_COORDINADOR
     *
     * @param nuevoPdf Nuevo PDF del Formato A
     * @param nuevaCarta Nueva carta (si aplica)
     */
    public void reenviarFormatoA(ArchivoAdjunto nuevoPdf, ArchivoAdjunto nuevaCarta) {
        validarTransicion(EstadoProyecto.CORRECCIONES_SOLICITADAS);

        if (this.formatoA.haAlcanzadoMaximoIntentos()) {
            throw new MaximosIntentosExcedidosException(
                "No se puede reenviar, se alcanzó el máximo de 3 intentos"
            );
        }

        // ✅ Incrementar intento AL REENVIAR (no al rechazar)
        this.formatoA.incrementarIntentos();

        // Actualizar archivos
        this.formatoA.actualizarArchivos(nuevoPdf, nuevaCarta);
        this.estado = EstadoProyecto.EN_EVALUACION_COORDINADOR;
        this.fechaModificacion = LocalDateTime.now();

        registrarEvento(new FormatoAReenviado(
            this.id != null ? this.id.getValue() : null,
            this.titulo.getValue(),
            this.formatoA.getNumeroIntento() // Ahora será 2 o 3
        ));
    }

    // ==========================================
    // COMPORTAMIENTO DE NEGOCIO - ANTEPROYECTO
    // ==========================================

    /**
     * Sube el anteproyecto tras aprobación del Formato A.
     * RF6: El director sube el anteproyecto.
     *
     * Transición: FORMATO_A_APROBADO → ANTEPROYECTO_ENVIADO
     *
     * Pre-condiciones:
     * - Formato A debe estar aprobado
     * - Usuario debe ser el director
     * - No debe existir anteproyecto previo
     *
     * @param pdfAnteproyecto PDF del anteproyecto
     * @param directorId ID del usuario que sube (debe ser director)
     */
    public void subirAnteproyecto(ArchivoAdjunto pdfAnteproyecto, Long directorId) {
        // Validar estado
        if (this.estado != EstadoProyecto.FORMATO_A_APROBADO) {
            throw new FormatoANoAprobadoException(
                "El Formato A debe estar aprobado antes de subir el anteproyecto. Estado actual: " + this.estado
            );
        }

        // Validar que el usuario es el director
        if (!this.participantes.esDirector(directorId)) {
            throw new UsuarioNoAutorizadoException(
                "Solo el director del proyecto puede subir el anteproyecto"
            );
        }

        // Validar que no existe anteproyecto previo
        if (this.anteproyecto != null) {
            throw new DomainException("Ya existe un anteproyecto para este proyecto");
        }

        this.anteproyecto = new AnteproyectoInfo(pdfAnteproyecto);
        this.estado = EstadoProyecto.ANTEPROYECTO_ENVIADO;
        this.fechaModificacion = LocalDateTime.now();

        registrarEvento(new AnteproyectoSubido(
            this.id != null ? this.id.getValue() : null,
            this.titulo.getValue(),
            pdfAnteproyecto.getRuta()
        ));
    }

    /**
     * Asigna evaluadores al anteproyecto.
     * RF8: El jefe de departamento asigna 2 evaluadores.
     *
     * Transición: ANTEPROYECTO_ENVIADO → ANTEPROYECTO_EN_EVALUACION
     *
     * @param evaluador1Id ID del primer evaluador
     * @param evaluador2Id ID del segundo evaluador
     */
    public void asignarEvaluadores(Long evaluador1Id, Long evaluador2Id) {
        validarTransicion(EstadoProyecto.ANTEPROYECTO_ENVIADO);

        if (this.anteproyecto == null) {
            throw new DomainException("No existe anteproyecto para asignar evaluadores");
        }

        this.anteproyecto.asignarEvaluadores(evaluador1Id, evaluador2Id);
        this.estado = EstadoProyecto.ANTEPROYECTO_EN_EVALUACION;
        this.fechaModificacion = LocalDateTime.now();

        registrarEvento(new EvaluadoresAsignados(
            this.id != null ? this.id.getValue() : null,
            evaluador1Id,
            evaluador2Id
        ));
    }

    /**
     * Evalúa el anteproyecto (aprobar o rechazar).
     *
     * Transiciones posibles:
     * - Aprobado → ANTEPROYECTO_APROBADO
     * - Rechazado → ANTEPROYECTO_RECHAZADO
     *
     * @param aprobado true si aprueba, false si rechaza
     * @param comentarios Observaciones de los evaluadores
     * @param evaluadorId ID del evaluador que evalúa
     */
    public void evaluarAnteproyecto(boolean aprobado, String comentarios, Long evaluadorId) {
        validarTransicion(EstadoProyecto.ANTEPROYECTO_EN_EVALUACION);

        if (this.anteproyecto == null) {
            throw new DomainException("No existe anteproyecto para evaluar");
        }

        Evaluacion evaluacion = new Evaluacion(aprobado, comentarios, evaluadorId);
        this.anteproyecto.agregarEvaluacion(evaluacion);

        if (aprobado) {
            this.estado = EstadoProyecto.ANTEPROYECTO_APROBADO;
        } else {
            this.estado = EstadoProyecto.ANTEPROYECTO_RECHAZADO;
        }

        this.fechaModificacion = LocalDateTime.now();

        registrarEvento(new AnteproyectoEvaluado(
            this.id != null ? this.id.getValue() : null,
            aprobado,
            comentarios
        ));
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    /**
     * Valida que el proyecto esté en el estado esperado.
     */
    private void validarTransicion(EstadoProyecto estadoEsperado) {
        if (!this.estado.equals(estadoEsperado)) {
            throw new EstadoInvalidoException(
                this.estado,
                estadoEsperado
            );
        }
    }

    /**
     * Registra un evento de dominio para ser publicado posteriormente.
     */
    private void registrarEvento(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * Obtiene los eventos de dominio pendientes de publicar.
     */
    public List<DomainEvent> obtenerEventosPendientes() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Limpia los eventos después de haberlos publicado.
     */
    public void limpiarEventos() {
        this.domainEvents.clear();
    }

    /**
     * Verifica si el proyecto está en un estado final.
     */
    public boolean esEstadoFinal() {
        return this.estado.isEstadoFinal();
    }

    /**
     * Actualiza el ID del proyecto (llamado después del save en repositorio).
     */
    public void setId(ProyectoId id) {
        this.id = id;
    }

    // ==========================================
    // GETTERS (sin setters para inmutabilidad)
    // ==========================================

    public ProyectoId getId() {
        return id;
    }

    public Titulo getTitulo() {
        return titulo;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public ObjetivosProyecto getObjetivos() {
        return objetivos;
    }

    public Participantes getParticipantes() {
        return participantes;
    }

    public EstadoProyecto getEstado() {
        return estado;
    }

    public FormatoAInfo getFormatoA() {
        return formatoA;
    }

    public AnteproyectoInfo getAnteproyecto() {
        return anteproyecto;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    @Override
    public String toString() {
        return "Proyecto{" +
                "id=" + id +
                ", titulo=" + titulo +
                ", estado=" + estado +
                ", modalidad=" + modalidad +
                '}';
    }
}


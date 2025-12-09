package co.unicauca.submission.domain.model;

import co.unicauca.submission.domain.event.DomainEvent;
import co.unicauca.submission.domain.event.FormatoACreado;
import co.unicauca.submission.domain.event.FormatoAEvaluado;
import co.unicauca.submission.domain.event.FormatoAReenviado;
import co.unicauca.submission.domain.event.AnteproyectoSubido;
import co.unicauca.submission.domain.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios del Aggregate Proyecto.
 * Tests puros sin dependencias externas (no Spring, no BD, no mocks).
 */
@DisplayName("Proyecto - Aggregate Root")
class ProyectoTest {

    // ==========================================
    // TESTS DE CREACIÓN
    // ==========================================

    @Test
    @DisplayName("Cuando crear proyecto con datos válidos, debe crear exitosamente")
    void cuandoCrearProyectoConDatosValidos_debeCrearExitosamente() {
        // Arrange
        Titulo titulo = Titulo.of("Sistema de gestión académica universitaria");
        Modalidad modalidad = Modalidad.INVESTIGACION;
        ObjetivosProyecto objetivos = ObjetivosProyecto.of(
            "Desarrollar un sistema de gestión",
            Arrays.asList("Objetivo 1", "Objetivo 2")
        );
        Participantes participantes = Participantes.of(1L, null, 2L, null);
        ArchivoAdjunto pdf = ArchivoAdjunto.pdf("/path/to/formatoA.pdf", "formatoA.pdf");

        // Act
        Proyecto proyecto = Proyecto.crearConFormatoA(
            titulo, modalidad, objetivos, participantes, pdf, null
        );

        // Assert
        assertNotNull(proyecto);
        assertEquals(EstadoProyecto.FORMATO_A_DILIGENCIADO, proyecto.getEstado());
        assertEquals(titulo, proyecto.getTitulo());
        assertEquals(modalidad, proyecto.getModalidad());
        assertNotNull(proyecto.getFormatoA());
        assertEquals(1, proyecto.getFormatoA().getNumeroIntento());
        assertNull(proyecto.getAnteproyecto());
    }

    @Test
    @DisplayName("Cuando crear proyecto con práctica profesional sin carta, debe lanzar excepción")
    void cuandoCrearProyectoConPracticaProfesionalSinCarta_debeLanzarExcepcion() {
        // Arrange
        Titulo titulo = Titulo.of("Proyecto de práctica profesional");
        Modalidad modalidad = Modalidad.PRACTICA_PROFESIONAL;
        ObjetivosProyecto objetivos = ObjetivosProyecto.of("Objetivo", Arrays.asList("Esp1"));
        Participantes participantes = Participantes.of(1L, null, 2L, null);
        ArchivoAdjunto pdf = ArchivoAdjunto.pdf("/path/to/formatoA.pdf", "formatoA.pdf");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            Proyecto.crearConFormatoA(titulo, modalidad, objetivos, participantes, pdf, null);
        });
    }

    @Test
    @DisplayName("Cuando crear proyecto, debe registrar evento FormatoACreado")
    void cuandoCrearProyecto_debeRegistrarEventoFormatoACreado() {
        // Arrange & Act
        Proyecto proyecto = crearProyectoBasico();

        // Assert
        List<DomainEvent> eventos = proyecto.obtenerEventosPendientes();
        assertEquals(1, eventos.size());
        assertTrue(eventos.get(0) instanceof FormatoACreado);
    }

    // ==========================================
    // TESTS DE EVALUACIÓN DE FORMATO A
    // ==========================================

    @Test
    @DisplayName("Cuando evaluar Formato A aprobado, debe cambiar a FORMATO_A_APROBADO")
    void cuandoEvaluarFormatoAAprobado_debeCambiarAEstadoAprobado() {
        // Arrange
        Proyecto proyecto = crearProyectoBasico();
        proyecto.presentarAlCoordinador();
        proyecto.limpiarEventos();

        // Act
        proyecto.evaluarFormatoA(true, "Excelente trabajo", 999L);

        // Assert
        assertEquals(EstadoProyecto.FORMATO_A_APROBADO, proyecto.getEstado());
        assertEquals(1, proyecto.getFormatoA().getNumeroIntento());
    }

    @Test
    @DisplayName("Cuando evaluar Formato A rechazado (intento 1), debe cambiar a CORRECCIONES_SOLICITADAS")
    void cuandoEvaluarFormatoARechazadoIntento1_debeCambiarACorrecciones() {
        // Arrange
        Proyecto proyecto = crearProyectoBasico();
        proyecto.presentarAlCoordinador();

        // Act
        proyecto.evaluarFormatoA(false, "Necesita mejoras", 999L);

        // Assert
        assertEquals(EstadoProyecto.CORRECCIONES_SOLICITADAS, proyecto.getEstado());
        assertEquals(2, proyecto.getFormatoA().getNumeroIntento());
    }

    @Test
    @DisplayName("Cuando evaluar Formato A rechazado 3 veces, debe cambiar a FORMATO_A_RECHAZADO")
    void cuandoEvaluarFormatoARechazado3Veces_debeCambiarARechazadoDefinitivo() {
        // Arrange
        Proyecto proyecto = crearProyectoBasico();
        proyecto.presentarAlCoordinador();
        proyecto.evaluarFormatoA(false, "Necesita mejoras", 999L);

        // Segundo intento
        ArchivoAdjunto nuevoPdf = ArchivoAdjunto.pdf("/path/v2.pdf", "v2.pdf");
        proyecto.reenviarFormatoA(nuevoPdf, null);
        proyecto.presentarAlCoordinador(); // Necesario para volver a EN_EVALUACION_COORDINADOR
        proyecto.evaluarFormatoA(false, "Aún necesita mejoras", 999L);

        // Tercer intento
        ArchivoAdjunto nuevoPdf2 = ArchivoAdjunto.pdf("/path/v3.pdf", "v3.pdf");
        proyecto.reenviarFormatoA(nuevoPdf2, null);
        proyecto.presentarAlCoordinador(); // Necesario para volver a EN_EVALUACION_COORDINADOR

        // Act
        proyecto.evaluarFormatoA(false, "No cumple los requisitos", 999L);

        // Assert
        assertEquals(EstadoProyecto.FORMATO_A_RECHAZADO, proyecto.getEstado());
        assertEquals(3, proyecto.getFormatoA().getNumeroIntento());
        assertTrue(proyecto.esEstadoFinal());
    }

    @Test
    @DisplayName("Cuando evaluar sin estar en evaluación, debe lanzar EstadoInvalidoException")
    void cuandoEvaluarSinEstarEnEvaluacion_debeLanzarExcepcion() {
        // Arrange
        Proyecto proyecto = crearProyectoBasico();
        // No llamamos a presentarAlCoordinador()

        // Act & Assert
        assertThrows(EstadoInvalidoException.class, () -> {
            proyecto.evaluarFormatoA(true, "Aprobado", 999L);
        });
    }

    // ==========================================
    // TESTS DE REENVÍO DE FORMATO A
    // ==========================================

    @Test
    @DisplayName("Cuando reenviar Formato A tras correcciones, debe cambiar a EN_EVALUACION_COORDINADOR")
    void cuandoReenviarFormatoATrasCorrecciones_debeCambiarAEnEvaluacion() {
        // Arrange
        Proyecto proyecto = crearProyectoBasico();
        proyecto.presentarAlCoordinador();
        proyecto.evaluarFormatoA(false, "Necesita correcciones", 999L);
        proyecto.limpiarEventos();

        ArchivoAdjunto nuevoPdf = ArchivoAdjunto.pdf("/path/v2.pdf", "v2.pdf");

        // Act
        proyecto.reenviarFormatoA(nuevoPdf, null);

        // Assert
        assertEquals(EstadoProyecto.EN_EVALUACION_COORDINADOR, proyecto.getEstado());
        assertEquals(2, proyecto.getFormatoA().getNumeroIntento());

        List<DomainEvent> eventos = proyecto.obtenerEventosPendientes();
        assertEquals(1, eventos.size());
        assertTrue(eventos.get(0) instanceof FormatoAReenviado);
    }

    @Test
    @DisplayName("Cuando reenviar después de 3 intentos, debe lanzar EstadoInvalidoException")
    void cuandoReenviarDespuesDe3Intentos_debeLanzarExcepcion() {
        // Arrange
        Proyecto proyecto = crearProyectoBasico();
        proyecto.presentarAlCoordinador();
        proyecto.evaluarFormatoA(false, "Necesita mejoras", 999L);

        ArchivoAdjunto pdf2 = ArchivoAdjunto.pdf("/v2.pdf", "v2.pdf");
        proyecto.reenviarFormatoA(pdf2, null);
        proyecto.presentarAlCoordinador();
        proyecto.evaluarFormatoA(false, "Mejoras", 999L);

        ArchivoAdjunto pdf3 = ArchivoAdjunto.pdf("/v3.pdf", "v3.pdf");
        proyecto.reenviarFormatoA(pdf3, null);
        proyecto.presentarAlCoordinador();
        proyecto.evaluarFormatoA(false, "Rechazado definitivo", 999L);

        // Ahora está en FORMATO_A_RECHAZADO (estado final)
        assertEquals(EstadoProyecto.FORMATO_A_RECHAZADO, proyecto.getEstado());

        // Act & Assert - Intentar reenviar desde estado final debe lanzar excepción
        ArchivoAdjunto pdf4 = ArchivoAdjunto.pdf("/v4.pdf", "v4.pdf");
        assertThrows(EstadoInvalidoException.class, () -> {
            proyecto.reenviarFormatoA(pdf4, null);
        });
    }

    // ==========================================
    // TESTS DE ANTEPROYECTO
    // ==========================================

    @Test
    @DisplayName("Cuando subir anteproyecto con Formato A aprobado, debe cambiar a ANTEPROYECTO_ENVIADO")
    void cuandoSubirAnteproyectoConFormatoAAprobado_debeCambiarAAnteproyectoEnviado() {
        // Arrange
        Proyecto proyecto = crearProyectoBasico();
        proyecto.presentarAlCoordinador();
        proyecto.evaluarFormatoA(true, "Aprobado", 999L);
        proyecto.limpiarEventos();

        ArchivoAdjunto pdfAnteproyecto = ArchivoAdjunto.pdf("/ante.pdf", "anteproyecto.pdf");
        Long directorId = proyecto.getParticipantes().getDirectorId();

        // Act
        proyecto.subirAnteproyecto(pdfAnteproyecto, directorId);

        // Assert
        assertEquals(EstadoProyecto.ANTEPROYECTO_ENVIADO, proyecto.getEstado());
        assertNotNull(proyecto.getAnteproyecto());

        List<DomainEvent> eventos = proyecto.obtenerEventosPendientes();
        assertEquals(1, eventos.size());
        assertTrue(eventos.get(0) instanceof AnteproyectoSubido);
    }

    @Test
    @DisplayName("Cuando subir anteproyecto sin Formato A aprobado, debe lanzar FormatoANoAprobadoException")
    void cuandoSubirAnteproyectoSinFormatoAAprobado_debeLanzarExcepcion() {
        // Arrange
        Proyecto proyecto = crearProyectoBasico();
        ArchivoAdjunto pdf = ArchivoAdjunto.pdf("/ante.pdf", "anteproyecto.pdf");
        Long directorId = proyecto.getParticipantes().getDirectorId();

        // Act & Assert
        assertThrows(FormatoANoAprobadoException.class, () -> {
            proyecto.subirAnteproyecto(pdf, directorId);
        });
    }

    @Test
    @DisplayName("Cuando subir anteproyecto siendo no director, debe lanzar UsuarioNoAutorizadoException")
    void cuandoSubirAnteproyectoSiendoNoDirector_debeLanzarExcepcion() {
        // Arrange
        Proyecto proyecto = crearProyectoBasico();
        proyecto.presentarAlCoordinador();
        proyecto.evaluarFormatoA(true, "Aprobado", 999L);

        ArchivoAdjunto pdf = ArchivoAdjunto.pdf("/ante.pdf", "anteproyecto.pdf");
        Long usuarioNoDirector = 9999L;

        // Act & Assert
        assertThrows(UsuarioNoAutorizadoException.class, () -> {
            proyecto.subirAnteproyecto(pdf, usuarioNoDirector);
        });
    }

    @Test
    @DisplayName("Cuando asignar evaluadores a anteproyecto, debe cambiar a ANTEPROYECTO_EN_EVALUACION")
    void cuandoAsignarEvaluadoresAAnteproyecto_debeCambiarAEnEvaluacion() {
        // Arrange
        Proyecto proyecto = crearProyectoYSubirAnteproyecto();

        // Act
        proyecto.asignarEvaluadores(100L, 101L);

        // Assert
        assertEquals(EstadoProyecto.ANTEPROYECTO_EN_EVALUACION, proyecto.getEstado());
        assertTrue(proyecto.getAnteproyecto().tieneEvaluadoresAsignados());
    }

    @Test
    @DisplayName("Cuando evaluar anteproyecto aprobado, debe cambiar a ANTEPROYECTO_APROBADO")
    void cuandoEvaluarAnteproyectoAprobado_debeCambiarAAprobado() {
        // Arrange
        Proyecto proyecto = crearProyectoYSubirAnteproyecto();
        proyecto.asignarEvaluadores(100L, 101L);

        // Act
        proyecto.evaluarAnteproyecto(true, "Excelente anteproyecto", 100L);

        // Assert
        assertEquals(EstadoProyecto.ANTEPROYECTO_APROBADO, proyecto.getEstado());
        assertTrue(proyecto.esEstadoFinal());
    }

    @Test
    @DisplayName("Cuando evaluar anteproyecto rechazado, debe cambiar a ANTEPROYECTO_RECHAZADO")
    void cuandoEvaluarAnteproyectoRechazado_debeCambiarARechazado() {
        // Arrange
        Proyecto proyecto = crearProyectoYSubirAnteproyecto();
        proyecto.asignarEvaluadores(100L, 101L);

        // Act
        proyecto.evaluarAnteproyecto(false, "No cumple requisitos", 100L);

        // Assert
        assertEquals(EstadoProyecto.ANTEPROYECTO_RECHAZADO, proyecto.getEstado());
        assertTrue(proyecto.esEstadoFinal());
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private Proyecto crearProyectoBasico() {
        Titulo titulo = Titulo.of("Proyecto de prueba para testing unitario");
        Modalidad modalidad = Modalidad.INVESTIGACION;
        ObjetivosProyecto objetivos = ObjetivosProyecto.of(
            "Objetivo general de prueba",
            Arrays.asList("Objetivo específico 1", "Objetivo específico 2")
        );
        Participantes participantes = Participantes.of(1L, null, 2L, null);
        ArchivoAdjunto pdf = ArchivoAdjunto.pdf("/test/formatoA.pdf", "formatoA.pdf");

        return Proyecto.crearConFormatoA(titulo, modalidad, objetivos, participantes, pdf, null);
    }

    private Proyecto crearProyectoYSubirAnteproyecto() {
        Proyecto proyecto = crearProyectoBasico();
        proyecto.presentarAlCoordinador();
        proyecto.evaluarFormatoA(true, "Aprobado", 999L);

        ArchivoAdjunto pdfAnteproyecto = ArchivoAdjunto.pdf("/ante.pdf", "anteproyecto.pdf");
        Long directorId = proyecto.getParticipantes().getDirectorId();
        proyecto.subirAnteproyecto(pdfAnteproyecto, directorId);

        return proyecto;
    }
}


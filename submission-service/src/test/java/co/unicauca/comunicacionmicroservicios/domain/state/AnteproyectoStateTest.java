package co.unicauca.comunicacionmicroservicios.domain.state;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.model.enumModalidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para verificar el flujo de estados del Anteproyecto
 * Según RF-6, RF-7, RF-8:
 * - RF-6: Docente sube anteproyecto tras Formato A aprobado
 * - RF-7: Jefe de departamento lista anteproyectos
 * - RF-8: Jefe asigna 2 evaluadores
 *
 * Flujo esperado:
 * FORMATO_A_APROBADO → ANTEPROYECTO_ENVIADO → ANTEPROYECTO_EN_EVALUACION → ANTEPROYECTO_APROBADO/RECHAZADO
 */
class AnteproyectoStateTest {

    private ProyectoSubmission proyecto;

    @BeforeEach
    void setUp() {
        proyecto = new ProyectoSubmission();
        proyecto.setTitulo("Proyecto de prueba");
        proyecto.setModalidad(enumModalidad.INVESTIGACION);
        proyecto.setDocenteDirectorId(1L);
        proyecto.setDocenteCodirectorId(2L);
        proyecto.setEstudiante1Id(100L);
        proyecto.setEstudiante2Id(101L);

        // Llevar el proyecto hasta FORMATO_A_APROBADO
        proyecto.presentarAlCoordinador();
        proyecto.evaluar(true, "Formato A aprobado");
    }

    @Test
    @DisplayName("Después de aprobar Formato A, estado debe ser FORMATO_A_APROBADO")
    void estadoDebeSerFormatoAAprobado() {
        assertEquals("FORMATO_A_APROBADO", proyecto.getEstadoNombre());
        assertFalse(proyecto.esEstadoFinal()); // Ya no es final porque puede subir anteproyecto
    }

    @Test
    @DisplayName("Flujo completo: Subir anteproyecto → Asignar evaluadores → Aprobar")
    void flujoCompletoAnteproyectoAprobado() {
        // 1. Docente sube anteproyecto (RF-6)
        proyecto.subirAnteproyecto("/anteproyectos/documento.pdf");
        assertEquals("ANTEPROYECTO_ENVIADO", proyecto.getEstadoNombre());
        assertEquals("/anteproyectos/documento.pdf", proyecto.getRutaAnteproyecto());
        assertNotNull(proyecto.getFechaEnvioAnteproyecto());

        // 2. Jefe de Departamento asigna 2 evaluadores (RF-8)
        proyecto.asignarEvaluadores(10L, 11L);
        assertEquals("ANTEPROYECTO_EN_EVALUACION", proyecto.getEstadoNombre());
        assertEquals(10L, proyecto.getEvaluador1Id());
        assertEquals(11L, proyecto.getEvaluador2Id());

        // 3. Evaluadores aprueban el anteproyecto
        proyecto.evaluarAnteproyecto(true, "Excelente anteproyecto");
        assertEquals("ANTEPROYECTO_APROBADO", proyecto.getEstadoNombre());
        assertTrue(proyecto.esEstadoFinal());
        assertEquals("Excelente anteproyecto", proyecto.getComentariosAnteproyecto());
    }

    @Test
    @DisplayName("Flujo con rechazo de anteproyecto")
    void flujoAnteproyectoRechazado() {
        // Subir anteproyecto
        proyecto.subirAnteproyecto("/anteproyectos/documento.pdf");

        // Asignar evaluadores
        proyecto.asignarEvaluadores(10L, 11L);

        // Evaluadores rechazan
        proyecto.evaluarAnteproyecto(false, "Falta profundidad en el marco teórico");
        assertEquals("ANTEPROYECTO_RECHAZADO", proyecto.getEstadoNombre());
        assertTrue(proyecto.esEstadoFinal());
        assertEquals("Falta profundidad en el marco teórico", proyecto.getComentariosAnteproyecto());
    }

    @Test
    @DisplayName("No se puede subir anteproyecto si Formato A no está aprobado")
    void noPuedeSubirAnteproyectoSinFormatoAAprobado() {
        // Crear proyecto nuevo (sin aprobar Formato A)
        ProyectoSubmission proyectoNuevo = new ProyectoSubmission();
        proyectoNuevo.setTitulo("Otro proyecto");
        proyectoNuevo.setModalidad(enumModalidad.INVESTIGACION);
        proyectoNuevo.setDocenteDirectorId(1L);
        proyectoNuevo.setEstudiante1Id(100L);

        // Intentar subir anteproyecto desde estado inicial
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> proyectoNuevo.subirAnteproyecto("/anteproyectos/documento.pdf")
        );
        assertTrue(exception.getMessage().contains("No se puede subir anteproyecto"));
    }

    @Test
    @DisplayName("No se pueden asignar evaluadores sin subir anteproyecto")
    void noPuedeAsignarEvaluadoresSinAnteproyecto() {
        // Intentar asignar evaluadores directamente desde FORMATO_A_APROBADO
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> proyecto.asignarEvaluadores(10L, 11L)
        );
        assertTrue(exception.getMessage().contains("No se pueden asignar evaluadores"));
    }

    @Test
    @DisplayName("Evaluadores deben ser diferentes")
    void evaluadoresDebenSerDiferentes() {
        proyecto.subirAnteproyecto("/anteproyectos/documento.pdf");

        // Intentar asignar el mismo evaluador dos veces
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> proyecto.asignarEvaluadores(10L, 10L)
        );
        assertTrue(exception.getMessage().contains("diferentes"));
    }

    @Test
    @DisplayName("Director no puede ser evaluador")
    void directorNoPuedeSerEvaluador() {
        proyecto.subirAnteproyecto("/anteproyectos/documento.pdf");

        // Intentar asignar al director como evaluador
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> proyecto.asignarEvaluadores(1L, 10L) // 1L es el director
        );
        assertTrue(exception.getMessage().contains("director"));
    }

    @Test
    @DisplayName("Codirector no puede ser evaluador")
    void codirectorNoPuedeSerEvaluador() {
        proyecto.subirAnteproyecto("/anteproyectos/documento.pdf");

        // Intentar asignar al codirector como evaluador
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> proyecto.asignarEvaluadores(10L, 2L) // 2L es el codirector
        );
        assertTrue(exception.getMessage().contains("codirector"));
    }

    @Test
    @DisplayName("Se deben asignar exactamente 2 evaluadores")
    void debeAsignarDosEvaluadores() {
        proyecto.subirAnteproyecto("/anteproyectos/documento.pdf");

        // Intentar asignar solo 1 evaluador (null en el segundo)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> proyecto.asignarEvaluadores(10L, null)
        );
        assertTrue(exception.getMessage().contains("2 evaluadores"));
    }

    @Test
    @DisplayName("No se puede evaluar anteproyecto sin evaluadores asignados")
    void noPuedeEvaluarSinEvaluadoresAsignados() {
        proyecto.subirAnteproyecto("/anteproyectos/documento.pdf");

        // Intentar evaluar sin asignar evaluadores
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> proyecto.evaluarAnteproyecto(true, "Aprobado")
        );
        assertTrue(exception.getMessage().contains("No se puede evaluar anteproyecto"));
    }

    @Test
    @DisplayName("No se pueden hacer transiciones desde ANTEPROYECTO_APROBADO")
    void noPuedeTransicionarDesdeAprobado() {
        proyecto.subirAnteproyecto("/anteproyectos/documento.pdf");
        proyecto.asignarEvaluadores(10L, 11L);
        proyecto.evaluarAnteproyecto(true, "Aprobado");

        assertEquals("ANTEPROYECTO_APROBADO", proyecto.getEstadoNombre());

        // Intentar cualquier operación
        assertThrows(IllegalStateException.class, () -> proyecto.subirAnteproyecto("/otro.pdf"));
        assertThrows(IllegalStateException.class, () -> proyecto.asignarEvaluadores(12L, 13L));
        assertThrows(IllegalStateException.class, () -> proyecto.evaluarAnteproyecto(false, "Re-evaluar"));
    }

    @Test
    @DisplayName("No se pueden hacer transiciones desde ANTEPROYECTO_RECHAZADO")
    void noPuedeTransicionarDesdeRechazado() {
        proyecto.subirAnteproyecto("/anteproyectos/documento.pdf");
        proyecto.asignarEvaluadores(10L, 11L);
        proyecto.evaluarAnteproyecto(false, "Rechazado");

        assertEquals("ANTEPROYECTO_RECHAZADO", proyecto.getEstadoNombre());

        // Intentar cualquier operación
        assertThrows(IllegalStateException.class, () -> proyecto.subirAnteproyecto("/otro.pdf"));
        assertThrows(IllegalStateException.class, () -> proyecto.asignarEvaluadores(12L, 13L));
        assertThrows(IllegalStateException.class, () -> proyecto.evaluarAnteproyecto(true, "Aprobar"));
    }

    @Test
    @DisplayName("Los datos del Formato A se mantienen al subir anteproyecto")
    void datosSeMantienen() {
        // Verificar datos antes de subir anteproyecto
        assertEquals("Proyecto de prueba", proyecto.getTitulo());
        assertEquals(1L, proyecto.getDocenteDirectorId());
        assertEquals(100L, proyecto.getEstudiante1Id());
        assertEquals(101L, proyecto.getEstudiante2Id());

        // Subir anteproyecto
        proyecto.subirAnteproyecto("/anteproyectos/documento.pdf");

        // Verificar que los datos se mantienen
        assertEquals("Proyecto de prueba", proyecto.getTitulo());
        assertEquals(1L, proyecto.getDocenteDirectorId());
        assertEquals(100L, proyecto.getEstudiante1Id());
        assertEquals(101L, proyecto.getEstudiante2Id());
    }

    @Test
    @DisplayName("Compatibilidad con estados de anteproyecto en BD")
    void compatibilidadEstadosAnteproyecto() {
        // Simular carga desde BD con estado de anteproyecto
        proyecto.setEstadoNombre("ANTEPROYECTO_ENVIADO");
        proyecto.reconstruirEstado();
        assertEquals("ANTEPROYECTO_ENVIADO", proyecto.getEstadoActual().getNombreEstado());

        proyecto.setEstadoNombre("ANTEPROYECTO_EN_EVALUACION");
        proyecto.reconstruirEstado();
        assertEquals("ANTEPROYECTO_EN_EVALUACION", proyecto.getEstadoActual().getNombreEstado());

        proyecto.setEstadoNombre("ANTEPROYECTO_APROBADO");
        proyecto.reconstruirEstado();
        assertEquals("ANTEPROYECTO_APROBADO", proyecto.getEstadoActual().getNombreEstado());

        proyecto.setEstadoNombre("ANTEPROYECTO_RECHAZADO");
        proyecto.reconstruirEstado();
        assertEquals("ANTEPROYECTO_RECHAZADO", proyecto.getEstadoActual().getNombreEstado());
    }
}


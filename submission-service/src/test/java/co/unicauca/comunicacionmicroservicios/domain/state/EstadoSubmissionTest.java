package co.unicauca.comunicacionmicroservicios.domain.state;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.model.enumModalidad;
import co.unicauca.comunicacionmicroservicios.domain.state.concrete.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para verificar el flujo de estados del Formato A
 * Según RF-3: El COORDINADOR evalúa el Formato A
 *
 * Flujo esperado:
 * FORMATO_A_DILIGENCIADO → EN_EVALUACION_COORDINADOR → FORMATO_A_APROBADO/RECHAZADO
 */
class EstadoSubmissionTest {

    private ProyectoSubmission proyecto;

    @BeforeEach
    void setUp() {
        proyecto = new ProyectoSubmission();
        proyecto.setTitulo("Proyecto de prueba");
        proyecto.setModalidad(enumModalidad.PRACTICA_PROFESIONAL);
        proyecto.setDocenteDirectorId(1L);
        proyecto.setEstudiante1Id(100L);
    }

    @Test
    @DisplayName("Estado inicial debe ser FORMATO_A_DILIGENCIADO")
    void estadoInicialDebeSerFormatoADiligenciado() {
        assertEquals("FORMATO_A_DILIGENCIADO", proyecto.getEstadoNombre());
        assertFalse(proyecto.esEstadoFinal());
    }

    @Test
    @DisplayName("Flujo exitoso: DILIGENCIADO → EN_EVALUACION → APROBADO")
    void flujoExitosoAprobadoPrimerIntento() {
        // Estado inicial
        assertEquals("FORMATO_A_DILIGENCIADO", proyecto.getEstadoNombre());

        // Presentar al coordinador
        proyecto.presentarAlCoordinador();
        assertEquals("EN_EVALUACION_COORDINADOR", proyecto.getEstadoNombre());

        // Coordinador aprueba
        proyecto.evaluar(true, "Excelente propuesta");
        assertEquals("FORMATO_A_APROBADO", proyecto.getEstadoNombre());
        // FORMATO_A_APROBADO ya no es final porque puede subir anteproyecto
        assertFalse(proyecto.esEstadoFinal());
        assertEquals("Excelente propuesta", proyecto.getComentariosComite());
    }

    @Test
    @DisplayName("Flujo con correcciones: RECHAZO → CORRECCIONES → REENVIO → APROBADO")
    void flujoConCorreccionesYAprobacion() {
        // Presentar al coordinador
        proyecto.presentarAlCoordinador();
        assertEquals("EN_EVALUACION_COORDINADOR", proyecto.getEstadoNombre());

        // Coordinador rechaza (intento 1)
        proyecto.evaluar(false, "Mejorar metodología");
        assertEquals("CORRECCIONES_SOLICITADAS", proyecto.getEstadoNombre());
        assertEquals(1, proyecto.getNumeroIntentos());
        assertFalse(proyecto.esEstadoFinal());

        // Docente sube nueva versión
        proyecto.subirNuevaVersion();
        assertEquals("EN_EVALUACION_COORDINADOR", proyecto.getEstadoNombre());

        // Coordinador aprueba segunda versión
        proyecto.evaluar(true, "Correcciones satisfactorias");
        assertEquals("FORMATO_A_APROBADO", proyecto.getEstadoNombre());
        // FORMATO_A_APROBADO ya no es final porque puede subir anteproyecto
        assertFalse(proyecto.esEstadoFinal());
    }

    @Test
    @DisplayName("Flujo con rechazo definitivo tras 3 intentos")
    void flujoRechazoDefinitivoTresIntentos() {
        // Presentar al coordinador
        proyecto.presentarAlCoordinador();

        // Intento 1: Rechazado
        proyecto.evaluar(false, "Mejorar objetivos");
        assertEquals("CORRECCIONES_SOLICITADAS", proyecto.getEstadoNombre());
        assertEquals(1, proyecto.getNumeroIntentos());

        // Reenviar y rechazar intento 2
        proyecto.subirNuevaVersion();
        proyecto.evaluar(false, "Mejorar justificación");
        assertEquals("CORRECCIONES_SOLICITADAS", proyecto.getEstadoNombre());
        assertEquals(2, proyecto.getNumeroIntentos());

        // Reenviar y rechazar intento 3 (FINAL)
        proyecto.subirNuevaVersion();
        proyecto.evaluar(false, "No cumple requisitos mínimos");
        assertEquals("FORMATO_A_RECHAZADO", proyecto.getEstadoNombre());
        assertEquals(3, proyecto.getNumeroIntentos());
        assertTrue(proyecto.esEstadoFinal());
    }

    @Test
    @DisplayName("No se puede evaluar desde estado FORMATO_A_DILIGENCIADO")
    void noPuedeEvaluarDesdeEstadoInicial() {
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> proyecto.evaluar(true, "Intento inválido")
        );
        assertTrue(exception.getMessage().contains("No se puede evaluar"));
    }

    @Test
    @DisplayName("No se puede subir nueva versión desde estado EN_EVALUACION_COORDINADOR")
    void noPuedeSubirVersionDesdeEvaluacion() {
        proyecto.presentarAlCoordinador();

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> proyecto.subirNuevaVersion()
        );
        assertTrue(exception.getMessage().contains("No se puede subir nueva version"));
    }

    @Test
    @DisplayName("Desde FORMATO_A_APROBADO solo se puede subir anteproyecto")
    void desdeAprobadoSoloPuedeSubirAnteproyecto() {
        proyecto.presentarAlCoordinador();
        proyecto.evaluar(true, "Aprobado");

        // Estas operaciones de Formato A NO están permitidas
        assertThrows(IllegalStateException.class, () -> proyecto.presentarAlCoordinador());
        assertThrows(IllegalStateException.class, () -> proyecto.evaluar(false, "Re-evaluar"));
        assertThrows(IllegalStateException.class, () -> proyecto.subirNuevaVersion());

        // Pero SÍ se puede subir anteproyecto
        assertDoesNotThrow(() -> proyecto.subirAnteproyecto("/anteproyectos/doc.pdf"));
        assertEquals("ANTEPROYECTO_ENVIADO", proyecto.getEstadoNombre());
    }

    @Test
    @DisplayName("No se puede hacer transiciones desde estado final RECHAZADO")
    void noPuedeTransicionarDesdeRechazado() {
        proyecto.presentarAlCoordinador();

        // Agotar los 3 intentos
        for (int i = 0; i < 3; i++) {
            proyecto.evaluar(false, "Rechazo " + (i + 1));
            if (i < 2) {
                proyecto.subirNuevaVersion();
            }
        }

        assertEquals("FORMATO_A_RECHAZADO", proyecto.getEstadoNombre());

        // Intentar cualquier operación
        assertThrows(IllegalStateException.class, () -> proyecto.presentarAlCoordinador());
        assertThrows(IllegalStateException.class, () -> proyecto.evaluar(true, "Aprobar"));
        assertThrows(IllegalStateException.class, () -> proyecto.subirNuevaVersion());
    }

    @Test
    @DisplayName("Compatibilidad con estados antiguos en BD")
    void compatibilidadEstadosAntiguos() {
        // Simular que se carga un proyecto con estado antiguo de BD
        proyecto.setEstadoNombre("EN_EVALUACION_COMITE");
        proyecto.reconstruirEstado();

        // Debe mapear al nuevo estado
        assertEquals("EN_EVALUACION_COORDINADOR", proyecto.getEstadoActual().getNombreEstado());

        // Probar otros estados antiguos
        proyecto.setEstadoNombre("CORRECCIONES_COMITE");
        proyecto.reconstruirEstado();
        assertEquals("CORRECCIONES_SOLICITADAS", proyecto.getEstadoActual().getNombreEstado());

        proyecto.setEstadoNombre("ACEPTADO_POR_COMITE");
        proyecto.reconstruirEstado();
        assertEquals("FORMATO_A_APROBADO", proyecto.getEstadoActual().getNombreEstado());

        proyecto.setEstadoNombre("RECHAZADO_POR_COMITE");
        proyecto.reconstruirEstado();
        assertEquals("FORMATO_A_RECHAZADO", proyecto.getEstadoActual().getNombreEstado());
    }

    @Test
    @DisplayName("Título es requerido para presentar al coordinador")
    void tituloRequeridoParaPresentar() {
        proyecto.setTitulo(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> proyecto.presentarAlCoordinador()
        );
        assertTrue(exception.getMessage().contains("titulo"));
    }
}


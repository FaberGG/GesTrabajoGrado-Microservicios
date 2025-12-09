package co.unicauca.submission.domain.model;

import co.unicauca.submission.domain.exception.MaximosIntentosExcedidosException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entity: FormatoAInfo
 *
 * Representa la información del Formato A dentro del aggregate Proyecto.
 * Gestiona los intentos, archivos y evaluaciones del Formato A.
 */
public class FormatoAInfo {

    private static final int MAX_INTENTOS = 3;

    private int numeroIntento;
    private ArchivoAdjunto pdfFormatoA;
    private ArchivoAdjunto cartaAceptacion; // Opcional
    private List<Evaluacion> evaluaciones;

    /**
     * Constructor para crear un nuevo Formato A.
     *
     * @param numeroIntento Número de intento (1-3)
     * @param pdfFormatoA PDF del Formato A (obligatorio)
     * @param cartaAceptacion Carta de aceptación (opcional)
     */
    public FormatoAInfo(int numeroIntento, ArchivoAdjunto pdfFormatoA,
                       ArchivoAdjunto cartaAceptacion) {
        if (numeroIntento < 1 || numeroIntento > MAX_INTENTOS) {
            throw new IllegalArgumentException(
                "Número de intento inválido: " + numeroIntento + ". Debe estar entre 1 y " + MAX_INTENTOS
            );
        }
        if (pdfFormatoA == null) {
            throw new IllegalArgumentException("El PDF del Formato A es obligatorio");
        }
        if (!pdfFormatoA.esPDF()) {
            throw new IllegalArgumentException("El archivo debe ser PDF");
        }

        this.numeroIntento = numeroIntento;
        this.pdfFormatoA = pdfFormatoA;
        this.cartaAceptacion = cartaAceptacion;
        this.evaluaciones = new ArrayList<>();
    }

    /**
     * Incrementa el número de intentos.
     * Se llama cuando el Formato A es rechazado.
     */
    public void incrementarIntentos() {
        if (this.numeroIntento >= MAX_INTENTOS) {
            throw new MaximosIntentosExcedidosException(
                "Ya se alcanzó el máximo de " + MAX_INTENTOS + " intentos"
            );
        }
        this.numeroIntento++;
    }

    /**
     * Verifica si se alcanzó el máximo de intentos permitidos.
     */
    public boolean haAlcanzadoMaximoIntentos() {
        return this.numeroIntento >= MAX_INTENTOS;
    }

    /**
     * Actualiza los archivos del Formato A (usado en reenvíos).
     *
     * @param nuevoPdf Nuevo PDF del Formato A
     * @param nuevaCarta Nueva carta de aceptación (puede ser null)
     */
    public void actualizarArchivos(ArchivoAdjunto nuevoPdf, ArchivoAdjunto nuevaCarta) {
        if (nuevoPdf != null) {
            if (!nuevoPdf.esPDF()) {
                throw new IllegalArgumentException("El archivo debe ser PDF");
            }
            this.pdfFormatoA = nuevoPdf;
        }
        if (nuevaCarta != null) {
            if (!nuevaCarta.esPDF()) {
                throw new IllegalArgumentException("La carta debe ser PDF");
            }
            this.cartaAceptacion = nuevaCarta;
        }
    }

    /**
     * Agrega una evaluación al historial.
     *
     * @param evaluacion Evaluación realizada por el coordinador
     */
    public void agregarEvaluacion(Evaluacion evaluacion) {
        if (evaluacion == null) {
            throw new IllegalArgumentException("La evaluación no puede ser null");
        }
        this.evaluaciones.add(evaluacion);
    }

    /**
     * Obtiene la última evaluación realizada.
     */
    public Evaluacion getUltimaEvaluacion() {
        if (evaluaciones.isEmpty()) {
            return null;
        }
        return evaluaciones.get(evaluaciones.size() - 1);
    }

    /**
     * Verifica si el Formato A tiene carta de aceptación.
     */
    public boolean tieneCarta() {
        return this.cartaAceptacion != null;
    }

    /**
     * Verifica si el Formato A fue evaluado al menos una vez.
     */
    public boolean fueEvaluado() {
        return !this.evaluaciones.isEmpty();
    }

    // Getters

    public int getNumeroIntento() {
        return numeroIntento;
    }

    public ArchivoAdjunto getPdfFormatoA() {
        return pdfFormatoA;
    }

    public ArchivoAdjunto getCartaAceptacion() {
        return cartaAceptacion;
    }

    public List<Evaluacion> getEvaluaciones() {
        return Collections.unmodifiableList(evaluaciones);
    }

    @Override
    public String toString() {
        return "FormatoAInfo{" +
                "numeroIntento=" + numeroIntento +
                ", tieneCarta=" + tieneCarta() +
                ", evaluaciones=" + evaluaciones.size() +
                '}';
    }
}


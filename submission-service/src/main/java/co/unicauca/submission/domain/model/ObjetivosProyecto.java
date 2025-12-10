package co.unicauca.submission.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value Object que representa los objetivos del proyecto.
 * Agrupa objetivo general y objetivos específicos.
 */
public class ObjetivosProyecto {

    private final String objetivoGeneral;
    private final List<String> objetivosEspecificos;

    private ObjetivosProyecto(String objetivoGeneral, List<String> objetivosEspecificos) {
        if (objetivoGeneral == null || objetivoGeneral.trim().isEmpty()) {
            throw new IllegalArgumentException("El objetivo general es obligatorio");
        }

        if (objetivosEspecificos == null || objetivosEspecificos.isEmpty()) {
            throw new IllegalArgumentException("Debe haber al menos un objetivo específico");
        }

        this.objetivoGeneral = objetivoGeneral.trim();
        this.objetivosEspecificos = new ArrayList<>(objetivosEspecificos);
    }

    public static ObjetivosProyecto of(String objetivoGeneral, List<String> objetivosEspecificos) {
        return new ObjetivosProyecto(objetivoGeneral, objetivosEspecificos);
    }

    public String getObjetivoGeneral() {
        return objetivoGeneral;
    }

    public List<String> getObjetivosEspecificos() {
        return Collections.unmodifiableList(objetivosEspecificos);
    }

    public String getObjetivosEspecificosAsString() {
        return String.join("; ", objetivosEspecificos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjetivosProyecto that = (ObjetivosProyecto) o;
        return Objects.equals(objetivoGeneral, that.objetivoGeneral) &&
               Objects.equals(objetivosEspecificos, that.objetivosEspecificos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objetivoGeneral, objetivosEspecificos);
    }
}


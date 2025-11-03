package co.unicauca.gestiontrabajogrado.presentation.demo;


import co.unicauca.gestiontrabajogrado.presentation.dashboard.coordinadorview.PropuestaRow;
import co.unicauca.gestiontrabajogrado.domain.model.enumEstadoFormato;
import co.unicauca.gestiontrabajogrado.domain.model.enumEstadoProyecto;

import java.util.List;

public final class DemoData {
    private DemoData() {}
    public static List<PropuestaRow> simuladas() {
        return List.of(
                new PropuestaRow(101, 1001, "El papel de la Inteligencia Artificial en la Automatización de la Industria Manofacturera",
                        enumEstadoProyecto.APROBADO, enumEstadoFormato.APROBADO, 1),
                new PropuestaRow(102, 1002, "Explorando las implicaciones éticas de la clonación humana",
                        enumEstadoProyecto.EN_PROCESO, enumEstadoFormato.RECHAZADO, 2),
                new PropuestaRow(103, 1003, "Seguridad Cibernética para Infraestructura Crítica",
                        enumEstadoProyecto.EN_PROCESO, enumEstadoFormato.RECHAZADO, 2),
                new PropuestaRow(104, null,  "Impacto de la IHC en habilidades blandas",
                        enumEstadoProyecto.EN_PROCESO, enumEstadoFormato.PENDIENTE, 1)
        );
    }
}


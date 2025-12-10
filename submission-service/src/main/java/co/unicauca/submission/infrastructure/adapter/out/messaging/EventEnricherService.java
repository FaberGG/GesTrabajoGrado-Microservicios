package co.unicauca.submission.infrastructure.adapter.out.messaging;

import co.unicauca.submission.application.port.out.IIdentityServicePort;
import co.unicauca.submission.domain.model.Modalidad;
import co.unicauca.submission.domain.model.Proyecto;
import co.unicauca.submission.infrastructure.adapter.out.messaging.event.AnteproyectoEnviadoEvent;
import co.unicauca.submission.infrastructure.adapter.out.messaging.event.FormatoAEnviadoEvent;
import co.unicauca.submission.infrastructure.adapter.out.messaging.event.FormatoAReenviadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio para enriquecer eventos de dominio con información de participantes.
 *
 * Consulta identity-service para obtener nombres completos y emails,
 * creando eventos de integración completos para progress-tracking.
 */
@Component
public class EventEnricherService {

    private static final Logger log = LoggerFactory.getLogger(EventEnricherService.class);

    private final IIdentityServicePort identityService;

    public EventEnricherService(IIdentityServicePort identityService) {
        this.identityService = identityService;
    }

    /**
     * Enriquece un evento de Formato A Creado con información de identity-service.
     */
    public FormatoAEnviadoEvent enrichFormatoACreado(Proyecto proyecto) {
        try {
            log.info("🔍 Enriqueciendo evento FormatoACreado para proyecto {}", proyecto.getId());

            // Obtener información del director
            var director = identityService.obtenerUsuario(proyecto.getParticipantes().getDirectorId());
            String directorNombre = director != null ? director.nombreCompleto() : "Director desconocido";

            // Obtener información del codirector si existe
            Long codirectorId = proyecto.getParticipantes().getCodirectorId();
            String codirectorNombre = null;
            if (codirectorId != null) {
                var codirector = identityService.obtenerUsuario(codirectorId);
                codirectorNombre = codirector != null ? codirector.nombreCompleto() : null;
            }

            // Obtener información de estudiante 1
            var estudiante1 = identityService.obtenerUsuario(proyecto.getParticipantes().getEstudiante1Id());
            String est1Nombre = estudiante1 != null ? estudiante1.nombreCompleto() : "Estudiante 1 desconocido";
            String est1Email = estudiante1 != null ? estudiante1.email() : "";

            // Obtener información de estudiante 2 si existe (modalidad DUPLA)
            Long est2Id = proyecto.getParticipantes().getEstudiante2Id();
            String est2Nombre = null;
            String est2Email = null;
            if (est2Id != null) {
                var estudiante2 = identityService.obtenerUsuario(est2Id);
                est2Nombre = estudiante2 != null ? estudiante2.nombreCompleto() : null;
                est2Email = estudiante2 != null ? estudiante2.email() : null;
            }

            // Construir lista de estudiantes
            List<Map<String, Object>> estudiantes = new ArrayList<>();
            estudiantes.add(Map.of(
                "id", proyecto.getParticipantes().getEstudiante1Id(),
                "nombre", est1Nombre,
                "email", est1Email
            ));
            if (est2Id != null) {
                estudiantes.add(Map.of(
                    "id", est2Id,
                    "nombre", est2Nombre != null ? est2Nombre : "",
                    "email", est2Email != null ? est2Email : ""
                ));
            }

            // Construir evento enriquecido
            FormatoAEnviadoEvent evento = FormatoAEnviadoEvent.builder()
                .proyectoId(proyecto.getId().getValue())
                .version(1)
                .titulo(proyecto.getTitulo().getValue())
                .modalidad(proyecto.getModalidad().name())
                .programa("INGENIERIA_SISTEMAS") // TODO: Obtener de configuración o request
                .directorId(proyecto.getParticipantes().getDirectorId())
                .directorNombre(directorNombre)
                .codirectorId(codirectorId)
                .codirectorNombre(codirectorNombre)
                .estudiante1Id(proyecto.getParticipantes().getEstudiante1Id())
                .estudiante1Nombre(est1Nombre)
                .estudiante1Email(est1Email)
                .estudiante2Id(est2Id)
                .estudiante2Nombre(est2Nombre)
                .estudiante2Email(est2Email)
                .estudiantes(estudiantes)
                .descripcion("Primera versión del Formato A")
                .build();

            log.info("✅ Evento FormatoACreado enriquecido: Proyecto {}, Director: {}, Estudiante(s): {}",
                    proyecto.getId(), directorNombre, estudiantes.size());

            return evento;

        } catch (Exception e) {
            log.error("❌ Error enriqueciendo evento FormatoACreado: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo enriquecer el evento", e);
        }
    }

    /**
     * Enriquece un evento de Formato A Reenviado con información de identity-service.
     */
    public FormatoAReenviadoEvent enrichFormatoAReenviado(Proyecto proyecto) {
        try {
            log.info("🔍 Enriqueciendo evento FormatoAReenviado para proyecto {}", proyecto.getId());

            // Similar a enrichFormatoACreado pero con version del FormatoA
            var director = identityService.obtenerUsuario(proyecto.getParticipantes().getDirectorId());
            String directorNombre = director != null ? director.nombreCompleto() : "Director desconocido";

            Long codirectorId = proyecto.getParticipantes().getCodirectorId();
            String codirectorNombre = null;
            if (codirectorId != null) {
                var codirector = identityService.obtenerUsuario(codirectorId);
                codirectorNombre = codirector != null ? codirector.nombreCompleto() : null;
            }

            var estudiante1 = identityService.obtenerUsuario(proyecto.getParticipantes().getEstudiante1Id());
            String est1Nombre = estudiante1 != null ? estudiante1.nombreCompleto() : "Estudiante 1 desconocido";
            String est1Email = estudiante1 != null ? estudiante1.email() : "";

            Long est2Id = proyecto.getParticipantes().getEstudiante2Id();
            String est2Nombre = null;
            String est2Email = null;
            if (est2Id != null) {
                var estudiante2 = identityService.obtenerUsuario(est2Id);
                est2Nombre = estudiante2 != null ? estudiante2.nombreCompleto() : null;
                est2Email = estudiante2 != null ? estudiante2.email() : null;
            }

            List<Map<String, Object>> estudiantes = new ArrayList<>();
            estudiantes.add(Map.of(
                "id", proyecto.getParticipantes().getEstudiante1Id(),
                "nombre", est1Nombre,
                "email", est1Email
            ));
            if (est2Id != null) {
                estudiantes.add(Map.of(
                    "id", est2Id,
                    "nombre", est2Nombre != null ? est2Nombre : "",
                    "email", est2Email != null ? est2Email : ""
                ));
            }

            int version = proyecto.getFormatoA().getNumeroIntento();

            FormatoAReenviadoEvent evento = FormatoAReenviadoEvent.builder()
                .proyectoId(proyecto.getId().getValue())
                .version(version)
                .titulo(proyecto.getTitulo().getValue())
                .modalidad(proyecto.getModalidad().name())
                .programa("INGENIERIA_SISTEMAS")
                .directorId(proyecto.getParticipantes().getDirectorId())
                .directorNombre(directorNombre)
                .codirectorId(codirectorId)
                .codirectorNombre(codirectorNombre)
                .estudiante1Id(proyecto.getParticipantes().getEstudiante1Id())
                .estudiante1Nombre(est1Nombre)
                .estudiante1Email(est1Email)
                .estudiante2Id(est2Id)
                .estudiante2Nombre(est2Nombre)
                .estudiante2Email(est2Email)
                .estudiantes(estudiantes)
                .descripcion("Formato A v" + version + " con correcciones aplicadas")
                .build();

            log.info("✅ Evento FormatoAReenviado enriquecido: Proyecto {}, Versión {}, Director: {}",
                    proyecto.getId(), version, directorNombre);

            return evento;

        } catch (Exception e) {
            log.error("❌ Error enriqueciendo evento FormatoAReenviado: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo enriquecer el evento", e);
        }
    }

    /**
     * Enriquece un evento de Anteproyecto Subido con información de identity-service.
     */
    public AnteproyectoEnviadoEvent enrichAnteproyectoSubido(Proyecto proyecto, String rutaArchivo) {
        try {
            log.info("🔍 Enriqueciendo evento AnteproyectoSubido para proyecto {}", proyecto.getId());

            var director = identityService.obtenerUsuario(proyecto.getParticipantes().getDirectorId());
            String directorNombre = director != null ? director.nombreCompleto() : "Director desconocido";

            Long codirectorId = proyecto.getParticipantes().getCodirectorId();
            String codirectorNombre = null;
            if (codirectorId != null) {
                var codirector = identityService.obtenerUsuario(codirectorId);
                codirectorNombre = codirector != null ? codirector.nombreCompleto() : null;
            }

            var estudiante1 = identityService.obtenerUsuario(proyecto.getParticipantes().getEstudiante1Id());
            String est1Nombre = estudiante1 != null ? estudiante1.nombreCompleto() : "Estudiante 1 desconocido";
            String est1Email = estudiante1 != null ? estudiante1.email() : "";

            Long est2Id = proyecto.getParticipantes().getEstudiante2Id();
            String est2Nombre = null;
            String est2Email = null;
            if (est2Id != null) {
                var estudiante2 = identityService.obtenerUsuario(est2Id);
                est2Nombre = estudiante2 != null ? estudiante2.nombreCompleto() : null;
                est2Email = estudiante2 != null ? estudiante2.email() : null;
            }

            List<Map<String, Object>> estudiantes = new ArrayList<>();
            estudiantes.add(Map.of(
                "id", proyecto.getParticipantes().getEstudiante1Id(),
                "nombre", est1Nombre,
                "email", est1Email
            ));
            if (est2Id != null) {
                estudiantes.add(Map.of(
                    "id", est2Id,
                    "nombre", est2Nombre != null ? est2Nombre : "",
                    "email", est2Email != null ? est2Email : ""
                ));
            }

            AnteproyectoEnviadoEvent evento = AnteproyectoEnviadoEvent.builder()
                .proyectoId(proyecto.getId().getValue())
                .titulo(proyecto.getTitulo().getValue())
                .modalidad(proyecto.getModalidad().name())
                .programa("INGENIERIA_SISTEMAS")
                .directorId(proyecto.getParticipantes().getDirectorId())
                .directorNombre(directorNombre)
                .codirectorId(codirectorId)
                .codirectorNombre(codirectorNombre)
                .estudiante1Id(proyecto.getParticipantes().getEstudiante1Id())
                .estudiante1Nombre(est1Nombre)
                .estudiante1Email(est1Email)
                .estudiante2Id(est2Id)
                .estudiante2Nombre(est2Nombre)
                .estudiante2Email(est2Email)
                .estudiantes(estudiantes)
                .rutaArchivo(rutaArchivo)
                .descripcion("Anteproyecto enviado para evaluación")
                .build();

            log.info("✅ Evento AnteproyectoSubido enriquecido: Proyecto {}, Director: {}",
                    proyecto.getId(), directorNombre);

            return evento;

        } catch (Exception e) {
            log.error("❌ Error enriqueciendo evento AnteproyectoSubido: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo enriquecer el evento", e);
        }
    }
}


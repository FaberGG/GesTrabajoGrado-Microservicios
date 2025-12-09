package edu.unicauca.progresstracking.mapper;

import edu.unicauca.progresstracking.domain.entity.HistorialEvento;
import edu.unicauca.progresstracking.domain.entity.ProyectoEstado;
import edu.unicauca.progresstracking.dto.response.*;
import edu.unicauca.progresstracking.service.ProjectStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades a DTOs de respuesta
 */
@Component
@RequiredArgsConstructor
public class ProyectoMapper {

    private final ProjectStateService projectStateService;

    /**
     * Convierte ProyectoEstado a EstadoProyectoResponseDTO completo
     */
    public EstadoProyectoResponseDTO toEstadoProyectoDTO(ProyectoEstado estado) {
        return EstadoProyectoResponseDTO.builder()
                .proyectoId(estado.getProyectoId())
                .titulo(estado.getTitulo())
                .modalidad(estado.getModalidad())
                .programa(estado.getPrograma())
                .estadoActual(estado.getEstadoActual())
                .estadoLegible(projectStateService.convertirEstadoLegible(estado.getEstadoActual()))
                .fase(estado.getFase())
                .ultimaActualizacion(estado.getUltimaActualizacion())
                .siguientePaso(projectStateService.determinarSiguientePaso(estado.getEstadoActual()))
                .formatoA(toFormatoAInfoDTO(estado))
                .anteproyecto(toAnteproyectoInfoDTO(estado))
                .participantes(toParticipantesDTO(estado))
                .estudiantes(toEstudiantesDTO(estado))
                .tieneProyecto(true)
                .build();
    }

    /**
     * Convierte ProyectoEstado a EstadoProyectoResponseDTO con información de estudiante
     */
    public EstadoProyectoResponseDTO toEstadoProyectoPorEstudianteDTO(ProyectoEstado estado, Long estudianteId) {
        EstadoProyectoResponseDTO dto = toEstadoProyectoDTO(estado);
        dto.setEstudianteId(estudianteId);
        return dto;
    }

    /**
     * Crea un DTO de error cuando no se encuentra proyecto
     */
    public EstadoProyectoResponseDTO toEstadoProyectoNoEncontradoDTO(Long proyectoId) {
        return EstadoProyectoResponseDTO.builder()
                .error(true)
                .mensaje("Proyecto no encontrado")
                .proyectoId(proyectoId)
                .build();
    }

    /**
     * Crea un DTO cuando un estudiante no tiene proyectos
     */
    public EstadoProyectoResponseDTO toEstadoSinProyectoDTO(Long estudianteId) {
        return EstadoProyectoResponseDTO.builder()
                .error(false)
                .mensaje("El estudiante no tiene proyectos asignados actualmente")
                .estudianteId(estudianteId)
                .tieneProyecto(false)
                .build();
    }

    /**
     * Convierte información del Formato A
     */
    public FormatoAInfoDTO toFormatoAInfoDTO(ProyectoEstado estado) {
        return FormatoAInfoDTO.builder()
                .estado(estado.getFormatoAEstado())
                .versionActual(estado.getFormatoAVersion())
                .intentoActual(estado.getFormatoAIntentoActual())
                .maxIntentos(estado.getFormatoAMaxIntentos())
                .fechaUltimoEnvio(estado.getFormatoAFechaUltimoEnvio())
                .fechaUltimaEvaluacion(estado.getFormatoAFechaUltimaEvaluacion())
                .build();
    }

    /**
     * Convierte información del Anteproyecto
     */
    public AnteproyectoInfoDTO toAnteproyectoInfoDTO(ProyectoEstado estado) {
        return AnteproyectoInfoDTO.builder()
                .estado(estado.getAnteproyectoEstado())
                .fechaEnvio(estado.getAnteproyectoFechaEnvio())
                .evaluadoresAsignados(estado.getAnteproyectoEvaluadoresAsignados())
                .build();
    }

    /**
     * Convierte información de participantes
     */
    public ParticipantesDTO toParticipantesDTO(ProyectoEstado estado) {
        ParticipantesDTO.ParticipantesDTOBuilder builder = ParticipantesDTO.builder();

        if (estado.getDirectorId() != null) {
            builder.director(PersonaDTO.builder()
                    .id(estado.getDirectorId())
                    .nombre(estado.getDirectorNombre() != null ? estado.getDirectorNombre() : "No asignado")
                    .build());
        }

        if (estado.getCodirectorId() != null) {
            builder.codirector(PersonaDTO.builder()
                    .id(estado.getCodirectorId())
                    .nombre(estado.getCodirectorNombre() != null ? estado.getCodirectorNombre() : "No asignado")
                    .build());
        }

        return builder.build();
    }

    /**
     * Convierte información de estudiantes
     */
    public EstudiantesDTO toEstudiantesDTO(ProyectoEstado estado) {
        EstudiantesDTO.EstudiantesDTOBuilder builder = EstudiantesDTO.builder();

        if (estado.getEstudiante1Id() != null) {
            builder.estudiante1(EstudianteDTO.builder()
                    .id(estado.getEstudiante1Id())
                    .nombre(estado.getEstudiante1Nombre() != null ? estado.getEstudiante1Nombre() : "Sin nombre")
                    .email(estado.getEstudiante1Email())
                    .build());
        }

        if (estado.getEstudiante2Id() != null) {
            builder.estudiante2(EstudianteDTO.builder()
                    .id(estado.getEstudiante2Id())
                    .nombre(estado.getEstudiante2Nombre() != null ? estado.getEstudiante2Nombre() : "Sin nombre")
                    .email(estado.getEstudiante2Email())
                    .build());
        }

        return builder.build();
    }

    /**
     * Convierte ProyectoEstado a ProyectoResumenDTO
     */
    public ProyectoResumenDTO toProyectoResumenDTO(ProyectoEstado proyecto) {
        return ProyectoResumenDTO.builder()
                .proyectoId(proyecto.getProyectoId())
                .titulo(proyecto.getTitulo())
                .estadoActual(proyecto.getEstadoActual())
                .estadoLegible(projectStateService.convertirEstadoLegible(proyecto.getEstadoActual()))
                .fase(proyecto.getFase())
                .modalidad(proyecto.getModalidad())
                .programa(proyecto.getPrograma())
                .ultimaActualizacion(proyecto.getUltimaActualizacion())
                .director(proyecto.getDirectorId() != null ? PersonaDTO.builder()
                        .id(proyecto.getDirectorId())
                        .nombre(proyecto.getDirectorNombre() != null ? proyecto.getDirectorNombre() : "No asignado")
                        .build() : null)
                .codirector(proyecto.getCodirectorId() != null ? PersonaDTO.builder()
                        .id(proyecto.getCodirectorId())
                        .nombre(proyecto.getCodirectorNombre() != null ? proyecto.getCodirectorNombre() : "No asignado")
                        .build() : null)
                .estudiantes(toEstudiantesDTO(proyecto))
                .build();
    }

    /**
     * Convierte ProyectoEstado a ProyectoResumenDTO con rol del usuario
     */
    public ProyectoResumenDTO toProyectoResumenDTO(ProyectoEstado proyecto, Long userId) {
        ProyectoResumenDTO dto = toProyectoResumenDTO(proyecto);

        // Determinar rol del usuario en el proyecto
        if (userId.equals(proyecto.getDirectorId())) {
            dto.setRol("DIRECTOR");
        } else if (userId.equals(proyecto.getCodirectorId())) {
            dto.setRol("CODIRECTOR");
        } else if (userId.equals(proyecto.getEstudiante1Id()) || userId.equals(proyecto.getEstudiante2Id())) {
            dto.setRol("ESTUDIANTE");
        } else {
            dto.setRol("PARTICIPANTE");
        }

        return dto;
    }

    /**
     * Convierte HistorialEvento a HistorialEventoDTO
     */
    public HistorialEventoDTO toHistorialEventoDTO(HistorialEvento evento) {
        HistorialEventoDTO.HistorialEventoDTOBuilder builder = HistorialEventoDTO.builder()
                .eventoId(evento.getEventoId())
                .proyectoId(evento.getProyectoId())
                .tipoEvento(evento.getTipoEvento())
                .fecha(evento.getFecha())
                .descripcion(evento.getDescripcion())
                .version(evento.getVersion())
                .resultado(evento.getResultado())
                .observaciones(evento.getObservaciones());

        if (evento.getUsuarioResponsableId() != null) {
            builder.responsable(PersonaDTO.builder()
                    .id(evento.getUsuarioResponsableId())
                    .nombre(evento.getUsuarioResponsableNombre())
                    .build());
        }

        return builder.build();
    }
}


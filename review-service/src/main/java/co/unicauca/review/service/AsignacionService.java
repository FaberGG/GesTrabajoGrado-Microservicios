package co.unicauca.review.service;

import co.unicauca.review.client.SubmissionServiceClient;
import co.unicauca.review.dto.request.AsignacionRequestDTO;
import co.unicauca.review.dto.response.AsignacionDTO;
import co.unicauca.review.dto.response.EvaluadorInfoDTO;
import co.unicauca.review.entity.AsignacionEvaluadores;
import co.unicauca.review.enums.AsignacionEstado;
import co.unicauca.review.event.EvaluadoresAsignadosEvent;
import co.unicauca.review.exception.ResourceNotFoundException;
import co.unicauca.review.repository.AsignacionEvaluadoresRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AsignacionService {

    private static final Logger log = LoggerFactory.getLogger(AsignacionService.class);

    private final AsignacionEvaluadoresRepository asignacionRepository;
    private final SubmissionServiceClient submissionClient;
    private final EventPublisherService eventPublisher;

    public AsignacionService(
            AsignacionEvaluadoresRepository asignacionRepository,
            SubmissionServiceClient submissionClient,
            EventPublisherService eventPublisher) {
        this.asignacionRepository = asignacionRepository;
        this.submissionClient = submissionClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AsignacionDTO asignar(AsignacionRequestDTO request) {
        log.info("Asignando evaluadores al anteproyecto {}: eval1={}, eval2={}",
                request.anteproyectoId(), request.evaluador1Id(), request.evaluador2Id());

        // Verificar que el anteproyecto no tenga asignación previa
        if (asignacionRepository.existsByAnteproyectoId(request.anteproyectoId())) {
            throw new IllegalArgumentException(
                "El anteproyecto ya tiene evaluadores asignados"
            );
        }

        // Verificar que el anteproyecto exista
        SubmissionServiceClient.AnteproyectoDTO anteproyecto =
            submissionClient.getAnteproyecto(request.anteproyectoId());

        // Crear asignación
        AsignacionEvaluadores asignacion = new AsignacionEvaluadores();
        asignacion.setAnteproyectoId(request.anteproyectoId());
        asignacion.setEvaluador1Id(request.evaluador1Id());
        asignacion.setEvaluador2Id(request.evaluador2Id());
        asignacion.setEstado(AsignacionEstado.PENDIENTE);
        asignacion.setFechaAsignacion(LocalDateTime.now());

        AsignacionEvaluadores saved = asignacionRepository.save(asignacion);

        log.info("Asignación creada exitosamente: id={}", saved.getId());

        // Publicar evento a progress-tracking
        publishEvaluadoresAsignadosEvent(saved, request);

        return mapToDTO(saved, anteproyecto.getTitulo());
    }

    /**
     * Publica evento de evaluadores asignados a progress-tracking.
     */
    private void publishEvaluadoresAsignadosEvent(AsignacionEvaluadores asignacion, AsignacionRequestDTO request) {
        try {
            log.debug("Preparando evento evaluadores.asignados para progress-tracking");

            // Construir lista de evaluadores
            List<java.util.Map<String, Object>> evaluadores = new ArrayList<>();

            // Evaluador 1
            evaluadores.add(java.util.Map.of(
                "id", asignacion.getEvaluador1Id(),
                "nombre", "Evaluador 1" // TODO: Obtener nombre real de identity-service
            ));

            // Evaluador 2
            evaluadores.add(java.util.Map.of(
                "id", asignacion.getEvaluador2Id(),
                "nombre", "Evaluador 2" // TODO: Obtener nombre real de identity-service
            ));

            // Crear y publicar evento
            EvaluadoresAsignadosEvent evento = EvaluadoresAsignadosEvent.builder()
                .proyectoId(asignacion.getAnteproyectoId())
                .evaluadores(evaluadores)
                .usuarioResponsableId(1L) // TODO: Obtener ID real del jefe que asigna
                .usuarioResponsableNombre("Jefe de Departamento") // TODO: Obtener nombre real
                .usuarioResponsableRol("JEFE_DEPARTAMENTO")
                .build();

            eventPublisher.publishEvaluadoresAsignados(evento);

        } catch (Exception e) {
            log.error("⚠️ Error publicando evento evaluadores.asignados (no crítico): {}", e.getMessage());
            // No propagar excepción - la asignación ya se guardó
        }
    }

    @Transactional(readOnly = true)
    public Page<AsignacionDTO> findAll(AsignacionEstado estado, int page, int size) {
        log.info("Listando asignaciones - estado: {}, page: {}, size: {}", estado, page, size);

        // Si se solicitan asignaciones PENDIENTES, obtener anteproyectos pendientes desde submission
        if (estado == AsignacionEstado.PENDIENTE) {
            log.info("Obteniendo anteproyectos pendientes desde submission-service");
            Page<AsignacionDTO> anteproyectosPendientes = submissionClient.getAnteproyectosPendientes(page, size);

            // Filtrar los que ya tienen asignación en la BD local
            List<AsignacionDTO> filtrados = anteproyectosPendientes.getContent().stream()
                .filter(dto -> {
                    boolean existeAsignacion = asignacionRepository.existsByAnteproyectoId(dto.anteproyectoId());
                    if (existeAsignacion) {
                        log.debug("Anteproyecto {} ya tiene asignación, se excluye de pendientes", dto.anteproyectoId());
                    }
                    return !existeAsignacion; // Mantener solo los que NO tienen asignación
                })
                .toList();

            log.info("Se filtraron {} anteproyectos pendientes de {} totales (excluyendo los que ya tienen asignación)",
                    filtrados.size(), anteproyectosPendientes.getContent().size());

            // Crear nueva página con los filtrados
            return new org.springframework.data.domain.PageImpl<>(
                filtrados,
                anteproyectosPendientes.getPageable(),
                filtrados.size() // Total ajustado después del filtro
            );
        }

        // Para otros estados, consultar BD local (asignaciones ya creadas)
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaAsignacion").descending());

        Page<AsignacionEvaluadores> asignaciones;
        if (estado != null) {
            asignaciones = asignacionRepository.findByEstado(estado, pageable);
        } else {
            asignaciones = asignacionRepository.findAll(pageable);
        }

        return asignaciones.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<AsignacionDTO> findByEvaluador(Long evaluadorId, AsignacionEstado estado,
                                                int page, int size) {
        log.debug("Listando asignaciones del evaluador {} - estado: {}", evaluadorId, estado);

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaAsignacion").descending());

        Page<AsignacionEvaluadores> asignaciones =
            asignacionRepository.findByEvaluador(evaluadorId, estado, pageable);

        return asignaciones.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public AsignacionDTO findByAnteproyectoId(Long anteproyectoId) {
        log.debug("Obteniendo asignación del anteproyecto {}", anteproyectoId);

        AsignacionEvaluadores asignacion = asignacionRepository
            .findByAnteproyectoId(anteproyectoId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No se encontró asignación para el anteproyecto: " + anteproyectoId
            ));

        return mapToDTO(asignacion);
    }

    /**
     * Mapea una entidad AsignacionEvaluadores a DTO
     */
    private AsignacionDTO mapToDTO(AsignacionEvaluadores asignacion) {
        String titulo = obtenerTituloAnteproyecto(asignacion.getAnteproyectoId());
        return mapToDTO(asignacion, titulo);
    }

    /**
     * Mapea una entidad AsignacionEvaluadores a DTO con título conocido
     */
    private AsignacionDTO mapToDTO(AsignacionEvaluadores asignacion, String titulo) {
        // En un escenario real, aquí se consultarían los datos de los evaluadores
        // desde el Identity Service. Por ahora usamos datos simulados.
        EvaluadorInfoDTO eval1 = new EvaluadorInfoDTO(
            asignacion.getEvaluador1Id(),
            "Evaluador " + asignacion.getEvaluador1Id(),
            "evaluador" + asignacion.getEvaluador1Id() + "@unicauca.edu.co",
            asignacion.getEvaluador1Decision(),
            asignacion.getEvaluador1Observaciones()
        );

        EvaluadorInfoDTO eval2 = new EvaluadorInfoDTO(
            asignacion.getEvaluador2Id(),
            "Evaluador " + asignacion.getEvaluador2Id(),
            "evaluador" + asignacion.getEvaluador2Id() + "@unicauca.edu.co",
            asignacion.getEvaluador2Decision(),
            asignacion.getEvaluador2Observaciones()
        );

        return new AsignacionDTO(
            asignacion.getId(),
            asignacion.getAnteproyectoId(),
            titulo,
            eval1,
            eval2,
            asignacion.getEstado(),
            asignacion.getFechaAsignacion(),
            asignacion.getFechaCompletado(),
            asignacion.getFinalDecision()
        );
    }

    /**
     * Obtiene el título del anteproyecto desde Submission Service
     */
    private String obtenerTituloAnteproyecto(Long anteproyectoId) {
        try {
            SubmissionServiceClient.AnteproyectoDTO anteproyecto =
                submissionClient.getAnteproyecto(anteproyectoId);
            return anteproyecto.getTitulo();
        } catch (Exception e) {
            log.warn("No se pudo obtener título del anteproyecto {}: {}",
                    anteproyectoId, e.getMessage());
            return "Anteproyecto #" + anteproyectoId;
        }
    }
}


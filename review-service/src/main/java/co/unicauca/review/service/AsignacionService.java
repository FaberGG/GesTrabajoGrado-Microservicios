package co.unicauca.review.service;

import co.unicauca.review.client.SubmissionServiceClient;
import co.unicauca.review.dto.request.AsignacionRequestDTO;
import co.unicauca.review.dto.response.AsignacionDTO;
import co.unicauca.review.dto.response.EvaluadorInfoDTO;
import co.unicauca.review.entity.AsignacionEvaluadores;
import co.unicauca.review.enums.AsignacionEstado;
import co.unicauca.review.exception.ResourceNotFoundException;
import co.unicauca.review.repository.AsignacionEvaluadoresRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AsignacionService {

    private static final Logger log = LoggerFactory.getLogger(AsignacionService.class);

    private final AsignacionEvaluadoresRepository asignacionRepository;
    private final SubmissionServiceClient submissionClient;

    public AsignacionService(
            AsignacionEvaluadoresRepository asignacionRepository,
            SubmissionServiceClient submissionClient) {
        this.asignacionRepository = asignacionRepository;
        this.submissionClient = submissionClient;
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

        return mapToDTO(saved, anteproyecto.getTitulo());
    }

    @Transactional(readOnly = true)
    public Page<AsignacionDTO> findAll(AsignacionEstado estado, int page, int size) {
        log.debug("Listando todas las asignaciones - estado: {}, page: {}, size: {}",
                estado, page, size);

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


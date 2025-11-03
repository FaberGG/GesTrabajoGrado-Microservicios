package co.unicauca.comunicacionmicroservicios.service;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.dto.*;
import co.unicauca.comunicacionmicroservicios.infrastructure.persistence.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar las submissions con patrón State
 */
@Service
@Transactional
public class SubmissionService implements ISubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    /**
     * Crear un nuevo proyecto submission (estado inicial: FORMATO_A_DILIGENCIADO)
     */
    public SubmissionResponseDTO crearSubmission(CreateSubmissionDTO dto) {
        ProyectoSubmission proyecto = new ProyectoSubmission();

        // Mapear datos del DTO a la entidad
        proyecto.setTitulo(dto.getTitulo());
        proyecto.setDescripcion(dto.getDescripcion());
        proyecto.setModalidad(dto.getModalidad());
        proyecto.setDocenteDirectorId(dto.getDocenteDirectorId());
        proyecto.setDocenteCodirectorId(dto.getDocenteCodirectorId());
        proyecto.setEstudianteId(dto.getEstudianteId());
        proyecto.setObjetivoGeneral(dto.getObjetivoGeneral());
        proyecto.setObjetivosEspecificos(dto.getObjetivosEspecificos());
        proyecto.setRutaFormatoA(dto.getRutaFormatoA());
        proyecto.setRutaCarta(dto.getRutaCarta());

        // El constructor ya inicializa el estado en FORMATO_A_DILIGENCIADO
        ProyectoSubmission guardado = submissionRepository.save(proyecto);

        System.out.println("✅ Nuevo proyecto creado con ID: " + guardado.getId() +
                         " en estado: " + guardado.getEstadoNombre());

        return convertirADTO(guardado);
    }

    /**
     * Presentar el formato A al coordinador
     * Transición: FORMATO_A_DILIGENCIADO -> PRESENTADO_AL_COORDINADOR
     */
    public SubmissionResponseDTO presentarAlCoordinador(Long id) {
        ProyectoSubmission proyecto = obtenerProyectoPorId(id);

        // Delegar al patrón State
        proyecto.presentarAlCoordinador();

        ProyectoSubmission actualizado = submissionRepository.save(proyecto);
        System.out.println("📤 Proyecto " + id + " presentado al coordinador");

        return convertirADTO(actualizado);
    }

    /**
     * Enviar el formato A al comité para evaluación
     * Transición: PRESENTADO_AL_COORDINADOR -> EN_EVALUACION_COMITE
     */
    public SubmissionResponseDTO enviarAComite(Long id) {
        ProyectoSubmission proyecto = obtenerProyectoPorId(id);

        // Delegar al patrón State
        proyecto.enviarAComite();

        ProyectoSubmission actualizado = submissionRepository.save(proyecto);
        System.out.println("📨 Proyecto " + id + " enviado al comité para evaluación");

        return convertirADTO(actualizado);
    }

    /**
     * Evaluar el formato A (aprobar o rechazar)
     * Transiciones posibles desde EN_EVALUACION_COMITE:
     * - Si aprueba -> ACEPTADO_POR_COMITE
     * - Si rechaza y intentos < 3 -> CORRECCIONES_COMITE
     * - Si rechaza y intentos >= 3 -> RECHAZADO_POR_COMITE
     */
    public SubmissionResponseDTO evaluar(Long id, EvaluacionDTO evaluacion) {
        ProyectoSubmission proyecto = obtenerProyectoPorId(id);

        // Delegar al patrón State
        proyecto.evaluar(evaluacion.getAprobado(), evaluacion.getComentarios());

        ProyectoSubmission actualizado = submissionRepository.save(proyecto);

        if (evaluacion.getAprobado()) {
            System.out.println("✅ Proyecto " + id + " APROBADO por el comité");
        } else {
            System.out.println("❌ Proyecto " + id + " RECHAZADO (Intento " +
                             actualizado.getNumeroIntentos() + "/3)");
        }

        return convertirADTO(actualizado);
    }

    /**
     * Subir una nueva versión del formato A tras correcciones
     * Transición: CORRECCIONES_COMITE -> EN_EVALUACION_COMITE
     */
    public SubmissionResponseDTO subirNuevaVersion(Long id) {
        ProyectoSubmission proyecto = obtenerProyectoPorId(id);

        // Delegar al patrón State
        proyecto.subirNuevaVersion();

        ProyectoSubmission actualizado = submissionRepository.save(proyecto);
        System.out.println("🔄 Proyecto " + id + " - Nueva versión subida, reenviando al comité");

        return convertirADTO(actualizado);
    }

    /**
     * Obtener un proyecto por ID
     */
    @Transactional(readOnly = true)
    public SubmissionResponseDTO obtenerSubmission(Long id) {
        ProyectoSubmission proyecto = obtenerProyectoPorId(id);
        return convertirADTO(proyecto);
    }

    /**
     * Listar todos los proyectos
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponseDTO> listarTodos() {
        return submissionRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar proyectos por estado
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponseDTO> listarPorEstado(String estado) {
        return submissionRepository.findByEstadoNombre(estado)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar proyectos por docente
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponseDTO> listarPorDocente(Long docenteId) {
        return submissionRepository.findByDocenteDirectorId(docenteId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar proyectos en proceso (no finalizados)
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponseDTO> listarEnProceso() {
        return submissionRepository.findProyectosEnProceso()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Métodos auxiliares privados

    private ProyectoSubmission obtenerProyectoPorId(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + id));
    }

    private SubmissionResponseDTO convertirADTO(ProyectoSubmission proyecto) {
        SubmissionResponseDTO dto = new SubmissionResponseDTO();

        dto.setId(proyecto.getId());
        dto.setTitulo(proyecto.getTitulo());
        dto.setDescripcion(proyecto.getDescripcion());
        dto.setModalidad(proyecto.getModalidad());
        dto.setFechaCreacion(proyecto.getFechaCreacion());
        dto.setFechaUltimaModificacion(proyecto.getFechaUltimaModificacion());

        dto.setDocenteDirectorId(proyecto.getDocenteDirectorId());
        dto.setDocenteCodirectorId(proyecto.getDocenteCodirectorId());
        dto.setEstudianteId(proyecto.getEstudianteId());

        dto.setObjetivoGeneral(proyecto.getObjetivoGeneral());
        dto.setObjetivosEspecificos(proyecto.getObjetivosEspecificos());

        dto.setEstadoActual(proyecto.getEstadoNombre());
        dto.setNumeroIntentos(proyecto.getNumeroIntentos());
        dto.setComentariosComite(proyecto.getComentariosComite());
        dto.setEsEstadoFinal(proyecto.esEstadoFinal());

        dto.setRutaFormatoA(proyecto.getRutaFormatoA());
        dto.setRutaCarta(proyecto.getRutaCarta());

        return dto;
    }

    // Implementación de métodos de ISubmissionService

    @Override
    public IdResponse crearFormatoA(String userId, FormatoAData data, MultipartFile pdf, MultipartFile carta) {
        // TODO: Implementar lógica completa
        throw new UnsupportedOperationException("Método crearFormatoA aún no implementado");
    }

    @Override
    public FormatoAView obtenerFormatoA(Long id) {
        // TODO: Implementar lógica completa
        throw new UnsupportedOperationException("Método obtenerFormatoA aún no implementado");
    }

    @Override
    public FormatoAPage listarFormatoA(Optional<String> docenteId, int page, int size) {
        // TODO: Implementar lógica completa
        throw new UnsupportedOperationException("Método listarFormatoA aún no implementado");
    }

    @Override
    public IdResponse reenviarFormatoA(String userId, Long proyectoId, MultipartFile pdf, MultipartFile carta) {
        // TODO: Implementar lógica completa
        throw new UnsupportedOperationException("Método reenviarFormatoA aún no implementado");
    }

    @Override
    public void cambiarEstadoFormatoA(Long versionId, EvaluacionRequest req) {
        // TODO: Implementar lógica completa
        throw new UnsupportedOperationException("Método cambiarEstadoFormatoA aún no implementado");
    }

    @Override
    public IdResponse subirAnteproyecto(String userId, AnteproyectoData data, MultipartFile pdf) {
        // TODO: Implementar lógica completa
        throw new UnsupportedOperationException("Método subirAnteproyecto aún no implementado");
    }

    @Override
    public AnteproyectoPage listarAnteproyectos(int page, int size) {
        // TODO: Implementar lógica completa
        throw new UnsupportedOperationException("Método listarAnteproyectos aún no implementado");
    }

    @Override
    public void cambiarEstadoAnteproyecto(Long id, CambioEstadoAnteproyectoRequest req) {
        // TODO: Implementar lógica completa
        throw new UnsupportedOperationException("Método cambiarEstadoAnteproyecto aún no implementado");
    }
}


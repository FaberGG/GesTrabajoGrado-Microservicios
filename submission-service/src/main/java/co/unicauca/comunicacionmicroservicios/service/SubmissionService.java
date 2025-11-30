package co.unicauca.comunicacionmicroservicios.service;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.dto.*;
import co.unicauca.comunicacionmicroservicios.dto.events.AnteproyectoEnviadoEvent;
import co.unicauca.comunicacionmicroservicios.dto.events.FormatoAEnviadoEvent;
import co.unicauca.comunicacionmicroservicios.dto.events.FormatoAReenviadoEvent;
import co.unicauca.comunicacionmicroservicios.infrastructure.persistence.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar las submissions con patrón State
 */
@Service
@Transactional
public class SubmissionService implements ISubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ProgressEventPublisher progressEventPublisher;

    @Autowired
    private NotificationPublisher notificationPublisher;


    @Autowired
    private IdentityClient identityClient;

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
        log.info("📝 Creando Formato A inicial - Usuario: {}, Título: {}", userId, data.getTitulo());

        // 1. Validar archivos
        if (pdf == null || pdf.isEmpty()) {
            throw new IllegalArgumentException("El PDF del Formato A es obligatorio");
        }

        if (data.getModalidad() == co.unicauca.comunicacionmicroservicios.domain.model.enumModalidad.PRACTICA_PROFESIONAL) {
            if (carta == null || carta.isEmpty()) {
                throw new IllegalArgumentException("La carta es obligatoria para modalidad PRACTICA_PROFESIONAL");
            }
        }

        // 2. Guardar archivos (TEMPORAL - delegar a FileStorageService si existe)
        String rutaPdf = "/uploads/formatoA/" + pdf.getOriginalFilename();
        String rutaCarta = carta != null ? "/uploads/cartas/" + carta.getOriginalFilename() : null;

        // 3. Crear proyecto
        ProyectoSubmission proyecto = new ProyectoSubmission();
        proyecto.setTitulo(data.getTitulo());
        proyecto.setModalidad(data.getModalidad());
        proyecto.setObjetivoGeneral(data.getObjetivoGeneral());
        proyecto.setObjetivosEspecificos(String.join("; ", data.getObjetivosEspecificos()));
        proyecto.setDocenteDirectorId(Long.valueOf(data.getDirectorId()));
        proyecto.setDocenteCodirectorId(data.getCodirectorId() != null ? Long.valueOf(data.getCodirectorId()) : null);
        proyecto.setEstudianteId(Long.valueOf(data.getEstudiante1Id()));
        proyecto.setRutaFormatoA(rutaPdf);
        proyecto.setRutaCarta(rutaCarta);
        proyecto.setNumeroIntentos(1); // Primera versión

        // 4. Guardar en BD
        ProyectoSubmission guardado = submissionRepository.save(proyecto);
        log.info("✅ Proyecto creado con ID: {}", guardado.getId());

        // TODO: Descomentar cuando los eventos estén correctamente implementados
        /*
        // 5. Obtener información del usuario responsable desde Identity Service
        IdentityClient.UserBasicInfo userInfo = identityClient.getUserById(Long.valueOf(userId));

        // 6. Obtener programa del estudiante
        IdentityClient.UserBasicInfo estudianteInfo = identityClient.getUserById(Long.valueOf(data.getEstudiante1Id()));
        String programa = estudianteInfo.programa() != null ? estudianteInfo.programa() : "DESCONOCIDO";

        // 7. Publicar evento a Progress Tracking (NUEVO)
        FormatoAEnviadoEvent event = FormatoAEnviadoEvent.builder()
                .proyectoId(guardado.getId())
                .titulo(guardado.getTitulo())
                .modalidad(guardado.getModalidad().name())
                .programa(programa)
                .version(1)
                .descripcion("Primera versión del Formato A")
                .timestamp(LocalDateTime.now())
                .usuarioResponsableId(Long.valueOf(userId))
                .usuarioResponsableNombre(userInfo.getNombreCompleto())
                .usuarioResponsableRol("DOCENTE")
                .build();

        progressEventPublisher.publicarFormatoAEnviado(event);
        */

        // 8. Obtener email del coordinador y enviar notificación (RF2)
        try {
            Optional<String> coordinadorEmailOpt = identityClient.getEmailByRole("COORDINADOR");
            if (coordinadorEmailOpt.isPresent()) {
                notificationPublisher.notificarFormatoAEnviado(
                        guardado.getId().intValue(),
                        guardado.getTitulo(),
                        1, // versión 1
                        userInfo.getNombreCompleto(),
                        coordinadorEmailOpt.get()
                );
                log.info("✉️ RF2: Notificación enviada al coordinador: {}", coordinadorEmailOpt.get());
            } else {
                log.warn("⚠️ RF2: No se encontró email de coordinador, notificación no enviada");
            }
        } catch (Exception e) {
            log.error("❌ RF2: Error al enviar notificación, pero el Formato A fue creado exitosamente", e);
            // No fallar la operación principal por error en notificación
        }

        // 9. Retornar respuesta
        return new IdResponse(guardado.getId());
    }

    @Override
    public FormatoAView obtenerFormatoA(Long id) {
        log.info("📋 Obteniendo Formato A por ID: {}", id);

        ProyectoSubmission proyecto = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formato A no encontrado: " + id));

        FormatoAView view = convertirProyectoAFormatoAView(proyecto);
        log.info("✅ Formato A {} encontrado: {}", id, proyecto.getTitulo());
        return view;
    }

    @Override
    public FormatoAPage listarFormatoA(Optional<String> docenteId, int page, int size) {
        // TODO: Implementar lógica completa
        throw new UnsupportedOperationException("Método listarFormatoA aún no implementado");
    }

    @Override
    public FormatoAPage listarFormatosAPendientes(int page, int size) {
        log.info("📋 Listando Formatos A pendientes - page: {}, size: {}", page, size);

        // Crear objeto Pageable para paginación
        org.springframework.data.domain.Pageable pageable =
            org.springframework.data.domain.PageRequest.of(page, size);

        // Obtener proyectos pendientes desde el repositorio
        org.springframework.data.domain.Page<ProyectoSubmission> proyectos =
            submissionRepository.findFormatosAPendientes(pageable);

        // Convertir entidades a DTOs
        List<FormatoAView> formatosView = proyectos.getContent().stream()
            .map(this::convertirProyectoAFormatoAView)
            .collect(Collectors.toList());

        // Construir respuesta paginada
        FormatoAPage response = new FormatoAPage();
        response.setContent(formatosView);
        response.setPage(proyectos.getNumber());
        response.setSize(proyectos.getSize());
        response.setTotalElements(proyectos.getTotalElements());

        log.info("✅ Se encontraron {} Formatos A pendientes", formatosView.size());
        return response;
    }

    private FormatoAView convertirProyectoAFormatoAView(ProyectoSubmission proyecto) {
        FormatoAView view = new FormatoAView();
        view.setId(proyecto.getId());
        view.setProyectoId(proyecto.getId());
        view.setTitulo(proyecto.getTitulo());
        view.setVersion(proyecto.getNumeroIntentos());

        // Mapear estado del proyecto a estado del formato - SIEMPRE establecer un estado
        if ("FORMATO_A_DILIGENCIADO".equals(proyecto.getEstadoNombre()) ||
            "PRESENTADO_AL_COORDINADOR".equals(proyecto.getEstadoNombre()) ||
            "EN_EVALUACION_COMITE".equals(proyecto.getEstadoNombre())) {
            view.setEstado(co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoFormato.PENDIENTE);
        } else if ("ACEPTADO_POR_COMITE".equals(proyecto.getEstadoNombre())) {
            view.setEstado(co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoFormato.APROBADO);
        } else if ("RECHAZADO_POR_COMITE".equals(proyecto.getEstadoNombre()) ||
                   "CORRECCIONES_COMITE".equals(proyecto.getEstadoNombre())) {
            view.setEstado(co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoFormato.RECHAZADO);
        } else {
            // Valor por defecto
            view.setEstado(co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoFormato.PENDIENTE);
        }

        view.setObservaciones(proyecto.getComentariosComite());
        view.setPdfUrl(proyecto.getRutaFormatoA());
        view.setCartaUrl(proyecto.getRutaCarta());
        view.setFechaEnvio(proyecto.getFechaCreacion());

        // Extraer nombre del archivo de la ruta
        if (proyecto.getRutaFormatoA() != null) {
            String[] partes = proyecto.getRutaFormatoA().split("/");
            view.setNombreArchivo(partes[partes.length - 1]);
        }

        // Obtener información del docente director desde Identity Service
        try {
            IdentityClient.UserBasicInfo directorInfo = identityClient.getUserById(proyecto.getDocenteDirectorId());
            view.setDocenteDirectorNombre(directorInfo.getNombreCompleto());
            view.setDocenteDirectorEmail(directorInfo.email());
        } catch (Exception e) {
            log.warn("No se pudo obtener información del director {}: {}", proyecto.getDocenteDirectorId(), e.getMessage());
            view.setDocenteDirectorNombre("Director ID: " + proyecto.getDocenteDirectorId());
            view.setDocenteDirectorEmail("director." + proyecto.getDocenteDirectorId() + "@unicauca.edu.co");
        }

        // Obtener información del estudiante desde Identity Service
        List<String> estudiantesEmails = new java.util.ArrayList<>();
        if (proyecto.getEstudianteId() != null) {
            try {
                IdentityClient.UserBasicInfo estudianteInfo = identityClient.getUserById(proyecto.getEstudianteId());
                estudiantesEmails.add(estudianteInfo.email());
            } catch (Exception e) {
                log.warn("No se pudo obtener información del estudiante {}: {}", proyecto.getEstudianteId(), e.getMessage());
                estudiantesEmails.add("estudiante." + proyecto.getEstudianteId() + "@unicauca.edu.co");
            }
        }
        view.setEstudiantesEmails(estudiantesEmails);

        return view;
    }

    @Override
    public IdResponse reenviarFormatoA(String userId, Long proyectoId, MultipartFile pdf, MultipartFile carta) {
        log.info("🔄 Reenviando Formato A - Proyecto: {}, Usuario: {}", proyectoId, userId);

        // 1. Validar que el proyecto existe
        ProyectoSubmission proyecto = submissionRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado: " + proyectoId));

        // 2. Validar que el usuario es el director
        if (!proyecto.getDocenteDirectorId().equals(Long.valueOf(userId))) {
            throw new IllegalArgumentException("Solo el director del proyecto puede reenviar el Formato A");
        }

        // 3. Validar que no está rechazado definitivamente
        if ("RECHAZADO_POR_COMITE".equals(proyecto.getEstadoNombre())) {
            throw new IllegalArgumentException("El proyecto fue rechazado definitivamente, no se puede reenviar");
        }

        // 4. Validar que no excede 3 intentos
        if (proyecto.getNumeroIntentos() >= 3) {
            throw new IllegalArgumentException("Se alcanzó el máximo de intentos (3)");
        }

        // 5. Validar archivos
        if (pdf == null || pdf.isEmpty()) {
            throw new IllegalArgumentException("El PDF del Formato A es obligatorio");
        }

        // 6. Guardar nuevos archivos
        String rutaPdf = "/uploads/formatoA/v" + (proyecto.getNumeroIntentos() + 1) + "_" + pdf.getOriginalFilename();
        String rutaCarta = carta != null ? "/uploads/cartas/v" + (proyecto.getNumeroIntentos() + 1) + "_" + carta.getOriginalFilename() : null;

        // 7. Actualizar proyecto
        proyecto.setRutaFormatoA(rutaPdf);
        if (rutaCarta != null) {
            proyecto.setRutaCarta(rutaCarta);
        }
        proyecto.setNumeroIntentos(proyecto.getNumeroIntentos() + 1);
        proyecto.setFechaUltimaModificacion(LocalDateTime.now());

        // 8. Guardar en BD
        ProyectoSubmission actualizado = submissionRepository.save(proyecto);
        log.info("✅ Formato A reenviado - Intento: {}/3", actualizado.getNumeroIntentos());

        // TODO: Descomentar cuando los eventos estén correctamente implementados
        /*
        // 9. Obtener información del usuario
        IdentityClient.UserBasicInfo userInfo = identityClient.getUserById(Long.valueOf(userId));

        // 10. Publicar evento a Progress Tracking
        FormatoAReenviadoEvent event = FormatoAReenviadoEvent.builder()
                .proyectoId(actualizado.getId())
                .version(actualizado.getNumeroIntentos())
                .descripcion("Correcciones aplicadas - versión " + actualizado.getNumeroIntentos())
                .timestamp(LocalDateTime.now())
                .usuarioResponsableId(Long.valueOf(userId))
                .usuarioResponsableNombre(userInfo.getNombreCompleto())
                .usuarioResponsableRol("DOCENTE")
                .build();

        progressEventPublisher.publicarFormatoAReenviado(event);
        */

        // 11. Obtener email del coordinador y enviar notificación (RF4)
        try {
            Optional<String> coordinadorEmailOpt = identityClient.getEmailByRole("COORDINADOR");
            if (coordinadorEmailOpt.isPresent()) {
                notificationPublisher.notificarFormatoAEnviado(
                        actualizado.getId().intValue(),
                        actualizado.getTitulo(),
                        actualizado.getNumeroIntentos(), // versión 2 o 3
                        userInfo.getNombreCompleto(),
                        coordinadorEmailOpt.get()
                );
                log.info("✉️ RF4: Notificación de reenvío (v{}) enviada al coordinador: {}",
                         actualizado.getNumeroIntentos(), coordinadorEmailOpt.get());
            } else {
                log.warn("⚠️ RF4: No se encontró email de coordinador, notificación no enviada");
            }
        } catch (Exception e) {
            log.error("❌ RF4: Error al enviar notificación, pero el Formato A fue reenviado exitosamente", e);
            // No fallar la operación principal por error en notificación
        }

        // 12. Retornar respuesta
        return new IdResponse(actualizado.getId());
    }

    @Override
    public void cambiarEstadoFormatoA(Long versionId, EvaluacionRequest req) {
        log.info("📝 Cambiando estado de Formato A (versionId: {}) a: {} por evaluador: {}",
                 versionId, req.getEstado(), req.getEvaluadoPor());

        // 1. Buscar el proyecto por ID (versionId es el proyectoId en este contexto)
        ProyectoSubmission proyecto = submissionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Formato A no encontrado: " + versionId));

        // 2. Obtener estado actual
        String estadoActual = proyecto.getEstadoNombre();
        log.debug("Estado actual del proyecto: {}", estadoActual);

        // 3. Realizar transiciones automáticas si es necesario para llegar a un estado evaluable
        // El flujo normal es: FORMATO_A_DILIGENCIADO -> PRESENTADO_AL_COORDINADOR -> EN_EVALUACION_COMITE -> EVALUADO
        try {
            if ("FORMATO_A_DILIGENCIADO".equals(estadoActual)) {
                log.info("🔄 Transición automática: FORMATO_A_DILIGENCIADO -> PRESENTADO_AL_COORDINADOR");
                proyecto.presentarAlCoordinador();
                estadoActual = proyecto.getEstadoNombre();
            }

            if ("PRESENTADO_AL_COORDINADOR".equals(estadoActual)) {
                log.info("🔄 Transición automática: PRESENTADO_AL_COORDINADOR -> EN_EVALUACION_COMITE");
                proyecto.enviarAComite();
                estadoActual = proyecto.getEstadoNombre();
            }

            // 4. Determinar si fue aprobado o rechazado
            boolean aprobado = "APROBADO".equalsIgnoreCase(req.getEstado());
            String observaciones = req.getObservaciones() != null ? req.getObservaciones() : "";

            // 5. Ahora sí evaluar (debería estar en EN_EVALUACION_COMITE)
            log.info("📋 Evaluando proyecto desde estado: {}", estadoActual);
            proyecto.evaluar(aprobado, observaciones);

            // 6. Actualizar fecha de modificación
            proyecto.setFechaUltimaModificacion(LocalDateTime.now());

            // 7. Persistir cambios
            submissionRepository.save(proyecto);

            log.info("✅ Estado de Formato A {} actualizado exitosamente a: {}",
                     versionId, proyecto.getEstadoNombre());

        } catch (IllegalStateException e) {
            log.error("❌ Error al cambiar estado del Formato A {}: {}", versionId, e.getMessage());
            throw new IllegalStateException("No se puede cambiar el estado del Formato A: " + e.getMessage());
        }
    }

    @Override
    public IdResponse subirAnteproyecto(String userId, AnteproyectoData data, MultipartFile pdf) {
        log.info("📄 Subiendo anteproyecto - Proyecto: {}, Usuario: {}", data.getProyectoId(), userId);

        // 1. Validar archivo
        if (pdf == null || pdf.isEmpty()) {
            throw new IllegalArgumentException("El PDF del anteproyecto es obligatorio");
        }

        // 2. Validar que el proyecto existe
        ProyectoSubmission proyecto = submissionRepository.findById(data.getProyectoId())
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado: " + data.getProyectoId()));

        // 3. Validar que el usuario es el director
        if (!proyecto.getDocenteDirectorId().equals(Long.valueOf(userId))) {
            throw new IllegalArgumentException("Solo el director del proyecto puede subir el anteproyecto");
        }

        // 4. Validar que el Formato A está aprobado
        if (!"ACEPTADO_POR_COMITE".equals(proyecto.getEstadoNombre())) {
            throw new IllegalArgumentException("El Formato A debe estar aprobado para subir el anteproyecto");
        }

        // 5. Guardar archivo del anteproyecto
        String rutaAnteproyecto = "/uploads/anteproyectos/" + data.getProyectoId() + "_" + pdf.getOriginalFilename();

        // 6. Actualizar estado del proyecto
        // TODO: Si la entidad tiene campo rutaAnteproyecto, descomentarlo
        // proyecto.setRutaAnteproyecto(rutaAnteproyecto);
        proyecto.setFechaUltimaModificacion(LocalDateTime.now());
        // TODO: Si existe estado ANTEPROYECTO_ENVIADO, descomentarlo
        // proyecto.setEstadoNombre("ANTEPROYECTO_ENVIADO");

        // 7. Guardar en BD
        ProyectoSubmission actualizado = submissionRepository.save(proyecto);
        log.info("✅ Anteproyecto subido para proyecto: {}", actualizado.getId());

        // TODO: Descomentar cuando los eventos estén correctamente implementados
        /*
        // 8. Obtener información del usuario
        IdentityClient.UserBasicInfo userInfo = identityClient.getUserById(Long.valueOf(userId));

        // 9. Publicar evento a Progress Tracking (NUEVO)
        AnteproyectoEnviadoEvent event = AnteproyectoEnviadoEvent.builder()
                .proyectoId(actualizado.getId())
                .descripcion("Anteproyecto completo enviado")
                .timestamp(LocalDateTime.now())
                .usuarioResponsableId(Long.valueOf(userId))
                .usuarioResponsableNombre(userInfo.getNombreCompleto())
                .usuarioResponsableRol("DOCENTE")
                .build();

        progressEventPublisher.publicarAnteproyectoEnviado(event);
        */

        // 10. Obtener email del jefe de departamento y enviar notificación (RF6)
        try {
            Optional<String> jefeEmailOpt = identityClient.getEmailByRole("JEFE_DEPARTAMENTO");
            if (jefeEmailOpt.isPresent()) {
                notificationPublisher.notificarAnteproyectoEnviado(
                        actualizado.getId().intValue(),
                        actualizado.getTitulo(),
                        userInfo.getNombreCompleto(),
                        jefeEmailOpt.get()
                );
                log.info("✉️ RF6: Notificación enviada al jefe de departamento: {}", jefeEmailOpt.get());
            } else {
                log.warn("⚠️ RF6: No se encontró email de jefe de departamento, notificación no enviada");
            }
        } catch (Exception e) {
            log.error("❌ RF6: Error al enviar notificación, pero el Anteproyecto fue creado exitosamente", e);
            // No fallar la operación principal por error en notificación
        }

        // 11. Retornar respuesta
        return new IdResponse(actualizado.getId());
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


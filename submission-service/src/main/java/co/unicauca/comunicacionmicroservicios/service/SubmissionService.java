package co.unicauca.comunicacionmicroservicios.service;

import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoSubmission;
import co.unicauca.comunicacionmicroservicios.domain.model.ProyectoGrado;
import co.unicauca.comunicacionmicroservicios.domain.model.Anteproyecto;
import co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoProyecto;
import co.unicauca.comunicacionmicroservicios.dto.*;
import co.unicauca.comunicacionmicroservicios.dto.events.AnteproyectoEnviadoEvent;
import co.unicauca.comunicacionmicroservicios.dto.events.FormatoAEnviadoEvent;
import co.unicauca.comunicacionmicroservicios.dto.events.FormatoAReenviadoEvent;
import co.unicauca.comunicacionmicroservicios.infrastructure.persistence.SubmissionRepository;
import co.unicauca.comunicacionmicroservicios.infraestructure.repository.IProyectoGradoRepository;
import co.unicauca.comunicacionmicroservicios.infraestructure.repository.IAnteproyectoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private IProyectoGradoRepository proyectoGradoRepository;

    @Autowired
    private IAnteproyectoRepository anteproyectoRepository;

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
        proyecto.setEstudiante1Id(dto.getEstudianteId());
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
     * Transición: FORMATO_A_DILIGENCIADO -> EN_EVALUACION_COORDINADOR
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
     * Evaluar el formato A (aprobar o rechazar) - RF-3
     * El COORDINADOR evalúa el formato A.
     *
     * Transiciones posibles desde EN_EVALUACION_COORDINADOR:
     * - Si aprueba → FORMATO_A_APROBADO
     * - Si rechaza y intentos < 3 → CORRECCIONES_SOLICITADAS
     * - Si rechaza y intentos >= 3 → FORMATO_A_RECHAZADO
     */
    public SubmissionResponseDTO evaluar(Long id, EvaluacionDTO evaluacion) {
        ProyectoSubmission proyecto = obtenerProyectoPorId(id);

        // Delegar al patrón State
        proyecto.evaluar(evaluacion.getAprobado(), evaluacion.getComentarios());

        ProyectoSubmission actualizado = submissionRepository.save(proyecto);

        if (evaluacion.getAprobado()) {
            System.out.println("✅ Proyecto " + id + " APROBADO por el coordinador");
        } else {
            System.out.println("❌ Proyecto " + id + " RECHAZADO por el coordinador (Intento " +
                             actualizado.getNumeroIntentos() + "/3)");
        }

        return convertirADTO(actualizado);
    }

    /**
     * Subir una nueva versión del formato A tras correcciones
     * Transición: CORRECCIONES_SOLICITADAS → EN_EVALUACION_COORDINADOR
     */
    public SubmissionResponseDTO subirNuevaVersion(Long id) {
        ProyectoSubmission proyecto = obtenerProyectoPorId(id);

        // Delegar al patrón State
        proyecto.subirNuevaVersion();

        ProyectoSubmission actualizado = submissionRepository.save(proyecto);
        System.out.println("🔄 Proyecto " + id + " - Nueva versión subida, reenviando al coordinador");

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
        dto.setEstudianteId(proyecto.getEstudiante1Id());

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

        // IMPORTANTE: Si el usuario que crea el FormatoA es DOCENTE, se asigna como director automáticamente
        // Esto permite que quien crea el proyecto pueda luego subir el anteproyecto
        Long directorIdFinal = Long.valueOf(userId); // El usuario logueado es el director
        proyecto.setDocenteDirectorId(directorIdFinal);

        proyecto.setDocenteCodirectorId(data.getCodirectorId() != null ? Long.valueOf(data.getCodirectorId()) : null);
        proyecto.setEstudiante1Id(Long.valueOf(data.getEstudiante1Id()));
        proyecto.setEstudiante2Id(data.getEstudiante2Id() != null ? Long.valueOf(data.getEstudiante2Id()) : null);
        proyecto.setRutaFormatoA(rutaPdf);
        proyecto.setRutaCarta(rutaCarta);
        proyecto.setNumeroIntentos(1); // Primera versión

        log.info("✅ Proyecto configurado - Director: {} (usuario que crea el FormatoA)", directorIdFinal);

        // 4. Guardar en BD
        ProyectoSubmission guardado = submissionRepository.save(proyecto);
        log.info("✅ Proyecto creado con ID: {}", guardado.getId());

        // 5. Obtener información del usuario responsable desde Identity Service
        IdentityClient.UserBasicInfo userInfo = identityClient.getUserById(Long.valueOf(userId));
        log.info("✅ Usuario responsable obtenido: {}", userInfo != null ? userInfo.getNombreCompleto() : "DESCONOCIDO");

        // 6. Obtener programa del estudiante (manejo seguro de nulls)
        String programa = "DESCONOCIDO";
        String estudiante1Nombre = null;
        String estudiante1Email = null;
        try {
            IdentityClient.UserBasicInfo estudianteInfo = identityClient.getUserById(Long.valueOf(data.getEstudiante1Id()));
            if (estudianteInfo != null && estudianteInfo.programa() != null) {
                programa = estudianteInfo.programa();
                estudiante1Nombre = estudianteInfo.getNombreCompleto();
                estudiante1Email = estudianteInfo.email();
                log.info("✅ Programa del estudiante obtenido: {}", programa);
            } else {
                log.warn("⚠️ No se pudo obtener el programa del estudiante {}, usando valor por defecto", data.getEstudiante1Id());
            }
        } catch (Exception e) {
            log.error("❌ Error al obtener programa del estudiante {}, usando valor por defecto", data.getEstudiante1Id(), e);
        }

        // 6.1. Obtener información completa del estudiante 2 (si existe)
        String estudiante2Nombre = null;
        String estudiante2Email = null;
        if (data.getEstudiante2Id() != null) {
            try {
                IdentityClient.UserBasicInfo est2Info = identityClient.getUserById(Long.valueOf(data.getEstudiante2Id()));
                if (est2Info != null) {
                    estudiante2Nombre = est2Info.getNombreCompleto();
                    estudiante2Email = est2Info.email();
                    log.info("✅ Información del estudiante 2 obtenida: {}", estudiante2Nombre);
                }
            } catch (Exception e) {
                log.error("❌ Error al obtener información del estudiante 2: {}", data.getEstudiante2Id(), e);
            }
        }

        // 6.2. Obtener información del co-director (si existe)
        String codirectorNombre = null;
        if (guardado.getDocenteCodirectorId() != null) {
            try {
                IdentityClient.UserBasicInfo codirInfo = identityClient.getUserById(guardado.getDocenteCodirectorId());
                if (codirInfo != null) {
                    codirectorNombre = codirInfo.getNombreCompleto();
                    log.info("✅ Información del co-director obtenida: {}", codirectorNombre);
                }
            } catch (Exception e) {
                log.error("❌ Error al obtener información del co-director: {}", guardado.getDocenteCodirectorId(), e);
            }
        }

        // 7. Publicar evento a Progress Tracking con información completa
        FormatoAEnviadoEvent event = FormatoAEnviadoEvent.builder()
                .proyectoId(guardado.getId())
                .titulo(guardado.getTitulo())
                .modalidad(guardado.getModalidad().name())
                .programa(programa)
                .version(1)
                .descripcion("Primera versión del Formato A")
                .timestamp(LocalDateTime.now())
                .usuarioResponsableId(Long.valueOf(userId))
                .usuarioResponsableNombre(userInfo != null ? userInfo.getNombreCompleto() : "DESCONOCIDO")
                .usuarioResponsableRol("DOCENTE")
                // ✨ Información completa del proyecto
                .directorId(guardado.getDocenteDirectorId())
                .directorNombre(userInfo != null ? userInfo.getNombreCompleto() : "DESCONOCIDO")
                .codirectorId(guardado.getDocenteCodirectorId())
                .codirectorNombre(codirectorNombre)
                .estudiante1Id(guardado.getEstudiante1Id())
                .estudiante1Nombre(estudiante1Nombre)
                .estudiante1Email(estudiante1Email)
                .estudiante2Id(guardado.getEstudiante2Id())
                .estudiante2Nombre(estudiante2Nombre)
                .estudiante2Email(estudiante2Email)
                .build();

        log.info("📤 Publicando evento FormatoAEnviado para proyecto: {}", guardado.getId());
        progressEventPublisher.publicarFormatoAEnviado(event);
        log.info("✅ Evento FormatoAEnviado publicado exitosamente");

        // 8. Obtener email del coordinador y enviar notificación (RF2)
        try {
            String coordinadorEmail = identityClient.getCoordinadorEmail();
            notificationPublisher.notificarFormatoAEnviado(
                    guardado.getId().intValue(),
                    guardado.getTitulo(),
                    1, // versión 1
                    userInfo != null ? userInfo.getNombreCompleto() : "DESCONOCIDO",
                    coordinadorEmail
            );
            log.info("✉️ RF2: Notificación enviada al coordinador: {}", coordinadorEmail);
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

        // Mapear estado del proyecto a estado del formato
        // Soporta tanto estados nuevos como antiguos para compatibilidad con BD
        String estado = proyecto.getEstadoNombre();
        if ("FORMATO_A_DILIGENCIADO".equals(estado) ||
            "EN_EVALUACION_COORDINADOR".equals(estado) ||
            "PRESENTADO_AL_COORDINADOR".equals(estado) ||
            "EN_EVALUACION_COMITE".equals(estado)) {
            view.setEstado(co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoFormato.PENDIENTE);
        } else if ("FORMATO_A_APROBADO".equals(estado) ||
                   "ACEPTADO_POR_COMITE".equals(estado)) {
            view.setEstado(co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoFormato.APROBADO);
        } else if ("FORMATO_A_RECHAZADO".equals(estado) ||
                   "CORRECCIONES_SOLICITADAS".equals(estado) ||
                   "RECHAZADO_POR_COMITE".equals(estado) ||
                   "CORRECCIONES_COMITE".equals(estado)) {
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
        if (proyecto.getEstudiante1Id() != null) {
            try {
                IdentityClient.UserBasicInfo estudianteInfo = identityClient.getUserById(proyecto.getEstudiante1Id());
                estudiantesEmails.add(estudianteInfo.email());
            } catch (Exception e) {
                log.warn("No se pudo obtener información del estudiante 1 {}: {}", proyecto.getEstudiante1Id(), e.getMessage());
                estudiantesEmails.add("estudiante." + proyecto.getEstudiante1Id() + "@unicauca.edu.co");
            }
        }
        if (proyecto.getEstudiante2Id() != null) {
            try {
                IdentityClient.UserBasicInfo estudiante2Info = identityClient.getUserById(proyecto.getEstudiante2Id());
                estudiantesEmails.add(estudiante2Info.email());
            } catch (Exception e) {
                log.warn("No se pudo obtener información del estudiante 2 {}: {}", proyecto.getEstudiante2Id(), e.getMessage());
                estudiantesEmails.add("estudiante." + proyecto.getEstudiante2Id() + "@unicauca.edu.co");
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

        // 3. Validar que no está rechazado definitivamente (soporta nombres antiguos y nuevos)
        String estadoActual = proyecto.getEstadoNombre();
        if ("FORMATO_A_RECHAZADO".equals(estadoActual) || "RECHAZADO_POR_COMITE".equals(estadoActual)) {
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

        // 9. Obtener información del usuario
        IdentityClient.UserBasicInfo userInfo = identityClient.getUserById(Long.valueOf(userId));
        log.info("✅ Usuario responsable obtenido: {}", userInfo != null ? userInfo.getNombreCompleto() : "DESCONOCIDO");

        // 10. Obtener información completa del director
        IdentityClient.UserBasicInfo directorInfo = identityClient.getUserById(actualizado.getDocenteDirectorId());
        String directorNombre = directorInfo != null ? directorInfo.getNombreCompleto() : "Director Desconocido";

        // 11. Obtener información del codirector (si existe)
        String codirectorNombre = null;
        if (actualizado.getDocenteCodirectorId() != null) {
            IdentityClient.UserBasicInfo codirectorInfo = identityClient.getUserById(actualizado.getDocenteCodirectorId());
            codirectorNombre = codirectorInfo != null ? codirectorInfo.getNombreCompleto() : "Codirector Desconocido";
        }

        // 12. Obtener información completa del estudiante 1 (incluyendo programa y email)
        IdentityClient.UserBasicInfo estudiante1Info = identityClient.getUserById(actualizado.getEstudiante1Id());
        String estudiante1Nombre = estudiante1Info != null ? estudiante1Info.getNombreCompleto() : "Estudiante Desconocido";
        String estudiante1Email = estudiante1Info != null ? estudiante1Info.email() : null;
        String programa = "DESCONOCIDO";
        if (estudiante1Info != null && estudiante1Info.programa() != null) {
            programa = estudiante1Info.programa();
        }

        // 13. Obtener información completa del estudiante 2 (si existe)
        String estudiante2Nombre = null;
        String estudiante2Email = null;
        Long estudiante2Id = actualizado.getEstudiante2Id();
        if (estudiante2Id != null) {
            try {
                IdentityClient.UserBasicInfo est2Info = identityClient.getUserById(estudiante2Id);
                if (est2Info != null) {
                    estudiante2Nombre = est2Info.getNombreCompleto();
                    estudiante2Email = est2Info.email();
                    log.info("✅ Información del estudiante 2 obtenida: {}", estudiante2Nombre);
                }
            } catch (Exception e) {
                log.error("❌ Error al obtener información del estudiante 2: {}", estudiante2Id, e);
            }
        }

        // 14. Publicar evento a Progress Tracking con información completa
        FormatoAReenviadoEvent event = FormatoAReenviadoEvent.builder()
                .proyectoId(actualizado.getId())
                .titulo(actualizado.getTitulo())
                .version(actualizado.getNumeroIntentos())
                .descripcion("Correcciones aplicadas - versión " + actualizado.getNumeroIntentos())
                .timestamp(LocalDateTime.now())
                .usuarioResponsableId(Long.valueOf(userId))
                .usuarioResponsableNombre(userInfo != null ? userInfo.getNombreCompleto() : "DESCONOCIDO")
                .usuarioResponsableRol("DOCENTE")
                // ✨ Información completa del proyecto
                .directorId(actualizado.getDocenteDirectorId())
                .directorNombre(directorNombre)
                .codirectorId(actualizado.getDocenteCodirectorId())
                .codirectorNombre(codirectorNombre)
                .estudiante1Id(actualizado.getEstudiante1Id())
                .estudiante1Nombre(estudiante1Nombre)
                .estudiante1Email(estudiante1Email)
                .estudiante2Id(estudiante2Id)
                .estudiante2Nombre(estudiante2Nombre)
                .estudiante2Email(estudiante2Email)
                .build();

        log.info("📤 Publicando evento FormatoAReenviado para proyecto: {} versión: {}",
                actualizado.getId(), actualizado.getNumeroIntentos());
        progressEventPublisher.publicarFormatoAReenviado(event);
        log.info("✅ Evento FormatoAReenviado publicado exitosamente");

        // 11. Obtener email del coordinador y enviar notificación (RF4)
        try {
            String coordinadorEmail = identityClient.getCoordinadorEmail();
            notificationPublisher.notificarFormatoAEnviado(
                    actualizado.getId().intValue(),
                    actualizado.getTitulo(),
                    actualizado.getNumeroIntentos(), // versión 2 o 3
                    userInfo != null ? userInfo.getNombreCompleto() : "DESCONOCIDO",
                    coordinadorEmail
            );
            log.info("✉️ RF4: Notificación de reenvío (v{}) enviada al coordinador: {}",
                     actualizado.getNumeroIntentos(), coordinadorEmail);
        } catch (Exception e) {
            log.error("❌ RF4: Error al enviar notificación, pero el Formato A fue reenviado exitosamente", e);
            // No fallar la operación principal por error en notificación
        }

        // 12. Retornar respuesta
        return new IdResponse(actualizado.getId());
    }

    @Override
    public void cambiarEstadoFormatoA(Long versionId, EvaluacionRequest req) {
        log.info("📋 Cambiando estado de Formato A - ID: {}, Nuevo Estado: {}", versionId, req.getEstado());

        // Buscar el proyecto por ID (versionId es el proyectoId en este contexto)
        ProyectoSubmission proyecto = submissionRepository.findById(versionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Proyecto no encontrado con ID: " + versionId));

        log.info("🔍 Proyecto encontrado: ID={}, Estado actual={}, Intento={}/3",
                proyecto.getId(), proyecto.getEstadoNombre(), proyecto.getNumeroIntentos());

        // Validar que el proyecto está en un estado evaluable (soporta nuevos y antiguos nombres)
        String estadoActual = proyecto.getEstadoNombre();
        if (!"EN_EVALUACION_COORDINADOR".equals(estadoActual) && !"EN_EVALUACION_COMITE".equals(estadoActual)) {
            log.warn("⚠️ El proyecto no está en estado EN_EVALUACION_COORDINADOR. Estado actual: {}",
                    estadoActual);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El proyecto no está en evaluación. Estado actual: " + estadoActual);
        }

        // Evaluar según la decisión (puede venir como enum o string)
        boolean aprobado = req.getEstadoAsEnum() == co.unicauca.comunicacionmicroservicios.domain.model.enumEstadoFormato.APROBADO;
        String observaciones = req.getObservaciones() != null ? req.getObservaciones() : "";

        // Delegar al patrón State para manejar la transición
        proyecto.evaluar(aprobado, observaciones);

        // Guardar los cambios
        ProyectoSubmission actualizado = submissionRepository.save(proyecto);

        log.info("✅ Estado de Formato A {} actualizado exitosamente: {} (Intento {}/3)",
                actualizado.getId(), actualizado.getEstadoNombre(), actualizado.getNumeroIntentos());
    }

    @Override
    public IdResponse subirAnteproyecto(String userId, AnteproyectoData data, MultipartFile pdf) {
        log.info("📄 Subiendo anteproyecto - Proyecto: {}, Usuario: {}", data.getProyectoId(), userId);

        // 1. Validar PDF obligatorio
        validarArchivoPdfObligatorio(pdf);

        // 2. Buscar proyecto en ProyectoSubmission (donde está el Formato A aprobado)
        ProyectoSubmission proyectoSubmission = submissionRepository.findById(data.getProyectoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Proyecto no existe"));

        log.info("🔍 Proyecto encontrado: ID={}, Título={}, Director={}",
                proyectoSubmission.getId(), proyectoSubmission.getTitulo(), proyectoSubmission.getDocenteDirectorId());

        // 3. Validar que el usuario es el DIRECTOR del proyecto
        if (proyectoSubmission.getDocenteDirectorId() == null) {
            log.error("❌ El proyecto {} no tiene director asignado", proyectoSubmission.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El proyecto no tiene director asignado");
        }

        // Convertir userId a Long para comparar correctamente
        Long userIdLong = Long.valueOf(userId);
        if (!proyectoSubmission.getDocenteDirectorId().equals(userIdLong)) {
            log.error("❌ Usuario {} NO es el director del proyecto (director: {})", userId, proyectoSubmission.getDocenteDirectorId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el director del proyecto puede subir el anteproyecto");
        }

        log.info("✅ Validación de director exitosa: Usuario {} es el director", userId);

        // 4. Validar que el Formato A está APROBADO (soporta nuevos y antiguos nombres de estado)
        String estadoFormato = proyectoSubmission.getEstadoNombre();
        if (!"FORMATO_A_APROBADO".equals(estadoFormato) && !"ACEPTADO_POR_COMITE".equals(estadoFormato)) {
            log.error("❌ El Formato A del proyecto {} NO está aprobado (estado actual: {})",
                    proyectoSubmission.getId(), estadoFormato);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El Formato A debe estar aprobado antes de subir anteproyecto. Estado actual: " + estadoFormato);
        }

        log.info("✅ Formato A está aprobado, se puede subir anteproyecto");

        // 5. Crear o buscar ProyectoGrado (para mantener compatibilidad con el resto del código)
        // NOTA: proyectos_grado mantiene su propio ID independiente de proyecto_submissions
        // Esto es por diseño - son dos tablas diferentes con propósitos diferentes:
        //   - proyecto_submissions: gestiona el flujo de Formato A (con estados)
        //   - proyectos_grado: gestiona el anteproyecto y datos legacy
        ProyectoGrado proyecto = proyectoGradoRepository.findById(data.getProyectoId().intValue())
                .orElseGet(() -> {
                    log.info("🆕 Creando ProyectoGrado desde ProyectoSubmission (ID submission={})", proyectoSubmission.getId());
                    ProyectoGrado nuevo = new ProyectoGrado();
                    // No asignamos ID manualmente - dejamos que PostgreSQL lo genere
                    nuevo.setTitulo(proyectoSubmission.getTitulo());
                    nuevo.setModalidad(proyectoSubmission.getModalidad());
                    nuevo.setDirectorId(proyectoSubmission.getDocenteDirectorId().intValue());
                    nuevo.setCodirectorId(proyectoSubmission.getDocenteCodirectorId() != null ?
                            proyectoSubmission.getDocenteCodirectorId().intValue() : null);
                    nuevo.setEstudiante1Id(proyectoSubmission.getEstudiante1Id().intValue());
                    nuevo.setEstudiante2Id(proyectoSubmission.getEstudiante2Id() != null ?
                            proyectoSubmission.getEstudiante2Id().intValue() : null);
                    nuevo.setObjetivoGeneral(proyectoSubmission.getObjetivoGeneral());
                    nuevo.setObjetivosEspecificos(proyectoSubmission.getObjetivosEspecificos());
                    nuevo.setEstado(enumEstadoProyecto.APROBADO);
                    nuevo.setNumeroIntentos(proyectoSubmission.getNumeroIntentos());
                    ProyectoGrado guardado = proyectoGradoRepository.save(nuevo);
                    log.info("✅ ProyectoGrado creado: ID_ProyectoGrado={} basado en ProyectoSubmission ID={}",
                            guardado.getId(), proyectoSubmission.getId());
                    return guardado;
                });

        log.info("✅ ProyectoGrado preparado: ID_ProyectoGrado={} | ID_ProyectoSubmission={}",
                proyecto.getId(), proyectoSubmission.getId());

        // 6. Validar que NO existe anteproyecto previo
        Optional<Anteproyecto> existente = anteproyectoRepository.findByProyecto(proyecto);
        if (existente.isPresent()) {
            log.error("❌ Ya existe un anteproyecto para el proyecto {}", proyecto.getId());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un anteproyecto para este proyecto");
        }

        log.info("✅ No hay anteproyecto previo, se procede a crear uno nuevo");

        // 6. Guardar archivo PDF en disco
        String baseDir = "anteproyectos/" + proyecto.getId();
        String pdfPath = guardarArchivo(baseDir, "documento.pdf", pdf);
        log.info("✅ PDF guardado en: {}", pdfPath);

        // 7. Crear entidad Anteproyecto
        Anteproyecto anteproyecto = new Anteproyecto();
        anteproyecto.setProyecto(proyecto);
        anteproyecto.setRutaArchivo(pdfPath);
        anteproyecto.setNombreArchivo(pdf.getOriginalFilename() != null ?
                pdf.getOriginalFilename() : "documento.pdf");
        anteproyecto.setFechaEnvio(LocalDateTime.now());
        anteproyecto.setEstado("PENDIENTE");

        // 8. Guardar en BD
        anteproyectoRepository.save(anteproyecto);
        log.info("✅ Anteproyecto guardado en BD para proyecto: {}", proyecto.getId());

        // 9. Obtener información del usuario responsable (director)
        IdentityClient.UserBasicInfo userInfo = identityClient.getUserById(Long.valueOf(userId));
        log.info("✅ Usuario responsable obtenido: {}", userInfo != null ? userInfo.getNombreCompleto() : "DESCONOCIDO");

        // 10. Obtener información del director
        String directorNombre = "Director Desconocido";
        if (proyecto.getDirectorId() != null) {
            IdentityClient.UserBasicInfo directorInfo = identityClient.getUserById(proyecto.getDirectorId().longValue());
            directorNombre = directorInfo != null ? directorInfo.getNombreCompleto() : "Director Desconocido";
            log.info("✅ Director obtenido: {}", directorNombre);
        }

        // 11. Obtener información del codirector (si existe)
        String codirectorNombre = null;
        if (proyecto.getCodirectorId() != null) {
            IdentityClient.UserBasicInfo codirectorInfo = identityClient.getUserById(proyecto.getCodirectorId().longValue());
            codirectorNombre = codirectorInfo != null ? codirectorInfo.getNombreCompleto() : "Codirector Desconocido";
            log.info("✅ Codirector obtenido: {}", codirectorNombre);
        }

        // 12. Obtener información del estudiante 1 (y su programa)
        String estudiante1Nombre = "Estudiante Desconocido";
        String programa = "DESCONOCIDO";
        if (proyecto.getEstudiante1Id() != null) {
            IdentityClient.UserBasicInfo estudiante1Info = identityClient.getUserById(proyecto.getEstudiante1Id().longValue());
            estudiante1Nombre = estudiante1Info != null ? estudiante1Info.getNombreCompleto() : "Estudiante Desconocido";
            if (estudiante1Info != null && estudiante1Info.programa() != null) {
                programa = estudiante1Info.programa();
            }
            log.info("✅ Estudiante 1 obtenido: {} - Programa: {}", estudiante1Nombre, programa);
        }

        // 13. Obtener información del estudiante 2 (si existe)
        String estudiante2Nombre = null;
        String estudiante2Email = null;
        if (proyecto.getEstudiante2Id() != null) {
            IdentityClient.UserBasicInfo estudiante2Info = identityClient.getUserById(proyecto.getEstudiante2Id().longValue());
            if (estudiante2Info != null) {
                estudiante2Nombre = estudiante2Info.getNombreCompleto();
                estudiante2Email = estudiante2Info.email();
                log.info("✅ Estudiante 2 obtenido: {} - Email: {}", estudiante2Nombre, estudiante2Email);
            }
        }

        // 13.1. Obtener email del estudiante 1 (ya tenemos el nombre y programa)
        String estudiante1Email = null;
        if (proyecto.getEstudiante1Id() != null) {
            try {
                IdentityClient.UserBasicInfo est1Info = identityClient.getUserById(proyecto.getEstudiante1Id().longValue());
                if (est1Info != null) {
                    estudiante1Email = est1Info.email();
                    log.info("✅ Email del estudiante 1 obtenido: {}", estudiante1Email);
                }
            } catch (Exception e) {
                log.error("❌ Error al obtener email del estudiante 1: {}", proyecto.getEstudiante1Id(), e);
            }
        }

        // 14. Publicar evento a Progress Tracking con TODOS los campos incluyendo emails
        AnteproyectoEnviadoEvent event = AnteproyectoEnviadoEvent.builder()
                .proyectoId(proyecto.getId().longValue())
                .titulo(proyecto.getTitulo())
                .modalidad(proyecto.getModalidad().name())
                .programa(programa)
                .descripcion("Anteproyecto completo enviado")
                .timestamp(LocalDateTime.now())
                .usuarioResponsableId(Long.valueOf(userId))
                .usuarioResponsableNombre(userInfo != null ? userInfo.getNombreCompleto() : "DESCONOCIDO")
                .usuarioResponsableRol("DOCENTE")
                // ✨ Información completa del proyecto
                .directorId(proyecto.getDirectorId() != null ? proyecto.getDirectorId().longValue() : null)
                .directorNombre(directorNombre)
                .codirectorId(proyecto.getCodirectorId() != null ? proyecto.getCodirectorId().longValue() : null)
                .codirectorNombre(codirectorNombre)
                .estudiante1Id(proyecto.getEstudiante1Id() != null ? proyecto.getEstudiante1Id().longValue() : null)
                .estudiante1Nombre(estudiante1Nombre)
                .estudiante1Email(estudiante1Email)
                .estudiante2Id(proyecto.getEstudiante2Id() != null ? proyecto.getEstudiante2Id().longValue() : null)
                .estudiante2Nombre(estudiante2Nombre)
                .estudiante2Email(estudiante2Email)
                .build();

        log.info("📤 Publicando evento AnteproyectoEnviado para proyecto: {}", proyecto.getId());
        progressEventPublisher.publicarAnteproyectoEnviado(event);
        log.info("✅ Evento AnteproyectoEnviado publicado exitosamente");

        // 15. Obtener email del jefe de departamento y enviar notificación (RF6)
        try {
            String jefeDepartamentoEmail = identityClient.getJefeDepartamentoEmail();
            notificationPublisher.notificarAnteproyectoEnviado(
                    proyecto.getId(),
                    proyecto.getTitulo(),
                    userInfo != null ? userInfo.getNombreCompleto() : "DESCONOCIDO",
                    jefeDepartamentoEmail
            );
            log.info("✉️ RF6: Notificación enviada al jefe de departamento: {}", jefeDepartamentoEmail);
        } catch (Exception e) {
            log.error("❌ RF6: Error al enviar notificación, pero el Anteproyecto fue creado exitosamente", e);
            // No fallar la operación principal por error en notificación
        }

        // 16. Retornar respuesta con el ID del PROYECTO (no del anteproyecto)
        return new IdResponse(proyecto.getId().longValue());
    }

    /**
     * Método auxiliar para validar que el archivo PDF es obligatorio
     */
    private void validarArchivoPdfObligatorio(MultipartFile pdf) {
        if (pdf == null || pdf.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El archivo PDF es obligatorio");
        }

        String contentType = pdf.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El archivo debe ser un PDF");
        }
    }

    /**
     * Método auxiliar para guardar archivos en disco
     */
    private String guardarArchivo(String baseDir, String nombreArchivo, MultipartFile file) {
        try {
            // Crear directorio base si no existe
            Path uploadPath = Paths.get("/app/uploads", baseDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Guardar archivo
            Path filePath = uploadPath.resolve(nombreArchivo);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
            log.error("❌ Error al guardar archivo: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al guardar el archivo", e);
        }
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


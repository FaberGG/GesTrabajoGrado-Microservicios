package co.unicauca.comunicacionmicroservicios.service;

import co.unicauca.comunicacionmicroservicios.domain.model.*;
import co.unicauca.comunicacionmicroservicios.dto.*;
import co.unicauca.comunicacionmicroservicios.infraestructure.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubmissionService implements ISubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    private final IProyectoGradoRepository proyectoRepo;
    private final IFormatoARepository formatoRepo;
    private final IAnteproyectoRepository anteproyectoRepo;

    private final NotificationPublisher notificationPublisher;
    private final IdentityClient identityClient;

    @Value("${file.storage.path:/app/uploads}")
    private String storageBasePath;

    // Constructor explícito en lugar de @RequiredArgsConstructor
    public SubmissionService(IProyectoGradoRepository proyectoRepo,
                            IFormatoARepository formatoRepo,
                            IAnteproyectoRepository anteproyectoRepo,
                            NotificationPublisher notificationPublisher,
                            IdentityClient identityClient) {
        this.proyectoRepo = proyectoRepo;
        this.formatoRepo = formatoRepo;
        this.anteproyectoRepo = anteproyectoRepo;
        this.notificationPublisher = notificationPublisher;
        this.identityClient = identityClient;
    }

    // ---------------------------
    // RF2: Crear Formato A v1
    // ---------------------------
    @Override
    @Transactional
    public IdResponse crearFormatoA(String userId, FormatoAData data, MultipartFile pdf, MultipartFile carta) {
        validarArchivoPdfObligatorio(pdf);
        validarCartaSiPractica(data, carta);

        // Crear proyecto
        ProyectoGrado proyecto = new ProyectoGrado();
        proyecto.setTitulo(data.getTitulo());
        proyecto.setModalidad(data.getModalidad());
        proyecto.setDirectorId(data.getDirectorId());
        proyecto.setCodirectorId(data.getCodirectorId());
        proyecto.setObjetivoGeneral(data.getObjetivoGeneral());
        // Convertimos la lista a un texto (uno por línea)
        proyecto.setObjetivosEspecificos(String.join("\n",
                data.getObjetivosEspecificos() != null ? data.getObjetivosEspecificos() : List.of()));
        proyecto.setEstudiante1Id(data.getEstudiante1Id());
        proyecto.setEstudiante2Id(data.getEstudiante2Id());
        proyecto.setNumeroIntentos(1); // v1
        // Estado inicial: mantenemos el que tengas por defecto (EN_PROCESO); si tienes EN_EVALUACION_1, cámbialo aquí.

        proyecto = proyectoRepo.save(proyecto);

        // Guardar archivos
        String base = "formato-a/" + proyecto.getId() + "/v1";
        String pdfPath = guardarArchivo(base, "documento.pdf", pdf);
        String cartaPath = (data.getModalidad() == enumModalidad.PRACTICA_PROFESIONAL)
                ? guardarArchivo(base, "carta.pdf", carta)
                : null;

        // Crear versión Formato A v1
        FormatoA v1 = new FormatoA();
        v1.setProyecto(proyecto);
        v1.setNumeroIntento(1);
        v1.setRutaArchivo(pdfPath);
        v1.setNombreArchivo(pdf.getOriginalFilename() != null ? pdf.getOriginalFilename() : "documento.pdf");
        v1.setFechaCarga(LocalDateTime.now());
        if (cartaPath != null) {
            v1.setRutaCartaAceptacion(cartaPath);
            v1.setNombreCartaAceptacion(carta != null ? carta.getOriginalFilename() : "carta.pdf");
        }
        v1.setEstado(enumEstadoFormato.PENDIENTE);
        formatoRepo.save(v1);

        // RF2: Enviar notificación asíncrona al coordinador
        String coordinadorEmail = identityClient.getCoordinadorEmail();
        String submittedByName = identityClient.getUserName(userId);

        notificationPublisher.notificarFormatoAEnviado(
                proyecto.getId(),
                proyecto.getTitulo(),
                1, // versión 1
                submittedByName,
                coordinadorEmail
        );

        log.info("Formato A v1 creado para proyecto {} - Notificación enviada al coordinador: {}",
                proyecto.getId(), coordinadorEmail);
        return new IdResponse(proyecto.getId().longValue());
    }

    // ---------------------------
    // Lecturas de Formato A
    // ---------------------------
    @Override
    @Transactional(readOnly = true)
    public FormatoAView obtenerFormatoA(Long id) {
        FormatoA fa = formatoRepo.findById(id.intValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formato A no encontrado"));

        FormatoAView view = new FormatoAView();
        view.setId(fa.getId().longValue());
        view.setProyectoId(fa.getProyecto().getId().longValue());
        view.setVersion(fa.getNumeroIntento());
        view.setEstado(fa.getEstado());
        view.setObservaciones(fa.getObservaciones());
        view.setNombreArchivo(fa.getNombreArchivo());
        view.setPdfUrl(fa.getRutaArchivo());
        view.setCartaUrl(fa.getRutaCartaAceptacion());
        view.setFechaEnvio(fa.getFechaCarga());
        return view;
    }

    @Override
    @Transactional(readOnly = true)
    public FormatoAPage listarFormatoA(Optional<String> docenteId, int page, int size) {
        // Implementación simple (sin Pageable real para no complicar):
        List<FormatoAView> content = formatoRepo.findAll().stream()
                .filter(fa -> docenteId.isEmpty() ||
                        Objects.equals(fa.getProyecto().getDirectorId(), parseIntSafe(docenteId.get())))
                .sorted(Comparator.comparing(FormatoA::getFechaCarga).reversed())
                .limit(size)
                .map(fa -> {
                    FormatoAView v = new FormatoAView();
                    v.setId(fa.getId().longValue());
                    v.setProyectoId(fa.getProyecto().getId().longValue());
                    v.setVersion(fa.getNumeroIntento());
                    v.setEstado(fa.getEstado());
                    v.setNombreArchivo(fa.getNombreArchivo());
                    v.setPdfUrl(fa.getRutaArchivo());
                    v.setCartaUrl(fa.getRutaCartaAceptacion());
                    v.setFechaEnvio(fa.getFechaCarga());
                    return v;
                }).collect(Collectors.toList());

        FormatoAPage res = new FormatoAPage();
        res.setContent(content);
        res.setPage(page);
        res.setSize(size);
        res.setTotalElements(content.size());
        return res;
    }

    // ---------------------------
    // RF4: Reenviar Formato A
    // ---------------------------
    @Override
    @Transactional
    public IdResponse reenviarFormatoA(String userId, Long proyectoId, MultipartFile pdf, MultipartFile carta) {
        validarArchivoPdfObligatorio(pdf);

        ProyectoGrado proyecto = proyectoRepo.findById(proyectoId.intValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no existe"));

        if (Boolean.TRUE.equals(proyecto.getEstado() == enumEstadoProyecto.RECHAZADO_DEFINITIVO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Proyecto rechazado definitivamente");
        }
        // Debe haber sido rechazado para permitir reenvío (si manejas ese estado)
        if (proyecto.getEstado() != enumEstadoProyecto.RECHAZADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El proyecto no está en estado de reenvío");
        }
        if (proyecto.getNumeroIntentos() >= 3) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Se alcanzó el máximo de 3 intentos");
        }

        int next = proyecto.getNumeroIntentos() + 1;
        proyecto.setNumeroIntentos(next);
        proyectoRepo.save(proyecto);

        // Si modalidad es práctica y en la nueva versión exigen carta, la validamos:
        if (proyecto.getModalidad() == enumModalidad.PRACTICA_PROFESIONAL && (carta == null || carta.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carta de aceptación obligatoria en Práctica Profesional");
        }

        String base = "formato-a/" + proyecto.getId() + "/v" + next;
        String pdfPath = guardarArchivo(base, "documento.pdf", pdf);
        String cartaPath = (proyecto.getModalidad() == enumModalidad.PRACTICA_PROFESIONAL)
                ? guardarArchivo(base, "carta.pdf", carta)
                : null;

        FormatoA nueva = new FormatoA();
        nueva.setProyecto(proyecto);
        nueva.setNumeroIntento(next);
        nueva.setRutaArchivo(pdfPath);
        nueva.setNombreArchivo(pdf.getOriginalFilename() != null ? pdf.getOriginalFilename() : "documento.pdf");
        nueva.setFechaCarga(LocalDateTime.now());
        if (cartaPath != null) {
            nueva.setRutaCartaAceptacion(cartaPath);
            nueva.setNombreCartaAceptacion(carta.getOriginalFilename());
        }
        nueva.setEstado(enumEstadoFormato.PENDIENTE);
        formatoRepo.save(nueva);

        // RF4: Enviar notificación asíncrona al coordinador (reenvío)
        String coordinadorEmail = identityClient.getCoordinadorEmail();
        String submittedByName = identityClient.getUserName(userId);

        notificationPublisher.notificarFormatoAEnviado(
                proyecto.getId(),
                proyecto.getTitulo(),
                next, // versión 2 o 3
                submittedByName,
                coordinadorEmail
        );

        log.info("Formato A v{} reenviado para proyecto {} - Notificación enviada al coordinador: {}",
                next, proyecto.getId(), coordinadorEmail);

        return new IdResponse(proyecto.getId().longValue());
        }

    // ------------------------------------------
    // RF3: Cambiar estado de una versión (por Review/Coordinador)
    // ------------------------------------------
    @Override
    @Transactional
    public void cambiarEstadoFormatoA(Long versionId, EvaluacionRequest req) {
        FormatoA formato = formatoRepo.findById(versionId.intValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Versión de Formato A no existe"));
        ProyectoGrado proyecto = formato.getProyecto();

        if (req.getEstado() == enumEstadoFormato.APROBADO) {
            formato.aprobar(req.getEvaluadoPor(), req.getObservaciones());
            proyecto.marcarAprobado();
            proyectoRepo.save(proyecto);
            formatoRepo.save(formato);

            // Nota: Las notificaciones de evaluación completada son responsabilidad del review-service

            return;
        }

        if (req.getEstado() == enumEstadoFormato.RECHAZADO) {
            formato.rechazar(req.getEvaluadoPor(), req.getObservaciones());
            proyecto.marcarRechazado();
            proyectoRepo.save(proyecto);
            formatoRepo.save(formato);

            // Nota: Las notificaciones de evaluación completada son responsabilidad del review-service

            // Si este rechazo ocurre en el 3er intento => Rechazo definitivo
            if (Objects.equals(proyecto.getNumeroIntentos(), 3)) {
                proyecto.marcarComoRechazadoDefinitivo();
                proyectoRepo.save(proyecto);

                // Notificar a estudiantes y director sobre rechazo definitivo
                java.util.List<String> recipientEmails = new java.util.ArrayList<>();

                // Agregar emails de estudiantes
                if (proyecto.getEstudiante1Id() != null) {
                    recipientEmails.add(identityClient.getUserEmail(proyecto.getEstudiante1Id().toString()));
                }
                if (proyecto.getEstudiante2Id() != null) {
                    recipientEmails.add(identityClient.getUserEmail(proyecto.getEstudiante2Id().toString()));
                }
                // Agregar email del director
                if (proyecto.getDirectorId() != null) {
                    recipientEmails.add(identityClient.getUserEmail(proyecto.getDirectorId().toString()));
                }

                if (!recipientEmails.isEmpty()) {
                    notificationPublisher.notificarRechazoDefinitivo(
                            proyecto.getId(),
                            proyecto.getTitulo(),
                            recipientEmails
                    );
                    log.info("Notificaciones de rechazo definitivo enviadas para proyecto {}", proyecto.getId());
                }
            }
            return;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido para evaluación");
    }


    // ---------------------------
    // RF6: Subir Anteproyecto
    // ---------------------------
    @Override
    @Transactional
    public IdResponse subirAnteproyecto(String userId, AnteproyectoData data, MultipartFile pdf) {
        validarArchivoPdfObligatorio(pdf);

        ProyectoGrado proyecto = proyectoRepo.findById(data.getProyectoId().intValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no existe"));

        // Validar que el user sea el director
        if (proyecto.getDirectorId() == null || !proyecto.getDirectorId().toString().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el director del proyecto puede subir el anteproyecto");
        }

        // Validar que Formato A esté aprobado
        if (proyecto.getEstado() != enumEstadoProyecto.APROBADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Formato A no está aprobado");
        }

        // Validar que no exista anteproyecto previo
        anteproyectoRepo.findByProyecto(proyecto).ifPresent(a -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un anteproyecto para este proyecto");
        });

        String base = "anteproyectos/" + proyecto.getId();
        String pdfPath = guardarArchivo(base, "documento.pdf", pdf);

        Anteproyecto ant = new Anteproyecto();
        ant.setProyecto(proyecto);
        ant.setRutaArchivo(pdfPath);
        ant.setNombreArchivo(pdf.getOriginalFilename() != null ? pdf.getOriginalFilename() : "documento.pdf");
        ant.setFechaEnvio(LocalDateTime.now());
        anteproyectoRepo.save(ant);

        // (Opcional) Cambiar estado del proyecto si quieres marcar "ANTEPROYECTO_ENVIADO"
        // proyecto.setEstado(enumEstadoProyecto.ANTEPROYECTO_ENVIADO); // si existe en tu enum
        // proyectoRepo.save(proyecto);

        // RF6: Enviar notificación asíncrona al jefe de departamento
        String jefeDepartamentoEmail = identityClient.getJefeDepartamentoEmail();
        String submittedByName = identityClient.getUserName(userId);

        notificationPublisher.notificarAnteproyectoEnviado(
                proyecto.getId(),
                proyecto.getTitulo(),
                submittedByName,
                jefeDepartamentoEmail
        );

        log.info("Anteproyecto enviado para proyecto {} - Notificación enviada al jefe de departamento: {}",
                proyecto.getId(), jefeDepartamentoEmail);


        return new IdResponse(ant.getId().longValue());
    }

    @Override
    @Transactional(readOnly = true)
    public AnteproyectoPage listarAnteproyectos(int page, int size) {
        var content = anteproyectoRepo.findAll().stream()
                .sorted(Comparator.comparing(Anteproyecto::getFechaEnvio).reversed())
                .limit(size)
                .map(a -> {
                    AnteproyectoView v = new AnteproyectoView();
                    v.setId(a.getId().longValue());
                    v.setProyectoId(a.getProyecto().getId().longValue());
                    v.setPdfUrl(a.getRutaArchivo());
                    v.setFechaEnvio(a.getFechaEnvio());
                    v.setEstado(a.getEstado());
                    return v;
                }).collect(Collectors.toList());

        AnteproyectoPage pageRes = new AnteproyectoPage();
        pageRes.setContent(content);
        pageRes.setPage(page);
        pageRes.setSize(size);
        pageRes.setTotalElements(content.size());
        return pageRes;
    }

    @Override
    @Transactional
    public void cambiarEstadoAnteproyecto(Long id, CambioEstadoAnteproyectoRequest req) {
        Anteproyecto ant = anteproyectoRepo.findById(id.intValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anteproyecto no existe"));
        ant.setEstado(req.getEstado());
        // si quieres guardar observaciones, añade un campo en la entidad
        anteproyectoRepo.save(ant);
    }

    // ---------------------------
    // Utilidades
    // ---------------------------
    private void validarArchivoPdfObligatorio(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo PDF obligatorio");
        }
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            // No todos los clientes mandan el content-type correcto, así que esto es laxo
            if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser PDF");
            }
        }
    }

    private void validarCartaSiPractica(FormatoAData data, MultipartFile carta) {
        if (data.getModalidad() == enumModalidad.PRACTICA_PROFESIONAL) {
            if (carta == null || carta.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Carta de aceptación de la empresa es obligatoria en Práctica Profesional");
            }
        }
    }

    private String guardarArchivo(String relativeDir, String fileName, MultipartFile file) {
        try {
            Path base = Paths.get(storageBasePath);
            Path dir = base.resolve(relativeDir).normalize();
            Files.createDirectories(dir);
            Path full = dir.resolve(fileName).normalize();
            Files.write(full, file.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            // Retornamos ruta absoluta en disco (o relativa si prefieres)
            return full.toString();
        } catch (IOException e) {
            log.error("Error guardando archivo", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar el archivo");
        }
    }

    private Integer parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }
}

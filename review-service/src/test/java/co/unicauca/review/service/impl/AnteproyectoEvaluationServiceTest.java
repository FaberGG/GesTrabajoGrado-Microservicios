package co.unicauca.review.service.impl;

import co.unicauca.review.client.SubmissionServiceClient;
import co.unicauca.review.dto.request.EvaluationRequestDTO;
import co.unicauca.review.dto.response.EvaluationResultDTO;
import co.unicauca.review.entity.AsignacionEvaluadores;
import co.unicauca.review.entity.Evaluation;
import co.unicauca.review.enums.AsignacionEstado;
import co.unicauca.review.enums.Decision;
import co.unicauca.review.enums.EvaluatorRole;
import co.unicauca.review.exception.InvalidStateException;
import co.unicauca.review.repository.AsignacionEvaluadoresRepository;
import co.unicauca.review.repository.EvaluationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import co.unicauca.review.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Anteproyecto Evaluation Service Tests - Template Method with 2 Evaluators")
class AnteproyectoEvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private SubmissionServiceClient submissionClient;

    @Mock
    private AsignacionEvaluadoresRepository asignacionRepository;

    @InjectMocks
    private AnteproyectoEvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(evaluationService, "exchange", "evaluation.exchange");
        ReflectionTestUtils.setField(evaluationService, "routingKey", "evaluation.completed");
    }

    @Test
    @DisplayName("First evaluator should NOT trigger notification")
    void firstEvaluatorShouldNotTriggerNotification() {
        // Given
        Long anteproyectoId = 1L;
        Long evaluador1Id = 15L;

        EvaluationRequestDTO request = new EvaluationRequestDTO(
            anteproyectoId,
            Decision.APROBADO,
            "Metodología sólida",
            evaluador1Id,
            EvaluatorRole.EVALUADOR
        );

        SubmissionServiceClient.AnteproyectoDTO anteproyectoDTO = new SubmissionServiceClient.AnteproyectoDTO();
        anteproyectoDTO.setId(anteproyectoId);
        anteproyectoDTO.setTitulo("Sistema ML");
        anteproyectoDTO.setEstado("EN_REVISION");
        anteproyectoDTO.setDocenteDirectorNombre("Director");
        anteproyectoDTO.setDocenteDirectorEmail("director@test.com");
        anteproyectoDTO.setEstudiantesEmails(List.of("estudiante@test.com"));

        AsignacionEvaluadores asignacion = new AsignacionEvaluadores();
        asignacion.setId(1L);
        asignacion.setAnteproyectoId(anteproyectoId);
        asignacion.setEvaluador1Id(evaluador1Id);
        asignacion.setEvaluador2Id(20L);
        asignacion.setEstado(AsignacionEstado.PENDIENTE);
        asignacion.setFechaAsignacion(LocalDateTime.now());
        // No decisions yet

        Evaluation savedEval = new Evaluation();
        savedEval.setId(100L);
        savedEval.setDocumentId(anteproyectoId);
        savedEval.setDecision(Decision.APROBADO);
        savedEval.setObservaciones("Metodología sólida");

        when(submissionClient.getAnteproyecto(anteproyectoId)).thenReturn(anteproyectoDTO);
        when(asignacionRepository.findByAnteproyectoId(anteproyectoId))
            .thenReturn(Optional.of(asignacion));
        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(savedEval);
        when(asignacionRepository.save(any(AsignacionEvaluadores.class))).thenReturn(asignacion);

        // Mock SecurityUtil
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(evaluador1Id);

            // When
            EvaluationResultDTO result = evaluationService.evaluate(request);

            // Then
            assertNotNull(result);
            assertFalse(result.notificacionEnviada(), "First evaluator should NOT trigger notification");

            verify(submissionClient, never()).updateAnteproyectoEstado(anyLong(), anyMap());
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }
    }

    @Test
    @DisplayName("Second evaluator should trigger notification and update submission")
    void secondEvaluatorShouldTriggerNotificationAndUpdate() {
        // Given
        Long anteproyectoId = 1L;
        Long evaluador2Id = 20L;

        EvaluationRequestDTO request = new EvaluationRequestDTO(
            anteproyectoId,
            Decision.APROBADO,
            "Excelente propuesta",
            evaluador2Id,
            EvaluatorRole.EVALUADOR
        );

        SubmissionServiceClient.AnteproyectoDTO anteproyectoDTO = new SubmissionServiceClient.AnteproyectoDTO();
        anteproyectoDTO.setId(anteproyectoId);
        anteproyectoDTO.setTitulo("Sistema ML");
        anteproyectoDTO.setEstado("EN_REVISION");
        anteproyectoDTO.setDocenteDirectorNombre("Director");
        anteproyectoDTO.setDocenteDirectorEmail("director@test.com");
        anteproyectoDTO.setEstudiantesEmails(List.of("estudiante@test.com"));

        AsignacionEvaluadores asignacion = new AsignacionEvaluadores();
        asignacion.setId(1L);
        asignacion.setAnteproyectoId(anteproyectoId);
        asignacion.setEvaluador1Id(15L);
        asignacion.setEvaluador2Id(evaluador2Id);
        asignacion.setEvaluador1Decision(Decision.APROBADO);  // First evaluator already evaluated
        asignacion.setEvaluador1Observaciones("Metodología sólida");
        asignacion.setEstado(AsignacionEstado.EN_EVALUACION);
        asignacion.setFechaAsignacion(LocalDateTime.now());

        Evaluation savedEval = new Evaluation();
        savedEval.setId(101L);
        savedEval.setDocumentId(anteproyectoId);
        savedEval.setDecision(Decision.APROBADO);
        savedEval.setObservaciones("Excelente propuesta");

        when(submissionClient.getAnteproyecto(anteproyectoId)).thenReturn(anteproyectoDTO);
        when(asignacionRepository.findByAnteproyectoId(anteproyectoId))
            .thenReturn(Optional.of(asignacion));
        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(savedEval);
        when(asignacionRepository.save(any(AsignacionEvaluadores.class))).thenReturn(asignacion);
        doNothing().when(submissionClient).updateAnteproyectoEstado(anyLong(), anyMap());
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // Mock SecurityUtil
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(evaluador2Id);

            // When
            EvaluationResultDTO result = evaluationService.evaluate(request);

            // Then
            assertNotNull(result);
            assertTrue(result.notificacionEnviada(), "Second evaluator SHOULD trigger notification");

            verify(submissionClient, times(1)).updateAnteproyectoEstado(eq(anteproyectoId), anyMap());
            verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
            verify(asignacionRepository, times(1)).save(argThat(asig ->
                asig.getEstado() == AsignacionEstado.COMPLETADA &&
                asig.getFechaCompletado() != null
            ));
        }
    }

    @Test
    @DisplayName("Should calculate RECHAZADO when at least one evaluator rejects")
    void shouldCalculateRechazadoWhenAtLeastOneRejects() {
        // Given
        Long anteproyectoId = 1L;
        Long evaluador2Id = 20L;

        EvaluationRequestDTO request = new EvaluationRequestDTO(
            anteproyectoId,
            Decision.RECHAZADO,
            "Faltan objetivos claros",
            evaluador2Id,
            EvaluatorRole.EVALUADOR
        );

        SubmissionServiceClient.AnteproyectoDTO anteproyectoDTO = new SubmissionServiceClient.AnteproyectoDTO();
        anteproyectoDTO.setId(anteproyectoId);
        anteproyectoDTO.setTitulo("Test");
        anteproyectoDTO.setEstado("EN_REVISION");

        AsignacionEvaluadores asignacion = new AsignacionEvaluadores();
        asignacion.setAnteproyectoId(anteproyectoId);
        asignacion.setEvaluador1Id(15L);
        asignacion.setEvaluador2Id(evaluador2Id);
        asignacion.setEvaluador1Decision(Decision.APROBADO);  // First approved
        asignacion.setEstado(AsignacionEstado.EN_EVALUACION);

        when(submissionClient.getAnteproyecto(anteproyectoId)).thenReturn(anteproyectoDTO);
        when(asignacionRepository.findByAnteproyectoId(anteproyectoId))
            .thenReturn(Optional.of(asignacion));
        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(new Evaluation());
        when(asignacionRepository.save(any(AsignacionEvaluadores.class))).thenReturn(asignacion);

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(evaluador2Id);

            // When
            evaluationService.evaluate(request);

            // Then
            verify(submissionClient).updateAnteproyectoEstado(eq(anteproyectoId), argThat(body ->
                body.get("estado").equals("RECHAZADO")
            ));
        }
    }

    @Test
    @DisplayName("Should throw InvalidStateException when evaluator already evaluated")
    void shouldThrowInvalidStateExceptionWhenAlreadyEvaluated() {
        // Given
        Long anteproyectoId = 1L;
        Long evaluador1Id = 15L;

        EvaluationRequestDTO request = new EvaluationRequestDTO(
            anteproyectoId,
            Decision.APROBADO,
            "Test",
            evaluador1Id,
            EvaluatorRole.EVALUADOR
        );

        SubmissionServiceClient.AnteproyectoDTO anteproyectoDTO = new SubmissionServiceClient.AnteproyectoDTO();
        anteproyectoDTO.setId(anteproyectoId);

        AsignacionEvaluadores asignacion = new AsignacionEvaluadores();
        asignacion.setAnteproyectoId(anteproyectoId);
        asignacion.setEvaluador1Id(evaluador1Id);
        asignacion.setEvaluador2Id(20L);
        asignacion.setEvaluador1Decision(Decision.APROBADO);  // Already evaluated!

        when(submissionClient.getAnteproyecto(anteproyectoId)).thenReturn(anteproyectoDTO);
        when(asignacionRepository.findByAnteproyectoId(anteproyectoId))
            .thenReturn(Optional.of(asignacion));

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(evaluador1Id);

            // When & Then
            Exception exception = assertThrows(Exception.class, () -> {
                evaluationService.evaluate(request);
            });

            assertTrue(exception.getMessage().contains("ya registró su evaluación"));
        }
    }

    @Test
    @DisplayName("Should throw InvalidStateException when no assignment exists")
    void shouldThrowInvalidStateExceptionWhenNoAssignment() {
        // Given
        Long anteproyectoId = 1L;

        EvaluationRequestDTO request = new EvaluationRequestDTO(
            anteproyectoId,
            Decision.APROBADO,
            "Test",
            15L,
            EvaluatorRole.EVALUADOR
        );

        SubmissionServiceClient.AnteproyectoDTO anteproyectoDTO = new SubmissionServiceClient.AnteproyectoDTO();
        anteproyectoDTO.setId(anteproyectoId);

        when(submissionClient.getAnteproyecto(anteproyectoId)).thenReturn(anteproyectoDTO);
        when(asignacionRepository.findByAnteproyectoId(anteproyectoId))
            .thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            evaluationService.evaluate(request);
        });

        assertTrue(exception.getMessage().contains("no tiene evaluadores asignados"));
    }
}

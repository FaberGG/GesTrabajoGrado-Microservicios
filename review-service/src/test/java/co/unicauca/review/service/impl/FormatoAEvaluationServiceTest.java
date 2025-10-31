package co.unicauca.review.service.impl;

import co.unicauca.review.client.SubmissionServiceClient;
import co.unicauca.review.dto.request.EvaluationRequestDTO;
import co.unicauca.review.dto.response.EvaluationResultDTO;
import co.unicauca.review.entity.Evaluation;
import co.unicauca.review.enums.Decision;
import co.unicauca.review.enums.EvaluatorRole;
import co.unicauca.review.exception.InvalidStateException;
import co.unicauca.review.repository.EvaluationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FormatoA Evaluation Service Tests")
class FormatoAEvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private SubmissionServiceClient submissionClient;

    @InjectMocks
    private FormatoAEvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(evaluationService, "exchange", "evaluation.exchange");
        ReflectionTestUtils.setField(evaluationService, "routingKey", "evaluation.completed");
    }

    @Test
    @DisplayName("Should evaluate FormatoA successfully when state is EN_REVISION")
    void shouldEvaluateFormatoASuccessfully() {
        // Given
        Long formatoAId = 1L;
        EvaluationRequestDTO request = new EvaluationRequestDTO(
            formatoAId,
            Decision.APROBADO,
            "Cumple todos los requisitos",
            5L,
            EvaluatorRole.COORDINADOR
        );

        SubmissionServiceClient.FormatoADTO formatoADTO = new SubmissionServiceClient.FormatoADTO();
        formatoADTO.setId(formatoAId);
        formatoADTO.setTitulo("Sistema de IA");
        formatoADTO.setEstado("EN_REVISION");
        formatoADTO.setDocenteDirectorNombre("Dr. Juan Pérez");
        formatoADTO.setDocenteDirectorEmail("juan@unicauca.edu.co");
        formatoADTO.setEstudiantesEmails(List.of("estudiante@unicauca.edu.co"));

        Evaluation savedEval = new Evaluation();
        savedEval.setId(100L);
        savedEval.setDocumentId(formatoAId);
        savedEval.setDecision(Decision.APROBADO);

        when(submissionClient.getFormatoA(formatoAId)).thenReturn(formatoADTO);
        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(savedEval);
        doNothing().when(submissionClient).updateFormatoAEstado(anyLong(), anyMap());
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // When
        EvaluationResultDTO result = evaluationService.evaluate(request);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.evaluationId());
        assertEquals(formatoAId, result.documentId());
        assertEquals(Decision.APROBADO, result.decision());
        assertTrue(result.notificacionEnviada());

        verify(submissionClient).getFormatoA(formatoAId);
        verify(submissionClient).updateFormatoAEstado(eq(formatoAId), anyMap());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should throw InvalidStateException when FormatoA is not EN_REVISION")
    void shouldThrowInvalidStateExceptionWhenNotEnRevision() {
        // Given
        Long formatoAId = 1L;
        EvaluationRequestDTO request = new EvaluationRequestDTO(
            formatoAId,
            Decision.APROBADO,
            "Test",
            5L,
            EvaluatorRole.COORDINADOR
        );

        SubmissionServiceClient.FormatoADTO formatoADTO = new SubmissionServiceClient.FormatoADTO();
        formatoADTO.setId(formatoAId);
        formatoADTO.setEstado("BORRADOR");  // Wrong state

        when(submissionClient.getFormatoA(formatoAId)).thenReturn(formatoADTO);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            evaluationService.evaluate(request);
        });

        assertTrue(exception.getMessage().contains("EN_REVISION"));
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should publish notification event immediately after evaluation")
    void shouldPublishNotificationEventImmediately() {
        // Given
        Long formatoAId = 1L;
        EvaluationRequestDTO request = new EvaluationRequestDTO(
            formatoAId,
            Decision.RECHAZADO,
            "Documento incompleto",
            5L,
            EvaluatorRole.COORDINADOR
        );

        SubmissionServiceClient.FormatoADTO formatoADTO = new SubmissionServiceClient.FormatoADTO();
        formatoADTO.setId(formatoAId);
        formatoADTO.setTitulo("Test");
        formatoADTO.setEstado("EN_REVISION");
        formatoADTO.setDocenteDirectorNombre("Director");
        formatoADTO.setDocenteDirectorEmail("director@test.com");
        formatoADTO.setEstudiantesEmails(List.of("estudiante@test.com"));

        Evaluation savedEval = new Evaluation();
        savedEval.setId(100L);
        savedEval.setDocumentId(formatoAId);
        savedEval.setDecision(Decision.RECHAZADO);
        savedEval.setObservaciones("Documento incompleto");

        when(submissionClient.getFormatoA(formatoAId)).thenReturn(formatoADTO);
        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(savedEval);

        // When
        evaluationService.evaluate(request);

        // Then - notification should be published
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq("evaluation.exchange"),
            eq("evaluation.completed"),
            any(Object.class)
        );
    }
}

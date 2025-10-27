package co.unicauca.review.service;

import co.unicauca.review.dto.request.EvaluationRequestDTO;
import co.unicauca.review.dto.response.EvaluationResultDTO;
import co.unicauca.review.entity.Evaluation;
import co.unicauca.review.enums.Decision;
import co.unicauca.review.enums.DocumentType;
import co.unicauca.review.enums.EvaluatorRole;
import co.unicauca.review.exception.UnauthorizedException;
import co.unicauca.review.repository.EvaluationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Template Method Pattern Tests")
class EvaluationTemplateTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private WebClient.Builder webClientBuilder;

    private TestEvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        evaluationService = new TestEvaluationService();
        evaluationService.evaluationRepository = evaluationRepository;
        evaluationService.rabbitTemplate = rabbitTemplate;
        evaluationService.webClientBuilder = webClientBuilder;
        evaluationService.exchange = "test.exchange";
        evaluationService.routingKey = "test.routing.key";
    }

    @Test
    @DisplayName("Should execute template method in correct order")
    void shouldExecuteTemplateMethodInCorrectOrder() {
        // Given
        EvaluationRequestDTO request = new EvaluationRequestDTO(
            1L,
            Decision.APROBADO,
            "Test observaciones",
            100L,
            EvaluatorRole.COORDINADOR
        );

        Evaluation savedEval = new Evaluation();
        savedEval.setId(1L);
        savedEval.setDocumentId(1L);
        savedEval.setDecision(Decision.APROBADO);
        savedEval.setDocumentType(DocumentType.FORMATO_A);

        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(savedEval);

        // When
        EvaluationResultDTO result = evaluationService.evaluate(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.evaluationId());
        assertEquals(1L, result.documentId());
        assertEquals(Decision.APROBADO, result.decision());
        assertEquals(DocumentType.FORMATO_A, result.documentType());

        // Verify all steps were executed
        assertTrue(evaluationService.validatePermissionsCalled);
        assertTrue(evaluationService.fetchDocumentCalled);
        assertTrue(evaluationService.validateStateCalled);
        assertTrue(evaluationService.updateSubmissionCalled);
        assertTrue(evaluationService.publishEventCalled);

        verify(evaluationRepository, times(1)).save(any(Evaluation.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when role is invalid")
    void shouldThrowUnauthorizedExceptionWhenRoleIsInvalid() {
        // Given
        EvaluationRequestDTO request = new EvaluationRequestDTO(
            1L,
            Decision.APROBADO,
            "Test observaciones",
            100L,
            EvaluatorRole.EVALUADOR  // Wrong role - expected COORDINADOR
        );

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            evaluationService.evaluate(request);
        });

        // Verify no further steps were executed
        assertFalse(evaluationService.fetchDocumentCalled);
        assertFalse(evaluationService.validateStateCalled);
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should save evaluation with correct data")
    void shouldSaveEvaluationWithCorrectData() {
        // Given
        EvaluationRequestDTO request = new EvaluationRequestDTO(
            1L,
            Decision.RECHAZADO,
            "Documento incompleto",
            100L,
            EvaluatorRole.COORDINADOR
        );

        Evaluation savedEval = new Evaluation();
        savedEval.setId(1L);

        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> {
            Evaluation eval = invocation.getArgument(0);
            assertEquals(1L, eval.getDocumentId());
            assertEquals(Decision.RECHAZADO, eval.getDecision());
            assertEquals("Documento incompleto", eval.getObservaciones());
            assertEquals(100L, eval.getEvaluatorId());
            assertEquals(EvaluatorRole.COORDINADOR, eval.getEvaluatorRole());
            assertEquals(DocumentType.FORMATO_A, eval.getDocumentType());
            assertNotNull(eval.getFechaEvaluacion());
            return savedEval;
        });

        // When
        evaluationService.evaluate(request);

        // Then
        verify(evaluationRepository, times(1)).save(any(Evaluation.class));
    }

    @Test
    @DisplayName("Should handle notification failure gracefully")
    void shouldHandleNotificationFailureGracefully() {
        // Given
        EvaluationRequestDTO request = new EvaluationRequestDTO(
            1L,
            Decision.APROBADO,
            "Test",
            100L,
            EvaluatorRole.COORDINADOR
        );

        Evaluation savedEval = new Evaluation();
        savedEval.setId(1L);
        savedEval.setDocumentId(1L);
        savedEval.setDecision(Decision.APROBADO);
        savedEval.setDocumentType(DocumentType.FORMATO_A);

        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(savedEval);

        // Simulate notification failure
        evaluationService.shouldFailNotification = true;

        // When
        EvaluationResultDTO result = evaluationService.evaluate(request);

        // Then - evaluation should still complete
        assertNotNull(result);
        assertEquals(1L, result.evaluationId());
        assertFalse(result.notificacionEnviada()); // Notification failed
    }

    @Test
    @DisplayName("Template method should be final and not overridable")
    void templateMethodShouldBeFinal() throws NoSuchMethodException {
        // Given
        Class<?> clazz = EvaluationTemplate.class;

        // When
        java.lang.reflect.Method evaluateMethod =
            clazz.getDeclaredMethod("evaluate", EvaluationRequestDTO.class);

        // Then
        assertTrue(java.lang.reflect.Modifier.isFinal(evaluateMethod.getModifiers()),
            "Template method 'evaluate' should be final to prevent override");
    }

    // Test implementation of EvaluationTemplate for testing purposes
    private static class TestEvaluationService extends EvaluationTemplate {
        boolean validatePermissionsCalled = false;
        boolean fetchDocumentCalled = false;
        boolean validateStateCalled = false;
        boolean updateSubmissionCalled = false;
        boolean publishEventCalled = false;
        boolean shouldFailNotification = false;

        @Override
        protected void validatePermissions(EvaluationRequestDTO request) {
            validatePermissionsCalled = true;
            super.validatePermissions(request);
        }

        @Override
        protected DocumentInfo fetchDocument(Long documentId) {
            fetchDocumentCalled = true;
            DocumentInfo doc = new DocumentInfo();
            doc.setId(documentId);
            doc.setTitulo("Test Document");
            doc.setEstado("EN_REVISION");
            doc.setDocenteDirectorName("Test Director");
            doc.setDocenteDirectorEmail("director@test.com");
            doc.setAutoresEmails(java.util.List.of("autor1@test.com"));
            return doc;
        }

        @Override
        protected void validateDocumentState(DocumentInfo document) {
            validateStateCalled = true;
            // No validation needed for test
        }

        @Override
        protected void updateSubmissionService(Long docId, Decision decision, String obs) {
            updateSubmissionCalled = true;
            // Mock implementation - no actual HTTP call
        }

        @Override
        protected boolean publishNotificationEvent(Evaluation eval, DocumentInfo doc) {
            publishEventCalled = true;
            return !shouldFailNotification;
        }

        @Override
        protected DocumentType getDocumentType() {
            return DocumentType.FORMATO_A;
        }

        @Override
        protected EvaluatorRole getRequiredRole() {
            return EvaluatorRole.COORDINADOR;
        }
    }
}


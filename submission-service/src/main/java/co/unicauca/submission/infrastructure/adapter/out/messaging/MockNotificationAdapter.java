package co.unicauca.submission.infrastructure.adapter.out.messaging;

import co.unicauca.submission.application.port.out.INotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adaptador MOCK para notificaciones - Solo para desarrollo local.
 */
@Component
@Profile("local")
public class MockNotificationAdapter implements INotificationPort {

    private static final Logger log = LoggerFactory.getLogger(MockNotificationAdapter.class);

    @Override
    public void enviarNotificacion(String destinatario, String asunto, String mensaje, Map<String, Object> datos) {
        log.info("📧 [MOCK] Email a: {} - Asunto: {}", destinatario, asunto);
    }

    @Override
    public void notificarCoordinadorFormatoAEnviado(Long proyectoId, int version) {
        log.info("📧 [MOCK] Notificar coordinador - Proyecto: {}, Version: {}", proyectoId, version);
    }

    @Override
    public void notificarJefeDepartamentoAnteproyecto(Long proyectoId) {
        log.info("📧 [MOCK] Notificar jefe departamento - Proyecto: {}", proyectoId);
    }

    @Override
    public void notificarEvaluadoresAsignados(Long proyectoId, Long evaluador1Id, Long evaluador2Id) {
        log.info("📧 [MOCK] Notificar evaluadores - Proyecto: {}, Eval1: {}, Eval2: {}",
                 proyectoId, evaluador1Id, evaluador2Id);
    }
}

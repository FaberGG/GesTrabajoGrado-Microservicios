-- ==================================================
-- PROGRESS TRACKING SERVICE - SCHEMA INICIAL
-- ==================================================
-- Este script crea las tablas para el Read Model CQRS
-- del progress-tracking-service

-- ==================================================
-- TABLA: historial_eventos (Event Store)
-- ==================================================
-- Almacena todos los eventos de dominio de forma inmutable
-- Garantiza trazabilidad completa del proyecto

CREATE TABLE historial_eventos (
    evento_id BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT NOT NULL,
    tipo_evento VARCHAR(100) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion TEXT,

    -- Información específica del evento
    version INTEGER,
    resultado VARCHAR(50),
    observaciones TEXT,

    -- Usuario responsable del evento
    usuario_responsable_id BIGINT,
    usuario_responsable_nombre VARCHAR(200),
    usuario_responsable_rol VARCHAR(50),

    -- Metadata adicional en formato JSON
    metadata TEXT,

    -- Índices para consultas rápidas
    CONSTRAINT chk_tipo_evento CHECK (tipo_evento IN (
        'FORMATO_A_ENVIADO',
        'FORMATO_A_REENVIADO',
        'FORMATO_A_EVALUADO',
        'ANTEPROYECTO_ENVIADO',
        'ANTEPROYECTO_EVALUADO',
        'EVALUADORES_ASIGNADOS',
        'PROYECTO_RECHAZADO_DEFINITIVO'
    ))
);

-- Índices para optimizar consultas
CREATE INDEX idx_historial_proyecto ON historial_eventos(proyecto_id);
CREATE INDEX idx_historial_fecha ON historial_eventos(fecha DESC);
CREATE INDEX idx_historial_tipo_evento ON historial_eventos(tipo_evento);
CREATE INDEX idx_historial_proyecto_fecha ON historial_eventos(proyecto_id, fecha DESC);

-- Comentario descriptivo
COMMENT ON TABLE historial_eventos IS 'Event Store inmutable - Historial completo de eventos de proyectos';

-- ==================================================
-- TABLA: proyecto_estado (Vista Materializada)
-- ==================================================
-- Vista optimizada del estado actual de cada proyecto
-- Permite consultas rápidas sin reconstruir desde eventos

CREATE TABLE proyecto_estado (
    proyecto_id BIGINT PRIMARY KEY,

    -- Información básica
    titulo VARCHAR(500),
    modalidad VARCHAR(50),
    programa VARCHAR(100),

    -- Estado y fase actuales
    estado_actual VARCHAR(100) NOT NULL,
    fase VARCHAR(50),

    -- ========== FORMATO A ==========
    formato_a_version INTEGER DEFAULT 0,
    formato_a_intento_actual INTEGER DEFAULT 0,
    formato_a_max_intentos INTEGER DEFAULT 3,
    formato_a_estado VARCHAR(50),
    formato_a_fecha_ultimo_envio TIMESTAMP,
    formato_a_fecha_ultima_evaluacion TIMESTAMP,

    -- ========== ANTEPROYECTO ==========
    anteproyecto_estado VARCHAR(50),
    anteproyecto_fecha_envio TIMESTAMP,
    anteproyecto_evaluadores_asignados BOOLEAN DEFAULT FALSE,

    -- ========== PARTICIPANTES ==========
    director_id BIGINT,
    director_nombre VARCHAR(200),
    codirector_id BIGINT,
    codirector_nombre VARCHAR(200),

    -- ========== AUDITORÍA ==========
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimizar consultas
CREATE INDEX idx_proyecto_estado_actual ON proyecto_estado(estado_actual);
CREATE INDEX idx_proyecto_fase ON proyecto_estado(fase);
CREATE INDEX idx_proyecto_director ON proyecto_estado(director_id);
CREATE INDEX idx_proyecto_ultima_actualizacion ON proyecto_estado(ultima_actualizacion DESC);

-- Comentario descriptivo
COMMENT ON TABLE proyecto_estado IS 'Vista materializada CQRS - Estado actual optimizado para consultas';

-- ==================================================
-- RESTRICCIONES Y VALIDACIONES
-- ==================================================

-- Estados válidos de Formato A
ALTER TABLE proyecto_estado ADD CONSTRAINT chk_formato_a_estado
    CHECK (formato_a_estado IS NULL OR formato_a_estado IN (
        'EN_EVALUACION', 'APROBADO', 'RECHAZADO', 'RECHAZADO_DEFINITIVO'
    ));

-- Estados válidos de Anteproyecto
ALTER TABLE proyecto_estado ADD CONSTRAINT chk_anteproyecto_estado
    CHECK (anteproyecto_estado IS NULL OR anteproyecto_estado IN (
        'ENVIADO', 'EN_EVALUACION', 'APROBADO', 'RECHAZADO'
    ));

-- Fase del proyecto
ALTER TABLE proyecto_estado ADD CONSTRAINT chk_fase
    CHECK (fase IS NULL OR fase IN (
        'INICIAL', 'FORMATO_A', 'ANTEPROYECTO', 'DEFENSA', 'FINALIZADO'
    ));

-- Validar versiones de Formato A
ALTER TABLE proyecto_estado ADD CONSTRAINT chk_formato_a_version
    CHECK (formato_a_version >= 0 AND formato_a_version <= 3);

ALTER TABLE proyecto_estado ADD CONSTRAINT chk_formato_a_intento
    CHECK (formato_a_intento_actual >= 0 AND formato_a_intento_actual <= formato_a_max_intentos);

-- ==================================================
-- DATOS DE EJEMPLO (OPCIONAL - COMENTAR EN PRODUCCIÓN)
-- ==================================================

-- Proyecto 1: En primera evaluación
-- INSERT INTO proyecto_estado (
--     proyecto_id, titulo, estado_actual, fase,
--     formato_a_version, formato_a_intento_actual, formato_a_estado,
--     formato_a_fecha_ultimo_envio, director_id, director_nombre
-- ) VALUES (
--     1, 'Sistema de gestión de proyectos con IA',
--     'EN_PRIMERA_EVALUACION_FORMATO_A', 'FORMATO_A',
--     1, 1, 'EN_EVALUACION',
--     CURRENT_TIMESTAMP, 101, 'Dr. Juan Pérez'
-- );

-- INSERT INTO historial_eventos (
--     proyecto_id, tipo_evento, fecha, descripcion, version
-- ) VALUES (
--     1, 'FORMATO_A_ENVIADO', CURRENT_TIMESTAMP,
--     'Primera versión del Formato A enviada', 1
-- );

-- ==================================================
-- FIN DEL SCRIPT
-- ==================================================


-- =====================================================
-- SCRIPT DE MIGRACIÓN: Renombrar estados de Formato A
-- Submission Service - Compatibilidad RF-3
-- =====================================================
-- Este script actualiza los nombres de estados en la BD
-- de los nombres legacy (COMITE) a los nuevos (COORDINADOR)
-- =====================================================

-- 1. Actualizar EN_EVALUACION_COMITE -> EN_EVALUACION_COORDINADOR
UPDATE proyecto_submissions
SET estado_nombre = 'EN_EVALUACION_COORDINADOR'
WHERE estado_nombre = 'EN_EVALUACION_COMITE';

-- 2. Actualizar CORRECCIONES_COMITE -> CORRECCIONES_SOLICITADAS
UPDATE proyecto_submissions
SET estado_nombre = 'CORRECCIONES_SOLICITADAS'
WHERE estado_nombre = 'CORRECCIONES_COMITE';

-- 3. Actualizar ACEPTADO_POR_COMITE -> FORMATO_A_APROBADO
UPDATE proyecto_submissions
SET estado_nombre = 'FORMATO_A_APROBADO'
WHERE estado_nombre = 'ACEPTADO_POR_COMITE';

-- 4. Actualizar RECHAZADO_POR_COMITE -> FORMATO_A_RECHAZADO
UPDATE proyecto_submissions
SET estado_nombre = 'FORMATO_A_RECHAZADO'
WHERE estado_nombre = 'RECHAZADO_POR_COMITE';

-- =====================================================
-- VERIFICACIÓN (ejecutar después de la migración)
-- =====================================================
-- SELECT estado_nombre, COUNT(*) as cantidad
-- FROM proyecto_submissions
-- GROUP BY estado_nombre;
-- =====================================================


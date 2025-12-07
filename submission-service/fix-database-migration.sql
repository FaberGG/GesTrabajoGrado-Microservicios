-- ================================================================
-- FIX: Migración de base de datos submission-service
-- ================================================================
-- Este script corrige el problema de columnas NOT NULL con valores NULL
-- Ejecutar si quieres PRESERVAR los datos existentes
-- ================================================================

-- Opción A: Establecer valores por defecto en registros existentes
-- Esto asigna un ID temporal (0) a los registros que no tienen estudiante
UPDATE proyecto_submissions
SET estudiante1id = 0
WHERE estudiante1id IS NULL;

-- Si también tienes problemas con otras columnas, descomenta estas líneas:
-- UPDATE proyecto_submissions SET estudiante2id = NULL WHERE estudiante2id IS NULL;
-- UPDATE proyecto_submissions SET docentedirectorid = 0 WHERE docentedirectorid IS NULL;
-- UPDATE proyecto_submissions SET docentecodirectorid = NULL WHERE docentecodirectorid IS NULL;

-- Opción B: Eliminar registros huérfanos (si los valores NULL son basura)
-- CUIDADO: Esto ELIMINA datos
-- DELETE FROM proyecto_submissions WHERE estudiante1id IS NULL;

-- ================================================================
-- NOTA: Después de ejecutar este script, reinicia el submission-service
-- ================================================================


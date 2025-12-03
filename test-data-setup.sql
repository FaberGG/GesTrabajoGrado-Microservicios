-- ================================================
-- SCRIPT SQL PARA PREPARAR DATOS DE PRUEBA
-- Review Service Testing
-- ================================================
-- Este script inserta datos de prueba en submission-service
-- para poder probar el review-service

-- IMPORTANTE: Ejecutar este script en la BD de submission_db
-- Conexión: postgresql://localhost:5433/submission_db

-- ================================================
-- 1. CREAR PROYECTO BASE
-- ================================================
INSERT INTO proyectos (id, titulo, estado, fecha_creacion)
VALUES (1, 'Sistema de Gestión Académica con IA', 'EN_DESARROLLO', NOW())
ON CONFLICT (id) DO NOTHING;

-- ================================================
-- 2. CREAR FORMATO A PARA PRUEBAS
-- ================================================
INSERT INTO formatos_a (
    id,
    proyecto_id,
    version,
    titulo,
    estado,
    descripcion,
    docente_director_id,
    docente_director_nombre,
    docente_director_email,
    fecha_creacion
) VALUES (
    1,
    1,  -- proyecto_id
    1,  -- version
    'Formato A - Sistema de Gestión Académica con IA',
    'EN_REVISION',
    'Sistema innovador que utiliza inteligencia artificial para mejorar la gestión académica universitaria',
    5,  -- coordinador
    'Dr. Carlos Rodriguez',
    'carlos.rodriguez@unicauca.edu.co',
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- ================================================
-- 3. CREAR ESTUDIANTES ASOCIADOS
-- ================================================
INSERT INTO formato_a_estudiantes (formato_a_id, estudiante_email)
VALUES
    (1, 'estudiante1@unicauca.edu.co'),
    (1, 'estudiante2@unicauca.edu.co')
ON CONFLICT DO NOTHING;

-- ================================================
-- 4. CREAR ANTEPROYECTO PARA PRUEBAS
-- ================================================
INSERT INTO anteproyectos (
    id,
    proyecto_id,
    titulo,
    estado,
    descripcion,
    docente_director_id,
    docente_director_nombre,
    docente_director_email,
    fecha_creacion
) VALUES (
    1,
    1,  -- mismo proyecto
    'Anteproyecto - Sistema de Gestión Académica con IA',
    'EN_REVISION',
    'Desarrollo completo del sistema con arquitectura de microservicios',
    5,
    'Dr. Carlos Rodriguez',
    'carlos.rodriguez@unicauca.edu.co',
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- ================================================
-- 5. CREAR ESTUDIANTES DEL ANTEPROYECTO
-- ================================================
INSERT INTO anteproyecto_estudiantes (anteproyecto_id, estudiante_email)
VALUES
    (1, 'estudiante1@unicauca.edu.co'),
    (1, 'estudiante2@unicauca.edu.co')
ON CONFLICT DO NOTHING;

-- ================================================
-- 6. VERIFICAR DATOS INSERTADOS
-- ================================================
SELECT 'PROYECTOS:' as tabla;
SELECT * FROM proyectos WHERE id = 1;

SELECT 'FORMATOS A:' as tabla;
SELECT * FROM formatos_a WHERE id = 1;

SELECT 'ANTEPROYECTOS:' as tabla;
SELECT * FROM anteproyectos WHERE id = 1;

-- ================================================
-- RESUMEN DE DATOS CREADOS
-- ================================================
-- Proyecto ID: 1
-- Formato A ID: 1 (proyecto_id: 1, version: 1)
-- Anteproyecto ID: 1 (proyecto_id: 1)
-- Coordinador: ID 5 (Dr. Carlos Rodriguez)
-- Evaluadores: ID 10, ID 11 (se asignarán durante las pruebas)
-- ================================================


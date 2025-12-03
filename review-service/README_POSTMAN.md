# 🔍 Review Service - Guía de Uso de Postman

## 📋 Archivos de Postman

Este directorio contiene dos archivos necesarios para probar el Review Service:

1. **`postman_collection.json`** - Colección con todos los endpoints del servicio
2. **`postman_environment.json`** - Variables de entorno configuradas

---

## 🚀 Configuración Inicial

### 1. Importar Archivos en Postman

1. Abre Postman
2. Haz clic en **Import** (esquina superior izquierda)
3. Arrastra o selecciona ambos archivos:
   - `postman_collection.json`
   - `postman_environment.json`
4. Haz clic en **Import**

### 2. Seleccionar el Environment

1. En la esquina superior derecha, busca el selector de **Environment**
2. Selecciona: **"Review Service - Local Environment"**

### 3. Configurar Variables del Environment

**Variables principales ya configuradas:**
- `base_url`: `http://localhost:8080` (API Gateway)
- `coordinador_id`: `2` ✅ (ID del coordinador)
- `formato_a_id`: `2` (ID del Formato A a evaluar)
- `evaluador_id`: `3` (ID de un evaluador)

⚠️ **IMPORTANTE**: Las variables `coordinador_id` y `formato_a_id` ya están configuradas con los valores correctos según tus logs.

---

## 📝 Flujo de Prueba: Evaluar Formato A

### Paso 1: Listar Formatos A Pendientes

**Endpoint:** `GET /api/review/formatoA/pendientes`

Este endpoint lista todos los Formatos A pendientes de evaluación y **automáticamente guarda el primer `formatoAId` en el environment**.

**Headers configurados:**
- `X-User-Role`: `COORDINADOR`

**Resultado esperado:** 
- Status: `200 OK`
- Retorna una lista paginada de formatos pendientes

---

### Paso 2: Evaluar Formato A ✅

**Endpoint:** `POST /api/review/formatoA/{{formato_a_id}}/evaluar`

Este es el endpoint principal que estabas probando.

**Headers configurados automáticamente:**
- `Authorization`: `Bearer {{coordinador_token}}`
- `X-User-Id`: `{{coordinador_id}}` ✅ **(Ahora usa el ID 2, no el 1)**
- `X-User-Role`: `COORDINADOR`

**Body (ejemplo aprobación):**
```json
{
  "decision": "APROBADO",
  "observaciones": "El formato cumple con todos los requisitos establecidos. Se aprueba para continuar con el proceso."
}
```

**Body (ejemplo rechazo):**
```json
{
  "decision": "RECHAZADO",
  "observaciones": "El formato presenta inconsistencias en la metodología propuesta."
}
```

**Resultado esperado:**
- Status: `201 Created`
- La evaluación se registra en el review-service
- El estado se actualiza en el submission-service ✅
- Se envía notificación a los involucrados

---

## 🔧 Cambios Realizados

### ✅ Problema Corregido

**Antes:**
```json
{
  "key": "X-User-Id",
  "value": "1"  ❌ (ID incorrecto)
}
```

**Ahora:**
```json
{
  "key": "X-User-Id",
  "value": "{{coordinador_id}}"  ✅ (Usa variable = 2)
}
```

### ✅ Correcciones en el Código

El `review-service` ahora envía correctamente al `submission-service`:

1. **Campo `evaluadoPor`**: ID del coordinador que evalúa (antes faltaba)
2. **Header `X-Service`**: `review-service` (para autenticación entre servicios)
3. **Campos `estado` y `observaciones`**: Como antes

---

## 🎯 Endpoints Disponibles

### Formato A - Review

1. **Listar Formatos A Pendientes**
   - `GET /api/review/formatoA/pendientes?page=0&size=10`
   - Rol: COORDINADOR

2. **Evaluar Formato A** ⭐
   - `POST /api/review/formatoA/{id}/evaluar`
   - Rol: COORDINADOR
   - Body: `{ "decision": "APROBADO|RECHAZADO", "observaciones": "..." }`

### Anteproyectos - Review

3. **Asignar Evaluadores**
   - `POST /api/review/anteproyectos/asignar`
   - Rol: JEFE_DEPARTAMENTO

4. **Listar Asignaciones**
   - `GET /api/review/anteproyectos/asignaciones`
   - Roles: JEFE_DEPARTAMENTO, EVALUADOR

5. **Evaluar Anteproyecto**
   - `POST /api/review/anteproyectos/{id}/evaluar`
   - Rol: EVALUADOR

### Health Check

6. **Verificar Estado del Servicio**
   - `GET /api/review/health`

---

## 🐛 Solución de Problemas

### Error 400 Bad Request

**Causa:** Faltaba el campo `evaluadoPor` en la petición al submission-service

**Solución:** ✅ Ya corregido en el código. El servicio ahora envía:
```json
{
  "estado": "APROBADO",
  "observaciones": "...",
  "evaluadoPor": 2  ← NUEVO CAMPO
}
```

### Error 403 Forbidden

**Causa:** Rol incorrecto o falta el header `X-User-Role`

**Solución:** Verifica que el header `X-User-Role` sea `COORDINADOR` para evaluar Formato A

### Error 404 Not Found

**Causa:** El Formato A con ese ID no existe o no está en estado PENDIENTE

**Solución:** Ejecuta primero el endpoint "Listar Formatos A Pendientes" para obtener IDs válidos

---

## 📊 Respuestas de Ejemplo

### Evaluación Exitosa (201 Created)
```json
{
  "success": true,
  "message": "Formato A evaluado exitosamente",
  "data": {
    "evaluationId": 4,
    "documentId": 2,
    "documentType": "FORMATO_A",
    "decision": "APROBADO",
    "observaciones": "El formato cumple con todos los requisitos...",
    "fechaEvaluacion": "2025-12-02T18:30:00",
    "notificacionEnviada": true
  }
}
```

### Error de Estado (400 Bad Request)
```json
{
  "success": false,
  "data": null,
  "errors": "Formato A no está en estado evaluable. Estado actual: APROBADO. Se requiere: EN_REVISION o PENDIENTE"
}
```

---

## 🔄 Próximos Pasos

1. ✅ Importa la colección y el environment en Postman
2. ✅ Verifica que el environment esté seleccionado
3. ✅ Ejecuta "Listar Formatos A Pendientes" para obtener IDs válidos
4. ✅ Ejecuta "Evaluar Formato A" con el ID obtenido
5. ✅ Revisa los logs del servicio para confirmar la actualización

---

## 📌 Notas Importantes

- **El servicio ya está actualizado** y funcionando con los cambios
- **El `coordinador_id` es 2** según tus logs de prueba
- **El formato con ID 2** está en estado PENDIENTE y listo para evaluar
- Todos los headers necesarios están pre-configurados en la colección
- Las variables se actualizan automáticamente al ejecutar los endpoints

---

¿Listo para probar? 🚀

Ejecuta la petición "Evaluar Formato A" desde Postman y deberías recibir un **201 Created** exitoso.


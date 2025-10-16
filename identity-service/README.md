# Microservicio de Identidad (Identity Service)

Este repositorio contiene un microservicio de identidad y autenticación implementado con Spring Boot, que proporciona funcionalidades de registro, login, gestión de perfiles de usuario y verificación de tokens JWT.

## 📋 Descripción del Servicio

El Microservicio de Identidad es responsable de:
- Registro de nuevos usuarios
- Autenticación de usuarios (login)
- Gestión de perfiles de usuario
- Validación de tokens JWT
- Proporcionar información sobre roles y programas disponibles

## 🛠️ Tecnologías Utilizadas

- **Runtime**: Java 21 LTS
- **Framework**: Spring Boot 3.2.x
- **Base de Datos**: PostgreSQL 15+
- **ORM**: Spring Data JPA + Hibernate
- **Migraciones**: Flyway
- **Autenticación**: Spring Security + JWT (jjwt 0.12.x)
- **Validaciones**: Jakarta Bean Validation (Hibernate Validator)
- **Documentación API**: SpringDoc OpenAPI 3 (Swagger)
- **Gestión de Dependencias**: Maven
- **Testing**: JUnit 5 + Mockito + Spring Boot Test
- **Containerización**: Docker + Docker Compose
- **Logging**: SLF4J + Logback

## 🚀 Requisitos Previos

- Java 21 o superior
- Maven 3.8 o superior
- Docker y Docker Compose (opcional, para ejecución containerizada)
- PostgreSQL 15 o superior (si se ejecuta sin Docker)

## ⚙️ Instalación y Configuración

### Opción 1: Usando Docker Compose (Recomendado)

1. **Clonar el repositorio**
   ```bash
   git clone <repo>
   cd identity-service-java
   ```

2. **Iniciar los servicios con Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Verificar que los servicios están funcionando**
   ```bash
   docker-compose ps
   ```

### Opción 2: Ejecución local (requiere PostgreSQL instalado)

1. **Clonar el repositorio**
   ```bash
   git clone <repo>
   cd identity-service-java
   ```

2. **Configurar variables de entorno**
   ```bash
   # Windows
   set SPRING_PROFILES_ACTIVE=dev
   set DATABASE_URL=jdbc:postgresql://localhost:5432/identity_db
   set DB_USER=identity_user
   set DB_PASSWORD=identity_pass
   set JWT_SECRET=your-super-secure-jwt-secret-key-minimum-32-characters

   # Linux/Mac
   export SPRING_PROFILES_ACTIVE=dev
   export DATABASE_URL=jdbc:postgresql://localhost:5432/identity_db
   export DB_USER=identity_user
   export DB_PASSWORD=identity_pass
   export JWT_SECRET=your-super-secure-jwt-secret-key-minimum-32-characters
   ```

3. **Compilar y ejecutar la aplicación**
   ```bash
   mvn clean package -DskipTests
   mvn spring-boot:run
   ```

4. **Acceder a la aplicación**
   - API: http://localhost:8080/api/auth
   - Documentación Swagger: http://localhost:8080/swagger-ui.html

## 📡 Endpoints API

### Registro de Usuario
- **URL**: `/api/auth/register`
- **Método**: `POST`
- **Autenticación**: No requerida
- **Body**:
  ```json
  {
    "nombres": "Juan Carlos",
    "apellidos": "Pérez García",
    "celular": "3201234567",
    "programa": "INGENIERIA_DE_SISTEMAS",
    "rol": "ESTUDIANTE",
    "email": "juan.perez@unicauca.edu.co",
    "password": "Pass123!"
  }
  ```
- **Respuesta (201 Created)**:
  ```json
  {
    "success": true,
    "message": "Usuario registrado exitosamente",
    "data": {
      "id": 1,
      "nombres": "Juan Carlos",
      "apellidos": "Pérez García",
      "celular": "3201234567",
      "programa": "INGENIERIA_DE_SISTEMAS",
      "rol": "ESTUDIANTE",
      "email": "juan.perez@unicauca.edu.co",
      "createdAt": "2025-10-16T11:27:56.972816",
      "updatedAt": "2025-10-16T11:27:56.972816"
    },
    "errors": null
  }
  ```

### Login
- **URL**: `/api/auth/login`
- **Método**: `POST`
- **Autenticación**: No requerida
- **Body**:
  ```json
  {
    "email": "juan.perez@unicauca.edu.co",
    "password": "Pass123!"
  }
  ```
- **Respuesta (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Login exitoso",
    "data": {
      "user": {
        "id": 1,
        "nombres": "Juan Carlos",
        "apellidos": "Perez Garcia",
        "celular": "3001234567",
        "programa": "INGENIERIA_DE_SISTEMAS",
        "rol": "ESTUDIANTE",
        "email": "juan.perez@unicauca.edu.co",
        "createdAt": "2025-10-16T11:27:56.972816",
        "updatedAt": "2025-10-16T11:27:56.972816"
      },
      "token": "eyJhbGciOiJIUzUxMiJ9..."
    },
    "errors": null
  }
  ```

### Perfil de Usuario
- **URL**: `/api/auth/profile`
- **Método**: `GET`
- **Autenticación**: Requerida (Bearer Token)
- **Headers**:
  ```
  Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
  ```
- **Respuesta (200 OK)**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "id": 1,
      "nombres": "Juan Carlos",
      "apellidos": "Perez Garcia",
      "celular": "3001234567",
      "programa": "INGENIERIA_DE_SISTEMAS",
      "rol": "ESTUDIANTE",
      "email": "juan.perez@unicauca.edu.co",
      "createdAt": "2025-10-16T11:27:56.972816",
      "updatedAt": "2025-10-16T11:27:56.972816"
    },
    "errors": null
  }
  ```

### Roles y Programas Disponibles
- **URL**: `/api/auth/roles`
- **Método**: `GET`
- **Autenticación**: No requerida
- **Respuesta (200 OK)**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "roles": ["ESTUDIANTE", "DOCENTE", "ADMIN"],
      "programas": [
        "INGENIERIA_DE_SISTEMAS",
        "INGENIERIA_ELECTRONICA_Y_TELECOMUNICACIONES",
        "AUTOMATICA_INDUSTRIAL",
        "TECNOLOGIA_EN_TELEMATICA"
      ]
    },
    "errors": null
  }
  ```

### Verificar Token
- **URL**: `/api/auth/verify-token`
- **Método**: `POST`
- **Autenticación**: No requerida
- **Body**:
  ```json
  {
    "token": "eyJhbGciOiJIUzUxMiJ9..."
  }
  ```
- **Respuesta (200 OK - Token Válido)**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "valid": true,
      "userId": 1,
      "email": "juan.perez@unicauca.edu.co",
      "rol": "ESTUDIANTE",
      "programa": "INGENIERIA_DE_SISTEMAS"
    },
    "errors": null
  }
  ```
- **Respuesta (401 Unauthorized - Token Inválido)**:
  ```json
  {
    "success": false,
    "message": "Token inválido o expirado",
    "data": null,
    "errors": null
  }
  ```

## 🧪 Pruebas con Postman

### 1. Registro de Usuario
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "nombres": "Juan Carlos",
  "apellidos": "Pérez García",
  "celular": "3201234567",
  "programa": "INGENIERIA_DE_SISTEMAS",
  "rol": "ESTUDIANTE",
  "email": "juan.perez@unicauca.edu.co",
  "password": "Pass123!"
}
```

### 2. Login
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "juan.perez@unicauca.edu.co",
  "password": "Pass123!"
}
```

**Nota**: Guardar el token de la respuesta para usarlo en las siguientes peticiones.

### 3. Consultar Perfil
```http
GET http://localhost:8080/api/auth/profile
Authorization: Bearer {token-obtenido-del-login}
```

### 4. Verificar Token
```http
POST http://localhost:8080/api/auth/verify-token
Content-Type: application/json

{
  "token": "{token-obtenido-del-login}"
}
```

### 5. Obtener Roles y Programas
```http
GET http://localhost:8080/api/auth/roles
```

### 6. Actualizar Perfil
```http
PUT http://localhost:8080/api/auth/profile
Authorization: Bearer {token-obtenido-del-login}
Content-Type: application/json

{
  "nombres": "Juan Carlos",
  "apellidos": "Pérez García",
  "celular": "3209876543"
}
```

## 📊 Valores Válidos para Enums

### Programas
- `INGENIERIA_DE_SISTEMAS`
- `INGENIERIA_ELECTRONICA_Y_TELECOMUNICACIONES`
- `AUTOMATICA_INDUSTRIAL`
- `TECNOLOGIA_EN_TELEMATICA`

### Roles
- `ESTUDIANTE`
- `DOCENTE`
- `ADMIN`

**Importante**: Los valores deben escribirse exactamente como se muestran (en mayúsculas y con guiones bajos).

## 🧪 Ejecutar Pruebas

### Pruebas unitarias
```bash
mvn test
```

### Pruebas con cobertura
```bash
mvn test jacoco:report
```

## 🔐 Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|------------------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo (dev/prod/test) | `dev` |
| `DATABASE_URL` | URL de conexión a la base de datos | `jdbc:postgresql://localhost:5432/identity_db` |
| `DB_USER` | Usuario de la base de datos | `identity_user` |
| `DB_PASSWORD` | Contraseña de la base de datos | `identity_pass` |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT | `your-super-secret-jwt-key...` |
| `JWT_EXPIRATION` | Tiempo de expiración del token en ms | `3600000` (1 hora) |

## 📊 Monitoreo y Health Check

- Health Check: `http://localhost:8080/actuator/health`
- Métricas: `http://localhost:8080/actuator/metrics`
- Info: `http://localhost:8080/actuator/info`

## 🔍 Solución de Problemas

### Problemas comunes

1. **Error de conexión a la base de datos**
   - Verificar que PostgreSQL esté en ejecución
   - Comprobar las credenciales de acceso
   - Revisar logs en `logs/identity-service.log`

2. **Error de deserialización de enum** (ej: `INGENIERIA_ELECTRONICA` no válido)
   - Usar valores exactos: `INGENIERIA_ELECTRONICA_Y_TELECOMUNICACIONES`
   - Verificar que todos los valores estén en mayúsculas con guiones bajos

3. **Token JWT inválido**
   - Verificar que el token no haya expirado (1 hora de validez)
   - Comprobar formato correcto: `Bearer <token>`

4. **Fallos en la validación**
   - Los emails deben ser institucionales (@unicauca.edu.co)
   - Las contraseñas deben cumplir requisitos: mínimo 8 caracteres, mayúscula, número y carácter especial

## 📄 Licencia

Este proyecto está licenciado bajo [MIT License](LICENSE).

## 👥 Contacto

Universidad del Cauca - soporte@unicauca.edu.co

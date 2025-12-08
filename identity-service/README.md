# Microservicio de Identidad (Identity Service)

Este repositorio contiene un microservicio de identidad y autenticación implementado con Spring Boot, que proporciona funcionalidades de registro, login, gestión de perfiles de usuario y verificación de tokens JWT.

## 📋 Descripción del Servicio

El Microservicio de Identidad es responsable de:
- Registro de nuevos usuarios
- Autenticación de usuarios (login)
- Gestión de perfiles de usuario
- Validación de tokens JWT
- Proporcionar información sobre roles y programas disponibles
- Búsqueda y consulta de usuarios
- Comunicación interna entre microservicios (service-to-service)

## 🗝️ Arquitectura y Patrones de Diseño

### Patrón Facade

El microservicio implementa el **patrón Facade** para simplificar y centralizar las operaciones de identidad y autenticación.

**Ubicación**: `com.unicauca.identity.facade.IdentityFacade`

**Propósito**: El Facade proporciona una interfaz unificada y simplificada que encapsula la complejidad de las interacciones entre servicios y repositorios. Esto permite que los controladores tengan una API más limpia y desacoplada de la lógica de negocio interna.

**Beneficios**:
- **Simplificación**: Los controladores interactúan con una única clase (IdentityFacade) en lugar de múltiples servicios
- **Desacoplamiento**: Cambios internos en servicios o repositorios no afectan a los controladores
- **Mantenibilidad**: Lógica centralizada facilita el mantenimiento y testing
- **Cohesión**: Agrupa operaciones relacionadas de identidad en un solo punto de acceso

**Métodos principales**:
- `registerUser()`: Registro de nuevos usuarios
- `authenticateUser()`: Autenticación y generación de tokens
- `getUserProfile()`: Obtención de perfiles de usuario
- `verifyToken()`: Verificación de tokens JWT
- `searchUsers()`: Búsqueda paginada de usuarios
- `getEmailByRole()`: Consulta de emails por rol
- `getUserBasicInfo()`: Información básica para comunicación entre servicios
- `getCoordinador()`: Obtención del coordinador del sistema
- `getJefeDepartamento()`: Obtención del jefe de departamento

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
   set SERVICE_INTERNAL_TOKEN=your-secure-service-token

   # Linux/Mac
   export SPRING_PROFILES_ACTIVE=dev
   export DATABASE_URL=jdbc:postgresql://localhost:5432/identity_db
   export DB_USER=identity_user
   export DB_PASSWORD=identity_pass
   export JWT_SECRET=your-super-secure-jwt-secret-key-minimum-32-characters
   export SERVICE_INTERNAL_TOKEN=your-secure-service-token
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

### 1. Registro de Usuario
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

### 2. Login
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

### 3. Perfil de Usuario
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

### 4. Roles y Programas Disponibles
- **URL**: `/api/auth/roles`
- **Método**: `GET`
- **Autenticación**: No requerida
- **Respuesta (200 OK)**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "roles": ["ESTUDIANTE", "DOCENTE", "COORDINADOR", "JEFE_DEPARTAMENTO", "ADMIN"],
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

### 5. Verificar Token
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

### 6. Buscar Usuarios
- **URL**: `/api/auth/users/search`
- **Método**: `GET`
- **Autenticación**: Requerida (Bearer Token)
- **Query Parameters**:
    - `query` (opcional): Texto para buscar en nombres, apellidos o email
    - `rol` (opcional): Filtrar por rol específico
    - `programa` (opcional): Filtrar por programa académico
    - `page` (opcional, default: 0): Número de página
    - `size` (opcional, default: 10): Tamaño de página
- **Ejemplo**: `/api/auth/users/search?query=juan&rol=ESTUDIANTE&page=0&size=10`
- **Respuesta (200 OK)**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "content": [
        {
          "id": 1,
          "nombres": "Juan Carlos",
          "apellidos": "Pérez García",
          "celular": "3201234567",
          "programa": "INGENIERIA_DE_SISTEMAS",
          "rol": "ESTUDIANTE",
          "email": "juan.perez@unicauca.edu.co",
          "createdAt": "2025-10-16T11:27:56.972816",
          "updatedAt": "2025-10-16T11:27:56.972816"
        }
      ],
      "page": {
        "size": 10,
        "number": 0,
        "totalElements": 1,
        "totalPages": 1
      }
    }
  }
  ```

### 7. Obtener Email por Rol
- **URL**: `/api/auth/users/role/{role}/email`
- **Método**: `GET`
- **Autenticación**: No requerida
- **Path Parameter**:
    - `role`: Rol del usuario (ESTUDIANTE, DOCENTE, COORDINADOR, JEFE_DEPARTAMENTO, ADMIN)
- **Ejemplo**: `/api/auth/users/role/COORDINADOR/email`
- **Respuesta (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Email obtenido correctamente",
    "data": {
      "email": "coordinador@unicauca.edu.co"
    },
    "errors": null
  }
  ```

### 8. Obtener Información Básica de Usuario (Service-to-Service)
- **URL**: `/api/auth/users/{userId}/basic`
- **Método**: `GET`
- **Autenticación**: Token de servicio interno
- **Headers**:
  ```
  X-Service-Token: your-secure-service-token
  ```
- **Respuesta (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Usuario encontrado",
    "data": {
      "id": 1,
      "nombres": "Juan Carlos",
      "apellidos": "Pérez García",
      "email": "juan.perez@unicauca.edu.co",
      "rol": "ESTUDIANTE",
      "programa": "INGENIERIA_DE_SISTEMAS"
    }
  }
  ```

### 9. Obtener Coordinador (Service-to-Service)
- **URL**: `/api/auth/users/coordinador`
- **Método**: `GET`
- **Autenticación**: Token de servicio interno
- **Headers**:
  ```
  X-Service-Token: your-secure-service-token
  ```
- **Respuesta (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Coordinador encontrado",
    "data": {
      "id": 2,
      "nombres": "María",
      "apellidos": "González",
      "email": "coordinador@unicauca.edu.co",
      "rol": "COORDINADOR",
      "programa": "INGENIERIA_DE_SISTEMAS"
    }
  }
  ```

### 10. Obtener Jefe de Departamento (Service-to-Service)
- **URL**: `/api/auth/users/jefe-departamento`
- **Método**: `GET`
- **Autenticación**: Token de servicio interno
- **Headers**:
  ```
  X-Service-Token: your-secure-service-token
  ```
- **Respuesta (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Jefe de departamento encontrado",
    "data": {
      "id": 3,
      "nombres": "Carlos",
      "apellidos": "Ramírez",
      "email": "jefe.departamento@unicauca.edu.co",
      "rol": "JEFE_DEPARTAMENTO",
      "programa": "INGENIERIA_DE_SISTEMAS"
    }
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

### 6. Buscar Usuarios
```http
GET http://localhost:8080/api/auth/users/search?query=juan&page=0&size=10
Authorization: Bearer {token-obtenido-del-login}
```

### 7. Obtener Email por Rol
```http
GET http://localhost:8080/api/auth/users/role/COORDINADOR/email
```

### 8. Endpoints Internos (Service-to-Service)
```http
GET http://localhost:8080/api/auth/users/1/basic
X-Service-Token: your-secure-service-token
```

```http
GET http://localhost:8080/api/auth/users/coordinador
X-Service-Token: your-secure-service-token
```

```http
GET http://localhost:8080/api/auth/users/jefe-departamento
X-Service-Token: your-secure-service-token
```

## 📊 Valores Válidos para Enums

### Programas
- `INGENIERIA_DE_SISTEMAS`
- `INGENIERIA_ELECTRONICA_Y_TELECOMUNICACIONES`
- `AUTOMATICA_INDUSTRIAL`
- `TECNOLOGIA_EN_TELEMATICA`

### Roles
- `ESTUDIANTE` - Estudiante que realiza proyecto de grado
- `DOCENTE` - Director o codirector de proyecto de grado
- `COORDINADOR` - Coordinador de programa que evalúa Formato A
- `JEFE_DEPARTAMENTO` - Jefe de departamento que recibe anteproyectos
- `ADMIN` - Administrador del sistema

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

## 🔐 Seguridad y Comunicación Entre Servicios

### Autenticación de Usuarios
Los endpoints públicos (`/register`, `/login`) no requieren autenticación. Los endpoints protegidos requieren un token JWT válido en el header `Authorization: Bearer {token}`.

### Comunicación Service-to-Service
Los endpoints internos (`/users/{userId}/basic`, `/users/coordinador`, `/users/jefe-departamento`) requieren un token de servicio interno en el header `X-Service-Token`. Este token debe configurarse mediante la variable de entorno `SERVICE_INTERNAL_TOKEN` y debe ser compartido solo entre microservicios confiables.

## 📝 Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|------------------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo (dev/prod/test) | `dev` |
| `SPRING_DATASOURCE_URL` | URL de conexión a la base de datos | `jdbc:postgresql://localhost:5432/identity_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `identity_user` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `identity_pass` |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT | `your-super-secret-jwt-key...` |
| `JWT_EXPIRATION` | Tiempo de expiración del token en ms | `3600000` (1 hora) |
| `SERVICE_INTERNAL_TOKEN` | Token para comunicación entre servicios | `default-token-only-for-dev` |

## 📊 Monitoreo y Health Check

- Health Check: `http://localhost:8080/actuator/health`
- Métricas: `http://localhost:8080/actuator/metrics`
- Info: `http://localhost:8080/actuator/info`

## 🔧 Solución de Problemas

### Problemas comunes

1. **Error de conexión a la base de datos**
    - Verificar que PostgreSQL esté en ejecución
    - Comprobar las credenciales de acceso
    - Revisar logs en `logs/identity-service.log`

2. **Error de deserialización de enum**
    - Usar valores exactos: `INGENIERIA_ELECTRONICA_Y_TELECOMUNICACIONES`
    - Verificar que todos los valores estén en mayúsculas con guiones bajos
    - Roles válidos: `ESTUDIANTE`, `DOCENTE`, `COORDINADOR`, `JEFE_DEPARTAMENTO`, `ADMIN`

3. **Token JWT inválido**
    - Verificar que el token no haya expirado (1 hora de validez)
    - Comprobar formato correcto: `Bearer <token>`

4. **Fallos en la validación**
    - Los emails deben ser institucionales (@unicauca.edu.co)
    - Las contraseñas deben cumplir requisitos: mínimo 8 caracteres, mayúscula, número y carácter especial

5. **Acceso denegado a endpoints internos**
    - Verificar que el header `X-Service-Token` esté presente
    - Comprobar que el token coincida con `SERVICE_INTERNAL_TOKEN`

## 📄 Licencia

Este proyecto está licenciado bajo [MIT License](LICENSE).

## 👥 Contacto

Universidad del Cauca - soporte@unicauca.edu.co
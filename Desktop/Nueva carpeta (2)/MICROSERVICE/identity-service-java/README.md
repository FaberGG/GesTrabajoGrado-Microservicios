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

- **Runtime**: Java 17 LTS
- **Framework**: Spring Boot 3.2.x
- **Base de Datos**: PostgreSQL 15+
- **ORM**: Spring Data JPA + Hibernate
- **Autenticación**: Spring Security + JWT (jjwt 0.12.x)
- **Validaciones**: Jakarta Bean Validation (Hibernate Validator)
- **Documentación API**: SpringDoc OpenAPI 3 (Swagger)
- **Gestión de Dependencias**: Maven
- **Testing**: JUnit 5 + Mockito + Spring Boot Test
- **Containerización**: Docker + Docker Compose
- **Logging**: SLF4J + Logback

## 🚀 Requisitos Previos

- Java 17 o superior
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
   mvn clean install
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
    "email": "jperez@unicauca.edu.co",
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
      "email": "jperez@unicauca.edu.co",
      "rol": "ESTUDIANTE",
      "programa": "INGENIERIA_DE_SISTEMAS",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  }
  ```

### Login
- **URL**: `/api/auth/login`
- **Método**: `POST`
- **Autenticación**: No requerida
- **Body**:
  ```json
  {
    "email": "jperez@unicauca.edu.co",
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
        "apellidos": "Pérez García",
        "email": "jperez@unicauca.edu.co",
        "rol": "ESTUDIANTE",
        "programa": "INGENIERIA_DE_SISTEMAS"
      },
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
  }
  ```

### Perfil de Usuario
- **URL**: `/api/auth/profile`
- **Método**: `GET`
- **Autenticación**: Requerida (Bearer Token)
- **Headers**:
  ```
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  ```
- **Respuesta (200 OK)**:
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "nombres": "Juan Carlos",
      "apellidos": "Pérez García",
      "celular": "3201234567",
      "email": "jperez@unicauca.edu.co",
      "rol": "ESTUDIANTE",
      "programa": "INGENIERIA_DE_SISTEMAS",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  }
  ```

### Roles y Programas Disponibles
- **URL**: `/api/auth/roles`
- **Método**: `GET`
- **Autenticación**: Requerida (Bearer Token)
- **Headers**:
  ```
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  ```
- **Respuesta (200 OK)**:
  ```json
  {
    "success": true,
    "data": {
      "roles": ["ESTUDIANTE", "DOCENTE", "ADMIN"],
      "programas": [
        "INGENIERIA_DE_SISTEMAS",
        "INGENIERIA_ELECTRONICA_Y_TELECOMUNICACIONES",
        "AUTOMATICA_INDUSTRIAL",
        "TECNOLOGIA_EN_TELEMATICA"
      ]
    }
  }
  ```

### Verificar Token
- **URL**: `/api/auth/verify-token`
- **Método**: `POST`
- **Autenticación**: No requerida
- **Body**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```
- **Respuesta (200 OK - Token Válido)**:
  ```json
  {
    "success": true,
    "valid": true,
    "data": {
      "userId": 1,
      "email": "jperez@unicauca.edu.co",
      "rol": "ESTUDIANTE",
      "programa": "INGENIERIA_DE_SISTEMAS"
    }
  }
  ```
- **Respuesta (200 OK - Token Inválido)**:
  ```json
  {
    "success": false,
    "valid": false,
    "message": "Token inválido o expirado"
  }
  ```

## 🧪 Pruebas

### Ejecutar pruebas unitarias
```bash
mvn test
```

### Ejecutar pruebas con cobertura
```bash
mvn test jacoco:report
```

## 🔐 Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|------------------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo (dev/prod) | `dev` |
| `DATABASE_URL` | URL de conexión a la base de datos | `jdbc:postgresql://localhost:5432/identity_db` |
| `DB_USER` | Usuario de la base de datos | `identity_user` |
| `DB_PASSWORD` | Contraseña de la base de datos | `identity_pass` |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT | `your-super-secret-jwt-key...` |
| `JWT_EXPIRATION` | Tiempo de expiración del token en ms | `3600000` (1 hora) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos para CORS | `http://localhost:8080,http://localhost:3001` |

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

2. **Token JWT inválido**
   - Verificar que el token no haya expirado
   - Comprobar que se está utilizando el formato correcto: `Bearer <token>`

3. **Fallos en la validación**
   - Los emails deben ser institucionales (@unicauca.edu.co)
   - Las contraseñas deben cumplir los requisitos de seguridad (mayúscula, número, carácter especial)

## 📄 Licencia

Este proyecto está licenciado bajo [MIT License](LICENSE).

## 👥 Contacto

Universidad del Cauca - soporte@unicauca.edu.co

# Gestión Manual de JWT en la Arquitectura de Microservicios

## Índice
1. [Introducción](#introducción)
2. [Arquitectura de Seguridad](#arquitectura-de-seguridad)
3. [Componentes del Sistema JWT](#componentes-del-sistema-jwt)
4. [Flujo de Autenticación](#flujo-de-autenticación)
5. [Validación y Autorización](#validación-y-autorización)
6. [Implementación Detallada](#implementación-detallada)
7. [Configuración](#configuración)
8. [Mejores Prácticas](#mejores-prácticas)
9. [Troubleshooting](#troubleshooting)

---

## Introducción

Este sistema implementa una **gestión manual de JWT (JSON Web Tokens)** sin utilizar Spring Security OAuth2 o frameworks de autenticación de terceros. La implementación está distribuida entre el **Identity Service** (generación de tokens) y el **Gateway Service** (validación y propagación de claims).

### ¿Por qué "Manual"?

- **Control Total**: Gestión completa del ciclo de vida del token
- **Personalización**: Claims personalizados según necesidades de negocio
- **Sin Dependencias Externas**: No requiere OAuth2, Keycloak, Auth0, etc.
- **Arquitectura de Microservicios**: Diseñado específicamente para comunicación entre servicios

### Librerías Utilizadas

- **io.jsonwebtoken (JJWT)**: Para creación, parsing y validación de JWT
- **Spring Security**: Solo para gestión de contexto de seguridad (no OAuth2)
- **Spring Cloud Gateway**: Para enrutamiento y filtrado de peticiones

---

## Arquitectura de Seguridad

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTE                                  │
│  (Navegador / Aplicación Móvil / Postman)                       │
└────────────┬────────────────────────────────────────────────────┘
             │
             │ 1. POST /api/auth/login
             │    { "email": "...", "password": "..." }
             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      GATEWAY SERVICE                             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Ruta Pública (/api/auth/**) → Sin validación JWT       │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────┬────────────────────────────────────────────────────┘
             │
             │ 2. Proxy a Identity Service
             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    IDENTITY SERVICE                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  JwtTokenProvider.generateToken(user)                    │   │
│  │  ↓                                                        │   │
│  │  Token JWT generado con:                                 │   │
│  │  - Subject: email del usuario                            │   │
│  │  - Claims: userId, rol, programa                         │   │
│  │  - Expiración: 1 hora (configurable)                     │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────┬────────────────────────────────────────────────────┘
             │
             │ 3. Respuesta con token
             │    { "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...", 
             │      "userId": 123, "role": "DOCENTE" }
             ▼
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTE                                  │
│  Almacena token en localStorage / memoria                        │
└────────────┬────────────────────────────────────────────────────┘
             │
             │ 4. GET /api/submissions/formatoA
             │    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...
             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      GATEWAY SERVICE                             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  JwtGatewayFilter                                        │   │
│  │  ↓                                                        │   │
│  │  1. Extrae token del header Authorization               │   │
│  │  2. Valida firma con secret compartido                  │   │
│  │  3. Verifica expiración                                 │   │
│  │  4. Extrae claims: userId, rol, email                   │   │
│  │  5. Añade headers X-User-*                              │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  RoleFilter (opcional)                                   │   │
│  │  ↓                                                        │   │
│  │  Verifica autorización basada en rol                    │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────┬────────────────────────────────────────────────────┘
             │
             │ 5. Proxy con headers adicionales
             │    X-User-Id: 123
             │    X-User-Role: DOCENTE
             │    X-User-Email: docente@unicauca.edu.co
             ▼
┌─────────────────────────────────────────────────────────────────┐
│              SUBMISSION SERVICE / REVIEW SERVICE                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Controller recibe:                                      │   │
│  │  @RequestHeader("X-User-Id") Long userId                │   │
│  │  @RequestHeader("X-User-Role") String role              │   │
│  │                                                          │   │
│  │  Ejecuta lógica de negocio con contexto de usuario      │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Componentes del Sistema JWT

### 1. Identity Service - Generación de Tokens

#### JwtTokenProvider.java
**Ubicación**: `identity-service/src/main/java/com/unicauca/identity/security/JwtTokenProvider.java`

**Responsabilidades**:
- Generar tokens JWT con claims personalizados
- Validar tokens en endpoints protegidos del Identity Service
- Extraer información del usuario desde tokens

**Métodos Principales**:

```java
public String generateToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

    return Jwts.builder()
            .subject(user.getEmail())              // Subject: email del usuario
            .claim("userId", user.getId())         // Claim personalizado
            .claim("rol", user.getRol())           // Claim personalizado
            .claim("programa", user.getPrograma()) // Claim personalizado
            .issuedAt(now)                         // Timestamp de creación
            .expiration(expiryDate)                // Timestamp de expiración
            .signWith(getSigningKey())             // Firma con HMAC-SHA256
            .compact();
}
```

**Estructura del Token Generado**:

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "docente@unicauca.edu.co",
    "userId": 123,
    "rol": "DOCENTE",
    "programa": "INGENIERIA_SISTEMAS",
    "iat": 1702200000,
    "exp": 1702203600
  },
  "signature": "..."
}
```

#### JwtAuthenticationFilter.java
**Ubicación**: `identity-service/src/main/java/com/unicauca/identity/security/JwtAuthenticationFilter.java`

**Responsabilidades**:
- Interceptar todas las peticiones HTTP al Identity Service
- Validar JWT en endpoints protegidos (ej: `/api/auth/profile`)
- Establecer contexto de seguridad de Spring Security

**Flujo de Ejecución**:

```java
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) {
    // 1. Extraer token del header Authorization
    String jwt = getJwtFromRequest(request);
    
    // 2. Validar token
    if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
        
        // 3. Obtener email del token
        String userEmail = tokenProvider.getUserEmailFromToken(jwt);
        
        // 4. Cargar detalles del usuario desde BD
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        
        // 5. Crear objeto Authentication
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                userDetails, 
                null, 
                userDetails.getAuthorities()
            );
        
        // 6. Establecer contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    // 7. Continuar cadena de filtros
    filterChain.doFilter(request, response);
}
```

---

### 2. Gateway Service - Validación y Propagación

#### JwtUtils.java
**Ubicación**: `gateway-service/src/main/java/co/unicauca/gateway/security/JwtUtils.java`

**Responsabilidades**:
- Validar firma y expiración de tokens JWT
- Extraer claims del payload
- Utilizar **exactamente el mismo secret** que Identity Service

**Características Clave**:

1. **Validación de Firma**:
```java
public boolean validateToken(String token) {
    try {
        Jwts.parser()
                .verifyWith(getSigningKey())  // Usa el mismo secret
                .build()
                .parseSignedClaims(token);
        return true;
    } catch (SignatureException e) {
        log.error("Firma JWT inválida");
        return false;
    } catch (ExpiredJwtException e) {
        log.error("Token JWT expirado");
        return false;
    }
    // ... otros catch
}
```

2. **Extracción de Claims**:
```java
public Map<String, String> extractClaims(String token) {
    Claims jwtClaims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

    Map<String, String> claims = new HashMap<>();
    claims.put("email", jwtClaims.getSubject());
    claims.put("userId", String.valueOf(jwtClaims.get("userId")));
    claims.put("role", String.valueOf(jwtClaims.get("rol")));
    claims.put("programa", String.valueOf(jwtClaims.get("programa")));
    
    return claims;
}
```

#### JwtGatewayFilter.java
**Ubicación**: `gateway-service/src/main/java/co/unicauca/gateway/security/JwtGatewayFilter.java`

**Responsabilidades**:
- Filtro de Spring Cloud Gateway para validar JWT
- Añadir headers `X-User-*` a peticiones proxy
- Rechazar peticiones sin token válido (401 Unauthorized)

**Flujo de Ejecución**:

```java
public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Verificar si es ruta pública
        if (isPublicPath(path)) {
            return chain.filter(exchange);  // Continuar sin validación
        }

        // 2. Extraer header Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            return unauthorizedResponse(exchange, "Token missing");
        }

        // 3. Extraer token (quitar "Bearer ")
        String token = jwtUtils.extractTokenFromHeader(authHeader);
        if (token == null) {
            return unauthorizedResponse(exchange, "Invalid token format");
        }

        // 4. Validar token
        if (!jwtUtils.validateToken(token)) {
            return unauthorizedResponse(exchange, "Token invalid or expired");
        }

        // 5. Extraer claims
        Map<String, String> claims = jwtUtils.extractClaims(token);

        // 6. Crear petición mutada con headers X-User-*
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", claims.getOrDefault("userId", ""))
                .header("X-User-Role", claims.getOrDefault("role", ""))
                .header("X-User-Email", claims.getOrDefault("email", ""))
                .build();

        // 7. Continuar con petición mutada
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    };
}
```

#### RoleFilter.java
**Ubicación**: `gateway-service/src/main/java/co/unicauca/gateway/security/RoleFilter.java`

**Responsabilidades**:
- Autorización basada en roles (capa adicional de seguridad)
- Verificar que el usuario tenga el rol requerido para acceder al endpoint
- Configurable mediante `gateway.security.enforceRoleCheck`

**Definición de Requisitos de Rol**:

```java
private static final Map<String, List<String>> roleRequirements = new HashMap<>();

static {
    // RF2: Subir Formato A - Solo DOCENTE
    roleRequirements.put("/api/submissions/formatoA", List.of("DOCENTE"));

    // RF3: Evaluar Formato A - Solo COORDINADOR
    roleRequirements.put("/api/review/formatoA/*/evaluar", List.of("COORDINADOR"));

    // RF5: Consultar estado - Todos los roles
    roleRequirements.put("/api/progress/proyectos/*/estado", 
        List.of("ESTUDIANTE", "DOCENTE", "COORDINADOR", "JEFE_DEPARTAMENTO"));

    // ... más reglas
}
```

**Flujo de Autorización**:

```java
public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
        // 1. Verificar si el chequeo de roles está activado
        if (!enforceRoleCheck) {
            return chain.filter(exchange);
        }

        String path = request.getURI().getPath();
        String userRole = request.getHeaders().getFirst("X-User-Role");

        // 2. Obtener roles requeridos para el path
        List<String> requiredRoles = getRequiredRoles(path);

        // 3. Verificar si el usuario tiene el rol requerido
        if (!requiredRoles.contains(userRole)) {
            return forbiddenResponse(exchange, "Access denied");
        }

        // 4. Usuario autorizado
        return chain.filter(exchange);
    };
}
```

---

### 3. Microservicios - Consumo de Claims

Los microservicios downstream (Submission, Review, Progress Tracking) **no validan JWT directamente**. En su lugar, confían en el Gateway y reciben la información del usuario a través de headers HTTP personalizados.

#### Ejemplo en Submission Service

```java
@RestController
@RequestMapping("/api/submissions")
public class FormatoAController {

    @PostMapping("/formatoA")
    public ResponseEntity<?> subirFormatoA(
            @RequestParam("file") MultipartFile file,
            @Valid @RequestBody FormatoADTO formatoADTO,
            @RequestHeader("X-User-Id") Long userId,           // Inyectado por Gateway
            @RequestHeader("X-User-Role") String role,         // Inyectado por Gateway
            @RequestHeader("X-User-Email") String email) {     // Inyectado por Gateway
        
        log.info("Usuario {} (rol: {}) subiendo Formato A", userId, role);
        
        // Ejecutar lógica de negocio
        FormatoAResponse response = formatoAService.crearFormatoA(
            userId, 
            formatoADTO, 
            file
        );
        
        return ResponseEntity.ok(response);
    }
}
```

#### Ejemplo en Review Service

```java
@RestController
@RequestMapping("/api/review")
public class FormatoAReviewController {

    @PostMapping("/formatoA/{formatoAId}/evaluar")
    public ResponseEntity<?> evaluarFormatoA(
            @PathVariable Long formatoAId,
            @Valid @RequestBody EvaluacionFormatoADTO evaluacionDTO,
            @RequestHeader("X-User-Id") Long coordinadorId) {  // Solo coordinadores
        
        // Lógica de evaluación
        EvaluacionResponse response = reviewService.evaluarFormatoA(
            formatoAId, 
            coordinadorId, 
            evaluacionDTO
        );
        
        return ResponseEntity.ok(response);
    }
}
```

---

## Flujo de Autenticación

### 1. Login - Obtención del Token

**Request**:
```http
POST http://localhost:8080/api/auth/login HTTP/1.1
Content-Type: application/json

{
  "email": "docente@unicauca.edu.co",
  "password": "password123"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkb2NlbnRlQHVuaWNhdWNhLmVkdS5jbyIsInVzZXJJZCI6MTIzLCJyb2wiOiJET0NFTlRFIiwicHJvZ3JhbWEiOiJJTkdFTklFUklBX1NJU1RFTUFTIiwiaWF0IjoxNzAyMjAwMDAwLCJleHAiOjE3MDIyMDM2MDB9.signature",
    "userId": 123,
    "email": "docente@unicauca.edu.co",
    "role": "DOCENTE",
    "programa": "INGENIERIA_SISTEMAS"
  }
}
```

### 2. Acceso a Recurso Protegido

**Request**:
```http
GET http://localhost:8080/api/submissions/formatoA HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Procesamiento en Gateway**:
1. Gateway intercepta la petición
2. `JwtGatewayFilter` valida el token
3. Se extraen claims: `userId=123`, `rol=DOCENTE`
4. Se añaden headers: `X-User-Id: 123`, `X-User-Role: DOCENTE`
5. `RoleFilter` (opcional) verifica autorización
6. Se proxy la petición a Submission Service con headers adicionales

**Petición que recibe Submission Service**:
```http
GET http://submission-service:8082/api/submissions/formatoA HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
X-User-Id: 123
X-User-Role: DOCENTE
X-User-Email: docente@unicauca.edu.co
```

---

## Validación y Autorización

### Niveles de Seguridad

#### 1. Validación en Gateway (JwtGatewayFilter)
- **Qué valida**: Firma del token, expiración
- **Cuándo se ejecuta**: Todas las rutas protegidas
- **Resultado**: 401 Unauthorized si el token es inválido

#### 2. Autorización en Gateway (RoleFilter)
- **Qué valida**: Rol del usuario contra requisitos del endpoint
- **Cuándo se ejecuta**: Si `gateway.security.enforceRoleCheck=true`
- **Resultado**: 403 Forbidden si el usuario no tiene el rol requerido

#### 3. Validación en Microservicios (Lógica de Negocio)
- **Qué valida**: Reglas de negocio específicas
- **Ejemplo**: "¿Este docente es el propietario del Formato A que intenta modificar?"
- **Cuándo se ejecuta**: En cada operación de negocio
- **Resultado**: Excepciones de dominio (ej: `UnauthorizedException`)

### Ejemplo de Validación Completa

**Endpoint**: `PUT /api/submissions/formatoA/{id}/nueva-version`

**Requisitos**:
- Usuario autenticado (JWT válido)
- Rol: `DOCENTE`
- Regla de negocio: Solo el docente propietario puede subir nueva versión

**Flujo de Validación**:

```java
// 1. Gateway valida JWT
JwtGatewayFilter → validateToken() → OK

// 2. Gateway verifica rol
RoleFilter → requiredRoles.contains("DOCENTE") → OK

// 3. Submission Service valida regla de negocio
@PostMapping("/formatoA/{id}/nueva-version")
public ResponseEntity<?> subirNuevaVersion(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file,
        @RequestHeader("X-User-Id") Long userId) {
    
    // Obtener Formato A desde BD
    FormatoA formatoA = formatoARepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Formato A no encontrado"));
    
    // VALIDACIÓN DE NEGOCIO: ¿Es el propietario?
    if (!formatoA.getDocenteId().equals(userId)) {
        throw new UnauthorizedException(
            "Solo el docente propietario puede subir una nueva versión"
        );
    }
    
    // Ejecutar lógica
    // ...
}
```

---

## Implementación Detallada

### Secret Compartido

**CRÍTICO**: El mismo `jwt.secret` debe estar configurado en:
- Identity Service (para generar tokens)
- Gateway Service (para validar tokens)

**Recomendaciones**:
- Mínimo 256 bits (32 caracteres)
- Generado aleatoriamente
- Almacenado en variables de entorno (nunca en código)
- Diferente para cada ambiente (dev, staging, prod)

**Ejemplo de Secret Seguro**:
```bash
# Generar con OpenSSL
openssl rand -base64 32

# Resultado ejemplo:
ZxK9m3Qp8vN7Jh2Wk5Rt4Yl6Fg1Hn0Bs
```

### Tiempo de Expiración

**Configuración**:
```yaml
jwt:
  expiration: 3600000  # 1 hora en milisegundos
```

**Consideraciones**:
- **1 hora**: Balance entre seguridad y experiencia de usuario
- **Tokens de corta duración**: Mayor seguridad, más re-logins
- **Tokens de larga duración**: Mejor UX, mayor riesgo si se compromete

**Implementación de Refresh Tokens** (no incluida actualmente):
```java
// Generar access token (corta duración) + refresh token (larga duración)
public LoginResponse login(LoginRequest request) {
    // ...
    String accessToken = generateAccessToken(user);   // 15 minutos
    String refreshToken = generateRefreshToken(user);  // 7 días
    
    return new LoginResponse(accessToken, refreshToken, user);
}
```

### Rutas Públicas vs Protegidas

**Configuración en Gateway**:
```yaml
gateway:
  security:
    publicPaths:
      - /api/identity/**     # Registro, login
      - /api/auth/**         # Autenticación
      - /api/gateway/health  # Health checks
      - /actuator/health     # Actuator
```

**Lógica de Verificación**:
```java
private boolean isPublicPath(String path) {
    for (String publicPath : publicPaths) {
        String pattern = publicPath.trim();
        
        // Pattern con wildcard /**
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        // Match exacto
        else if (pattern.equals(path)) {
            return true;
        }
    }
    return false;
}
```

---

## Configuración

### Identity Service - application.yml

```yaml
jwt:
  secret: ${JWT_SECRET:your-super-secret-jwt-key-change-in-production-MINIMUM-32-CHARACTERS}
  expiration: ${JWT_EXPIRATION:3600000}  # 1 hora

spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

logging:
  level:
    com.unicauca.identity: DEBUG
```

### Gateway Service - application.yml

```yaml
jwt:
  secret: ${JWT_SECRET:your-super-secret-jwt-key-change-in-production-MINIMUM-32-CHARACTERS}
  expiration: 3600000

gateway:
  security:
    enforceRoleCheck: ${ENFORCE_ROLE_CHECK:false}
    publicPaths:
      - /api/identity/**
      - /api/auth/**
      - /api/gateway/health
      - /actuator/health

services:
  identity:
    url: ${IDENTITY_SERVICE_URL:http://identity-service:8081}
  submission:
    url: ${SUBMISSION_SERVICE_URL:http://submission-service:8082}
  # ... otros servicios

logging:
  level:
    co.unicauca.gateway: DEBUG
```

### Variables de Entorno Recomendadas

**Desarrollo**:
```bash
JWT_SECRET=dev-secret-key-minimum-32-characters-long
JWT_EXPIRATION=3600000
ENFORCE_ROLE_CHECK=false
```

**Producción**:
```bash
JWT_SECRET=<generar-con-openssl-rand-base64-32>
JWT_EXPIRATION=1800000  # 30 minutos
ENFORCE_ROLE_CHECK=true
```

---

## Mejores Prácticas

### 1. Seguridad del Secret

✅ **HACER**:
- Usar variables de entorno
- Rotar secrets periódicamente
- Diferente secret por ambiente
- Mínimo 256 bits

❌ **NO HACER**:
- Hardcodear en código
- Commitear en Git
- Reutilizar el mismo secret en múltiples proyectos
- Usar secrets débiles (ej: "secret123")

### 2. Gestión de Expiración

✅ **HACER**:
- Tokens de corta duración (15-60 minutos)
- Implementar refresh tokens
- Validar expiración en cada request
- Loguear expiración de tokens

❌ **NO HACER**:
- Tokens que nunca expiran
- Ignorar ExpiredJwtException
- Confiar en validación solo del cliente

### 3. Claims del Token

✅ **HACER**:
- Incluir solo información necesaria
- Usar claims estándar (sub, iat, exp)
- Claims personalizados descriptivos (userId, rol)
- Validar types de claims

❌ **NO HACER**:
- Incluir passwords o información sensible
- Claims excesivamente grandes
- Modificar claims en el cliente

### 4. Comunicación entre Servicios

✅ **HACER**:
- Validar headers X-User-* obligatorios
- Loguear accesos con contexto de usuario
- Implementar validaciones de negocio adicionales
- Usar @RequestHeader con valores por defecto

❌ **NO HACER**:
- Confiar ciegamente en headers
- Permitir bypass de autenticación
- Exponer endpoints internos públicamente

### 5. Logging y Monitoreo

✅ **HACER**:
```java
log.info("Usuario {} (rol: {}) accedió a {}", userId, role, endpoint);
log.warn("Token inválido para IP: {}", ipAddress);
log.debug("Claims extraídos: {}", claims);
```

❌ **NO HACER**:
```java
log.info("Token recibido: {}", token);  // ¡Expone el token completo!
log.debug("Password: {}", password);    // ¡Nunca loguear passwords!
```

---

## Troubleshooting

### Problema 1: "Token invalid or expired"

**Síntomas**:
- Response 401 Unauthorized
- Mensaje: "Token invalid or expired"

**Causas Comunes**:
1. **Secret diferente entre Identity y Gateway**
   ```bash
   # Verificar configuración
   # Identity Service
   kubectl exec identity-service -- env | grep JWT_SECRET
   
   # Gateway Service
   kubectl exec gateway-service -- env | grep JWT_SECRET
   ```

2. **Token expirado**
   ```java
   // Decodificar token en https://jwt.io
   // Verificar claim "exp" (timestamp Unix)
   long exp = 1702203600;
   Date expirationDate = new Date(exp * 1000);
   System.out.println("Expira: " + expirationDate);
   ```

3. **Formato inválido del token**
   ```http
   # CORRECTO
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...
   
   # INCORRECTO
   Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6...  # Falta "Bearer "
   Authorization: Bearer  eyJhbGciOiJIUzI1NiIsInR5cCI6...  # Espacio extra
   ```

**Solución**:
```java
// Verificar logs del Gateway
co.unicauca.gateway.security.JwtUtils - Firma JWT inválida
co.unicauca.gateway.security.JwtUtils - Token JWT expirado

// Verificar secret
@Value("${jwt.secret}")
private String jwtSecret;

log.info("JWT Secret length: {}", jwtSecret.length());  // Debe ser >= 32
```

---

### Problema 2: "Access denied. Required roles: COORDINADOR"

**Síntomas**:
- Response 403 Forbidden
- Usuario autenticado pero sin permiso

**Causas Comunes**:
1. **Rol del usuario incorrecto**
   ```bash
   # Verificar rol en BD
   SELECT email, rol FROM usuarios WHERE email = 'docente@unicauca.edu.co';
   ```

2. **Claim "rol" no se extrae correctamente**
   ```java
   // En JwtUtils.extractClaims()
   Object rolObj = jwtClaims.get("rol");  // Nota: "rol" sin 'e'
   if (rolObj != null) {
       claims.put("role", String.valueOf(rolObj));  // Se guarda como "role"
   }
   ```

3. **RoleFilter configurado incorrectamente**
   ```yaml
   gateway:
     security:
       enforceRoleCheck: true  # ¿Debe estar activado?
   ```

**Solución**:
```java
// Verificar header X-User-Role en logs
log.info("X-User-Role header: {}", request.getHeaders().getFirst("X-User-Role"));

// Verificar requisitos de rol
List<String> requiredRoles = roleRequirements.get("/api/review/formatoA/*/evaluar");
log.info("Required roles: {}", requiredRoles);  // Debe incluir el rol del usuario
```

---

### Problema 3: "X-User-Id header not found"

**Síntomas**:
- Exception en microservicio downstream
- Header `X-User-Id` no está presente

**Causas Comunes**:
1. **Petición no pasó por Gateway**
   ```bash
   # Verificar que se use la URL del Gateway
   # CORRECTO
   curl http://gateway:8080/api/submissions/formatoA
   
   # INCORRECTO (bypass del Gateway)
   curl http://submission-service:8082/api/submissions/formatoA
   ```

2. **JwtGatewayFilter no se aplicó**
   ```java
   // Verificar RouteConfig
   .route("submission-service", r -> r
       .path("/api/submissions/**")
       .filters(f -> f
           .filter(jwtGatewayFilter.apply(new JwtGatewayFilter.Config())) // Debe estar
       )
       .uri(submissionServiceUrl))
   ```

3. **Header requerido pero opcional en endpoint**
   ```java
   // INCORRECTO
   @RequestHeader(value = "X-User-Id", required = false) Long userId
   
   // CORRECTO
   @RequestHeader("X-User-Id") Long userId
   ```

**Solución**:
```java
// Debugging en Gateway
log.info("Headers añadidos: X-User-Id={}, X-User-Role={}", 
    claims.get("userId"), claims.get("role"));

// Debugging en Microservicio
@PostMapping("/test")
public ResponseEntity<?> test(HttpServletRequest request) {
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        log.info("{}: {}", headerName, request.getHeader(headerName));
    }
    return ResponseEntity.ok("Check logs");
}
```

---

### Problema 4: Token válido pero claims vacías

**Síntomas**:
- Token valida correctamente
- Pero `extractClaims()` retorna Map vacío

**Causas Comunes**:
1. **Claims no se incluyeron al generar token**
   ```java
   // Verificar JwtTokenProvider.generateToken()
   return Jwts.builder()
           .subject(user.getEmail())
           .claim("userId", user.getId())      // ¿Presente?
           .claim("rol", user.getRol())        // ¿Presente?
           .claim("programa", user.getPrograma())
           .signWith(getSigningKey())
           .compact();
   ```

2. **Nombre de claim incorrecto**
   ```java
   // En generación (Identity Service)
   .claim("rol", user.getRol())  // "rol" sin 'e'
   
   // En extracción (Gateway)
   Object rolObj = jwtClaims.get("role");  // ❌ INCORRECTO: "role" con 'e'
   Object rolObj = jwtClaims.get("rol");   // ✅ CORRECTO: "rol" sin 'e'
   ```

**Solución**:
```java
// Decodificar token manualmente
Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

log.info("All claims: {}", claims);  // Ver todos los claims presentes
claims.forEach((key, value) -> 
    log.info("Claim: {} = {}", key, value)
);
```

---

## Resumen

### Ventajas de la Gestión Manual de JWT

✅ **Control Total**: Completo control sobre generación, validación y claims  
✅ **Sin Dependencias**: No requiere OAuth2, Keycloak, Auth0  
✅ **Personalización**: Claims personalizados según necesidades  
✅ **Simplicidad**: Lógica directa y fácil de debuggear  
✅ **Performance**: Sin overhead de frameworks pesados  

### Desventajas

❌ **Mantenimiento**: Código custom que debes mantener  
❌ **Seguridad**: Responsabilidad completa de implementar buenas prácticas  
❌ **Features**: No incluye refresh tokens, revocación, etc. (debes implementarlos)  
❌ **Escalabilidad**: Secret compartido puede ser limitante  

### Cuándo Usar Gestión Manual

✅ Proyectos académicos o prototipos  
✅ Microservicios internos con necesidades simples  
✅ Cuando necesitas control granular de claims  
✅ Equipos con conocimiento profundo de JWT  

### Cuándo Usar Solución Managed

✅ Producción de alta escala  
✅ Múltiples aplicaciones cliente (web, móvil, desktop)  
✅ Requisitos complejos (SSO, federación, MFA)  
✅ Equipos sin experiencia en seguridad  

---

## Siguientes Pasos Recomendados

### Mejoras de Seguridad

1. **Implementar Refresh Tokens**
   - Access token: 15 minutos
   - Refresh token: 7 días
   - Endpoint: `POST /api/auth/refresh`

2. **Blacklist de Tokens**
   - Redis para tokens revocados
   - Validación en Gateway

3. **Rotación de Secrets**
   - Multiple secrets activos
   - Migración gradual

4. **Rate Limiting por Usuario**
   - Basado en `userId` del token
   - Prevenir abuso

### Mejoras Funcionales

1. **Auditoría de Accesos**
   - Loguear todos los accesos con userId
   - Almacenar en base de datos

2. **Analytics de Seguridad**
   - Dashboard de tokens generados
   - Alertas de intentos fallidos

3. **Gestión de Sesiones**
   - Múltiples dispositivos por usuario
   - Logout remoto

4. **Permisos Granulares**
   - Más allá de roles
   - Permisos a nivel de recurso

---

## Referencias

- [JWT.io](https://jwt.io) - Decodificar y validar tokens
- [JJWT GitHub](https://github.com/jwtk/jjwt) - Librería Java JWT
- [Spring Cloud Gateway Docs](https://spring.io/projects/spring-cloud-gateway)
- [RFC 7519 - JWT Specification](https://datatracker.ietf.org/doc/html/rfc7519)

---

**Autor**: GitHub Copilot  
**Fecha**: Diciembre 10, 2025  
**Versión**: 1.0.0  
**Proyecto**: GesTrabajoGrado-Microservicios


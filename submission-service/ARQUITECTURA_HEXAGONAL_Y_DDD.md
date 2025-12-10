# Arquitectura Hexagonal y Domain-Driven Design en Submission Service

**Fecha:** Diciembre 10, 2025  
**Versión:** 1.0.0  
**Microservicio:** Submission Service

---

## Tabla de Contenidos

1. [Contextualización - Arquitectura Hexagonal](#1-contextualización---arquitectura-hexagonal)
2. [Implementación de Arquitectura Hexagonal](#2-implementación-de-arquitectura-hexagonal)
3. [Contextualización - Domain-Driven Design](#3-contextualización---domain-driven-design)
4. [Implementación de Domain-Driven Design](#4-implementación-de-domain-driven-design)
5. [Por Qué Es Necesaria Esta Implementación](#5-por-qué-es-necesaria-esta-implementación)
6. [Cómo Trabajan en Conjunto Hexagonal y DDD](#6-cómo-trabajan-en-conjunto-hexagonal-y-ddd)

---

## 1. Contextualización - Arquitectura Hexagonal

La **Arquitectura Hexagonal**, también conocida como **Arquitectura de Puertos y Adaptadores**, fue propuesta por Alistair Cockburn en 2005. Esta arquitectura busca crear sistemas de software que sean independientes de sus mecanismos de entrega (UI, bases de datos, frameworks externos) y que puedan ser fácilmente probados de manera aislada.

### Principios Fundamentales

La arquitectura hexagonal se basa en tres principios clave:

**1. Inversión de Dependencias**: El núcleo del negocio (dominio) no debe depender de detalles de implementación como bases de datos, frameworks o APIs externas. En su lugar, estos detalles deben depender del dominio a través de interfaces (puertos).

**2. Separación de Responsabilidades**: El sistema se divide en capas claramente diferenciadas donde cada una tiene una responsabilidad específica y bien definida. El dominio contiene la lógica de negocio pura, la aplicación orquesta los casos de uso, y la infraestructura maneja los detalles técnicos.

**3. Testabilidad**: Al aislar la lógica de negocio de los detalles de implementación, se facilita la creación de pruebas unitarias sin necesidad de levantar bases de datos, servidores o dependencias externas.

### Conceptos Clave

**Puertos**: Son interfaces que definen contratos de entrada (input ports) o salida (output ports) del núcleo de la aplicación. Los puertos de entrada representan los casos de uso que la aplicación expone, mientras que los puertos de salida definen servicios que la aplicación necesita del mundo exterior.

**Adaptadores**: Son implementaciones concretas de los puertos. Los adaptadores de entrada reciben peticiones del mundo exterior y las traducen a llamadas del dominio, mientras que los adaptadores de salida traducen las necesidades del dominio a tecnologías específicas (JPA, REST clients, message brokers).

**Núcleo del Dominio**: Es el corazón de la aplicación que contiene toda la lógica de negocio. Este núcleo es completamente agnóstico de frameworks, bases de datos y protocolos de comunicación.

### Estructura de Capas

La arquitectura hexagonal organiza el código en tres capas concéntricas, donde las dependencias siempre apuntan hacia adentro:

**Capa de Dominio (Centro)**: Contiene las entidades, value objects, aggregates, domain events y reglas de negocio puras. No tiene dependencias externas y representa el lenguaje ubicuo del negocio.

**Capa de Aplicación (Intermedia)**: Contiene los casos de uso que orquestan la lógica de dominio. Define los puertos (interfaces) que conectan con el mundo exterior y coordina las operaciones del dominio sin contener lógica de negocio en sí misma.

**Capa de Infraestructura (Externa)**: Contiene todos los adaptadores que implementan los puertos definidos en la capa de aplicación. Aquí se encuentran los controllers REST, repositorios JPA, clientes HTTP, publishers de eventos y cualquier integración con tecnologías específicas.

---

## 2. Implementación de Arquitectura Hexagonal

El Submission Service implementa arquitectura hexagonal siguiendo una estructura de directorios que refleja claramente la separación en las tres capas mencionadas. Esta organización se encuentra en el directorio base:

```
submission-service/src/main/java/co/unicauca/submission/
```

### 2.1 Estructura de Directorios

La estructura del proyecto refleja directamente las tres capas de la arquitectura hexagonal:

```
co/unicauca/submission/
├── domain/                    # CAPA DE DOMINIO (núcleo)
│   ├── model/                 # Entidades y Value Objects
│   ├── event/                 # Domain Events
│   ├── exception/             # Excepciones de dominio
│   ├── repository/            # Interfaces de repositorio (puerto)
│   ├── service/               # Servicios de dominio
│   └── specification/         # Specifications (reglas de negocio)
│
├── application/               # CAPA DE APLICACIÓN (orquestación)
│   ├── dto/                   # Data Transfer Objects
│   │   ├── request/           # DTOs de entrada
│   │   └── response/          # DTOs de salida
│   ├── port/                  # PUERTOS
│   │   ├── in/                # Puertos de entrada (casos de uso)
│   │   └── out/               # Puertos de salida (servicios externos)
│   └── usecase/               # Implementación de casos de uso
│       ├── formatoa/          # Casos de uso de Formato A
│       ├── anteproyecto/      # Casos de uso de Anteproyecto
│       └── query/             # Casos de uso de consulta
│
└── infrastructure/            # CAPA DE INFRAESTRUCTURA (adaptadores)
    ├── adapter/               # ADAPTADORES
    │   ├── in/                # Adaptadores de entrada
    │   │   └── rest/          # Controllers REST
    │   └── out/               # Adaptadores de salida
    │       ├── persistence/   # Adaptador JPA
    │       ├── messaging/     # Adaptador RabbitMQ
    │       ├── filesystem/    # Adaptador almacenamiento archivos
    │       └── client/        # Clientes HTTP
    ├── config/                # Configuración de Spring
    └── mapper/                # Mappers entre capas
```

Esta estructura garantiza que las dependencias fluyan siempre desde la infraestructura hacia el dominio, nunca en sentido contrario.

### 2.2 Capa de Dominio

La capa de dominio es el corazón del microservicio y se encuentra completamente aislada de cualquier tecnología o framework externo. Esta capa contiene únicamente código Java puro sin anotaciones de Spring, JPA o cualquier otra librería externa.

#### Directorio: `domain/model/`

Este directorio contiene las entidades y value objects del dominio. El archivo más importante es:

**`Proyecto.java`** - Aggregate Root principal que encapsula todo el ciclo de vida de un proyecto de grado. Este archivo contiene aproximadamente 500 líneas de código y representa la entidad central del sistema. El Proyecto encapsula tanto la información del Formato A como del Anteproyecto y gestiona todas las transiciones de estado.

La clase Proyecto es responsable de:
- Crear proyectos con su Formato A inicial
- Gestionar el ciclo de hasta 3 reenvíos del Formato A
- Controlar las transiciones de estado según las evaluaciones recibidas
- Validar las reglas de negocio antes de cada operación
- Generar eventos de dominio cuando ocurren cambios importantes
- Mantener la consistencia del aggregate en todo momento

**Value Objects implementados**:

`Titulo.java` - Encapsula el título del proyecto con sus reglas de validación (mínimo 10 caracteres, máximo 500 caracteres). Este value object es inmutable y garantiza que nunca exista un proyecto con un título inválido.

`Participantes.java` - Representa a todos los participantes del proyecto (director, codirector opcional, estudiante1 obligatorio, estudiante2 opcional). Contiene métodos de consulta como `esDirector(userId)` y `esEstudiante(userId)` que encapsulan la lógica de verificación de roles.

`ObjetivosProyecto.java` - Agrupa el objetivo general y los objetivos específicos (mínimo 1 requerido). Valida que siempre exista al menos un objetivo específico y que ninguno esté vacío.

`ArchivoAdjunto.java` - Representa un archivo PDF con su ruta y nombre. Valida que solo se acepten archivos con extensión PDF.

`ProyectoId.java` - Value object que representa el identificador único del proyecto, encapsulando el Long ID en un tipo con significado semántico del dominio.

**Enums del dominio**:

`EstadoProyecto.java` - Define todos los estados posibles del ciclo de vida: FORMATO_A_DILIGENCIADO, EN_EVALUACION_COORDINADOR, CORRECCIONES_SOLICITADAS, FORMATO_A_APROBADO, ANTEPROYECTO_ENVIADO, ANTEPROYECTO_EN_EVALUACION, RECHAZADO.

`Modalidad.java` - Define las modalidades de proyecto (INVESTIGACION, PRACTICA_PROFESIONAL, EMPRENDIMIENTO) con un método `requiereCarta()` que indica si necesita carta de aceptación.

**Entities internas del Aggregate**:

`FormatoAInfo.java` - Entidad que encapsula toda la información específica del Formato A: número de intento actual, máximo de intentos permitidos, archivos PDF adjuntos, y el historial de evaluaciones recibidas.

`AnteproyectoInfo.java` - Entidad que gestiona la información del anteproyecto: archivo PDF, evaluadores asignados, y el historial de evaluaciones.

`Evaluacion.java` - Representa una evaluación recibida con su resultado (APROBADO/RECHAZADO), observaciones, evaluador y fecha.

#### Directorio: `domain/event/`

Los eventos de dominio son cruciales en la arquitectura hexagonal ya que permiten la comunicación asíncrona sin acoplar el dominio a infraestructura. Este directorio contiene:

`DomainEvent.java` - Interfaz marcador que todos los eventos deben implementar.

`FormatoACreado.java` - Evento generado cuando se crea la primera versión del Formato A.

`FormatoAReenviado.java` - Evento generado cuando se reenvía una versión 2 o 3 del Formato A.

`FormatoAEvaluado.java` - Evento generado cuando el coordinador evalúa el Formato A.

`AnteproyectoSubido.java` - Evento generado cuando se sube el anteproyecto.

`EvaluadoresAsignados.java` - Evento generado cuando se asignan evaluadores al anteproyecto.

`AnteproyectoEvaluado.java` - Evento generado cuando un evaluador emite su evaluación.

Estos eventos se registran en el aggregate Proyecto y son extraídos y publicados por la capa de aplicación después de persistir los cambios, garantizando consistencia eventual.

#### Directorio: `domain/specification/`

Las Specifications implementan el patrón Specification de Eric Evans para encapsular reglas de negocio complejas que determinan si un objeto satisface ciertos criterios. Este directorio contiene:

`Specification.java` - Interfaz genérica que define el contrato: `boolean isSatisfiedBy(T entity)` y `String getRazonRechazo(T entity)`.

`PuedeReenviarFormatoASpec.java` - Especificación que verifica si un proyecto puede reenviar su Formato A. Valida que esté en estado CORRECCIONES_SOLICITADAS y no haya alcanzado el máximo de 3 intentos.

`PuedeSubirAnteproyectoSpec.java` - Especificación que verifica si un proyecto puede subir anteproyecto. Valida que el Formato A esté aprobado y que aún no se haya subido un anteproyecto.

`EsDirectorDelProyectoSpec.java` - Especificación que verifica si un usuario es el director del proyecto, utilizada para validar permisos en operaciones sensibles.

Estas specifications son utilizadas por los casos de uso antes de ejecutar operaciones críticas, separando la lógica de validación de la lógica de ejecución.

#### Directorio: `domain/exception/`

Las excepciones de dominio representan violaciones de reglas de negocio y son lanzadas por el propio dominio cuando se intenta realizar una operación inválida. Algunas excepciones clave son:

`ProyectoNotFoundException.java` - Lanzada cuando se busca un proyecto que no existe.

`MaximoIntentosAlcanzadoException.java` - Lanzada cuando se intenta reenviar un Formato A que ya agotó sus 3 intentos.

`EstadoProyectoInvalidoException.java` - Lanzada cuando se intenta una operación en un estado que no lo permite.

`CartaAceptacionRequeridaException.java` - Lanzada cuando se crea un proyecto de práctica profesional sin carta de aceptación.

`UsuarioNoAutorizadoException.java` - Lanzada cuando un usuario intenta realizar una operación para la cual no tiene permisos.

### 2.3 Capa de Aplicación

La capa de aplicación orquesta los casos de uso del sistema sin contener lógica de negocio propia. Su responsabilidad es coordinar las operaciones del dominio, gestionar transacciones y publicar eventos.

#### Directorio: `application/port/in/`

Los puertos de entrada definen los casos de uso que expone el microservicio. Cada interfaz representa una operación de negocio específica:

`ICrearFormatoAUseCase.java` - Define el contrato para crear un proyecto con su Formato A inicial. Método principal: `ProyectoResponse crear(CrearFormatoARequest request, Long userId)`.

`IReenviarFormatoAUseCase.java` - Define el contrato para reenviar una nueva versión del Formato A. Método: `ProyectoResponse reenviar(ReenviarFormatoARequest request, Long proyectoId, Long userId)`.

`IEvaluarFormatoAUseCase.java` - Define el contrato para que el coordinador evalúe el Formato A. Método: `ProyectoResponse evaluar(EvaluarFormatoARequest request, Long proyectoId, Long coordinadorId)`.

`ISubirAnteproyectoUseCase.java` - Define el contrato para subir el anteproyecto. Método: `ProyectoResponse subir(SubirAnteproyectoRequest request, Long proyectoId, Long userId)`.

`IObtenerProyectoQuery.java` - Define consultas de lectura para obtener proyectos. Método: `ProyectoResponse obtenerPorId(Long proyectoId)`.

Estas interfaces siguen el principio de Segregación de Interfaces (ISP) donde cada puerto tiene una única responsabilidad claramente definida.

#### Directorio: `application/port/out/`

Los puertos de salida definen las dependencias que la aplicación necesita del mundo exterior. El dominio no conoce estas interfaces, pero la aplicación las utiliza para coordinar operaciones:

`IProyectoRepositoryPort.java` - Define operaciones de persistencia: `save()`, `findById()`, `findByEstado()`, `findByDirectorId()`, etc. Esta interfaz será implementada por el adaptador JPA en la capa de infraestructura.

`IFileStoragePort.java` - Define operaciones de almacenamiento de archivos: `guardarArchivo()`, `obtenerArchivo()`, `eliminarArchivo()`. Permite cambiar el mecanismo de almacenamiento sin afectar la lógica de negocio.

`IEventPublisherPort.java` - Define la operación de publicación de eventos: `publish(DomainEvent event)`. Abstraer la publicación de eventos permite cambiar de RabbitMQ a Kafka sin modificar casos de uso.

`IIdentityServicePort.java` - Define operaciones de consulta al servicio de identidad: `tieneRol()`, `obtenerUsuario()`, `obtenerCoordinador()`. Permite obtener información de usuarios sin acoplar a REST o gRPC.

`INotificationPort.java` - Define operaciones de notificación: `notificarNuevoFormatoA()`, `notificarEvaluacion()`. Permite enviar notificaciones sin conocer el mecanismo (email, SMS, push).

Estos puertos implementan el principio de Inversión de Dependencias donde la aplicación define lo que necesita y la infraestructura lo implementa.

#### Directorio: `application/usecase/`

Los casos de uso son las implementaciones concretas de los puertos de entrada. Cada caso de uso coordina una operación de negocio completa siguiendo este flujo general:

1. Validar precondiciones y permisos del usuario
2. Interactuar con puertos de salida para obtener datos necesarios
3. Ejecutar la lógica de dominio (delegar al aggregate)
4. Persistir los cambios en el aggregate
5. Extraer y publicar eventos de dominio
6. Retornar el resultado mediante DTOs

**`formatoa/CrearFormatoAUseCase.java`** - Implementa la creación del Formato A. Este caso de uso:
- Valida que el usuario tenga rol DOCENTE mediante el puerto IIdentityServicePort
- Guarda los archivos PDF mediante el puerto IFileStoragePort
- Crea el aggregate Proyecto utilizando el factory method del dominio
- Persiste el proyecto mediante IProyectoRepositoryPort
- Extrae los eventos de dominio del aggregate y los publica mediante IEventPublisherPort
- Notifica al coordinador mediante INotificationPort
- Retorna un ProyectoResponse con toda la información del proyecto creado

**`formatoa/ReenviarFormatoAUseCase.java`** - Implementa el reenvío del Formato A. Este caso de uso valida mediante la specification `PuedeReenviarFormatoASpec` que el proyecto esté en el estado correcto y no haya agotado sus intentos antes de permitir la operación.

**`formatoa/EvaluarFormatoAUseCase.java`** - Implementa la evaluación por parte del coordinador. Valida que el usuario sea COORDINADOR y delega la lógica de evaluación al método `evaluarFormatoA()` del aggregate Proyecto.

**`anteproyecto/SubirAnteproyectoUseCase.java`** - Implementa la subida del anteproyecto. Utiliza la specification `PuedeSubirAnteproyectoSpec` para verificar que el Formato A esté aprobado antes de permitir la operación.

**`query/ObtenerProyectoQuery.java`** - Implementa consultas de lectura sin modificar el estado del sistema, siguiendo el principio CQS (Command Query Separation).

Todos los casos de uso están anotados con `@Service` y `@Transactional` para garantizar que las operaciones sean atómicas y consistentes.

#### Directorio: `application/dto/`

Los DTOs (Data Transfer Objects) actúan como contratos de comunicación entre las capas, evitando exponer directamente las entidades de dominio:

**Request DTOs** (`dto/request/`):
- `CrearFormatoARequest.java` - Contiene título, modalidad, objetivos, participantes, streams de archivos PDF
- `ReenviarFormatoARequest.java` - Contiene los nuevos archivos PDF para la versión mejorada
- `EvaluarFormatoARequest.java` - Contiene aprobado (boolean) y comentarios del coordinador
- `SubirAnteproyectoRequest.java` - Contiene el stream del PDF del anteproyecto

**Response DTOs** (`dto/response/`):
- `ProyectoResponse.java` - DTO completo con 23 campos que representa el estado completo de un proyecto, incluyendo información de Formato A, Anteproyecto, participantes y estado actual

Los DTOs utilizan Jakarta Bean Validation (`@NotNull`, `@NotBlank`, `@Size`) para validar la entrada antes de llegar a la lógica de negocio.

### 2.4 Capa de Infraestructura

La capa de infraestructura contiene todas las implementaciones concretas de los puertos definidos en la aplicación. Esta capa conoce frameworks, bases de datos y protocolos específicos.

#### Directorio: `infrastructure/adapter/in/rest/`

Los adaptadores de entrada REST traducen peticiones HTTP a llamadas de casos de uso:

**`FormatoAController.java`** - Controller REST que expone endpoints para operaciones de Formato A:
- `POST /api/submissions/formatoA` - Recibe multipart/form-data con PDF y JSON, invoca ICrearFormatoAUseCase
- `POST /api/submissions/formatoA/{id}/reenviar` - Recibe nuevos PDFs, invoca IReenviarFormatoAUseCase
- `PATCH /api/submissions/formatoA/{id}/evaluar` - Recibe decisión de evaluación, invoca IEvaluarFormatoAUseCase
- `GET /api/submissions/formatoA/{id}` - Consulta proyecto, invoca IObtenerProyectoQuery

El controller extrae los headers `X-User-Id` y `X-User-Role` inyectados por el Gateway y los pasa a los casos de uso para validación de permisos.

**`AnteproyectoController.java`** - Controller para operaciones de anteproyecto:
- `POST /api/submissions/anteproyecto/{proyectoId}` - Sube anteproyecto
- `POST /api/submissions/anteproyecto/{proyectoId}/evaluadores` - Asigna evaluadores

**`SubmissionController.java`** - Controller para consultas generales:
- `GET /api/submissions/{id}` - Obtiene proyecto por ID
- `GET /api/submissions` - Lista proyectos con paginación
- `GET /api/submissions/estudiante/{id}` - Proyectos de un estudiante
- `GET /api/submissions/director/{id}` - Proyectos de un director

Estos controllers están anotados con `@RestController`, `@RequestMapping` y utilizan `@Operation` de Swagger para documentación automática.

#### Directorio: `infrastructure/adapter/out/persistence/`

El adaptador de persistencia implementa el puerto IProyectoRepositoryPort usando Spring Data JPA:

**`ProyectoRepositoryAdapter.java`** - Adaptador que traduce entre el dominio y JPA:
```java
@Component
public class ProyectoRepositoryAdapter implements IProyectoRepositoryPort {
    private final ProyectoJpaRepository jpaRepository;
    private final ProyectoMapper mapper;
    
    public Proyecto save(Proyecto proyecto) {
        ProyectoEntity entity = mapper.toEntity(proyecto);
        ProyectoEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
```

Este adaptador utiliza el patrón Repository de Spring Data y un mapper para convertir entre objetos de dominio (Proyecto) y entidades JPA (ProyectoEntity).

**`ProyectoJpaRepository.java`** - Interfaz de Spring Data JPA que extiende JpaRepository:
```java
public interface ProyectoJpaRepository extends JpaRepository<ProyectoEntity, Long> {
    List<ProyectoEntity> findByEstado(EstadoProyecto estado);
    List<ProyectoEntity> findByDirectorId(Long directorId);
    List<ProyectoEntity> findByEstudiante1IdOrEstudiante2Id(Long est1, Long est2);
}
```

**`entity/ProyectoEntity.java`** - Entidad JPA anotada con `@Entity`, `@Table`, `@Id`, etc. Esta entidad es un detalle de implementación de la capa de infraestructura y nunca se expone al dominio.

**`mapper/ProyectoMapper.java`** - Mapper que convierte entre Proyecto (dominio) y ProyectoEntity (infraestructura), garantizando que el dominio nunca conozca detalles de JPA.

#### Directorio: `infrastructure/adapter/out/messaging/`

El adaptador de mensajería implementa el puerto IEventPublisherPort usando RabbitMQ:

**`RabbitMQEventPublisher.java`** - Publica eventos de dominio a RabbitMQ:
```java
@Component
public class RabbitMQEventPublisher implements IEventPublisherPort {
    private final RabbitTemplate rabbitTemplate;
    
    public void publish(DomainEvent event) {
        String exchange = determinarExchange(event);
        String routingKey = determinarRoutingKey(event);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
```

Este adaptador traduce eventos de dominio a mensajes de RabbitMQ, enriqueciendo los eventos con información adicional de participantes obtenida del servicio de identidad.

**`EventEnricherService.java`** - Enriquece eventos con información de usuarios (nombres, emails) consultando el Identity Service antes de publicar.

**`RabbitMQConfig.java`** - Configuración de exchanges, queues y bindings de RabbitMQ.

#### Directorio: `infrastructure/adapter/out/filesystem/`

El adaptador de almacenamiento de archivos implementa IFileStoragePort:

**`LocalFileStorageAdapter.java`** - Implementación que guarda archivos en el sistema de archivos local:
```java
@Component
public class LocalFileStorageAdapter implements IFileStoragePort {
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    public String guardarArchivo(InputStream inputStream, String directorio, String nombreArchivo) {
        Path targetPath = Paths.get(uploadDir, directorio, nombreArchivo);
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toString();
    }
}
```

Esta implementación podría ser fácilmente reemplazada por un adaptador que use S3, Azure Blob Storage o cualquier otro sistema de almacenamiento sin modificar el dominio o la aplicación.

#### Directorio: `infrastructure/adapter/out/client/`

Los clientes HTTP implementan puertos que requieren comunicación con otros microservicios:

**`IdentityServiceClient.java`** - Implementa IIdentityServicePort usando WebClient:
```java
@Component
public class IdentityServiceClient implements IIdentityServicePort {
    private final WebClient webClient;
    
    public boolean tieneRol(Long userId, String rol) {
        UserDTO user = webClient.get()
            .uri("/api/auth/users/{id}", userId)
            .retrieve()
            .bodyToMono(UserDTO.class)
            .block();
        return user.getRol().equals(rol);
    }
}
```

Este adaptador traduce llamadas del puerto a peticiones HTTP REST al Identity Service, manejando errores de red, timeouts y circuit breakers.

### 2.5 Flujo Completo de una Operación

Para ilustrar cómo todas las capas trabajan juntas, analicemos el flujo completo de crear un Formato A:

**1. Petición HTTP llega al Controller (Adaptador de Entrada)**
```
POST /api/submissions/formatoA
Content-Type: multipart/form-data
Authorization: Bearer <jwt>
X-User-Id: 12
X-User-Role: DOCENTE

Files:
- pdf: formatoA.pdf
- data: { titulo: "...", modalidad: "INVESTIGACION", ... }
```

**2. Controller extrae headers y delega al Use Case**
```java
@PostMapping
public ResponseEntity<ProyectoResponse> crear(
    @RequestParam("pdf") MultipartFile pdf,
    @RequestParam("data") String jsonData,
    @RequestHeader("X-User-Id") Long userId) {
    
    CrearFormatoARequest request = objectMapper.readValue(jsonData, CrearFormatoARequest.class);
    request.setPdfStream(pdf.getInputStream());
    
    ProyectoResponse response = crearUseCase.crear(request, userId);
    return ResponseEntity.status(201).body(response);
}
```

**3. Use Case valida permisos usando puerto IIdentityServicePort**
```java
if (!identityServicePort.tieneRol(userId, "DOCENTE")) {
    throw new UsuarioNoAutorizadoException("crear Formato A", "DOCENTE");
}
```

**4. Use Case guarda archivo usando puerto IFileStoragePort**
```java
String rutaPdf = fileStoragePort.guardarArchivo(
    request.getPdfStream(),
    "proyectos/formatoA/" + userId,
    "formatoA_v1.pdf"
);
```

**5. Use Case crea el Aggregate usando factory method del dominio**
```java
Proyecto proyecto = Proyecto.crearConFormatoA(
    Titulo.of(request.getTitulo()),
    Modalidad.valueOf(request.getModalidad()),
    ObjetivosProyecto.of(request.getObjetivoGeneral(), request.getObjetivosEspecificos()),
    Participantes.of(userId, request.getCodirectorId(), request.getEstudiante1Id(), request.getEstudiante2Id()),
    ArchivoAdjunto.of(rutaPdf, "formatoA_v1.pdf"),
    cartaAceptacion
);
```

**6. Dominio valida reglas de negocio y registra evento**
```java
// Dentro de Proyecto.crearConFormatoA()
if (modalidad.requiereCarta() && cartaAceptacion == null) {
    throw new CartaAceptacionRequeridaException(modalidad.name());
}

this.registrarEvento(new FormatoACreado(this.id, this.titulo, ...));
```

**7. Use Case persiste usando puerto IProyectoRepositoryPort**
```java
Proyecto proyectoGuardado = repositoryPort.save(proyecto);
```

**8. Adaptador de persistencia traduce a JPA y guarda**
```java
ProyectoEntity entity = mapper.toEntity(proyecto);
ProyectoEntity savedEntity = jpaRepository.save(entity);
return mapper.toDomain(savedEntity);
```

**9. Use Case extrae eventos y los publica usando puerto IEventPublisherPort**
```java
List<DomainEvent> eventos = proyectoGuardado.obtenerEventos();
eventos.forEach(evento -> eventPublisherPort.publish(evento));
```

**10. Adaptador de mensajería publica a RabbitMQ**
```java
rabbitTemplate.convertAndSend("formato-a-exchange", "formato-a.enviado", evento);
```

**11. Use Case retorna Response DTO**
```java
return ProyectoResponse.fromDomain(proyectoGuardado);
```

**12. Controller retorna HTTP 201 Created con el JSON del proyecto**

Este flujo completo muestra cómo cada capa tiene su responsabilidad específica y cómo las dependencias siempre apuntan hacia el dominio, nunca hacia afuera.

---

## 3. Contextualización - Domain-Driven Design

**Domain-Driven Design (DDD)** es un enfoque de desarrollo de software propuesto por Eric Evans en su libro "Domain-Driven Design: Tackling Complexity in the Heart of Software" publicado en 2003. DDD propone que el diseño del software debe estar impulsado por el dominio del negocio y que el código debe reflejar fielmente el lenguaje y los conceptos utilizados por los expertos del dominio.

### Principios Fundamentales de DDD

**Lenguaje Ubicuo (Ubiquitous Language)**: El equipo de desarrollo y los expertos del dominio deben utilizar un vocabulario común que se refleje tanto en las conversaciones como en el código. Términos como "Proyecto", "Formato A", "Anteproyecto", "Evaluación" y "Director" deben significar exactamente lo mismo para desarrolladores y usuarios del negocio.

**Modelo de Dominio Rico**: En lugar de tener objetos anémicos que solo contienen datos y getters/setters, el modelo de dominio debe encapsular el comportamiento y las reglas de negocio. Las entidades no solo almacenan estado, sino que también contienen la lógica para modificar ese estado de manera válida.

**Bounded Contexts**: Cada microservicio representa un contexto delimitado con su propio modelo de dominio. En nuestro caso, el Submission Service tiene su propio concepto de "Proyecto" que puede diferir del concepto en otros servicios. Los bounded contexts previenen que un modelo se vuelva demasiado complejo intentando satisfacer todas las necesidades.

**Separación de Responsabilidades**: DDD distingue claramente entre entidades (objetos con identidad), value objects (objetos sin identidad definidos por sus atributos), aggregates (clusters de entidades y value objects), servicios de dominio (operaciones que no pertenecen a una entidad específica) y domain events (hechos importantes del dominio).

### Tactical Patterns de DDD

**Entities (Entidades)**: Objetos que tienen una identidad única que persiste en el tiempo. La identidad es lo que define a una entidad, no sus atributos. En nuestro caso, Proyecto es una entidad porque cada proyecto tiene un ID único que lo identifica, independientemente de que su título o participantes cambien.

**Value Objects**: Objetos inmutables definidos por sus atributos. Dos value objects con los mismos atributos son considerados iguales. Por ejemplo, dos objetos Titulo con el mismo texto son intercambiables. Los value objects no tienen identidad propia y son más económicos de crear y destruir que las entidades.

**Aggregates**: Cluster de objetos de dominio (entidades y value objects) tratados como una unidad para cambios de datos. Cada aggregate tiene una raíz (aggregate root) que es la única entidad a través de la cual objetos externos pueden referenciar miembros internos del aggregate. Esto garantiza la consistencia del aggregate.

**Domain Events**: Objetos que representan algo significativo que ocurrió en el dominio. Los eventos son inmutables y describen acciones pasadas. Permiten que diferentes partes del sistema reaccionen a cambios sin acoplamiento directo.

**Repositories**: Proporcionan la ilusión de una colección en memoria de aggregates. El repository encapsula la lógica de acceso a datos y permite que el dominio trabaje con objetos sin conocer detalles de persistencia.

**Specifications**: Encapsulan reglas de negocio complejas que determinan si un objeto satisface ciertos criterios. Las specifications son reutilizables y componibles, permitiendo expresar reglas de negocio de manera declarativa.

**Domain Services**: Operaciones de dominio que no pertenecen naturalmente a ninguna entidad o value object. Los servicios de dominio no tienen estado y encapsulan lógica de negocio que involucra múltiples aggregates.

---

## 4. Implementación de Domain-Driven Design

El Submission Service implementa los patrones tácticos de DDD de manera rigurosa en la capa de dominio, creando un modelo rico que expresa las reglas del negocio de gestión de trabajos de grado.

### 4.1 Aggregate Root: Proyecto

El corazón de la implementación DDD es el aggregate Proyecto, definido en:

```
domain/model/Proyecto.java
```

Este aggregate root encapsula toda la lógica relacionada con el ciclo de vida de un proyecto de grado. El aggregate Proyecto garantiza la consistencia transaccional de todos sus componentes internos (FormatoAInfo, AnteproyectoInfo, Evaluaciones).

#### Responsabilidades del Aggregate Root

**1. Control de Acceso**: Solo el aggregate root (Proyecto) puede ser obtenido directamente mediante un repository. Los objetos internos como FormatoAInfo y AnteproyectoInfo solo son accesibles a través del Proyecto, garantizando que todas las modificaciones pasen por el aggregate root.

**2. Invariantes del Aggregate**: El Proyecto es responsable de mantener todas las invariantes de negocio. Por ejemplo, garantiza que:
- Un proyecto no puede tener más de 3 intentos de Formato A
- No se puede subir anteproyecto si el Formato A no está aprobado
- Solo el director puede reenviar el Formato A
- Las transiciones de estado son válidas según las reglas del negocio

**3. Generación de Domain Events**: Cuando ocurren cambios importantes en el aggregate, el Proyecto registra eventos de dominio que serán publicados después de la transacción. Estos eventos notifican a otras partes del sistema sobre cambios significativos.

#### Factory Methods del Aggregate

El aggregate Proyecto no expone constructores públicos. En su lugar, utiliza factory methods estáticos que expresan intenciones de negocio claras:

```java
public static Proyecto crearConFormatoA(
    Titulo titulo,
    Modalidad modalidad,
    ObjetivosProyecto objetivos,
    Participantes participantes,
    ArchivoAdjunto pdfFormatoA,
    ArchivoAdjunto cartaAceptacion
) {
    // Validación de regla de negocio
    if (modalidad.requiereCarta() && cartaAceptacion == null) {
        throw new CartaAceptacionRequeridaException(modalidad.name());
    }
    
    // Crear proyecto en estado inicial válido
    Proyecto proyecto = new Proyecto();
    proyecto.titulo = titulo;
    proyecto.modalidad = modalidad;
    proyecto.objetivos = objetivos;
    proyecto.participantes = participantes;
    proyecto.estado = EstadoProyecto.FORMATO_A_DILIGENCIADO;
    proyecto.formatoA = new FormatoAInfo(1, pdfFormatoA, cartaAceptacion);
    
    // Registrar evento de dominio
    proyecto.registrarEvento(new FormatoACreado(...));
    
    return proyecto;
}
```

Este factory method garantiza que nunca se pueda crear un Proyecto en un estado inválido. La validación de la carta de aceptación es una regla de negocio que está encapsulada en el dominio.

#### Métodos de Comportamiento del Aggregate

El aggregate expone métodos que representan operaciones de negocio, no simples setters:

**`reenviarFormatoA()`**: Método que encapsula la lógica de reenvío del Formato A
```java
public void reenviarFormatoA(ArchivoAdjunto nuevoPdf, ArchivoAdjunto nuevaCarta) {
    // Validar reglas de negocio
    if (!this.estado.equals(EstadoProyecto.CORRECCIONES_SOLICITADAS)) {
        throw new EstadoProyectoInvalidoException(
            "Solo se puede reenviar cuando hay correcciones solicitadas"
        );
    }
    
    if (this.formatoA.haAlcanzadoMaximoIntentos()) {
        throw new MaximoIntentosAlcanzadoException();
    }
    
    // Ejecutar operación de negocio
    this.formatoA.incrementarIntento();
    this.formatoA.actualizarArchivos(nuevoPdf, nuevaCarta);
    this.estado = EstadoProyecto.EN_EVALUACION_COORDINADOR;
    this.fechaModificacion = LocalDateTime.now();
    
    // Registrar evento de dominio
    this.registrarEvento(new FormatoAReenviado(...));
}
```

Este método no solo actualiza el estado, sino que valida las reglas de negocio, mantiene las invariantes del aggregate y registra el evento correspondiente. La lógica de negocio está centralizada en el dominio, no dispersa en servicios o controllers.

**`evaluarFormatoA()`**: Método que procesa la evaluación del coordinador
```java
public void evaluarFormatoA(boolean aprobado, String observaciones, Long coordinadorId) {
    // Validar estado actual
    if (!this.estado.equals(EstadoProyecto.EN_EVALUACION_COORDINADOR)) {
        throw new EstadoProyectoInvalidoException(
            "El proyecto no está en evaluación"
        );
    }
    
    // Registrar evaluación en el historial
    Evaluacion evaluacion = new Evaluacion(
        aprobado,
        observaciones,
        coordinadorId,
        LocalDateTime.now()
    );
    this.formatoA.agregarEvaluacion(evaluacion);
    
    // Transición de estado según resultado
    if (aprobado) {
        this.estado = EstadoProyecto.FORMATO_A_APROBADO;
    } else {
        if (this.formatoA.haAlcanzadoMaximoIntentos()) {
            this.estado = EstadoProyecto.RECHAZADO;
        } else {
            this.estado = EstadoProyecto.CORRECCIONES_SOLICITADAS;
        }
    }
    
    this.fechaModificacion = LocalDateTime.now();
    this.registrarEvento(new FormatoAEvaluado(...));
}
```

El método encapsula la compleja lógica de transición de estados después de una evaluación. Si el Formato A es rechazado, el proyecto puede pasar a CORRECCIONES_SOLICITADAS (si no ha agotado intentos) o a RECHAZADO (si ya agotó los 3 intentos). Esta lógica compleja está protegida dentro del aggregate, no expuesta en servicios.

**`subirAnteproyecto()`**: Método que gestiona la subida del anteproyecto
```java
public void subirAnteproyecto(ArchivoAdjunto pdfAnteproyecto) {
    // Validar que el Formato A esté aprobado
    if (!this.estado.equals(EstadoProyecto.FORMATO_A_APROBADO)) {
        throw new EstadoProyectoInvalidoException(
            "Solo se puede subir anteproyecto si el Formato A está aprobado"
        );
    }
    
    if (this.anteproyecto != null) {
        throw new AnteproyectoYaExisteException();
    }
    
    // Crear entidad AnteproyectoInfo
    this.anteproyecto = new AnteproyectoInfo(pdfAnteproyecto);
    this.estado = EstadoProyecto.ANTEPROYECTO_ENVIADO;
    this.fechaModificacion = LocalDateTime.now();
    
    this.registrarEvento(new AnteproyectoSubido(...));
}
```

**`asignarEvaluadores()`**: Método que asigna evaluadores al anteproyecto
```java
public void asignarEvaluadores(Long evaluador1Id, Long evaluador2Id) {
    if (this.anteproyecto == null) {
        throw new AnteproyectoNoEncontradoException();
    }
    
    if (evaluador1Id.equals(evaluador2Id)) {
        throw new EvaluadoresDuplicadosException();
    }
    
    this.anteproyecto.asignarEvaluadores(evaluador1Id, evaluador2Id);
    this.estado = EstadoProyecto.ANTEPROYECTO_EN_EVALUACION;
    this.fechaModificacion = LocalDateTime.now();
    
    this.registrarEvento(new EvaluadoresAsignados(...));
}
```

Estos métodos demuestran cómo el aggregate encapsula comportamiento complejo mientras mantiene las invariantes del negocio.

### 4.2 Value Objects

Los value objects son componentes fundamentales de DDD que encapsulan conceptos del dominio sin identidad propia. El Submission Service implementa varios value objects clave en:

```
domain/model/Titulo.java
domain/model/Participantes.java
domain/model/ObjetivosProyecto.java
domain/model/ArchivoAdjunto.java
domain/model/ProyectoId.java
```

#### Características de los Value Objects

**1. Inmutabilidad**: Los value objects son inmutables. Una vez creados, sus valores no pueden cambiar. Si necesitas un valor diferente, creas un nuevo value object.

**2. Validación en Construcción**: Los value objects validan sus valores en el constructor privado. Es imposible crear un value object en estado inválido.

**3. Igualdad basada en Valor**: Dos value objects son iguales si todos sus atributos son iguales, independientemente de su identidad en memoria.

**4. Constructor Privado + Factory Method**: Los value objects no exponen constructores públicos, sino factory methods estáticos que expresan intención.

#### Implementación: Titulo

```java
public class Titulo {
    private static final int MAX_LENGTH = 500;
    private static final int MIN_LENGTH = 10;
    
    private final String value;  // Inmutable
    
    private Titulo(String value) {  // Constructor privado
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        
        String trimmed = value.trim();
        
        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                "El título debe tener al menos " + MIN_LENGTH + " caracteres"
            );
        }
        
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "El título no puede exceder " + MAX_LENGTH + " caracteres"
            );
        }
        
        this.value = trimmed;
    }
    
    public static Titulo of(String value) {  // Factory method
        return new Titulo(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Titulo titulo = (Titulo) o;
        return Objects.equals(value, titulo.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
```

El value object Titulo encapsula las reglas de validación del título. Es imposible crear un Titulo con menos de 10 caracteres o más de 500. Esta validación de negocio está protegida en el dominio, no en controllers o servicios.

#### Implementación: Participantes

```java
public class Participantes {
    private final Long directorId;
    private final Long codirectorId;  // Opcional
    private final Long estudiante1Id;
    private final Long estudiante2Id;  // Opcional
    
    private Participantes(Long directorId, Long codirectorId,
                         Long estudiante1Id, Long estudiante2Id) {
        if (directorId == null) {
            throw new IllegalArgumentException("El director es obligatorio");
        }
        if (estudiante1Id == null) {
            throw new IllegalArgumentException("Al menos un estudiante es obligatorio");
        }
        
        this.directorId = directorId;
        this.codirectorId = codirectorId;
        this.estudiante1Id = estudiante1Id;
        this.estudiante2Id = estudiante2Id;
    }
    
    public static Participantes of(Long directorId, Long codirectorId,
                                   Long estudiante1Id, Long estudiante2Id) {
        return new Participantes(directorId, codirectorId, estudiante1Id, estudiante2Id);
    }
    
    public boolean esDirector(Long userId) {
        return this.directorId.equals(userId);
    }
    
    public boolean esEstudiante(Long userId) {
        return this.estudiante1Id.equals(userId) ||
               (this.estudiante2Id != null && this.estudiante2Id.equals(userId));
    }
    
    // Getters, equals, hashCode...
}
```

El value object Participantes no solo almacena IDs, sino que también encapsula comportamiento útil como `esDirector()` y `esEstudiante()`. Este comportamiento forma parte del lenguaje ubicuo del dominio.

### 4.3 Domain Events

Los domain events son parte fundamental de la implementación DDD en el Submission Service. Se encuentran en:

```
domain/event/DomainEvent.java
domain/event/FormatoACreado.java
domain/event/FormatoAReenviado.java
domain/event/FormatoAEvaluado.java
domain/event/AnteproyectoSubido.java
domain/event/EvaluadoresAsignados.java
domain/event/AnteproyectoEvaluado.java
```

#### Características de Domain Events

**1. Inmutabilidad**: Los eventos son inmutables y representan hechos que ya ocurrieron en el pasado. Se nombran en pasado: "FormatoACreado", no "CrearFormatoA".

**2. Completos y Auto-contenidos**: Cada evento contiene toda la información necesaria para que los consumidores puedan reaccionar sin necesidad de consultar otros servicios.

**3. Generados por el Aggregate Root**: Los eventos son creados y registrados por el aggregate Proyecto cuando ocurren cambios significativos.

**4. Publicados Después de la Transacción**: Los eventos se publican solo después de que la transacción de base de datos se haya confirmado exitosamente, garantizando consistencia.

#### Implementación de Domain Events

```java
public interface DomainEvent {
    LocalDateTime occurredOn();
    String eventType();
}

public class FormatoACreado implements DomainEvent {
    private final Long proyectoId;
    private final Integer version;
    private final String titulo;
    private final String modalidad;
    private final Long directorId;
    private final String directorNombre;
    private final Long estudiante1Id;
    private final String estudiante1Nombre;
    private final String estudiante1Email;
    private final LocalDateTime timestamp;
    
    // Constructor, getters...
    
    @Override
    public LocalDateTime occurredOn() {
        return timestamp;
    }
    
    @Override
    public String eventType() {
        return "formato-a.creado";
    }
}
```

El aggregate Proyecto mantiene una lista interna de eventos pendientes:

```java
public class Proyecto {
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    private void registrarEvento(DomainEvent evento) {
        this.domainEvents.add(evento);
    }
    
    public List<DomainEvent> obtenerEventos() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void limpiarEventos() {
        this.domainEvents.clear();
    }
}
```

Los casos de uso extraen estos eventos después de persistir:

```java
// En CrearFormatoAUseCase
Proyecto proyectoGuardado = repositoryPort.save(proyecto);
List<DomainEvent> eventos = proyectoGuardado.obtenerEventos();
eventos.forEach(evento -> eventPublisherPort.publish(evento));
proyectoGuardado.limpiarEventos();
```

Este patrón garantiza que los eventos solo se publiquen si la transacción de base de datos fue exitosa, manteniendo la consistencia del sistema.

### 4.4 Specifications

Las Specifications implementan el patrón Specification de DDD para encapsular reglas de negocio complejas de manera reutilizable. Se encuentran en:

```
domain/specification/Specification.java
domain/specification/PuedeReenviarFormatoASpec.java
domain/specification/PuedeSubirAnteproyectoSpec.java
domain/specification/EsDirectorDelProyectoSpec.java
```

#### Interfaz Specification

```java
public interface Specification<T> {
    boolean isSatisfiedBy(T entity);
    String getRazonRechazo(T entity);
}
```

La interfaz define dos métodos: uno que verifica si el objeto satisface la especificación y otro que proporciona una razón legible si no la satisface.

#### Implementación: PuedeReenviarFormatoASpec

```java
public class PuedeReenviarFormatoASpec implements Specification<Proyecto> {
    
    @Override
    public boolean isSatisfiedBy(Proyecto proyecto) {
        if (proyecto == null) {
            return false;
        }
        
        // Regla 1: Debe estar en estado CORRECCIONES_SOLICITADAS
        if (!proyecto.getEstado().equals(EstadoProyecto.CORRECCIONES_SOLICITADAS)) {
            return false;
        }
        
        // Regla 2: No debe haber alcanzado el máximo de intentos (3)
        if (proyecto.getFormatoA().haAlcanzadoMaximoIntentos()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String getRazonRechazo(Proyecto proyecto) {
        if (proyecto == null) {
            return "El proyecto no existe";
        }
        
        if (!proyecto.getEstado().equals(EstadoProyecto.CORRECCIONES_SOLICITADAS)) {
            return "El proyecto no está en estado de correcciones solicitadas. " +
                   "Estado actual: " + proyecto.getEstado().getDescripcion();
        }
        
        if (proyecto.getFormatoA().haAlcanzadoMaximoIntentos()) {
            return "Se alcanzó el máximo de 3 intentos para el Formato A";
        }
        
        return null;
    }
}
```

Esta specification encapsula las reglas de negocio para determinar si un proyecto puede reenviar su Formato A. El caso de uso utiliza la specification antes de ejecutar la operación:

```java
// En ReenviarFormatoAUseCase
PuedeReenviarFormatoASpec spec = new PuedeReenviarFormatoASpec();
if (!spec.isSatisfiedBy(proyecto)) {
    throw new OperacionNoPermitidaException(spec.getRazonRechazo(proyecto));
}
proyecto.reenviarFormatoA(nuevoPdf, nuevaCarta);
```

#### Ventajas de las Specifications

**1. Reutilización**: La misma specification puede usarse en múltiples casos de uso y en diferentes contextos.

**2. Testabilidad**: Las specifications son fáciles de testear de manera aislada sin necesidad de levantar infraestructura.

**3. Composición**: Las specifications pueden combinarse usando operadores lógicos (AND, OR, NOT) para crear reglas más complejas.

**4. Expresividad**: Las specifications expresan reglas de negocio en el lenguaje del dominio, no en términos técnicos.

### 4.5 Entities vs Value Objects

Es crucial entender la diferencia entre entities y value objects en la implementación DDD del Submission Service:

**Entities (Entidades)**:
- `Proyecto` (Aggregate Root) - Tiene identidad única (ProyectoId)
- `FormatoAInfo` - Entidad interna del aggregate
- `AnteproyectoInfo` - Entidad interna del aggregate
- `Evaluacion` - Entidad que representa una evaluación específica

Las entidades tienen identidad que persiste en el tiempo. Un Proyecto con ID 123 sigue siendo el mismo proyecto incluso si cambia su título, participantes o estado.

**Value Objects**:
- `Titulo` - Definido por su valor textual
- `Participantes` - Definido por los IDs de sus miembros
- `ObjetivosProyecto` - Definido por objetivo general y específicos
- `ArchivoAdjunto` - Definido por ruta y nombre de archivo
- `ProyectoId` - Wrapper inmutable del ID (tipo de identidad)
- `EstadoProyecto` - Enum que representa estados
- `Modalidad` - Enum que representa modalidades

Los value objects no tienen identidad. Dos objetos Titulo con el mismo texto son intercambiables. Los value objects son más ligeros y económicos de crear que las entidades.

### 4.6 Lenguaje Ubicuo

El código del Submission Service refleja fielmente el lenguaje utilizado por los expertos del dominio (coordinadores, directores de proyecto, estudiantes):

**Términos del Dominio en el Código**:
- `Proyecto` - No "Project" ni "Submission"
- `FormatoA` - No "FormA" ni "ProposalForm"
- `Anteproyecto` - No "PreProject" ni "Draft"
- `Director` - Rol del docente que dirige el proyecto
- `Estudiante` - No "Student" (se mantiene el término en español)
- `Coordinador` - Rol que evalúa Formato A
- `Evaluación` - No "Review" ni "Assessment"
- `CORRECCIONES_SOLICITADAS` - Estado del proyecto, no "NEEDS_FIXES"

**Métodos que Reflejan Operaciones de Negocio**:
- `crearConFormatoA()` - No "create()" genérico
- `reenviarFormatoA()` - No "update()" genérico
- `evaluarFormatoA()` - No "setApproved()"
- `subirAnteproyecto()` - No "addDocument()"
- `asignarEvaluadores()` - No "setReviewers()"

Este lenguaje ubicuo garantiza que cualquier miembro del equipo (desarrollador o experto del dominio) pueda leer el código y entender qué hace sin necesidad de documentación adicional.

---

## 5. Por Qué Es Necesaria Esta Implementación

La implementación conjunta de Arquitectura Hexagonal y Domain-Driven Design en el Submission Service no es una decisión arbitraria ni un ejercicio académico. Esta arquitectura responde a necesidades reales del negocio y desafíos técnicos específicos.

### 5.1 Complejidad del Dominio

El dominio de gestión de trabajos de grado es inherentemente complejo con múltiples reglas de negocio interconectadas. Un proyecto de grado pasa por múltiples estados, tiene hasta 3 intentos para aprobar el Formato A, requiere evaluaciones de diferentes actores (coordinadores, evaluadores), maneja diferentes modalidades con requisitos específicos, y debe mantener consistencia en todas las transiciones de estado.

Esta complejidad no puede ser manejada adecuadamente con un modelo anémico donde las entidades son simples contenedores de datos y la lógica está dispersa en servicios. **DDD permite encapsular esta complejidad en el dominio** mediante aggregates que garantizan invariantes y especificaciones que expresan reglas de negocio de manera declarativa.

Sin DDD, tendríamos lógica de negocio duplicada en múltiples servicios, validaciones inconsistentes entre diferentes endpoints, y dificultad para razonar sobre el estado del sistema. Con DDD, todas las reglas están centralizadas en el aggregate Proyecto, garantizando consistencia.

### 5.2 Evolución y Mantenibilidad

Los requisitos de los sistemas de gestión académica cambian frecuentemente. Nuevas modalidades de proyecto pueden ser añadidas, el número de intentos permitidos puede cambiar, pueden surgir nuevos estados en el flujo de trabajo, o nuevas validaciones pueden ser requeridas por políticas institucionales.

**La Arquitectura Hexagonal hace que estos cambios sean más seguros y económicos**. Si las reglas de negocio cambian, solo modificamos el dominio. Si necesitamos cambiar de PostgreSQL a MongoDB, solo modificamos el adaptador de persistencia. Si queremos agregar una API GraphQL además de REST, solo agregamos un nuevo adaptador de entrada.

Sin arquitectura hexagonal, los cambios en frameworks o tecnologías requerirían modificar todo el código base, con alto riesgo de introducir bugs. Con hexagonal, el dominio está protegido y los cambios tecnológicos se limitan a la capa de infraestructura.

### 5.3 Testabilidad

El sistema de gestión de trabajos de grado debe garantizar que las reglas del negocio se cumplan estrictamente. Un error que permita aprobar un Formato A sin cumplir requisitos, o que permita más de 3 intentos, tendría consecuencias graves en los procesos académicos.

**La separación de capas facilita pruebas exhaustivas en cada nivel**. El dominio puede ser testeado con tests unitarios puros sin levantar Spring, bases de datos o RabbitMQ. Los casos de uso pueden testearse con mocks de los puertos. Los adaptadores pueden testearse independientemente con testcontainers.

```java
// Test unitario puro del dominio - sin infraestructura
@Test
void noDebePermitirReenvioSiAlcanzoMaximoIntentos() {
    // Given
    Proyecto proyecto = crearProyectoConTresIntentosAgotados();
    ArchivoAdjunto nuevoPdf = ArchivoAdjunto.of("ruta", "nuevo.pdf");
    
    // When & Then
    assertThrows(MaximoIntentosAlcanzadoException.class, () -> {
        proyecto.reenviarFormatoA(nuevoPdf, null);
    });
}
```

Este test no necesita Spring, JPA, RabbitMQ ni ninguna infraestructura. Testea la lógica de negocio pura en milisegundos. Sin arquitectura hexagonal, necesitaríamos levantar toda la aplicación para testear esta regla.

### 5.4 Independencia de Frameworks

Spring Boot es un framework excelente, pero frameworks cambian y evolucionan. Además, diferentes partes del sistema podrían usar diferentes tecnologías. El Submission Service usa Spring Boot, pero otros servicios podrían usar Quarkus, Micronaut o incluso Node.js.

**La Arquitectura Hexagonal garantiza que el dominio sea agnóstico de frameworks**. El 80% del código del Submission Service (todo el dominio) es Java puro sin anotaciones de Spring, JPA o cualquier librería externa. Si en el futuro decidimos migrar de Spring Boot a otro framework, el dominio permanece intacto.

Las anotaciones de Spring solo aparecen en la capa de infraestructura:
- `@RestController`, `@PostMapping` en controllers
- `@Entity`, `@Table`, `@Id` en entidades JPA
- `@Service`, `@Transactional` en casos de uso
- `@Component` en adaptadores

El dominio (Proyecto, Titulo, Participantes, FormatoAInfo) no tiene ninguna dependencia externa. Esto es posible solo con Arquitectura Hexagonal.

### 5.5 Comunicación con Stakeholders

Los stakeholders del sistema (coordinadores académicos, jefes de departamento, docentes) no entienden términos técnicos como "entity", "repository", "DTO" o "controller". Ellos hablan de proyectos, formatos A, anteproyectos, evaluaciones y directores.

**DDD con su lenguaje ubicuo permite que el código sea legible para expertos del dominio**. Un coordinador puede leer:

```java
public void evaluarFormatoA(boolean aprobado, String observaciones, Long coordinadorId) {
    if (!this.estado.equals(EstadoProyecto.EN_EVALUACION_COORDINADOR)) {
        throw new EstadoProyectoInvalidoException("El proyecto no está en evaluación");
    }
    // ...
}
```

Y entender exactamente qué hace el código sin ser programador. Este alineamiento entre código y dominio reduce malentendidos, facilita la validación de requisitos y mejora la comunicación en el equipo.

### 5.6 Escalabilidad y Microservicios

El sistema de gestión de trabajos de grado está diseñado como microservicios donde cada servicio tiene su propio bounded context. El Submission Service maneja el concepto de "Proyecto" desde la perspectiva de entregas y documentos. El Review Service tiene su propio concepto de "Evaluación". El Progress Tracking Service tiene su concepto de "Estado del Proyecto".

**La Arquitectura Hexagonal facilita la evolución a microservicios** porque cada servicio tiene su dominio claramente delimitado y se comunica con otros servicios solo a través de puertos bien definidos (HTTP clients, event publishers). Los bounded contexts previenen que los modelos de dominio se vuelvan excesivamente complejos intentando satisfacer todas las perspectivas.

Si mañana decidimos separar la gestión de Anteproyectos en un servicio independiente, el dominio ya está organizado de manera que facilita esta extracción. El aggregate Proyecto tiene una clara separación entre FormatoAInfo y AnteproyectoInfo.

### 5.7 Auditoría y Trazabilidad

Los procesos académicos requieren auditoría completa. Debe ser posible rastrear quién creó cada proyecto, quién lo evaluó, cuándo se realizó cada acción, y qué decisiones se tomaron.

**Los Domain Events proporcionan un log de auditoría inmutable**. Cada evento registra:
- Qué ocurrió (FormatoACreado, FormatoAEvaluado)
- Cuándo ocurrió (timestamp)
- Quién lo hizo (usuarioResponsableId, usuarioResponsableNombre)
- Contexto completo (todos los datos relevantes)

Estos eventos se almacenan en el Progress Tracking Service como Event Store, proporcionando un historial completo e inmutable de todos los cambios. Esta capacidad de auditoría sería muy difícil de implementar con una arquitectura tradicional donde los eventos son simples callbacks o notificaciones.

---

## 6. Cómo Trabajan en Conjunto Hexagonal y DDD

La Arquitectura Hexagonal y Domain-Driven Design no son tecnologías independientes que casualmente coexisten. Son enfoques complementarios que se potencian mutuamente para crear un sistema robusto, mantenible y alineado con el negocio.

### 6.1 Hexagonal Protege el Dominio DDD

La capa de dominio DDD necesita estar aislada de detalles técnicos para mantener su pureza y expresividad. **La Arquitectura Hexagonal proporciona este aislamiento mediante su estructura de capas concéntricas**.

El dominio DDD (Proyecto, Value Objects, Specifications) vive en el centro del hexágono, completamente protegido de:
- Frameworks (Spring, Hibernate)
- Protocolos (HTTP, AMQP)
- Bases de datos (PostgreSQL, JPA)
- Librerías externas (Jackson, Lombok)

Esta protección es crucial porque permite que el dominio evolucione según las necesidades del negocio, no según las limitaciones de frameworks. Si el dominio necesita una nueva regla de negocio, la agregamos sin preocuparnos por cómo se persistirá o cómo se expondrá en la API.

### 6.2 DDD Da Significado a las Capas Hexagonales

La Arquitectura Hexagonal define una estructura de tres capas, pero no prescribe qué va en cada capa. **DDD proporciona los patrones tácticos que dan contenido a estas capas**:

**Capa de Dominio** (Centro del hexágono):
- Aggregates (Proyecto)
- Entities (FormatoAInfo, AnteproyectoInfo)
- Value Objects (Titulo, Participantes)
- Domain Events (FormatoACreado)
- Specifications (PuedeReenviarFormatoASpec)
- Domain Services (si fueran necesarios)

**Capa de Aplicación** (Intermedia):
- Use Cases que orquestan el dominio
- Puertos (interfaces) que definen contratos
- DTOs que traducen entre capas
- Application Services que coordinan transacciones

**Capa de Infraestructura** (Externa):
- Adaptadores que implementan puertos
- Repositories JPA que persisten aggregates
- Event Publishers que publican domain events
- Controllers REST que exponen use cases

Sin DDD, las capas hexagonales serían solo carpetas organizativas. Con DDD, cada capa tiene patrones específicos y responsabilidades claras.

### 6.3 Puertos como Frontera de Bounded Context

En DDD, los bounded contexts definen límites donde un modelo de dominio es consistente. **Los puertos de la Arquitectura Hexagonal materializan estos límites**.

Los puertos de salida (`IIdentityServicePort`, `INotificationPort`) representan la frontera con otros bounded contexts. El Submission Service no conoce los detalles internos del Identity Service, solo interactúa a través del puerto:

```java
public interface IIdentityServicePort {
    boolean tieneRol(Long userId, String rol);
    UserBasicInfoDTO obtenerUsuario(Long userId);
}
```

Este puerto define exactamente qué necesita el Submission Service del mundo exterior, sin exponer ni conocer detalles de implementación. Si el Identity Service cambia internamente o incluso si lo reemplazamos con otro servicio, el dominio del Submission Service no se ve afectado.

### 6.4 Casos de Uso Orquestan Operaciones de Dominio

Los casos de uso de la capa de aplicación son el pegamento entre el dominio rico DDD y los adaptadores hexagonales. **Los casos de uso coordinan la interacción entre el dominio y el mundo exterior sin contener lógica de negocio propia**.

Flujo típico de un caso de uso:

1. **Validar precondiciones** usando puertos de salida (verificar rol del usuario)
2. **Obtener el aggregate** del repository port
3. **Delegar al dominio** llamando métodos del aggregate que encapsulan reglas de negocio
4. **Persistir cambios** usando el repository port
5. **Extraer eventos de dominio** del aggregate
6. **Publicar eventos** usando el event publisher port
7. **Retornar resultado** mediante DTOs

```java
@Service
@Transactional
public class ReenviarFormatoAUseCase implements IReenviarFormatoAUseCase {
    
    public ProyectoResponse reenviar(ReenviarFormatoARequest request, Long proyectoId, Long userId) {
        // 1. Validar con puerto
        Proyecto proyecto = repositoryPort.findById(ProyectoId.of(proyectoId))
            .orElseThrow(() -> new ProyectoNotFoundException(proyectoId));
        
        // 2. Validar con specification de dominio
        PuedeReenviarFormatoASpec spec = new PuedeReenviarFormatoASpec();
        if (!spec.isSatisfiedBy(proyecto)) {
            throw new OperacionNoPermitidaException(spec.getRazonRechazo(proyecto));
        }
        
        // 3. Guardar archivos con puerto
        String rutaPdf = fileStoragePort.guardarArchivo(...);
        
        // 4. Delegar al dominio
        proyecto.reenviarFormatoA(ArchivoAdjunto.of(rutaPdf, "..."), null);
        
        // 5. Persistir con puerto
        Proyecto proyectoActualizado = repositoryPort.save(proyecto);
        
        // 6. Publicar eventos con puerto
        proyectoActualizado.obtenerEventos()
            .forEach(evento -> eventPublisherPort.publish(evento));
        
        // 7. Retornar DTO
        return ProyectoResponse.fromDomain(proyectoActualizado);
    }
}
```

Este caso de uso orquesta múltiples operaciones pero no contiene lógica de negocio. La lógica está en `proyecto.reenviarFormatoA()` que valida el estado, incrementa el intento, cambia el estado y registra el evento.

### 6.5 Adaptadores Traducen Entre Lenguajes

Los adaptadores hexagonales actúan como traductores entre el lenguaje del dominio DDD y el lenguaje de tecnologías específicas. **Esta traducción preserva la pureza del dominio**.

**Adaptador JPA** traduce entre Proyecto (aggregate DDD) y ProyectoEntity (entidad JPA):

```java
@Component
public class ProyectoMapper {
    
    public ProyectoEntity toEntity(Proyecto proyecto) {
        ProyectoEntity entity = new ProyectoEntity();
        entity.setTitulo(proyecto.getTitulo().getValue());  // Value Object → String
        entity.setEstado(proyecto.getEstado());             // Enum → Enum
        entity.setDirectorId(proyecto.getParticipantes().getDirectorId());  // VO → Long
        // ... más traducciones
        return entity;
    }
    
    public Proyecto toDomain(ProyectoEntity entity) {
        // Reconstruir value objects desde primitivos
        Titulo titulo = Titulo.of(entity.getTitulo());
        Participantes participantes = Participantes.of(
            entity.getDirectorId(),
            entity.getCodirectorId(),
            entity.getEstudiante1Id(),
            entity.getEstudiante2Id()
        );
        // ... reconstruir aggregate
        return proyecto;
    }
}
```

El dominio trabaja con Value Objects inmutables y tipos ricos. JPA trabaja con primitivos y anotaciones. El adaptador traduce entre ambos mundos sin que el dominio conozca JPA.

**Adaptador REST** traduce entre Request DTOs y objetos de dominio:

```java
@PostMapping
public ResponseEntity<ProyectoResponse> crear(
    @RequestParam("pdf") MultipartFile pdf,
    @RequestParam("data") String jsonData) {
    
    // Traducir de JSON a Request DTO
    CrearFormatoARequest request = objectMapper.readValue(jsonData, CrearFormatoARequest.class);
    request.setPdfStream(pdf.getInputStream());
    
    // Delegar al caso de uso
    ProyectoResponse response = crearUseCase.crear(request, userId);
    
    // El caso de uso internamente traduce Request DTO a Value Objects de dominio
    return ResponseEntity.status(201).body(response);
}
```

### 6.6 Domain Events Atraviesan las Capas

Los Domain Events son un ejemplo perfecto de cómo Hexagonal y DDD trabajan juntos. **Los eventos nacen en el dominio DDD pero se publican mediante puertos hexagonales**.

1. **Dominio genera eventos** cuando ocurren cambios significativos:
```java
public class Proyecto {
    public void reenviarFormatoA(...) {
        // Lógica de negocio
        this.registrarEvento(new FormatoAReenviado(...));
    }
}
```

2. **Caso de uso extrae eventos** del aggregate:
```java
Proyecto proyectoGuardado = repositoryPort.save(proyecto);
List<DomainEvent> eventos = proyectoGuardado.obtenerEventos();
```

3. **Caso de uso publica eventos** a través del puerto:
```java
eventos.forEach(evento -> eventPublisherPort.publish(evento));
```

4. **Adaptador traduce eventos** a mensajes de RabbitMQ:
```java
public class RabbitMQEventPublisher implements IEventPublisherPort {
    public void publish(DomainEvent event) {
        String exchange = determinarExchange(event);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
```

El dominio no conoce RabbitMQ. El caso de uso no conoce exchanges ni routing keys. El adaptador maneja los detalles de mensajería. **Los eventos fluyen desde el centro del hexágono hacia afuera**, respetando la dirección de dependencias.

### 6.7 Testabilidad en Cada Nivel

La combinación de Hexagonal y DDD permite testear cada aspecto del sistema de manera aislada:

**Tests de Dominio** (DDD Puro):
```java
@Test
void debeCrearProyectoConFormatoAValido() {
    Proyecto proyecto = Proyecto.crearConFormatoA(
        Titulo.of("Título válido del proyecto"),
        Modalidad.INVESTIGACION,
        ObjetivosProyecto.of("Objetivo general", List.of("Específico 1")),
        Participantes.of(1L, null, 100L, null),
        ArchivoAdjunto.of("ruta", "formato.pdf"),
        null
    );
    
    assertEquals(EstadoProyecto.FORMATO_A_DILIGENCIADO, proyecto.getEstado());
    assertEquals(1, proyecto.getFormatoA().getNumeroIntento());
}
```

Este test no necesita Spring, JPA, RabbitMQ ni HTTP. Testea la lógica de negocio pura del dominio.

**Tests de Casos de Uso** (Hexagonal Application Layer):
```java
@Test
void debeCrearFormatoACorrectamente() {
    // Mocks de puertos
    when(identityServicePort.tieneRol(userId, "DOCENTE")).thenReturn(true);
    when(fileStoragePort.guardarArchivo(...)).thenReturn("ruta/archivo.pdf");
    when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    
    // Ejecutar caso de uso
    ProyectoResponse response = crearUseCase.crear(request, userId);
    
    // Verificar
    assertNotNull(response);
    verify(eventPublisherPort).publish(any(FormatoACreado.class));
}
```

Este test usa mocks de puertos para aislar el caso de uso. No necesita infraestructura real.

**Tests de Adaptadores** (Hexagonal Infrastructure Layer):
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class ProyectoRepositoryAdapterTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Test
    void debeGuardarYRecuperarProyecto() {
        Proyecto proyecto = crearProyectoEjemplo();
        
        Proyecto guardado = adapter.save(proyecto);
        Optional<Proyecto> recuperado = adapter.findById(guardado.getId());
        
        assertTrue(recuperado.isPresent());
        assertEquals(proyecto.getTitulo().getValue(), recuperado.get().getTitulo().getValue());
    }
}
```

Este test usa Testcontainers para probar el adaptador JPA con una base de datos real, pero aislado del resto del sistema.

### 6.8 Evolución Coordinada

Cuando los requisitos del negocio cambian, Hexagonal y DDD guían la evolución de manera coordinada:

**Nuevo Requisito**: "Los proyectos de maestría pueden tener hasta 5 intentos en lugar de 3"

**Impacto en DDD**:
1. Agregar enum `TipoPrograma` (PREGRADO, MAESTRIA, DOCTORADO)
2. Modificar `FormatoAInfo` para tener `maximoIntentos` variable
3. Actualizar `PuedeReenviarFormatoASpec` para considerar el tipo de programa

**Impacto en Hexagonal**:
1. Dominio: Los cambios anteriores
2. Aplicación: Los DTOs incluyen el nuevo campo `tipoPrograma`
3. Infraestructura REST: El controller recibe el nuevo parámetro
4. Infraestructura JPA: La entity tiene el nuevo campo y migración de Flyway

El cambio fluye desde el dominio hacia afuera. El lenguaje ubicuo se mantiene (hablamos de "tipo de programa" y "máximo de intentos", no de "config flags" o "limits"). Los tests unitarios del dominio se actualizan independientemente de los tests de integración.

---

## Conclusión

La implementación conjunta de Arquitectura Hexagonal y Domain-Driven Design en el Submission Service no es excesiva ni innecesaria. Es una respuesta deliberada y justificada a la complejidad inherente del dominio de gestión de trabajos de grado.

**Arquitectura Hexagonal** protege el núcleo del negocio de volatilidad tecnológica, facilita el testing en todos los niveles, permite la evolución independiente de cada capa, y hace posible cambiar tecnologías sin reescribir lógica de negocio.

**Domain-Driven Design** captura la complejidad del dominio en un modelo rico y expresivo, alinea el código con el lenguaje del negocio, centraliza las reglas en un solo lugar garantizando consistencia, y proporciona patrones probados para manejar complejidad.

**Juntas**, estas arquitecturas crean un sistema que es:
- **Mantenible**: Los cambios son localizados y seguros
- **Testeable**: Cada capa puede probarse independientemente
- **Comprensible**: El código refleja el lenguaje del negocio
- **Evolucionable**: Se adapta a nuevos requisitos sin colapsar
- **Robusto**: Las invariantes están protegidas en el dominio

Para un sistema que gestiona procesos académicos críticos, que debe evolucionar con políticas institucionales cambiantes, y que requiere auditoría completa, esta arquitectura no es opcional—es esencial.

---

**Documento creado:** Diciembre 10, 2025  
**Universidad del Cauca**  
**Ingeniería de Software II**  
**Submission Service - Microservicios de Gestión de Trabajos de Grado**



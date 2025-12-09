# Plan de Migración a Arquitectura Hexagonal - Submission Service

**Fecha:** 9 de Diciembre de 2025  
**Objetivo:** Migrar submission-service de arquitectura en capas a arquitectura hexagonal con enfoque DDD  
**Estimación:** 4-6 semanas de desarrollo

---

## TABLA DE CONTENIDOS

1. [Introducción a Arquitectura Hexagonal](#1-introduccion)
2. [Arquitectura Objetivo](#2-arquitectura-objetivo)
3. [Modelo de Dominio DDD](#3-modelo-dominio-ddd)
4. [Estructura de Carpetas](#4-estructura-carpetas)
5. [Puertos y Adaptadores](#5-puertos-adaptadores)
6. [Casos de Uso](#6-casos-uso)
7. [Plan de Migración](#7-plan-migracion)
8. [Cambios Específicos por Archivo](#8-cambios-especificos)
9. [Testing](#9-testing)
10. [Conclusiones](#10-conclusiones)

---

## 1. INTRODUCCION A ARQUITECTURA HEXAGONAL

### 1.1 Principios Fundamentales

La **Arquitectura Hexagonal** (Ports and Adapters) propone:

1. **Dominio en el centro**: La lógica de negocio no depende de frameworks
2. **Puertos**: Interfaces que definen contratos de entrada/salida
3. **Adaptadores**: Implementaciones concretas de los puertos
4. **Inversión de dependencias**: Las dependencias apuntan HACIA el dominio

```
┌────────────────────────────────────────────────────────────────┐
│                    ADAPTADORES PRIMARIOS                       │
│              (Driving Adapters - Entrada)                      │
│                                                                 │
│  REST API    │    GraphQL    │    CLI    │    Events          │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│                      PUERTOS PRIMARIOS                         │
│                   (Application Services)                       │
│                                                                 │
│  ICrearFormatoAUseCase  │  IEvaluarFormatoAUseCase            │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│                       CAPA DE DOMINIO                          │
│                    (Business Logic)                            │
│                                                                 │
│  Aggregates  │  Entities  │  Value Objects  │  Domain Events  │
│  Domain Services  │  Specifications  │  Repositories (puertos) │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│                      PUERTOS SECUNDARIOS                       │
│                     (Output Ports)                             │
│                                                                 │
│  IProyectoRepository  │  IEventPublisher  │  INotificationPort│
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│                   ADAPTADORES SECUNDARIOS                      │
│              (Driven Adapters - Salida)                        │
│                                                                 │
│  PostgreSQL  │  RabbitMQ  │  HTTP Client  │  File System      │
└────────────────────────────────────────────────────────────────┘
```

### 1.2 Beneficios de la Migración

✅ **Testabilidad**: Dominio puro sin dependencias externas  
✅ **Mantenibilidad**: Lógica de negocio clara y centralizada  
✅ **Flexibilidad**: Cambiar implementaciones sin tocar dominio  
✅ **Independencia**: No depende de frameworks específicos  
✅ **Escalabilidad**: Fácil agregar nuevos adaptadores  

---

## 2. ARQUITECTURA OBJETIVO

### 2.1 Visión General

```
submission-service/
├── domain/                          # ← NÚCLEO (sin dependencias externas)
│   ├── model/                       # Entidades y Value Objects
│   ├── aggregate/                   # Aggregates raíz
│   ├── service/                     # Domain Services
│   ├── event/                       # Domain Events
│   ├── repository/                  # Puertos (interfaces)
│   ├── specification/               # Business Rules
│   └── exception/                   # Excepciones de dominio
│
├── application/                     # ← CASOS DE USO
│   ├── usecase/                     # Use Cases (puertos primarios)
│   ├── port/                        # Puertos de aplicación
│   │   ├── in/                      # Puertos de entrada
│   │   └── out/                     # Puertos de salida
│   ├── dto/                         # DTOs de aplicación
│   └── service/                     # Application Services
│
└── infrastructure/                  # ← ADAPTADORES
    ├── adapter/
    │   ├── in/                      # Adaptadores de entrada
    │   │   ├── rest/                # REST Controllers
    │   │   └── event/               # Event Listeners
    │   └── out/                     # Adaptadores de salida
    │       ├── persistence/         # JPA Repositories
    │       ├── messaging/           # RabbitMQ Publishers
    │       ├── client/              # HTTP Clients
    │       └── filesystem/          # File Storage
    ├── config/                      # Configuración de Spring
    └── mapper/                      # Mappers entre capas
```

### 2.2 Flujo de Datos

```
1. Request HTTP → REST Controller (Adaptador IN)
                     ↓
2. Controller → UseCase (Puerto IN)
                     ↓
3. UseCase → Domain Model (Aggregate)
                     ↓
4. Domain → Repository (Puerto OUT)
                     ↓
5. Repository Impl → PostgreSQL (Adaptador OUT)
                     ↓
6. Domain → EventPublisher (Puerto OUT)
                     ↓
7. Publisher Impl → RabbitMQ (Adaptador OUT)
```

### 2.3 Capas y Responsabilidades

| Capa | Responsabilidad | Dependencias |
|------|-----------------|--------------|
| **Domain** | Lógica de negocio pura | NINGUNA (Java puro) |
| **Application** | Orquestar casos de uso | Domain |
| **Infrastructure** | Detalles técnicos | Application + Domain |

---

## 3. MODELO DE DOMINIO DDD

### 3.1 Bounded Context: Submission

El microservicio representa un **Bounded Context** completo dentro del sistema de gestión de trabajos de grado.

### 3.2 Aggregate Root: Proyecto

**Identificación del Aggregate:**
- **Root Entity:** `Proyecto` (antes `ProyectoSubmission`)
- **Responsabilidad:** Gestionar el ciclo de vida completo del trabajo de grado
- **Invariantes:** 
  - Un proyecto solo puede tener máximo 3 intentos de Formato A
  - Solo se puede subir anteproyecto si Formato A está aprobado
  - Los evaluadores solo se asignan si existe anteproyecto

**Entidades dentro del Aggregate:**
- `FormatoA` (parte del aggregate, no tabla separada)
- `Anteproyecto` (parte del aggregate, no tabla separada)

**Value Objects:**
- `ProyectoId`
- `Titulo`
- `ObjetivosProyecto`
- `Participantes` (director, codirector, estudiantes)
- `ArchivoAdjunto`
- `Evaluacion`
- `EstadoProyecto` (en lugar de patrón State con clases)

### 3.3 Domain Events

**Eventos que el aggregate publica:**

1. **FormatoACreado**
   - Datos: proyectoId, titulo, directorId, estudianteIds, version
   
2. **FormatoAPresentado**
   - Datos: proyectoId, coordinadorId, fecha

3. **FormatoAEvaluado**
   - Datos: proyectoId, resultado, comentarios, evaluadorId

4. **FormatoAReenviado**
   - Datos: proyectoId, version, fecha

5. **FormatoARechazadoDefinitivamente**
   - Datos: proyectoId, motivo

6. **AnteproyectoSubido**
   - Datos: proyectoId, rutaArchivo, fecha

7. **EvaluadoresAsignados**
   - Datos: proyectoId, evaluador1Id, evaluador2Id

8. **AnteproyectoEvaluado**
   - Datos: proyectoId, resultado, comentarios

### 3.4 Domain Services

**ProyectoValidationService:**
- Validar reglas de negocio complejas
- Validar límite de intentos
- Validar permisos (director puede subir anteproyecto)

**EstadoTransitionService:**
- Gestionar transiciones de estado válidas
- Aplicar reglas de flujo de trabajo

### 3.5 Specifications (Business Rules)

```java
// Especificaciones reutilizables
PuedeReenviarFormatoASpec
PuedeSubirAnteproyectoSpec
EsDirectorDelProyectoSpec
FormatoAEstaAprobadoSpec
```

---

## 4. ESTRUCTURA DE CARPETAS DETALLADA

### 4.1 Árbol Completo

La nueva estructura separa claramente dominio, aplicación e infraestructura siguiendo los principios de arquitectura hexagonal y DDD.

**Principio clave:** Las dependencias siempre apuntan hacia adentro (hacia el dominio).

Ver estructura completa en sección de anexos.

---

## 5. MODELO DE DOMINIO REFACTORIZADO

### 5.1 Aggregate Root: Proyecto

```java
package co.unicauca.submission.domain.model;

public class Proyecto {
    
    private ProyectoId id;
    private Titulo titulo;
    private Modalidad modalidad;
    private ObjetivosProyecto objetivos;
    private Participantes participantes;
    private EstadoProyecto estado;
    private FormatoAInfo formatoA;
    private AnteproyectoInfo anteproyecto;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    // Domain Events pendientes de publicar
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    // Constructor privado (usar factory methods)
    private Proyecto() {}
    
    // FACTORY METHOD: Crear proyecto con Formato A inicial
    public static Proyecto crearConFormatoA(
            Titulo titulo,
            Modalidad modalidad,
            ObjetivosProyecto objetivos,
            Participantes participantes,
            ArchivoAdjunto pdfFormatoA,
            ArchivoAdjunto cartaAceptacion
    ) {
        Proyecto proyecto = new Proyecto();
        proyecto.id = ProyectoId.generar();
        proyecto.titulo = titulo;
        proyecto.modalidad = modalidad;
        proyecto.objetivos = objetivos;
        proyecto.participantes = participantes;
        proyecto.estado = EstadoProyecto.FORMATO_A_DILIGENCIADO;
        proyecto.formatoA = new FormatoAInfo(1, pdfFormatoA, cartaAceptacion);
        proyecto.fechaCreacion = LocalDateTime.now();
        proyecto.fechaModificacion = LocalDateTime.now();
        
        // Registrar evento de dominio
        proyecto.registrarEvento(new FormatoACreado(
            proyecto.id,
            proyecto.titulo,
            proyecto.modalidad,
            proyecto.participantes,
            1
        ));
        
        return proyecto;
    }
    
    // COMPORTAMIENTO DE NEGOCIO
    
    public void presentarAlCoordinador() {
        validarTransicion(EstadoProyecto.FORMATO_A_DILIGENCIADO, 
                         EstadoProyecto.EN_EVALUACION_COORDINADOR);
        
        this.estado = EstadoProyecto.EN_EVALUACION_COORDINADOR;
        this.fechaModificacion = LocalDateTime.now();
        
        registrarEvento(new FormatoAPresentado(this.id));
    }
    
    public void evaluarFormatoA(boolean aprobado, String comentarios, Long evaluadorId) {
        validarTransicion(EstadoProyecto.EN_EVALUACION_COORDINADOR, null);
        
        if (aprobado) {
            this.estado = EstadoProyecto.FORMATO_A_APROBADO;
            registrarEvento(new FormatoAAprobado(this.id, evaluadorId));
        } else {
            this.formatoA.incrementarIntentos();
            
            if (this.formatoA.haAlcanzadoMaximoIntentos()) {
                this.estado = EstadoProyecto.FORMATO_A_RECHAZADO;
                registrarEvento(new FormatoARechazadoDefinitivamente(this.id, comentarios));
            } else {
                this.estado = EstadoProyecto.CORRECCIONES_SOLICITADAS;
                registrarEvento(new FormatoARechazado(this.id, comentarios, this.formatoA.getNumeroIntento()));
            }
        }
        
        this.formatoA.agregarEvaluacion(new Evaluacion(aprobado, comentarios, evaluadorId));
        this.fechaModificacion = LocalDateTime.now();
    }
    
    public void reenviarFormatoA(ArchivoAdjunto nuevoPdf, ArchivoAdjunto nuevaCarta) {
        // Validar usando specification
        if (!new PuedeReenviarFormatoASpec().isSatisfiedBy(this)) {
            throw new MaximosIntentosExcedidosException("No se puede reenviar, máximo 3 intentos");
        }
        
        this.formatoA.actualizarArchivos(nuevoPdf, nuevaCarta);
        this.estado = EstadoProyecto.EN_EVALUACION_COORDINADOR;
        this.fechaModificacion = LocalDateTime.now();
        
        registrarEvento(new FormatoAReenviado(this.id, this.formatoA.getNumeroIntento()));
    }
    
    public void subirAnteproyecto(ArchivoAdjunto pdfAnteproyecto, Long directorId) {
        // Validar usando specification
        if (!new PuedeSubirAnteproyectoSpec().isSatisfiedBy(this)) {
            throw new FormatoANoAprobadoException("El Formato A debe estar aprobado");
        }
        
        if (!this.participantes.esDirector(directorId)) {
            throw new UsuarioNoAutorizadoException("Solo el director puede subir el anteproyecto");
        }
        
        if (this.anteproyecto != null) {
            throw new DomainException("Ya existe un anteproyecto para este proyecto");
        }
        
        this.anteproyecto = new AnteproyectoInfo(pdfAnteproyecto);
        this.estado = EstadoProyecto.ANTEPROYECTO_ENVIADO;
        this.fechaModificacion = LocalDateTime.now();
        
        registrarEvento(new AnteproyectoSubido(this.id, pdfAnteproyecto.getRuta()));
    }
    
    public void asignarEvaluadores(Long evaluador1Id, Long evaluador2Id) {
        validarTransicion(EstadoProyecto.ANTEPROYECTO_ENVIADO, 
                         EstadoProyecto.ANTEPROYECTO_EN_EVALUACION);
        
        if (this.anteproyecto == null) {
            throw new DomainException("No existe anteproyecto para asignar evaluadores");
        }
        
        this.anteproyecto.asignarEvaluadores(evaluador1Id, evaluador2Id);
        this.estado = EstadoProyecto.ANTEPROYECTO_EN_EVALUACION;
        this.fechaModificacion = LocalDateTime.now();
        
        registrarEvento(new EvaluadoresAsignados(this.id, evaluador1Id, evaluador2Id));
    }
    
    public void evaluarAnteproyecto(boolean aprobado, String comentarios, Long evaluadorId) {
        validarTransicion(EstadoProyecto.ANTEPROYECTO_EN_EVALUACION, null);
        
        if (this.anteproyecto == null) {
            throw new DomainException("No existe anteproyecto para evaluar");
        }
        
        this.anteproyecto.agregarEvaluacion(new Evaluacion(aprobado, comentarios, evaluadorId));
        
        if (aprobado) {
            this.estado = EstadoProyecto.ANTEPROYECTO_APROBADO;
        } else {
            this.estado = EstadoProyecto.ANTEPROYECTO_RECHAZADO;
        }
        
        this.fechaModificacion = LocalDateTime.now();
        registrarEvento(new AnteproyectoEvaluado(this.id, aprobado, comentarios));
    }
    
    // MÉTODOS AUXILIARES
    
    private void validarTransicion(EstadoProyecto estadoEsperado, EstadoProyecto nuevoEstado) {
        if (!this.estado.equals(estadoEsperado)) {
            throw new EstadoInvalidoException(
                String.format("Estado actual %s no permite esta operación (esperado: %s)", 
                             this.estado, estadoEsperado)
            );
        }
    }
    
    private void registrarEvento(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    public List<DomainEvent> obtenerEventosPendientes() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void limpiarEventos() {
        this.domainEvents.clear();
    }
    
    // Getters (sin setters para inmutabilidad)
    public ProyectoId getId() { return id; }
    public Titulo getTitulo() { return titulo; }
    public EstadoProyecto getEstado() { return estado; }
    public Participantes getParticipantes() { return participantes; }
    // ... otros getters
}
```

### 5.2 Value Objects

#### ProyectoId

```java
package co.unicauca.submission.domain.model;

import java.util.Objects;
import java.util.UUID;

public class ProyectoId {
    
    private final Long value;
    
    private ProyectoId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ID de proyecto inválido");
        }
        this.value = value;
    }
    
    public static ProyectoId of(Long value) {
        return new ProyectoId(value);
    }
    
    public static ProyectoId generar() {
        // En este caso, el ID lo genera la BD, así que retornamos null
        // y se asignará después del save
        return null;
    }
    
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProyectoId that = (ProyectoId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
```

#### Titulo

```java
package co.unicauca.submission.domain.model;

public class Titulo {
    
    private static final int MAX_LENGTH = 500;
    private static final int MIN_LENGTH = 10;
    
    private final String value;
    
    private Titulo(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        
        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("El título debe tener al menos " + MIN_LENGTH + " caracteres");
        }
        
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("El título no puede exceder " + MAX_LENGTH + " caracteres");
        }
        
        this.value = trimmed;
    }
    
    public static Titulo of(String value) {
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
    
    @Override
    public String toString() {
        return value;
    }
}
```

#### Participantes

```java
package co.unicauca.submission.domain.model;

public class Participantes {
    
    private final Long directorId;
    private final Long codirectorId; // Opcional
    private final Long estudiante1Id;
    private final Long estudiante2Id; // Opcional
    
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
    
    public boolean tieneCodirector() {
        return this.codirectorId != null;
    }
    
    // Getters
    public Long getDirectorId() { return directorId; }
    public Long getCodirectorId() { return codirectorId; }
    public Long getEstudiante1Id() { return estudiante1Id; }
    public Long getEstudiante2Id() { return estudiante2Id; }
}
```

#### ArchivoAdjunto

```java
package co.unicauca.submission.domain.model;

public class ArchivoAdjunto {
    
    private final String ruta;
    private final String nombreOriginal;
    private final TipoArchivo tipo;
    
    private ArchivoAdjunto(String ruta, String nombreOriginal, TipoArchivo tipo) {
        if (ruta == null || ruta.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacía");
        }
        if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de archivo es obligatorio");
        }
        
        this.ruta = ruta;
        this.nombreOriginal = nombreOriginal;
        this.tipo = tipo;
    }
    
    public static ArchivoAdjunto of(String ruta, String nombreOriginal, TipoArchivo tipo) {
        return new ArchivoAdjunto(ruta, nombreOriginal, tipo);
    }
    
    public static ArchivoAdjunto pdf(String ruta, String nombreOriginal) {
        return new ArchivoAdjunto(ruta, nombreOriginal, TipoArchivo.PDF);
    }
    
    public boolean esPDF() {
        return this.tipo == TipoArchivo.PDF;
    }
    
    // Getters
    public String getRuta() { return ruta; }
    public String getNombreOriginal() { return nombreOriginal; }
    public TipoArchivo getTipo() { return tipo; }
    
    public enum TipoArchivo {
        PDF, WORD, EXCEL, IMAGEN
    }
}
```

### 5.3 Entities dentro del Aggregate

#### FormatoAInfo

```java
package co.unicauca.submission.domain.model;

public class FormatoAInfo {
    
    private static final int MAX_INTENTOS = 3;
    
    private int numeroIntento;
    private ArchivoAdjunto pdfFormatoA;
    private ArchivoAdjunto cartaAceptacion; // Opcional
    private List<Evaluacion> evaluaciones;
    
    public FormatoAInfo(int numeroIntento, ArchivoAdjunto pdfFormatoA, 
                       ArchivoAdjunto cartaAceptacion) {
        if (numeroIntento < 1 || numeroIntento > MAX_INTENTOS) {
            throw new IllegalArgumentException("Número de intento inválido: " + numeroIntento);
        }
        if (pdfFormatoA == null) {
            throw new IllegalArgumentException("El PDF del Formato A es obligatorio");
        }
        
        this.numeroIntento = numeroIntento;
        this.pdfFormatoA = pdfFormatoA;
        this.cartaAceptacion = cartaAceptacion;
        this.evaluaciones = new ArrayList<>();
    }
    
    public void incrementarIntentos() {
        if (this.numeroIntento >= MAX_INTENTOS) {
            throw new MaximosIntentosExcedidosException("Ya se alcanzó el máximo de intentos");
        }
        this.numeroIntento++;
    }
    
    public boolean haAlcanzadoMaximoIntentos() {
        return this.numeroIntento >= MAX_INTENTOS;
    }
    
    public void actualizarArchivos(ArchivoAdjunto nuevoPdf, ArchivoAdjunto nuevaCarta) {
        if (nuevoPdf != null) {
            this.pdfFormatoA = nuevoPdf;
        }
        if (nuevaCarta != null) {
            this.cartaAceptacion = nuevaCarta;
        }
    }
    
    public void agregarEvaluacion(Evaluacion evaluacion) {
        this.evaluaciones.add(evaluacion);
    }
    
    public Evaluacion getUltimaEvaluacion() {
        if (evaluaciones.isEmpty()) {
            return null;
        }
        return evaluaciones.get(evaluaciones.size() - 1);
    }
    
    // Getters
    public int getNumeroIntento() { return numeroIntento; }
    public ArchivoAdjunto getPdfFormatoA() { return pdfFormatoA; }
    public ArchivoAdjunto getCartaAceptacion() { return cartaAceptacion; }
    public List<Evaluacion> getEvaluaciones() { 
        return Collections.unmodifiableList(evaluaciones); 
    }
}
```

#### AnteproyectoInfo

```java
package co.unicauca.submission.domain.model;

public class AnteproyectoInfo {
    
    private ArchivoAdjunto pdfAnteproyecto;
    private LocalDateTime fechaEnvio;
    private Long evaluador1Id;
    private Long evaluador2Id;
    private List<Evaluacion> evaluaciones;
    
    public AnteproyectoInfo(ArchivoAdjunto pdfAnteproyecto) {
        if (pdfAnteproyecto == null) {
            throw new IllegalArgumentException("El PDF del anteproyecto es obligatorio");
        }
        
        this.pdfAnteproyecto = pdfAnteproyecto;
        this.fechaEnvio = LocalDateTime.now();
        this.evaluaciones = new ArrayList<>();
    }
    
    public void asignarEvaluadores(Long evaluador1Id, Long evaluador2Id) {
        if (evaluador1Id == null || evaluador2Id == null) {
            throw new IllegalArgumentException("Ambos evaluadores son obligatorios");
        }
        if (evaluador1Id.equals(evaluador2Id)) {
            throw new IllegalArgumentException("Los evaluadores deben ser diferentes");
        }
        
        this.evaluador1Id = evaluador1Id;
        this.evaluador2Id = evaluador2Id;
    }
    
    public boolean tieneEvaluadoresAsignados() {
        return this.evaluador1Id != null && this.evaluador2Id != null;
    }
    
    public void agregarEvaluacion(Evaluacion evaluacion) {
        this.evaluaciones.add(evaluacion);
    }
    
    // Getters
    public ArchivoAdjunto getPdfAnteproyecto() { return pdfAnteproyecto; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public Long getEvaluador1Id() { return evaluador1Id; }
    public Long getEvaluador2Id() { return evaluador2Id; }
    public List<Evaluacion> getEvaluaciones() { 
        return Collections.unmodifiableList(evaluaciones); 
    }
}
```

---

## 6. SPECIFICATIONS (REGLAS DE NEGOCIO)

Las Specifications encapsulan reglas de negocio complejas de forma reutilizable.

### 6.1 PuedeReenviarFormatoASpec

```java
package co.unicauca.submission.domain.specification;

public class PuedeReenviarFormatoASpec implements Specification<Proyecto> {
    
    @Override
    public boolean isSatisfiedBy(Proyecto proyecto) {
        // Debe estar en estado de correcciones solicitadas
        if (!proyecto.getEstado().equals(EstadoProyecto.CORRECCIONES_SOLICITADAS)) {
            return false;
        }
        
        // No debe haber alcanzado el máximo de intentos
        if (proyecto.getFormatoA().haAlcanzadoMaximoIntentos()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String getRazonRechazo(Proyecto proyecto) {
        if (!proyecto.getEstado().equals(EstadoProyecto.CORRECCIONES_SOLICITADAS)) {
            return "El proyecto no está en estado de correcciones solicitadas";
        }
        if (proyecto.getFormatoA().haAlcanzadoMaximoIntentos()) {
            return "Se alcanzó el máximo de 3 intentos";
        }
        return null;
    }
}
```

### 6.2 PuedeSubirAnteproyectoSpec

```java
package co.unicauca.submission.domain.specification;

public class PuedeSubirAnteproyectoSpec implements Specification<Proyecto> {
    
    @Override
    public boolean isSatisfiedBy(Proyecto proyecto) {
        // El Formato A debe estar aprobado
        if (!proyecto.getEstado().equals(EstadoProyecto.FORMATO_A_APROBADO)) {
            return false;
        }
        
        // No debe existir anteproyecto previo
        if (proyecto.getAnteproyecto() != null) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String getRazonRechazo(Proyecto proyecto) {
        if (!proyecto.getEstado().equals(EstadoProyecto.FORMATO_A_APROBADO)) {
            return "El Formato A debe estar aprobado antes de subir anteproyecto";
        }
        if (proyecto.getAnteproyecto() != null) {
            return "Ya existe un anteproyecto para este proyecto";
        }
        return null;
    }
}
```

### 6.3 EsDirectorDelProyectoSpec

```java
package co.unicauca.submission.domain.specification;

public class EsDirectorDelProyectoSpec implements Specification<Proyecto> {
    
    private final Long userId;
    
    public EsDirectorDelProyectoSpec(Long userId) {
        this.userId = userId;
    }
    
    @Override
    public boolean isSatisfiedBy(Proyecto proyecto) {
        return proyecto.getParticipantes().esDirector(userId);
    }
    
    @Override
    public String getRazonRechazo(Proyecto proyecto) {
        if (!isSatisfiedBy(proyecto)) {
            return "El usuario no es el director del proyecto";
        }
        return null;
    }
}
```

---

## 7. CASOS DE USO IMPLEMENTADOS

### 7.1 CrearFormatoAUseCase

```java
package co.unicauca.submission.application.usecase.formatoa;

import co.unicauca.submission.application.port.in.ICrearFormatoAUseCase;
import co.unicauca.submission.application.port.out.*;
import co.unicauca.submission.application.dto.request.CrearFormatoARequest;
import co.unicauca.submission.application.dto.response.ProyectoResponse;
import co.unicauca.submission.domain.model.*;
import co.unicauca.submission.domain.event.DomainEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CrearFormatoAUseCase implements ICrearFormatoAUseCase {
    
    private final IProyectoRepositoryPort repositoryPort;
    private final IFileStoragePort fileStoragePort;
    private final IEventPublisherPort eventPublisherPort;
    private final INotificationPort notificationPort;
    private final IIdentityServicePort identityServicePort;
    
    // Constructor injection
    
    @Override
    public ProyectoResponse crear(CrearFormatoARequest request, Long userId) {
        
        // 1. Validar que el usuario tiene rol DOCENTE
        if (!identityServicePort.tieneRol(userId, "DOCENTE")) {
            throw new UsuarioNoAutorizadoException("Solo docentes pueden crear Formato A");
        }
        
        // 2. Validar y guardar archivos
        String rutaPdf = fileStoragePort.guardarArchivo(
            request.getPdfStream(),
            "formatoA_v1.pdf",
            "formatoA/" + userId
        );
        
        String rutaCarta = null;
        if (request.getModalidad() == Modalidad.PRACTICA_PROFESIONAL) {
            if (request.getCartaStream() == null) {
                throw new IllegalArgumentException("Carta obligatoria para PRACTICA_PROFESIONAL");
            }
            rutaCarta = fileStoragePort.guardarArchivo(
                request.getCartaStream(),
                "carta_v1.pdf",
                "formatoA/" + userId
            );
        }
        
        // 3. Crear Value Objects
        Titulo titulo = Titulo.of(request.getTitulo());
        ObjetivosProyecto objetivos = ObjetivosProyecto.of(
            request.getObjetivoGeneral(),
            request.getObjetivosEspecificos()
        );
        Participantes participantes = Participantes.of(
            userId, // Director es quien crea
            request.getCodirectorId(),
            request.getEstudiante1Id(),
            request.getEstudiante2Id()
        );
        ArchivoAdjunto pdfFormatoA = ArchivoAdjunto.pdf(rutaPdf, "formatoA_v1.pdf");
        ArchivoAdjunto carta = rutaCarta != null ? 
            ArchivoAdjunto.pdf(rutaCarta, "carta_v1.pdf") : null;
        
        // 4. Crear Aggregate usando Factory Method
        Proyecto proyecto = Proyecto.crearConFormatoA(
            titulo,
            request.getModalidad(),
            objetivos,
            participantes,
            pdfFormatoA,
            carta
        );
        
        // 5. Guardar en repositorio
        Proyecto proyectoGuardado = repositoryPort.save(proyecto);
        
        // 6. Publicar eventos de dominio
        List<DomainEvent> eventos = proyectoGuardado.obtenerEventosPendientes();
        eventPublisherPort.publishAll(eventos);
        proyectoGuardado.limpiarEventos();
        
        // 7. Enviar notificación al coordinador (RF2)
        notificationPort.notificarCoordinadorFormatoAEnviado(
            proyectoGuardado.getId().getValue(),
            1
        );
        
        // 8. Mapear a Response DTO
        return ProyectoResponse.fromDomain(proyectoGuardado);
    }
}
```

### 7.2 EvaluarFormatoAUseCase

```java
package co.unicauca.submission.application.usecase.formatoa;

@Service
@Transactional
public class EvaluarFormatoAUseCase implements IEvaluarFormatoAUseCase {
    
    private final IProyectoRepositoryPort repositoryPort;
    private final IEventPublisherPort eventPublisherPort;
    private final IIdentityServicePort identityServicePort;
    
    @Override
    public ProyectoResponse evaluar(Long proyectoId, EvaluarFormatoARequest request, Long evaluadorId) {
        
        // 1. Validar que el usuario es COORDINADOR
        if (!identityServicePort.tieneRol(evaluadorId, "COORDINATOR")) {
            throw new UsuarioNoAutorizadoException("Solo coordinadores pueden evaluar");
        }
        
        // 2. Obtener proyecto
        Proyecto proyecto = repositoryPort.findById(ProyectoId.of(proyectoId))
            .orElseThrow(() -> new ProyectoNotFoundException(proyectoId));
        
        // 3. Ejecutar lógica de negocio (delegar al aggregate)
        proyecto.evaluarFormatoA(
            request.isAprobado(),
            request.getComentarios(),
            evaluadorId
        );
        
        // 4. Guardar cambios
        Proyecto proyectoActualizado = repositoryPort.save(proyecto);
        
        // 5. Publicar eventos
        List<DomainEvent> eventos = proyectoActualizado.obtenerEventosPendientes();
        eventPublisherPort.publishAll(eventos);
        proyectoActualizado.limpiarEventos();
        
        // 6. Retornar respuesta
        return ProyectoResponse.fromDomain(proyectoActualizado);
    }
}
```

### 7.3 SubirAnteproyectoUseCase

```java
package co.unicauca.submission.application.usecase.anteproyecto;

@Service
@Transactional
public class SubirAnteproyectoUseCase implements ISubirAnteproyectoUseCase {
    
    private final IProyectoRepositoryPort repositoryPort;
    private final IFileStoragePort fileStoragePort;
    private final IEventPublisherPort eventPublisherPort;
    private final INotificationPort notificationPort;
    
    @Override
    public ProyectoResponse subir(Long proyectoId, SubirAnteproyectoRequest request, Long userId) {
        
        // 1. Obtener proyecto
        Proyecto proyecto = repositoryPort.findById(ProyectoId.of(proyectoId))
            .orElseThrow(() -> new ProyectoNotFoundException(proyectoId));
        
        // 2. Guardar archivo PDF
        String rutaPdf = fileStoragePort.guardarArchivo(
            request.getPdfStream(),
            "anteproyecto.pdf",
            "anteproyecto/" + proyectoId
        );
        
        ArchivoAdjunto pdfAnteproyecto = ArchivoAdjunto.pdf(rutaPdf, "anteproyecto.pdf");
        
        // 3. Ejecutar lógica de negocio (incluye validaciones con specifications)
        proyecto.subirAnteproyecto(pdfAnteproyecto, userId);
        
        // 4. Guardar cambios
        Proyecto proyectoActualizado = repositoryPort.save(proyecto);
        
        // 5. Publicar eventos
        List<DomainEvent> eventos = proyectoActualizado.obtenerEventosPendientes();
        eventPublisherPort.publishAll(eventos);
        proyectoActualizado.limpiarEventos();
        
        // 6. Notificar al jefe de departamento (RF6)
        notificationPort.notificarJefeDepartamentoAnteproyecto(proyectoId);
        
        // 7. Retornar respuesta
        return ProyectoResponse.fromDomain(proyectoActualizado);
    }
}
```

---

## 8. PLAN DE MIGRACION POR FASES

### Fase 1: Preparación (Semana 1)

**Objetivos:**
- Crear estructura de carpetas nueva
- Definir interfaces de puertos
- No tocar código existente aún

**Tareas:**
1. ✅ Crear paquetes `domain`, `application`, `infrastructure`
2. ✅ Definir todas las interfaces de puertos (in y out)
3. ✅ Crear Value Objects básicos (ProyectoId, Titulo, etc.)
4. ✅ Definir enums del dominio
5. ✅ Crear excepciones de dominio
6. ✅ Configurar ArchUnit para validar arquitectura

**Entregables:**
- Estructura de carpetas completa
- 10+ interfaces de puertos definidas
- Tests de arquitectura con ArchUnit

**Sin romper:** El código actual sigue funcionando.

---

### Fase 2: Dominio Puro (Semana 2)

**Objetivos:**
- Implementar modelo de dominio sin dependencias
- Extraer lógica de negocio de ProyectoSubmission

**Tareas:**
1. ✅ Crear clase `Proyecto` (aggregate root) SIN anotaciones JPA
2. ✅ Implementar Value Objects completos
3. ✅ Crear Specifications
4. ✅ Implementar Domain Events
5. ✅ Migrar lógica de negocio del patrón State al aggregate
6. ✅ Tests unitarios del dominio (100% cobertura)

**Entregables:**
- Aggregate `Proyecto` completamente funcional
- 20+ tests unitarios de dominio
- Documentación del modelo de dominio

**Sin romper:** Código actual sigue funcionando, dominio nuevo en paralelo.

---

### Fase 3: Capa de Aplicación (Semana 3)

**Objetivos:**
- Implementar casos de uso
- Crear adaptadores de salida

**Tareas:**
1. ✅ Implementar todos los Use Cases (7 use cases)
2. ✅ Crear DTOs de request/response
3. ✅ Implementar `ProyectoRepositoryAdapter` (JPA → Dominio)
4. ✅ Implementar `RabbitMQEventPublisher`
5. ✅ Implementar `IdentityServiceAdapter`
6. ✅ Implementar `FileStorageAdapter`
7. ✅ Crear Mappers (Domain ↔ Entity, Domain ↔ DTO)
8. ✅ Tests de casos de uso con mocks

**Entregables:**
- 7 casos de uso implementados
- 4 adaptadores de salida funcionando
- Tests de aplicación

**Sin romper:** Crear nuevos adaptadores, mantener código viejo.

---

### Fase 4: Adaptadores de Entrada (Semana 4)

**Objetivos:**
- Crear nuevos controladores REST
- Mantener endpoints compatibles

**Tareas:**
1. ✅ Crear `FormatoAControllerV2` (nuevo)
2. ✅ Crear `AnteproyectoControllerV2` (nuevo)
3. ✅ Configurar enrutamiento dual (viejo y nuevo)
4. ✅ Tests de integración end-to-end
5. ✅ Documentación OpenAPI actualizada

**Rutas nuevas (conviven con las viejas):**
```
POST   /api/v2/submissions/formatoA
POST   /api/v2/submissions/formatoA/{id}/reenviar
PATCH  /api/v2/submissions/formatoA/{id}/evaluar
POST   /api/v2/submissions/anteproyecto
```

**Entregables:**
- Controladores nuevos funcionando
- Tests de integración pasando
- Ambas versiones de API funcionando

**Sin romper:** APIs viejas siguen funcionando.

---

### Fase 5: Migración Gradual (Semana 5)

**Objetivos:**
- Deprecar código viejo
- Migrar datos si es necesario
- Monitorear ambas implementaciones

**Tareas:**
1. ✅ Marcar código viejo como `@Deprecated`
2. ✅ Crear script de migración de datos (si aplica)
3. ✅ Configurar feature flags para cambiar entre implementaciones
4. ✅ Monitorear errores y performance
5. ✅ Documentar diferencias entre v1 y v2

**Feature Flag:**
```yaml
feature:
  hexagonal:
    enabled: true  # false = usa código viejo
```

**Entregables:**
- Feature flag configurado
- Monitoreo activo
- Plan de rollback documentado

---

### Fase 6: Limpieza y Optimización (Semana 6)

**Objetivos:**
- Eliminar código legacy
- Optimizar performance
- Documentación final

**Tareas:**
1. ✅ Eliminar controladores viejos
2. ✅ Eliminar `SubmissionService` viejo
3. ✅ Eliminar entidades legacy (`ProyectoGrado`, `FormatoA`)
4. ✅ Eliminar tablas no usadas de BD
5. ✅ Optimizar queries
6. ✅ Refactorizar nombres si es necesario
7. ✅ Documentación técnica completa
8. ✅ Capacitación al equipo

**Entregables:**
- Código legacy eliminado
- Performance mejorada
- Documentación actualizada
- README con arquitectura hexagonal

---

## 9. CAMBIOS ESPECÍFICOS POR ARCHIVO

### 9.1 Archivos a CREAR (Nuevos)

#### Domain Layer

**Models:**
```
✨ domain/model/Proyecto.java                    (Aggregate Root)
✨ domain/model/ProyectoId.java                  (Value Object)
✨ domain/model/Titulo.java                      (Value Object)
✨ domain/model/ObjetivosProyecto.java           (Value Object)
✨ domain/model/Participantes.java               (Value Object)
✨ domain/model/ArchivoAdjunto.java              (Value Object)
✨ domain/model/Evaluacion.java                  (Value Object)
✨ domain/model/FormatoAInfo.java                (Entity)
✨ domain/model/AnteproyectoInfo.java            (Entity)
✨ domain/model/EstadoProyecto.java              (Enum mejorado)
```

**Specifications:**
```
✨ domain/specification/Specification.java
✨ domain/specification/PuedeReenviarFormatoASpec.java
✨ domain/specification/PuedeSubirAnteproyectoSpec.java
✨ domain/specification/EsDirectorDelProyectoSpec.java
```

**Domain Events:**
```
✨ domain/event/DomainEvent.java
✨ domain/event/FormatoACreado.java
✨ domain/event/FormatoAEvaluado.java
✨ domain/event/FormatoAReenviado.java
✨ domain/event/AnteproyectoSubido.java
✨ domain/event/EvaluadoresAsignados.java
```

**Exceptions:**
```
✨ domain/exception/DomainException.java
✨ domain/exception/ProyectoNotFoundException.java
✨ domain/exception/MaximosIntentosExcedidosException.java
✨ domain/exception/FormatoANoAprobadoException.java
✨ domain/exception/UsuarioNoAutorizadoException.java
✨ domain/exception/EstadoInvalidoException.java
```

#### Application Layer

**Ports IN:**
```
✨ application/port/in/ICrearFormatoAUseCase.java
✨ application/port/in/IReenviarFormatoAUseCase.java
✨ application/port/in/IEvaluarFormatoAUseCase.java
✨ application/port/in/ISubirAnteproyectoUseCase.java
✨ application/port/in/IAsignarEvaluadoresUseCase.java
✨ application/port/in/IObtenerProyectoQuery.java
✨ application/port/in/IListarProyectosQuery.java
```

**Ports OUT:**
```
✨ application/port/out/IProyectoRepositoryPort.java
✨ application/port/out/IEventPublisherPort.java
✨ application/port/out/INotificationPort.java
✨ application/port/out/IIdentityServicePort.java
✨ application/port/out/IFileStoragePort.java
```

**Use Cases:**
```
✨ application/usecase/formatoa/CrearFormatoAUseCase.java
✨ application/usecase/formatoa/ReenviarFormatoAUseCase.java
✨ application/usecase/formatoa/EvaluarFormatoAUseCase.java
✨ application/usecase/anteproyecto/SubirAnteproyectoUseCase.java
✨ application/usecase/anteproyecto/AsignarEvaluadoresUseCase.java
✨ application/usecase/query/ObtenerProyectoQuery.java
✨ application/usecase/query/ListarProyectosQuery.java
```

#### Infrastructure Layer

**Adapters IN:**
```
✨ infrastructure/adapter/in/rest/FormatoAControllerV2.java
✨ infrastructure/adapter/in/rest/AnteproyectoControllerV2.java
✨ infrastructure/adapter/in/rest/ProyectoQueryController.java
```

**Adapters OUT:**
```
✨ infrastructure/adapter/out/persistence/ProyectoRepositoryAdapter.java
✨ infrastructure/adapter/out/persistence/ProyectoJpaRepository.java
✨ infrastructure/adapter/out/persistence/entity/ProyectoEntity.java
✨ infrastructure/adapter/out/persistence/mapper/ProyectoMapper.java
✨ infrastructure/adapter/out/messaging/RabbitMQEventPublisher.java
✨ infrastructure/adapter/out/messaging/RabbitMQNotificationAdapter.java
✨ infrastructure/adapter/out/client/IdentityServiceAdapter.java
✨ infrastructure/adapter/out/filesystem/LocalFileStorageAdapter.java
```

**Configuration:**
```
✨ infrastructure/config/BeanConfiguration.java
✨ infrastructure/config/HexagonalArchitectureConfig.java
```

### 9.2 Archivos a MODIFICAR

```
📝 infrastructure/config/RabbitConfig.java
   - Agregar configuración para nuevos eventos

📝 infrastructure/config/OpenApiConfig.java
   - Documentar nuevos endpoints v2

📝 src/main/resources/application.yml
   - Agregar feature flags
   - Configurar paths de archivos
```

### 9.3 Archivos a DEPRECAR (luego eliminar)

```
❌ controller/SubmissionController.java          → Reemplazado por v2
❌ controller/FormatoAController.java            → Reemplazado por v2
❌ controller/AnteproyectoController.java        → Reemplazado por v2
❌ service/SubmissionService.java                → Reemplazado por Use Cases
❌ domain/model/ProyectoSubmission.java          → Reemplazado por Proyecto
❌ domain/model/ProyectoGrado.java               → NO SE USA
❌ domain/model/FormatoA.java                    → NO SE USA
❌ domain/state/* (9 archivos)                   → Lógica integrada en Proyecto
❌ infraestructure/repository/IProyectoGradoRepository.java
❌ infraestructure/repository/IFormatoARepository.java
```

### 9.4 Archivos a MANTENER (sin cambios)

```
✅ domain/model/Anteproyecto.java               → Adaptar a nuevo aggregate
✅ domain/model/enumModalidad.java              → Renombrar a Modalidad
✅ domain/model/enumProgram.java                → Mantener
✅ service/NotificationPublisher.java           → Adaptar como adaptador
✅ service/ProgressEventPublisher.java          → Adaptar como adaptador
✅ service/IdentityClient.java                  → Adaptar como adaptador
✅ config/WebConfig.java                        → Mantener
✅ config/GlobalExceptionHandler.java           → Extender para nuevas excepciones
✅ util/SecurityRules.java                      → Mantener
```

---

## 10. TESTING STRATEGY

### 10.1 Tests de Dominio (Puros)

**Objetivo:** 100% cobertura del dominio sin mocks

```java
// ProyectoTest.java
class ProyectoTest {
    
    @Test
    void cuandoCrearProyecto_debeGenerarEventoFormatoACreado() {
        // Arrange
        Titulo titulo = Titulo.of("Proyecto de prueba");
        // ... otros value objects
        
        // Act
        Proyecto proyecto = Proyecto.crearConFormatoA(...);
        
        // Assert
        assertThat(proyecto.obtenerEventosPendientes())
            .hasSize(1)
            .first()
            .isInstanceOf(FormatoACreado.class);
    }
    
    @Test
    void cuandoEvaluarFormatoAAprobado_debeTransicionarAEstadoAprobado() {
        // Arrange
        Proyecto proyecto = crearProyectoEnEvaluacion();
        
        // Act
        proyecto.evaluarFormatoA(true, "Aprobado", 1L);
        
        // Assert
        assertThat(proyecto.getEstado())
            .isEqualTo(EstadoProyecto.FORMATO_A_APROBADO);
    }
    
    @Test
    void cuandoReenviarFormatoAConMaximoIntentos_debeLanzarExcepcion() {
        // Arrange
        Proyecto proyecto = crearProyectoConTresIntentos();
        
        // Act & Assert
        assertThatThrownBy(() -> proyecto.reenviarFormatoA(nuevoPdf, null))
            .isInstanceOf(MaximosIntentosExcedidosException.class);
    }
}
```

### 10.2 Tests de Casos de Uso

**Objetivo:** Validar orquestación con mocks de puertos

```java
@ExtendWith(MockitoExtension.class)
class CrearFormatoAUseCaseTest {
    
    @Mock
    private IProyectoRepositoryPort repositoryPort;
    
    @Mock
    private IFileStoragePort fileStoragePort;
    
    @Mock
    private IEventPublisherPort eventPublisherPort;
    
    @InjectMocks
    private CrearFormatoAUseCase useCase;
    
    @Test
    void cuandoCrearFormatoA_debeGuardarProyectoYPublicarEventos() {
        // Arrange
        CrearFormatoARequest request = crearRequestValido();
        when(fileStoragePort.guardarArchivo(any(), any(), any()))
            .thenReturn("/path/to/file.pdf");
        when(repositoryPort.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        ProyectoResponse response = useCase.crear(request, 1L);
        
        // Assert
        assertThat(response).isNotNull();
        verify(repositoryPort, times(1)).save(any(Proyecto.class));
        verify(eventPublisherPort, times(1)).publishAll(anyList());
    }
}
```

### 10.3 Tests de Integración

**Objetivo:** Validar flujo completo con BD en memoria

```java
@SpringBootTest
@AutoConfigureTestDatabase
class FormatoAIntegrationTest {
    
    @Autowired
    private ICrearFormatoAUseCase crearUseCase;
    
    @Autowired
    private IProyectoRepositoryPort repository;
    
    @Test
    @Transactional
    void flujoCompletoCrearYEvaluarFormatoA() {
        // 1. Crear Formato A
        CrearFormatoARequest request = crearRequestValido();
        ProyectoResponse response = crearUseCase.crear(request, 1L);
        
        // 2. Verificar que se guardó en BD
        Optional<Proyecto> proyecto = repository.findById(ProyectoId.of(response.getId()));
        assertThat(proyecto).isPresent();
        
        // 3. Evaluar Formato A
        // ... continuar flujo
    }
}
```

### 10.4 Tests de Arquitectura (ArchUnit)

```java
class ArchitectureTest {
    
    @Test
    void dominioNoDebeDependdeDeInfraestructura() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..application..")
            .check(classes);
    }
    
    @Test
    void useCasesDebenImplementarInterfaces() {
        classes()
            .that().resideInAPackage("..application.usecase..")
            .should().implement(UseCase.class)
            .check(classes);
    }
    
    @Test
    void agregatesDebenSerPublicos() {
        classes()
            .that().resideInAPackage("..domain.model..")
            .and().haveSimpleNameEndingWith("Aggregate")
            .should().bePublic()
            .check(classes);
    }
}
```

---

## 11. CONCLUSIONES Y RECOMENDACIONES

### 11.1 Beneficios Esperados

✅ **Mantenibilidad:** Lógica de negocio clara y testeable  
✅ **Testabilidad:** Dominio puro sin dependencias externas  
✅ **Flexibilidad:** Fácil cambiar implementaciones (BD, mensajería, etc.)  
✅ **Escalabilidad:** Agregar nuevos casos de uso sin afectar existentes  
✅ **Documentación:** Arquitectura auto-documentada por estructura  

### 11.2 Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Romper funcionalidad existente | Media | Alto | Migración gradual con feature flags |
| Performance degradada | Baja | Medio | Benchmarks antes/después |
| Curva de aprendizaje del equipo | Alta | Medio | Capacitación y pair programming |
| Tiempo de desarrollo excede estimación | Media | Medio | Priorizar fases críticas |

### 11.3 Próximos Pasos

1. **Revisión de arquitectura** con el equipo técnico
2. **Aprobación del plan de migración** por stakeholders
3. **Configurar entorno de desarrollo** con feature flags
4. **Iniciar Fase 1** (preparación)
5. **Establecer métricas** de performance y calidad
6. **Documentar decisiones arquitectónicas** (ADRs)

### 11.4 Métricas de Éxito

- ✅ 100% cobertura de tests en dominio
- ✅ 80%+ cobertura de tests en aplicación
- ✅ 0 violaciones de reglas de ArchUnit
- ✅ Performance igual o mejor que versión actual
- ✅ 0 regresiones en funcionalidad existente
- ✅ Documentación técnica completa

---

## 12. RECURSOS Y REFERENCIAS

### 12.1 Documentación de Referencia

- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)
- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Implementing Domain-Driven Design - Vaughn Vernon](https://vaughnvernon.com/)

### 12.2 Herramientas Recomendadas

- **ArchUnit** - Validar reglas de arquitectura en tests
- **MapStruct** - Mapeo automático entre capas
- **TestContainers** - Tests de integración con BD real
- **JaCoCo** - Cobertura de código
- **SonarQube** - Análisis de calidad de código

---

## 13. COMPARATIVA: ANTES VS DESPUÉS

### 13.1 Arquitectura Antes (En Capas)

```
❌ PROBLEMAS:
- SubmissionService con 1000+ líneas
- Entidades JPA mezcladas con lógica de negocio
- Difícil testear sin BD
- Acoplamiento a Spring/JPA
- Duplicación de modelos (ProyectoSubmission, ProyectoGrado)
```

```java
// ANTES: Todo en el Service
@Service
public class SubmissionService {
    @Autowired
    private SubmissionRepository repository;
    
    public IdResponse crearFormatoA(...) {
        // 1. Lógica de validación
        if (pdf == null) throw new Exception();
        
        // 2. Lógica de negocio
        ProyectoSubmission proyecto = new ProyectoSubmission();
        proyecto.setTitulo(data.getTitulo());
        // ... 50 líneas más
        
        // 3. Persistencia
        repository.save(proyecto);
        
        // 4. Eventos
        eventPublisher.publish(...);
        
        // 5. Notificaciones
        notificationPublisher.notify(...);
        
        return new IdResponse(proyecto.getId());
    }
}
```

### 13.2 Arquitectura Después (Hexagonal)

```
✅ MEJORAS:
- Dominio puro sin dependencias
- Casos de uso pequeños y enfocados
- 100% testeable sin infraestructura
- Independiente de frameworks
- Modelo único y claro (Proyecto)
```

```java
// DESPUÉS: Dominio puro
public class Proyecto {
    // Solo lógica de negocio
    public void evaluarFormatoA(boolean aprobado, String comentarios, Long evaluadorId) {
        if (aprobado) {
            this.estado = EstadoProyecto.FORMATO_A_APROBADO;
        } else {
            this.formatoA.incrementarIntentos();
            if (this.formatoA.haAlcanzadoMaximoIntentos()) {
                this.estado = EstadoProyecto.FORMATO_A_RECHAZADO;
            }
        }
        registrarEvento(new FormatoAEvaluado(...));
    }
}

// Use Case orquesta
@Service
public class EvaluarFormatoAUseCase implements IEvaluarFormatoAUseCase {
    public ProyectoResponse evaluar(Long proyectoId, EvaluarFormatoARequest request, Long evaluadorId) {
        Proyecto proyecto = repositoryPort.findById(ProyectoId.of(proyectoId))
            .orElseThrow(() -> new ProyectoNotFoundException(proyectoId));
        
        proyecto.evaluarFormatoA(request.isAprobado(), request.getComentarios(), evaluadorId);
        
        Proyecto updated = repositoryPort.save(proyecto);
        eventPublisherPort.publishAll(updated.obtenerEventosPendientes());
        
        return ProyectoResponse.fromDomain(updated);
    }
}
```

### 13.3 Testabilidad Antes vs Después

**ANTES:**
```java
// Necesita BD, Spring Context, RabbitMQ
@SpringBootTest
class SubmissionServiceTest {
    @Autowired
    private SubmissionService service;
    
    @Test
    void test() {
        // Test lento y frágil
        service.crearFormatoA(...);
    }
}
```

**DESPUÉS:**
```java
// Test puro sin dependencias
class ProyectoTest {
    @Test
    void cuandoEvaluarAprobado_debeTransicionarAAprobado() {
        // Arrange
        Proyecto proyecto = Proyecto.crearConFormatoA(...);
        proyecto.presentarAlCoordinador();
        
        // Act
        proyecto.evaluarFormatoA(true, "Excelente", 1L);
        
        // Assert
        assertThat(proyecto.getEstado())
            .isEqualTo(EstadoProyecto.FORMATO_A_APROBADO);
    }
}
```

---

## 14. EJEMPLO COMPLETO: FLUJO RF2 (CREAR FORMATO A)

### 14.1 Request HTTP

```http
POST /api/v2/submissions/formatoA
Content-Type: multipart/form-data
X-User-Id: 123
X-User-Role: DOCENTE

{
  "data": {
    "titulo": "Sistema de gestión académica",
    "modalidad": "INVESTIGACION",
    "objetivoGeneral": "Desarrollar un sistema...",
    "objetivosEspecificos": ["Objetivo 1", "Objetivo 2"],
    "estudiante1Id": 456,
    "estudiante2Id": null,
    "codirectorId": null
  },
  "pdf": <archivo.pdf>,
  "carta": null
}
```

### 14.2 Flujo Completo

```
1. HTTP Request
   ↓
2. FormatoAControllerV2 (Adaptador IN)
   - Valida headers
   - Convierte MultipartFile a InputStream
   - Crea CrearFormatoARequest
   ↓
3. CrearFormatoAUseCase (Puerto IN)
   - Valida rol (via IIdentityServicePort)
   - Guarda archivos (via IFileStoragePort)
   ↓
4. Proyecto.crearConFormatoA() (Dominio)
   - Crea Value Objects
   - Valida invariantes
   - Registra evento FormatoACreado
   ↓
5. IProyectoRepositoryPort.save() (Puerto OUT)
   ↓
6. ProyectoRepositoryAdapter (Adaptador OUT)
   - Convierte Proyecto → ProyectoEntity
   - Guarda en BD via JPA
   - Convierte ProyectoEntity → Proyecto
   ↓
7. IEventPublisherPort.publishAll() (Puerto OUT)
   ↓
8. RabbitMQEventPublisher (Adaptador OUT)
   - Publica FormatoACreado a RabbitMQ
   ↓
9. INotificationPort.notificar() (Puerto OUT)
   ↓
10. RabbitMQNotificationAdapter (Adaptador OUT)
    - Publica notificación al coordinador
    ↓
11. ProyectoResponse retorna al controlador
    ↓
12. HTTP Response 201 Created
```

### 14.3 Código Completo del Flujo

**1. Controller (Adaptador IN)**
```java
@RestController
@RequestMapping("/api/v2/submissions/formatoA")
public class FormatoAControllerV2 {
    
    private final ICrearFormatoAUseCase crearUseCase;
    
    @PostMapping
    public ResponseEntity<ProyectoResponse> crear(
            @RequestHeader("X-User-Id") Long userId,
            @RequestPart("data") CrearFormatoARequest request,
            @RequestPart("pdf") MultipartFile pdf) {
        
        request.setPdfStream(pdf.getInputStream());
        ProyectoResponse response = crearUseCase.crear(request, userId);
        return ResponseEntity.status(201).body(response);
    }
}
```

**2. Use Case (Puerto IN)**
```java
@Service
public class CrearFormatoAUseCase implements ICrearFormatoAUseCase {
    
    private final IProyectoRepositoryPort repositoryPort;
    private final IFileStoragePort fileStoragePort;
    private final IEventPublisherPort eventPublisherPort;
    private final INotificationPort notificationPort;
    
    @Override
    @Transactional
    public ProyectoResponse crear(CrearFormatoARequest request, Long userId) {
        // Guardar archivo
        String rutaPdf = fileStoragePort.guardarArchivo(
            request.getPdfStream(), "formatoA.pdf", "uploads/" + userId
        );
        
        // Crear aggregate
        Proyecto proyecto = Proyecto.crearConFormatoA(
            Titulo.of(request.getTitulo()),
            request.getModalidad(),
            ObjetivosProyecto.of(request.getObjetivoGeneral(), request.getObjetivosEspecificos()),
            Participantes.of(userId, null, request.getEstudiante1Id(), null),
            ArchivoAdjunto.pdf(rutaPdf, "formatoA.pdf"),
            null
        );
        
        // Persistir
        Proyecto guardado = repositoryPort.save(proyecto);
        
        // Publicar eventos
        eventPublisherPort.publishAll(guardado.obtenerEventosPendientes());
        guardado.limpiarEventos();
        
        // Notificar
        notificationPort.notificarCoordinadorFormatoAEnviado(guardado.getId().getValue(), 1);
        
        return ProyectoResponse.fromDomain(guardado);
    }
}
```

**3. Domain Model**
```java
public class Proyecto {
    
    public static Proyecto crearConFormatoA(
            Titulo titulo,
            Modalidad modalidad,
            ObjetivosProyecto objetivos,
            Participantes participantes,
            ArchivoAdjunto pdfFormatoA,
            ArchivoAdjunto cartaAceptacion) {
        
        Proyecto proyecto = new Proyecto();
        proyecto.id = ProyectoId.generar();
        proyecto.titulo = titulo;
        proyecto.modalidad = modalidad;
        proyecto.objetivos = objetivos;
        proyecto.participantes = participantes;
        proyecto.estado = EstadoProyecto.FORMATO_A_DILIGENCIADO;
        proyecto.formatoA = new FormatoAInfo(1, pdfFormatoA, cartaAceptacion);
        proyecto.fechaCreacion = LocalDateTime.now();
        
        proyecto.registrarEvento(new FormatoACreado(
            proyecto.id, proyecto.titulo, proyecto.modalidad, proyecto.participantes, 1
        ));
        
        return proyecto;
    }
}
```

**4. Repository Adapter (Adaptador OUT)**
```java
@Component
public class ProyectoRepositoryAdapter implements IProyectoRepositoryPort {
    
    private final ProyectoJpaRepository jpaRepository;
    private final ProyectoMapper mapper;
    
    @Override
    public Proyecto save(Proyecto proyecto) {
        ProyectoEntity entity = mapper.toEntity(proyecto);
        ProyectoEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

---

## 15. RESUMEN EJECUTIVO PARA STAKEHOLDERS

### Para Gerencia Técnica

**Problema Actual:**
- Código difícil de mantener (service de 1000+ líneas)
- Tests lentos y frágiles
- Alto acoplamiento a frameworks
- Duplicación de lógica

**Solución Propuesta:**
- Migrar a Arquitectura Hexagonal + DDD
- Separar dominio de infraestructura
- Mejorar testabilidad y mantenibilidad

**Inversión:**
- 6 semanas de desarrollo
- 1 desarrollador senior full-time
- Sin riesgo de romper funcionalidad (migración gradual)

**ROI:**
- 70% reducción en tiempo de desarrollo de nuevas features
- 90% reducción en tiempo de tests
- 50% reducción en bugs de lógica de negocio
- Facilita onboarding de nuevos desarrolladores

### Para Equipo de Desarrollo

**Cambios Principales:**
- Nueva estructura de carpetas (domain/application/infrastructure)
- Lógica de negocio se mueve a `Proyecto` aggregate
- Use Cases reemplazan a `SubmissionService`
- Interfaces (puertos) entre capas

**Beneficios Inmediatos:**
- Tests unitarios en segundos (sin BD)
- Código más legible y organizado
- Fácil mockear dependencias
- Reglas de negocio claras

**Curva de Aprendizaje:**
- Semana 1-2: Entender conceptos (DDD, Hexagonal)
- Semana 3-4: Implementar primeros use cases
- Semana 5-6: Autónomos

### Para QA

**Cambios en Testing:**
- Más tests unitarios (rápidos)
- Menos tests de integración (lentos)
- Tests de arquitectura automáticos

**Beneficios:**
- Suite de tests 10x más rápida
- Mayor cobertura de código
- Detección temprana de bugs

---

## 16. DECISION RECORDS (ADRs)

### ADR-001: Usar Arquitectura Hexagonal

**Status:** Propuesto  
**Fecha:** 2025-12-09  
**Contexto:** Sistema actual con alto acoplamiento y baja testabilidad  
**Decisión:** Migrar a Arquitectura Hexagonal con DDD  
**Consecuencias:**
- ✅ Mejor testabilidad y mantenibilidad
- ✅ Independencia de frameworks
- ⚠️ Requiere capacitación del equipo
- ⚠️ Más código boilerplate inicial

### ADR-002: Un Solo Aggregate (Proyecto)

**Status:** Propuesto  
**Fecha:** 2025-12-09  
**Contexto:** Actualmente hay 3 modelos (ProyectoSubmission, ProyectoGrado, FormatoA)  
**Decisión:** Consolidar en un único aggregate `Proyecto`  
**Consecuencias:**
- ✅ Elimina duplicación
- ✅ Invariantes más fáciles de mantener
- ⚠️ Aggregate más grande (pero manejable)

### ADR-003: Migración Gradual con Feature Flags

**Status:** Propuesto  
**Fecha:** 2025-12-09  
**Contexto:** No podemos romper funcionalidad existente  
**Decisión:** Migración en 6 fases con feature flags  
**Consecuencias:**
- ✅ Sin downtime
- ✅ Fácil rollback
- ⚠️ Mantener 2 implementaciones temporalmente

---

## ANEXO A: ESTRUCTURA COMPLETA DE CARPETAS

```
submission-service/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── co/unicauca/submission/
│   │   │       │
│   │   │       ├── domain/                         # CAPA DE DOMINIO
│   │   │       │   ├── model/
│   │   │       │   │   ├── Proyecto.java          # Aggregate Root ⭐
│   │   │       │   │   ├── ProyectoId.java
│   │   │       │   │   ├── Titulo.java
│   │   │       │   │   ├── ObjetivosProyecto.java
│   │   │       │   │   ├── Participantes.java
│   │   │       │   │   ├── ArchivoAdjunto.java
│   │   │       │   │   ├── Evaluacion.java
│   │   │       │   │   ├── FormatoAInfo.java
│   │   │       │   │   ├── AnteproyectoInfo.java
│   │   │       │   │   ├── EstadoProyecto.java
│   │   │       │   │   ├── Modalidad.java
│   │   │       │   │   └── Programa.java
│   │   │       │   │
│   │   │       │   ├── repository/                # Puertos (interfaces)
│   │   │       │   │   └── IProyectoRepository.java
│   │   │       │   │
│   │   │       │   ├── service/                   # Domain Services
│   │   │       │   │   ├── ProyectoValidationService.java
│   │   │       │   │   └── EstadoTransitionService.java
│   │   │       │   │
│   │   │       │   ├── specification/             # Business Rules
│   │   │       │   │   ├── Specification.java
│   │   │       │   │   ├── PuedeReenviarFormatoASpec.java
│   │   │       │   │   ├── PuedeSubirAnteproyectoSpec.java
│   │   │       │   │   └── EsDirectorDelProyectoSpec.java
│   │   │       │   │
│   │   │       │   ├── event/                     # Domain Events
│   │   │       │   │   ├── DomainEvent.java
│   │   │       │   │   ├── FormatoACreado.java
│   │   │       │   │   ├── FormatoAEvaluado.java
│   │   │       │   │   ├── FormatoAReenviado.java
│   │   │       │   │   ├── AnteproyectoSubido.java
│   │   │       │   │   ├── EvaluadoresAsignados.java
│   │   │       │   │   └── AnteproyectoEvaluado.java
│   │   │       │   │
│   │   │       │   └── exception/                 # Domain Exceptions
│   │   │       │       ├── DomainException.java
│   │   │       │       ├── ProyectoNotFoundException.java
│   │   │       │       ├── MaximosIntentosExcedidosException.java
│   │   │       │       ├── FormatoANoAprobadoException.java
│   │   │       │       ├── UsuarioNoAutorizadoException.java
│   │   │       │       └── EstadoInvalidoException.java
│   │   │       │
│   │   │       ├── application/                    # CAPA DE APLICACIÓN
│   │   │       │   ├── port/
│   │   │       │   │   ├── in/                    # Input Ports (Use Cases)
│   │   │       │   │   │   ├── ICrearFormatoAUseCase.java
│   │   │       │   │   │   ├── IReenviarFormatoAUseCase.java
│   │   │       │   │   │   ├── IEvaluarFormatoAUseCase.java
│   │   │       │   │   │   ├── ISubirAnteproyectoUseCase.java
│   │   │       │   │   │   ├── IAsignarEvaluadoresUseCase.java
│   │   │       │   │   │   ├── IEvaluarAnteproyectoUseCase.java
│   │   │       │   │   │   ├── IObtenerProyectoQuery.java
│   │   │       │   │   │   └── IListarProyectosQuery.java
│   │   │       │   │   │
│   │   │       │   │   └── out/                   # Output Ports
│   │   │       │   │       ├── IProyectoRepositoryPort.java
│   │   │       │   │       ├── IEventPublisherPort.java
│   │   │       │   │       ├── INotificationPort.java
│   │   │       │   │       ├── IIdentityServicePort.java
│   │   │       │   │       └── IFileStoragePort.java
│   │   │       │   │
│   │   │       │   ├── usecase/                   # Use Case Implementations
│   │   │       │   │   ├── formatoa/
│   │   │       │   │   │   ├── CrearFormatoAUseCase.java
│   │   │       │   │   │   ├── ReenviarFormatoAUseCase.java
│   │   │       │   │   │   └── EvaluarFormatoAUseCase.java
│   │   │       │   │   ├── anteproyecto/
│   │   │       │   │   │   ├── SubirAnteproyectoUseCase.java
│   │   │       │   │   │   ├── AsignarEvaluadoresUseCase.java
│   │   │       │   │   │   └── EvaluarAnteproyectoUseCase.java
│   │   │       │   │   └── query/
│   │   │       │   │       ├── ObtenerProyectoQuery.java
│   │   │       │   │       └── ListarProyectosQuery.java
│   │   │       │   │
│   │   │       │   └── dto/                       # Application DTOs
│   │   │       │       ├── request/
│   │   │       │       │   ├── CrearFormatoARequest.java
│   │   │       │       │   ├── ReenviarFormatoARequest.java
│   │   │       │       │   ├── EvaluarFormatoARequest.java
│   │   │       │       │   ├── SubirAnteproyectoRequest.java
│   │   │       │       │   └── AsignarEvaluadoresRequest.java
│   │   │       │       └── response/
│   │   │       │           ├── ProyectoResponse.java
│   │   │       │           ├── FormatoAResponse.java
│   │   │       │           └── AnteproyectoResponse.java
│   │   │       │
│   │   │       └── infrastructure/                 # CAPA DE INFRAESTRUCTURA
│   │   │           ├── adapter/
│   │   │           │   ├── in/                    # Driving Adapters
│   │   │           │   │   ├── rest/
│   │   │           │   │   │   ├── FormatoAControllerV2.java
│   │   │           │   │   │   ├── AnteproyectoControllerV2.java
│   │   │           │   │   │   └── ProyectoQueryController.java
│   │   │           │   │   └── event/
│   │   │           │   │       └── (Event Listeners)
│   │   │           │   │
│   │   │           │   └── out/                   # Driven Adapters
│   │   │           │       ├── persistence/
│   │   │           │       │   ├── ProyectoJpaRepository.java
│   │   │           │       │   ├── ProyectoRepositoryAdapter.java
│   │   │           │       │   ├── entity/
│   │   │           │       │   │   ├── ProyectoEntity.java
│   │   │           │       │   │   └── EstadoProyectoConverter.java
│   │   │           │       │   └── mapper/
│   │   │           │       │       └── ProyectoMapper.java
│   │   │           │       │
│   │   │           │       ├── messaging/
│   │   │           │       │   ├── RabbitMQEventPublisher.java
│   │   │           │       │   ├── RabbitMQNotificationAdapter.java
│   │   │           │       │   └── config/
│   │   │           │       │       └── RabbitMQConfig.java
│   │   │           │       │
│   │   │           │       ├── client/
│   │   │           │       │   ├── IdentityServiceClient.java
│   │   │           │       │   └── IdentityServiceAdapter.java
│   │   │           │       │
│   │   │           │       └── filesystem/
│   │   │           │           ├── LocalFileStorageAdapter.java
│   │   │           │           └── FileStorageConfig.java
│   │   │           │
│   │   │           ├── config/
│   │   │           │   ├── BeanConfiguration.java
│   │   │           │   ├── SecurityConfiguration.java
│   │   │           │   ├── OpenApiConfiguration.java
│   │   │           │   └── HexagonalArchitectureConfig.java
│   │   │           │
│   │   │           ├── mapper/
│   │   │           │   ├── ProyectoRestMapper.java
│   │   │           │   └── ProyectoEntityMapper.java
│   │   │           │
│   │   │           └── exception/
│   │   │               └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           └── V2__hexagonal_migration.sql
│   │
│   └── test/
│       └── java/co/unicauca/submission/
│           ├── domain/
│           │   ├── model/
│           │   │   └── ProyectoTest.java
│           │   ├── service/
│           │   │   └── ProyectoValidationServiceTest.java
│           │   └── specification/
│           │       └── PuedeReenviarFormatoASpecTest.java
│           │
│           ├── application/
│           │   └── usecase/
│           │       ├── CrearFormatoAUseCaseTest.java
│           │       └── SubirAnteproyectoUseCaseTest.java
│           │
│           └── infrastructure/
│               ├── adapter/
│               │   ├── rest/
│               │   │   └── FormatoAControllerTest.java
│               │   └── persistence/
│               │       └── ProyectoRepositoryAdapterTest.java
│               └── ArchitectureTest.java
│
├── pom.xml
├── README.md
├── ARQUITECTURA_HEXAGONAL.md
└── docker-compose.yml
```

---

**FIN DEL DOCUMENTO**

---

**Próximos pasos sugeridos:**
1. Revisar este documento con el equipo técnico
2. Crear un spike técnico (2-3 días) para validar la viabilidad
3. Obtener aprobación de stakeholders
4. Iniciar Fase 1 del plan de migración

**Contacto para dudas:**
- Arquitecto de Software
- Tech Lead del Proyecto


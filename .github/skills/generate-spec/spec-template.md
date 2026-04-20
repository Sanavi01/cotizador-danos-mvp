---
id: SPEC-###
status: DRAFT
feature: nombre-del-feature
created: YYYY-MM-DD
updated: YYYY-MM-DD
author: spec-generator
version: "1.0"
related-specs: []
---

# Spec: [Nombre de la Funcionalidad]

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción
Resumen de la funcionalidad en 2-3 oraciones. Qué hace, para quién y qué problema resuelve.

### Requerimiento de Negocio
El requerimiento original tal como fue proporcionado por el usuario (o copiado de `.github/requirements/<feature>.md`).

### Historias de Usuario

#### HU-01: [Título descriptivo corto]

```
Como:        [rol del usuario — ej. Usuario autenticado, Administrador]
Quiero:      [acción o funcionalidad concreta]
Para:        [valor o beneficio esperado por el negocio]

Prioridad:   Alta / Media / Baja
Estimación:  XS / S / M / L / XL
Dependencias: HU-X, HU-Y o Ninguna
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: [nombre del escenario exitoso]
  Dado que:  [contexto inicial válido]
  Cuando:    [acción del usuario]
  Entonces:  [resultado esperado verificable]
```

**Error Path**
```gherkin
CRITERIO-1.2: [nombre del escenario de error]
  Dado que:  [contexto inicial]
  Cuando:    [acción inválida o datos incorrectos]
  Entonces:  [manejo del error esperado con código HTTP y mensaje]
```

**Edge Case** *(si aplica)*
```gherkin
CRITERIO-1.3: [nombre del caso borde]
  Dado que:  [contexto de borde]
  Cuando:    [acción en el límite]
  Entonces:  [resultado esperado en el límite]
```

### Reglas de Negocio
1. Regla de validación (ej. "el campo X es obligatorio y no puede superar 100 caracteres")
2. Regla de autorización (ej. "solo el Administrador puede eliminar")
3. Regla de integridad (ej. "el nombre debe ser único en la colección")

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripción |
|---------|---------|---------|-------------|
| `Cotizacion` | tabla `cotizaciones_danos` | nueva / modificada | agregado principal del dominio |

#### Campos del modelo
| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `numeroFolio` | string | sí | secuencial único | Identificador principal de la cotización |
| `estadoCotizacion` | string | sí | catálogo del dominio | Estado global del folio |
| `version` | long | sí | `@Version` | Control de concurrencia optimista |
| `fechaUltimaActualizacion` | datetime | sí | auto-actualizado | Marca de última modificación lógica |

#### Índices / Constraints
- Listar índices necesarios con su justificación de uso (búsqueda frecuente, unicidad, etc.)

### API Endpoints

#### POST /v1/[features]
- **Descripción**: Crea un nuevo recurso o ejecuta una operación de inicio del flujo
- **Auth requerida**: no / sesión demo si aplica
- **Request Body**:
  ```json
  { "campoEjemplo": "valor" }
  ```
- **Response 201**:
  ```json
  { "data": { "campoEjemplo": "valor" } }
  ```
- **Response 400**: campo obligatorio faltante o inválido
- **Response 409**: conflicto funcional o de versión
- **Response 422**: validación semántica si aplica
- **Headers requeridos**: listar `Idempotency-Key` si aplica

#### GET /v1/[features]
- **Descripción**: Consulta recurso, listado o estado del flujo
- **Auth requerida**: no / sesión demo si aplica
- **Response 200**:
  ```json
  { "data": [] }
  ```

#### PUT /v1/[features]/{id}
- **Descripción**: Reemplazo lógico de una sección funcional
- **Auth requerida**: no / sesión demo si aplica
- **Request Body**: payload contractual de la sección
- **Response 200**: recurso actualizado dentro de `data`
- **Response 404**: no encontrado
- **Response 409**: conflicto de versión

#### PATCH /v1/[features]/{id}
- **Descripción**: Actualización parcial focalizada
- **Auth requerida**: no / sesión demo si aplica
- **Request Body**: campos puntuales a actualizar
- **Response 200**: recurso actualizado
- **Response 404**: no encontrado
- **Response 409**: ya existe un recurso con ese nombre

### Diseño Frontend

#### Componentes nuevos
| Componente | Archivo | Props principales | Descripción |
|------------|---------|------------------|-------------|
| `QuoteProgressCard` | `components/QuoteProgressCard` | `state, sections, alerts` | Tarjeta de progreso del folio |
| `LocationForm` | `components/LocationForm` | `location, onSubmit, catalogs` | Formulario de captura/edición de ubicación |

#### Páginas nuevas
| Página | Archivo | Ruta | Protegida |
|--------|---------|------|-----------|
| `CotizadorPage` | `pages/CotizadorPage` | `/cotizador` | no |

#### Hooks y State
| Hook | Archivo | Retorna | Descripción |
|------|---------|---------|-------------|
| `useQuoteFlow` | `hooks/useQuoteFlow` | `{ quote, state, loading, error, actions }` | Orquesta el flujo funcional del folio |

#### Services (llamadas API)
| Función | Archivo | Endpoint |
|---------|---------|---------|
| `createFolio(payload, idempotencyKey)` | `services/quoteService` | `POST /v1/folios` |
| `getQuoteState(folio)` | `services/quoteService` | `GET /v1/quotes/{folio}/state` |
| `updateSection(folio, body)` | `services/quoteService` | `PUT /v1/...` |

### Arquitectura y Dependencias
- Paquetes nuevos requeridos: listar dependencias Spring Boot, React o automatización si aplican
- Servicios externos: listar integraciones con `plataforma-core-ohs` mock y otros adaptadores
- Impacto en punto de entrada de la app: registrar controller, router React, cliente HTTP o migración si aplica

### Notas de Implementación
> Observaciones técnicas, decisiones de diseño o advertencias para los agentes de desarrollo.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada ítem (`[x]`) al completarlo.
> El Orchestrator monitorea este checklist para determinar el progreso.

### Backend

#### Implementación
- [ ] Crear request/response DTOs del feature
- [ ] Implementar entidades/agregados y puertos del dominio
- [ ] Implementar casos de uso de aplicación
- [ ] Implementar adaptadores JPA/clientes HTTP necesarios
- [ ] Implementar controller(s) `/v1/...` y documentar OpenAPI
- [ ] Agregar o actualizar migraciones Flyway si aplica

#### Tests Backend
- [ ] Caso de uso happy path
- [ ] Caso de uso con error de negocio o conflicto de versión
- [ ] Controller con respuesta 200/201 esperada
- [ ] Controller con Problem Details para error relevante
- [ ] Adaptador/repositorio crítico según aplique

### Frontend

#### Implementación
- [ ] Crear servicio(s) Axios para endpoints del feature
- [ ] Crear hook/store del flujo
- [ ] Implementar componentes de UI y estilos
- [ ] Implementar página(s) y registrar ruta(s)
- [ ] Integrar sesión demo si la pantalla lo requiere

#### Tests Frontend
- [ ] Componente crítico renderiza estado correcto
- [ ] Componente dispara acción esperada
- [ ] Hook maneja carga exitosa
- [ ] Hook maneja error y estado vacío
- [ ] Página integra flujo principal

### QA
- [ ] Ejecutar skill `/gherkin-case-generator` → criterios CRITERIO-1.1, 1.2, 1.3
- [ ] Ejecutar skill `/risk-identifier` → clasificación ASD de riesgos
- [ ] Revisar cobertura de tests contra criterios de aceptación
- [ ] Validar que todas las reglas de negocio están cubiertas
- [ ] Actualizar estado spec: `status: IMPLEMENTED`

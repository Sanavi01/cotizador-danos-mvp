---
id: SPEC-001
status: APPROVED
feature: folios-e-idempotencia
created: 2026-04-20
updated: 2026-04-20
author: spec-generator
version: "1.0"
related-specs: []
---

# Spec: Folios e Idempotencia

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción
Esta funcionalidad permite crear el folio inicial de una cotizacion con control de idempotencia y consultar su estado base para continuar la captura sin duplicar registros. Es la puerta de entrada al flujo del cotizador y establece la identidad del agregado principal.

### Requerimiento de Negocio
Fuente principal: `.github/requirements/folios-e-idempotencia.md`.

Resumen del requerimiento base:
- Crear un nuevo numeroFolio secuencial y unico para una cotizacion nueva.
- Exigir idempotencia en `POST /v1/folios` mediante `Idempotency-Key`.
- Permitir abrir un folio existente para continuar la captura.
- Retornar errores funcionales con Problem Details.
- Retornar respuestas exitosas con envelope `data`.

### Historias de Usuario

#### HU-01: Crear un folio nuevo

```
Como:        Usuario del cotizador
Quiero:      solicitar un numeroFolio nuevo
Para:        iniciar la captura de una nueva cotizacion

Prioridad:   Alta
Estimación:  S
Dependencias: Catalogos y Validaciones Core
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Crear un folio nuevo de forma exitosa
  Dado que el usuario inicia una nueva cotizacion
  Cuando solicita la creacion del folio con una llave de idempotencia valida
  Entonces el sistema genera un numeroFolio secuencial unico
  Y crea la cotizacion en estado BORRADOR
  Y retorna el resultado en un envelope data
```

**Error Path**
```gherkin
CRITERIO-1.2: Reintentar la creacion con la misma llave de idempotencia
  Dado que ya existe una solicitud previa exitosa con la misma llave de idempotencia
  Cuando el cliente reintenta la operacion
  Entonces el sistema no crea un segundo numeroFolio
  Y retorna la misma respuesta funcional de la solicitud original
```

**Edge Case** *(si aplica)*
```gherkin
CRITERIO-1.3: Rechazar una llave de idempotencia vacia
  Dado que el cliente intenta crear un folio sin el header Idempotency-Key
  Cuando envia la solicitud de creacion
  Entonces el sistema rechaza la operacion
  Y responde con un error funcional en formato Problem Details
```

#### HU-02: Abrir un folio existente

```
Como:        Usuario del cotizador
Quiero:      ingresar un numeroFolio ya creado
Para:        continuar una cotizacion sin perder el avance previo

Prioridad:   Alta
Estimación:  S
Dependencias: Estado y Progreso de Cotizacion
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptación — HU-02

**Happy Path**
```gherkin
CRITERIO-2.1: Abrir un folio existente
  Dado que existe una cotizacion asociada a un numeroFolio
  Cuando el usuario consulta el folio desde la SPA
  Entonces el sistema devuelve el estado actual de la cotizacion
  Y permite continuar el flujo desde la ultima informacion persistida
```

**Error Path**
```gherkin
CRITERIO-2.2: Consultar un folio inexistente
  Dado que el numeroFolio no existe en el sistema
  Cuando el usuario intenta abrirlo
  Entonces el sistema responde con un error funcional en formato Problem Details
```

**Edge Case** *(si aplica)*
```gherkin
CRITERIO-2.3: Consultar un folio recien creado sin mas datos
  Dado que el folio fue creado correctamente pero aun no tiene informacion adicional capturada
  Cuando la SPA consulta su estado
  Entonces el sistema devuelve BORRADOR
  Y muestra que la cotizacion puede continuar editandose
```

### Reglas de Negocio
1. `numeroFolio` es el identificador principal de la cotizacion y debe ser secuencial y unico.
2. `POST /v1/folios` requiere el header `Idempotency-Key`.
3. Una misma llave de idempotencia no debe producir mas de una cotizacion efectiva.
4. La cotizacion creada inicia en estado `BORRADOR`.
5. Las respuestas exitosas deben usar envelope `data`.
6. Los errores deben usar Problem Details.
7. La consulta de estado no debe modificar la cotizacion.

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripción |
|---------|---------|---------|-------------|
| `Cotizacion` | tabla `cotizaciones_danos` | nueva | agregado principal del dominio, identificado por `numeroFolio` |
| `SolicitudIdempotente` | tabla `idempotency_keys` | nueva | rastrea la llave de idempotencia y la respuesta funcional asociada |

#### Campos del modelo
| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `numeroFolio` | string | sí | secuencial único | Identificador principal de la cotizacion |
| `estadoCotizacion` | string | sí | catálogo del dominio | Estado global del folio |
| `version` | long | sí | `@Version` | Control de concurrencia optimista |
| `fechaUltimaActualizacion` | datetime | sí | auto-actualizado | Marca de última modificación lógica |
| `idempotencyKey` | string | sí | no vacia, unica por solicitud efectiva | Llave para evitar duplicados en la creacion |
| `requestHash` | string | sí | consistente con el payload recibido | Huella de la solicitud original |
| `responsePayload` | json | sí | serializable | Respuesta funcional devuelta para reintentos idempotentes |
| `createdAt` | datetime | sí | auto-actualizado | Fecha de creación de la solicitud idempotente |

#### Índices / Constraints
- Índice único sobre `numeroFolio` para garantizar unicidad del folio.
- Índice único sobre `idempotencyKey` para evitar duplicidad de creaciones.
- Constraint para evitar que una misma llave de idempotencia se asocie a respuestas distintas.
- Índice por `estadoCotizacion` si se requieren consultas de seguimiento o tablero operativo.

### API Endpoints

#### POST /v1/folios
- **Descripción**: Crea un nuevo folio y la cotizacion inicial en estado `BORRADOR`.
- **Auth requerida**: no / sesión demo si aplica
- **Headers requeridos**:
  - `Idempotency-Key`: string no vacio, obligatorio
- **Request Body**:
  ```json
  {
    "origin": "spa"
  }
  ```
- **Response 201**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "estadoCotizacion": "BORRADOR",
      "version": 0,
      "fechaUltimaActualizacion": "2026-04-20T00:00:00Z"
    }
  }
  ```
- **Response 200**: reutilizacion idempotente de la respuesta original si la llave ya existia
- **Response 400**: body invalido o llave ausente/vacia
- **Response 409**: conflicto funcional o de version
- **Response 422**: validacion semantica si aplica

#### GET /v1/quotes/{folio}/state
- **Descripción**: Consulta el estado actual de una cotizacion por numeroFolio.
- **Auth requerida**: no / sesión demo si aplica
- **Path Parameters**:
  - `folio`: string del numeroFolio
- **Response 200**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "estadoCotizacion": "BORRADOR",
      "tieneAlertas": false,
      "seccionesCompletadas": [],
      "ubicacionesCalculables": 0,
      "ubicacionesIncompletas": 0,
      "version": 0,
      "fechaUltimaActualizacion": "2026-04-20T00:00:00Z"
    }
  }
  ```
- **Response 404**: numeroFolio inexistente
- **Response 409**: conflicto de version si la consulta se usa en un contexto de edicion concurrente

### Diseño Frontend

#### Componentes nuevos
| Componente | Archivo | Props principales | Descripción |
|------------|---------|------------------|-------------|
| `FolioCard` | `components/FolioCard` | `numeroFolio, estadoCotizacion, onOpen` | Tarjeta o resumen del folio creado |
| `IdempotencyNotice` | `components/IdempotencyNotice` | `message, variant` | Mensaje visual sobre reintentos y comportamiento idempotente |

#### Páginas nuevas
| Página | Archivo | Ruta | Protegida |
|--------|---------|------|-----------|
| `FolioPage` | `pages/FolioPage` | `/cotizador` | no |
| `QuoteStatePage` | `pages/QuoteStatePage` | `/quotes/:folio/state` | no |

#### Hooks y State
| Hook | Archivo | Retorna | Descripción |
|------|---------|---------|-------------|
| `useFolioCreation` | `hooks/useFolioCreation` | `{ createFolio, loading, error, folio }` | Orquesta la creacion idempotente del folio |
| `useQuoteState` | `hooks/useQuoteState` | `{ state, loading, error, refresh }` | Consulta el estado consolidado del folio |

#### Services (llamadas API)
| Función | Archivo | Endpoint |
|---------|---------|---------|
| `createFolio(payload, idempotencyKey)` | `services/folioService` | `POST /v1/folios` |
| `getQuoteState(folio)` | `services/folioService` | `GET /v1/quotes/{folio}/state` |

### Arquitectura y Dependencias
- Paquetes nuevos requeridos: entidad de dominio `Cotizacion`, adaptador de persistencia de idempotencia, DTOs de request/response y servicio de consulta de estado.
- Servicios externos: no hay integracion obligatoria; la logica de idempotencia debe resolverse dentro del backend principal.
- Impacto en punto de entrada de la app: registrar la ruta de creacion/consulta de folio en la SPA y exponer el controller correspondiente en backend.
- Observacion: no se identificaron modelos o rutas previas en el workspace; esta spec define el contrato base para iniciar el feature.

### Notas de Implementación
> La idempotencia debe ser estable y reproducible. La respuesta funcional asociada a una llave existente debe ser la misma para reintentos equivalentes.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada ítem (`[x]`) al completarlo.
> El Orchestrator monitorea este checklist para determinar el progreso.

### Backend

#### Implementación
- [ ] Crear request/response DTOs para la creacion de folios y la consulta de estado
- [ ] Implementar entidad `Cotizacion` con `numeroFolio`, `estadoCotizacion`, `version` y `fechaUltimaActualizacion`
- [ ] Implementar entidad o tabla de soporte para idempotencia
- [ ] Implementar caso de uso de creacion de folio con control de duplicados
- [ ] Implementar caso de uso de consulta de estado de cotizacion
- [ ] Implementar controller(s) `/v1/folios` y `/v1/quotes/{folio}/state`
- [ ] Agregar o actualizar migraciones Flyway si aplica
- [ ] Documentar OpenAPI del contrato

#### Tests Backend
- [ ] Caso de uso happy path de creacion de folio
- [ ] Caso de uso reintenta con misma Idempotency-Key y retorna la misma respuesta
- [ ] Caso de uso consulta folio inexistente
- [ ] Controller retorna 201 con envelope `data`
- [ ] Controller retorna Problem Details ante error funcional
- [ ] Validacion de constraint de idempotencia o unicidad segun aplique

### Frontend

#### Implementación
- [ ] Crear servicio Axios para creacion de folios y consulta de estado
- [ ] Crear hook para administrar el flujo de creacion idempotente
- [ ] Crear pagina de inicio o apertura de folio
- [ ] Crear pagina de estado del folio
- [ ] Mostrar mensajes claros de reintento, folio creado y errores funcionales
- [ ] Registrar rutas necesarias en la SPA

#### Tests Frontend
- [ ] Componente crítico renderiza el numeroFolio creado
- [ ] Hook maneja exito y reintento idempotente
- [ ] Hook maneja error cuando el folio no existe
- [ ] Pagina de estado renderiza los datos de progreso del folio

### QA
- [ ] Ejecutar skill `/gherkin-case-generator` → criterios CRITERIO-1.1, 1.2, 1.3, 2.1, 2.2, 2.3
- [ ] Ejecutar skill `/risk-identifier` → clasificar riesgos de idempotencia y consulta de estado
- [ ] Revisar cobertura de tests contra criterios de aceptación
- [ ] Validar que todas las reglas de negocio están cubiertas
- [ ] Actualizar estado spec: `status: IMPLEMENTED`

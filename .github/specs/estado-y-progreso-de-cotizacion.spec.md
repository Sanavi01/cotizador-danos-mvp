---
id: SPEC-006
status: DRAFT
feature: estado-y-progreso-de-cotizacion
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-001", "SPEC-002", "SPEC-003", "SPEC-004", "SPEC-005"]
---

# Spec: Estado y Progreso de Cotizacion

> **Estado:** `DRAFT` -> aprobar con `status: APPROVED` antes de iniciar implementacion.
> **Ciclo de vida:** DRAFT -> APPROVED -> IN_PROGRESS -> IMPLEMENTED -> DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripcion
Esta funcionalidad permite consultar el `estadoCotizacion` y el progreso operativo de una cotizacion para orientar al usuario sobre que secciones estan completas, cuales faltan y que ubicaciones ya pueden calcularse. Este endpoint es un resumen operativo del agregado, no una copia completa de la cotizacion. La respuesta consolida el avance del agregado sin modificarlo, exponiendo `version`, `fechaUltimaActualizacion`, `progreso`, `resumenUbicaciones`, alertas vigentes y, cuando exista, un resumen financiero compacto.

### Requerimiento de Negocio
Fuente principal: `.github/requirements/estado-y-progreso-de-cotizacion.md`.

Resumen del requerimiento base:
- Consultar el estado global de la cotizacion.
- Exponer el avance por seccion funcional.
- Informar ubicaciones calculables, ubicaciones incompletas y alertas vigentes.
- Exponer `version` y `fechaUltimaActualizacion` para soporte de concurrencia y trazabilidad.
- Responder con envelope `data` en exito y Problem Details en error.

### Historias de Usuario

#### HU-01: Visualizar el progreso del folio

```
Como:        usuario del cotizador
Quiero:      consultar el estado y progreso del folio
Para:        saber que me falta capturar antes de ejecutar el calculo

Prioridad:   Alta
Estimacion:  S
Dependencias: Datos Generales de Cotizacion, Configuracion de Layout de Ubicaciones, Gestion de Ubicaciones, Opciones de Cobertura
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Consultar el estado consolidado de la cotizacion
  Dado que existe una cotizacion con informacion parcial o completa
  Cuando el usuario consulta el estado del folio
  Entonces el sistema devuelve `estadoCotizacion`
  Y reporta el progreso por seccion funcional
  Y resume ubicaciones calculables, incompletas, invalidas y alertas vigentes
```

**Happy Path**
```gherkin
CRITERIO-1.2: Reflejar la ultima actualizacion del folio
  Dado que la cotizacion ha recibido cambios funcionales
  Cuando se consulta el estado
  Entonces la respuesta incluye version y fechaUltimaActualizacion actuales
```

**Edge Case**
```gherkin
CRITERIO-1.3: Consultar un folio existente sin secciones adicionales
  Dado que existe una cotizacion recien creada con estado BORRADOR
  Cuando el usuario consulta el estado del folio
  Entonces el sistema devuelve el folio con las secciones aun pendientes
  Y no modifica la cotizacion consultada
```

#### HU-02: Reflejar transiciones de estado coherentes

```
Como:        sistema de cotizacion
Quiero:      actualizar el estado global segun el avance real del folio
Para:        guiar la operacion y el calculo de forma consistente

Prioridad:   Alta
Estimacion:  M
Dependencias: Calculo de Prima y Resultado Financiero
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-02

**Happy Path**
```gherkin
CRITERIO-2.1: Pasar de BORRADOR a EN_CAPTURA
  Dado que ya existe un numeroFolio creado
  Cuando el usuario empieza a registrar informacion funcional del folio
  Entonces el estado global deja de ser BORRADOR y refleja captura en progreso
```

**Happy Path**
```gherkin
CRITERIO-2.2: Marcar el folio como listo o calculado
  Dado que el folio cumple las condiciones necesarias para calcular o ya fue calculado
  Cuando el sistema consolida el avance real del flujo
  Entonces el estado global se actualiza a LISTA_PARA_CALCULO o CALCULADA segun corresponda
```

**Edge Case**
```gherkin
CRITERIO-2.3: Reflejar una ubicacion incompleta sin bloquear el folio
  Dado que existe al menos una ubicacion incompleta dentro de la cotizacion
  Cuando el usuario consulta el estado del folio
  Entonces el resumen operativo muestra ubicaciones incompletas y alertas vigentes
  Y el resto del folio sigue siendo visible y navegable
```

### Reglas de Negocio
1. Los estados globales del folio son `BORRADOR`, `EN_CAPTURA`, `LISTA_PARA_CALCULO` y `CALCULADA`.
2. El estado debe derivarse del avance real del agregado y no de un marcador aislado sin trazabilidad.
3. `version` y `fechaUltimaActualizacion` deben ser visibles en la consulta de estado.
4. El endpoint `/state` devuelve un resumen operativo y nunca una copia completa de la cotizacion.
5. `progreso` expone las secciones `datosGenerales`, `layoutUbicaciones`, `ubicaciones` y `opcionesCobertura` con valores `COMPLETED` o `INCOMPLETE`.
6. `progreso.ubicaciones` se mide contra los slots esperados del layout. Si no hay layout, si `totalEsperado = 0`, o si existe al menos un slot en `EMPTY`, `INCOMPLETE` o `INVALID`, el progreso es `INCOMPLETE`.
7. `progreso.ubicaciones` es `COMPLETED` solo cuando `totalEsperado > 0` y todos los slots normalizados estan en `VALID` o `CALCULABLE`.
8. `LISTA_PARA_CALCULO` requiere `datosGenerales = COMPLETED` y al menos una ubicacion en estado `CALCULABLE`.
9. Una ubicacion con alertas puede bloquear su propio calculo, pero no bloquea el folio completo.
10. Si existe resultado financiero, `/state` solo expone un resumen compacto del mismo; el desglose tecnico completo vive en `POST /calculate`.
11. Las respuestas exitosas deben usar envelope `data`; los errores deben publicarse como Problem Details.
12. El endpoint de estado no debe recalcular primas; solo debe reflejar el avance ya consolidado.

---

## 2. DISENO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripcion |
|---------|---------|---------|-------------|
| `Cotizacion` | tabla `cotizaciones_danos` | sin cambio estructural | agregado principal consultado para construir el estado consolidado |
| `EstadoCotizacionResumen` | vista o DTO de aplicacion | nueva | consolida estado, progreso, ubicaciones y trazabilidad |
| `ProgresoCotizacion` | DTO embebido | nueva | resume el estado de completitud por seccion funcional |
| `ResumenUbicaciones` | DTO embebido | nueva | resume ubicaciones esperadas, calculables, incompletas, invalidas y alertas |
| `ResultadoFinancieroResumen` | DTO embebido | nueva | resumen compacto del ultimo calculo vigente |

#### Campos del modelo
| Campo | Tipo | Obligatorio | Validacion | Descripcion |
|-------|------|-------------|------------|-------------|
| `numeroFolio` | string | si | unico, no vacio | Identificador funcional de la cotizacion |
| `estadoCotizacion` | string | si | enum del dominio | Estado global consolidado |
| `version` | long | si | control optimista | Version actual del agregado persistido |
| `fechaUltimaActualizacion` | datetime | si | auto-actualizado | Marca de la ultima modificacion logica |
| `progreso` | object | si | shape fijo | Estado de captura por seccion funcional |
| `datosGenerales` | string | si | `COMPLETED` o `INCOMPLETE` | Estado de la seccion general |
| `layoutUbicaciones` | string | si | `COMPLETED` o `INCOMPLETE` | Estado de la configuracion de layout |
| `ubicaciones` | string | si | `COMPLETED` o `INCOMPLETE` | Estado de la captura de ubicaciones contra layout |
| `opcionesCobertura` | string | si | `COMPLETED` o `INCOMPLETE` | Estado de la configuracion global de coberturas |
| `resumenUbicaciones` | object | si | shape fijo | Conteos operativos de ubicaciones |
| `totalEsperado` | integer | si | mayor o igual a 0 | Slots definidos por layout |
| `totalActual` | integer | si | mayor o igual a 0 | Ubicaciones realmente persistidas |
| `calculables` | integer | si | mayor o igual a 0 | Ubicaciones en `CALCULABLE` |
| `incompletas` | integer | si | mayor o igual a 0 | Ubicaciones en `INCOMPLETE` |
| `invalidas` | integer | si | mayor o igual a 0 | Ubicaciones en `INVALID` |
| `conAlertas` | integer | si | mayor o igual a 0 | Ubicaciones con alertas activas; puede solaparse con otras categorias |
| `alertasVigentes` | array | si | serializable | Alertas activas agregadas desde secciones y ubicaciones |
| `tieneAlertas` | boolean | si | derivado | Indica si existe al menos una alerta vigente |
| `readyToCalculate` | boolean | si | derivado | Indica si el folio ya puede pasar a `LISTA_PARA_CALCULO` |
| `resultadoFinanciero` | object | no | resumen compacto | Totales del ultimo calculo vigente |

#### Indices / Constraints
- El endpoint utiliza `numeroFolio` como llave de consulta y se apoya en el indice unico ya existente de `cotizaciones_danos`.
- No se requieren nuevas constraints de persistencia si el contrato se implementa como DTO de agregacion sobre la entidad ya existente.
- Si el resumen se materializa en una tabla o vista futura, debe mantenerse sincronizado con `cotizacion_id` y `version`.
- El estado derivado debe ser consistente con las secciones persistidas y no con banderas manuales independientes.

### API Endpoints

#### GET /v1/quotes/{folio}/state
- **Descripcion**: consulta el estado global y el progreso operativo de una cotizacion.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `folio`: numeroFolio de la cotizacion.
- **Response 200**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "estadoCotizacion": "EN_CAPTURA",
      "version": 4,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "progreso": {
        "datosGenerales": "COMPLETED",
        "layoutUbicaciones": "COMPLETED",
        "ubicaciones": "INCOMPLETE",
        "opcionesCobertura": "COMPLETED"
      },
      "resumenUbicaciones": {
        "totalEsperado": 3,
        "totalActual": 2,
        "calculables": 1,
        "incompletas": 0,
        "invalidas": 1,
        "conAlertas": 1
      },
      "tieneAlertas": true,
      "alertasVigentes": [
        {
          "codigo": "UBICACION_SIN_ZIP",
          "mensaje": "La ubicacion no tiene codigo postal valido.",
          "severidad": "Warning"
        }
      ],
      "readyToCalculate": false,
      "resultadoFinanciero": null
    }
  }
  ```
- **Response 200 para folio recien creado**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "estadoCotizacion": "BORRADOR",
      "version": 0,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "progreso": {
        "datosGenerales": "INCOMPLETE",
        "layoutUbicaciones": "INCOMPLETE",
        "ubicaciones": "INCOMPLETE",
        "opcionesCobertura": "INCOMPLETE"
      },
      "resumenUbicaciones": {
        "totalEsperado": 0,
        "totalActual": 0,
        "calculables": 0,
        "incompletas": 0,
        "invalidas": 0,
        "conAlertas": 0
      },
      "tieneAlertas": false,
      "alertasVigentes": [],
      "readyToCalculate": false,
      "resultadoFinanciero": null
    }
  }
  ```
- **Response 404**: numeroFolio inexistente.

### Diseno Frontend

#### Componentes nuevos
| Componente | Archivo | Props principales | Descripcion |
|------------|---------|------------------|-------------|
| `QuoteProgressCard` | `components/QuoteProgressCard.tsx` | `state` | Tarjeta principal con estado, progreso y alertas |
| `ProgressSectionList` | `components/ProgressSectionList.tsx` | `progress` | Lista visual de secciones completadas e incompletas |
| `QuoteStateSummaryCard` | `components/QuoteStateSummaryCard.tsx` | `quote, updatedAt, version` | Resumen de trazabilidad del folio |
| `ValidationAlertList` | `components/ValidationAlertList.tsx` | `alerts` | Reutilizado para alertas vigentes |
| `IdempotencyNotice` | `components/IdempotencyNotice.tsx` | `message, variant` | Reutilizado para estados informativos y errores |

#### Páginas nuevas
| Pagina | Archivo | Ruta | Protegida |
|--------|---------|------|-----------|
| `QuoteStatePage` | `pages/QuoteStatePage.tsx` | `/quotes/:folio/state` | no |

#### Hooks y State
| Hook | Archivo | Retorna | Descripcion |
|------|---------|---------|-------------|
| `useQuoteState` | `hooks/useQuoteState.ts` | `{ state, loading, error, refresh }` | Consulta y refresca el estado consolidado |
| `useQuoteProgress` | `hooks/useQuoteProgress.ts` | `{ progress, summary, alerts, readyToCalculate }` | Deriva metadatos de progreso para la UI |

#### Services (llamadas API)
| Funcion | Archivo | Endpoint |
|---------|---------|----------|
| `getQuoteState(folio)` | `services/folioService.ts` o `services/quoteStateService.ts` | `GET /v1/quotes/{folio}/state` |

### Arquitectura y Dependencias
- Backend nuevo en `plataforma-danos-back` bajo la capa hexagonal existente: controller -> application -> domain -> infrastructure.
- Esta funcionalidad consume el estado consolidado del agregado y puede derivar su informacion a partir de las secciones ya persistidas.
- Depende de `datos-generales-de-cotizacion`, `configuracion-de-layout-de-ubicaciones`, `gestion-de-ubicaciones`, `opciones-de-cobertura` y del resumen financiero definido en `SPEC-008` para reportar el avance real.
- La consulta no debe mutar el agregado ni disparar recalculos; solo agrega contexto operativo para la UI.
- La SPA ya cuenta con la pantalla de estado; esta spec formaliza el contrato y la logica de agregacion que alimenta dicha vista.
- Esta spec es la fuente de verdad del contrato `GET /v1/quotes/{folio}/state`.
- La respuesta del backend debe seguir el envelope `data` y los errores deben mapearse a Problem Details, compatible con el manejo ya existente en la app.

### Notas de Implementacion
> La regla principal es no confundir estado con edicion. El endpoint debe leer el avance real del agregado, consolidar secciones y alertas, y devolver una fotografia consistente para la UI. La normalizacion de slots de ubicacion se hace contra `configuracionLayout`: si falta un slot esperado, se trata como `EMPTY`. El endpoint nunca devuelve el breakdown tecnico completo del calculo.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada item (`[x]`) al completarlo.
> El Orchestrator monitorea este checklist para determinar el progreso.

### Backend

#### Implementacion
- [ ] Crear response DTO consolidado para estado y progreso del folio
- [ ] Implementar caso de uso de consulta del estado consolidado
- [ ] Implementar la derivacion de `progreso` por seccion funcional
- [ ] Implementar la derivacion de `resumenUbicaciones` contra slots esperados del layout
- [ ] Implementar controller `/v1/quotes/{folio}/state`
- [ ] Documentar OpenAPI del contrato

#### Tests Backend
- [ ] Caso de uso happy path de estado consolidado
- [ ] Caso de uso con folio recien creado en BORRADOR
- [ ] Caso de uso con ubicaciones incompletas y alertas vigentes
- [ ] Caso de uso con numeroFolio inexistente
- [ ] Controller con respuesta `200` y envelope `data`
- [ ] Controller con Problem Details ante error relevante

### Frontend

#### Implementacion
- [ ] Crear servicio Axios para consultar el estado consolidado
- [ ] Crear hook para refrescar estado y progreso
- [ ] Implementar componentes de progreso, secciones y alertas
- [ ] Integrar la pagina de estado existente con el nuevo contrato
- [ ] Mantener la navegacion hacia cotizador, layout y ubicaciones si aplica
- [ ] Mantener mensajes de error y estado con envelope `data` y Problem Details

#### Tests Frontend
- [ ] Componente principal renderiza estado y progreso
- [ ] Componente dispara refresco del estado
- [ ] Hook maneja carga exitosa y estado BORRADOR
- [ ] Hook maneja error de folio inexistente
- [ ] Pagina integra resumen, secciones y alertas

### QA
- [ ] Ejecutar skill `/gherkin-case-generator` -> criterios CRITERIO-1.1, 1.2, 1.3, 2.1, 2.2, 2.3
- [ ] Ejecutar skill `/risk-identifier` -> clasificacion ASD de riesgos para consulta consolidada y derivacion de progreso
- [ ] Revisar cobertura de tests contra criterios de aceptacion
- [ ] Validar que todas las reglas de negocio estan cubiertas
- [ ] Actualizar estado spec: `status: IMPLEMENTED`

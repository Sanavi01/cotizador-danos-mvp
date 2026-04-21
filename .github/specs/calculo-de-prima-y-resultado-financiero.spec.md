---
id: SPEC-008
status: DRAFT
feature: calculo-de-prima-y-resultado-financiero
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-001", "SPEC-002", "SPEC-003", "SPEC-004", "SPEC-005", "SPEC-006", "SPEC-007"]
---

# Spec: Calculo de Prima y Resultado Financiero

> **Estado:** `DRAFT` -> aprobar con `status: APPROVED` antes de iniciar implementacion.
> **Ciclo de vida:** DRAFT -> APPROVED -> IN_PROGRESS -> IMPLEMENTED -> DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripcion
Esta funcionalidad permite ejecutar el calculo tecnico y comercial de una cotizacion para obtener `primaNeta`, `primaComercial` y el desglose de `primasPorUbicacion`. El calculo debe leer el agregado completo, usar los insumos tecnicos ya persistidos, tolerar ubicaciones incompletas con alertas claras y guardar el resultado financiero sin sobrescribir las demas secciones del folio.

### Requerimiento de Negocio
Fuente principal: `.github/requirements/calculo-de-prima-y-resultado-financiero.md`.

Resumen del requerimiento base:
- Ejecutar el calculo del folio completo por `numeroFolio`.
- Leer la cotizacion, parametros globales, tarifas y factores tecnicos requeridos.
- Determinar que ubicaciones son calculables y cuales permanecen incompletas.
- Calcular y consolidar la prima por ubicacion, la `primaNeta` total y la `primaComercial` total.
- Persistir el resultado financiero sin sobrescribir otras secciones del agregado.
- Presentar alertas para ubicaciones incompletas sin bloquear completamente el folio.
- Responder con envelope `data` en exito y Problem Details en error.

### Historias de Usuario

#### HU-01: Calcular la prima del folio

```
Como:        usuario del cotizador
Quiero:      ejecutar el calculo del folio
Para:        conocer la primaNeta, la primaComercial y el desglose por ubicacion

Prioridad:   Alta
Estimacion:  L
Dependencias: Datos Generales de Cotizacion, Gestion de Ubicaciones, Opciones de Cobertura, Catalogos y Validaciones Core
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Calcular el folio con una o varias ubicaciones validas
  Dado que la cotizacion cuenta con datos suficientes para ejecutar el calculo
  Cuando el usuario solicita calcular el folio
  Entonces el backend lee la cotizacion completa y sus insumos tecnicos
  Y calcula la prima por cada ubicacion calculable
  Y consolida primaNeta y primaComercial del folio
  Y persiste el resultado financiero en una misma operacion logica
```

**Happy Path**
```gherkin
CRITERIO-1.2: Consultar el resultado financiero despues del calculo
  Dado que el folio ya fue calculado
  Cuando la UI consulta el estado funcional correspondiente
  Entonces el usuario puede ver primaNeta, primaComercial y primasPorUbicacion
```

**Edge Case**
```gherkin
CRITERIO-1.3: Calcular un folio con ubicaciones parcialmente incompletas
  Dado que el folio contiene ubicaciones calculables e incompletas
  Cuando el usuario ejecuta el calculo
  Entonces el sistema calcula las ubicaciones elegibles
  Y conserva alertas para las ubicaciones incompletas
  Y devuelve el resultado financiero parcial o consolidado segun el contexto del folio
```

#### HU-02: Continuar el calculo con ubicaciones incompletas

```
Como:        usuario del cotizador
Quiero:      obtener resultado para las ubicaciones validas aunque existan ubicaciones incompletas
Para:        no perder el avance comercial del folio por una sola ubicacion pendiente

Prioridad:   Alta
Estimacion:  M
Dependencias: Gestion de Ubicaciones, Estado y Progreso de Cotizacion
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-02

**Happy Path**
```gherkin
CRITERIO-2.1: Calcular solo las ubicaciones elegibles
  Dado que el folio tiene una ubicacion completa y otra incompleta
  Cuando el usuario ejecuta el calculo
  Entonces el sistema calcula la ubicacion elegible
  Y registra alerta para la ubicacion incompleta
  Y no bloquea completamente el resultado del folio
```

**Happy Path**
```gherkin
CRITERIO-2.2: Marcar una ubicacion como no calculable
  Dado que una ubicacion no tiene codigo postal valido, giro.claveIncendio o garantias tarifables
  Cuando el backend evalua la elegibilidad tecnica
  Entonces la ubicacion se marca como no calculable
  Y queda reportada con alertaBloqueante y estadoValidacion correspondiente
```

**Edge Case**
```gherkin
CRITERIO-2.3: Reintentar el calculo sin cambiar el folio
  Dado que la cotizacion no fue modificada desde el ultimo calculo exitoso
  Cuando el usuario vuelve a ejecutar el calculo
  Entonces el backend recalcula sobre el mismo snapshot funcional
  Y sobrescribe el resultado vigente con los mismos totales si no hubo cambios
  Y actualiza consistentemente version y fechaUltimaActualizacion
```

### Reglas de Negocio
1. El calculo debe leer la cotizacion completa por `numeroFolio`.
2. Deben usarse parametros globales de calculo, tarifas y factores tecnicos aprobados para el reto.
3. El resultado financiero debe persistir `primaNeta`, `primaComercial` y `primasPorUbicacion` en una misma operacion logica.
4. Persistir el resultado financiero no debe sobrescribir datos generales, layout, ubicaciones ni opcionesCobertura.
5. Una ubicacion no debe calcularse si no tiene codigo postal valido, `giro.claveIncendio` o garantias tarifables.
6. Una ubicacion incompleta genera alerta, pero no debe impedir calcular las demas.
7. El desglose financiero debe contemplar, como minimo, un arbol `ubicacion -> garantia -> componente` para trazabilidad.
8. La ejecucion del calculo sobrescribe el resultado vigente anterior, incrementa `version` y actualiza `fechaUltimaActualizacion`; no se conserva historial en el MVP.
9. El calculo debe ser determinista respecto al estado persistido que consume.
10. `POST /calculate` devuelve el desglose tecnico completo; `GET /state` solo expone un resumen compacto del resultado vigente.

---

## 2. DISENO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripcion |
|---------|---------|---------|-------------|
| `Cotizacion` | tabla `cotizaciones_danos` | modificada | agregado principal; conserva estado, version, trazabilidad y totales financieros vigentes |
| `PrimaPorUbicacion` | tabla `cotizacion_primas_por_ubicacion` | nueva | desglose vigente por ubicacion dentro del ultimo resultado |
| `GarantiaCalculada` | JSONB o subestructura de `PrimaPorUbicacion` | nueva | snapshot de garantias efectivamente usadas en el calculo |
| `ComponenteTecnicoCalculado` | JSONB o subestructura de `GarantiaCalculada` | nueva | trazabilidad tecnica del monto calculado |

#### Campos del modelo
| Campo | Tipo | Obligatorio | Validacion | Descripcion |
|-------|------|-------------|------------|-------------|
| `numeroFolio` | string | si | unico, no vacio | Identificador funcional de la cotizacion |
| `version` | long | si | control optimista | Version del agregado raiz al momento del calculo |
| `fechaUltimaActualizacion` | datetime | si | auto-actualizado | Marca de la ultima modificacion logica |
| `primaNeta` | decimal | si | precision monetaria COP | Total tecnico consolidado del folio |
| `primaComercial` | decimal | si | precision monetaria COP | Total comercial consolidado del folio |
| `primasPorUbicacion` | array | si | puede estar vacio si no hay ubicaciones calculables | Desglose del calculo por ubicacion |
| `indiceUbicacion` | integer | si por item | unico dentro del folio | Ubicacion calculada |
| `ubicacionCalculable` | boolean | si por item | derivado | Indica si la ubicacion entro al calculo |
| `primaNetaUbicacion` | decimal | si por item | precision monetaria COP | Prima neta de la ubicacion |
| `primaComercialUbicacion` | decimal | si por item | precision monetaria COP | Prima comercial de la ubicacion |
| `garantiasCalculadas` | array | si por item | serializable | Garantias efectivamente usadas por ubicacion |
| `garantiaCode` | string | si por item | id canonico | Garantia calculada |
| `primaGarantia` | decimal | si por item | precision monetaria COP | Prima consolidada de la garantia |
| `componentes` | array | si por item | serializable | Componentes tecnicos usados para llegar al resultado |
| `alertas` | array | no | serializable | Alertas informativas o bloqueantes del calculo |
| `estadoCalculo` | string | si | enum del dominio | Resultado operativo del calculo |
| `calculatedAt` | datetime | si | auto-actualizado | Momento en que se consolidó el resultado |

#### Indices / Constraints
- Foreign key desde `cotizacion_primas_por_ubicacion.cotizacion_id` hacia `cotizaciones_danos.id` con borrado en cascada.
- Constraint unico sobre `cotizacion_id + indiceUbicacion` para evitar duplicidad en el desglose vigente por ubicacion.
- Indice por `estadoCalculo` para consultar resultados calculados, parciales o rechazados.
- Constraint monetario para que los montos se almacenen con escala 2 y redondeo `HALF_UP`.

### API Endpoints

#### POST /v1/quotes/{folio}/calculate
- **Descripcion**: ejecuta el calculo tecnico y comercial del folio y persiste el resultado financiero.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `folio`: numeroFolio de la cotizacion.
- **Request Body**:
  ```json
  {
    "version": 4
  }
  ```
- **Response 200**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "estadoCotizacion": "CALCULADA",
      "primaNeta": 125000.00,
      "primaComercial": 148000.00,
      "primasPorUbicacion": [
        {
          "indiceUbicacion": 1,
          "ubicacionCalculable": true,
          "primaNetaUbicacion": 75000.00,
          "primaComercialUbicacion": 88500.00,
          "garantiasCalculadas": [
            {
              "garantiaCode": "GAR-INC-ED",
              "primaGarantia": 60000.00,
              "componentes": [
                { "tipo": "base", "monto": 40000.00 },
                { "tipo": "factor_zona", "monto": 20000.00 }
              ]
            },
            {
              "garantiaCode": "GAR-CAT-TEV",
              "primaGarantia": 15000.00,
              "componentes": [
                { "tipo": "base", "monto": 10000.00 },
                { "tipo": "factor_cat", "monto": 5000.00 }
              ]
            }
          ],
          "alertas": []
        },
        {
          "indiceUbicacion": 2,
          "ubicacionCalculable": false,
          "primaNetaUbicacion": 0.00,
          "primaComercialUbicacion": 0.00,
          "garantiasCalculadas": [],
          "alertas": [
            {
              "codigo": "UBICACION_SIN_ZIP",
              "mensaje": "La ubicacion no tiene codigo postal valido.",
              "severidad": "Warning"
            }
          ]
        }
      ],
      "alertasVigentes": [
        {
          "codigo": "UBICACION_SIN_ZIP",
          "mensaje": "La ubicacion no tiene codigo postal valido.",
          "severidad": "Warning"
        }
      ],
      "version": 5,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z"
    }
  }
  ```
- **Response 404**: numeroFolio inexistente.
- **Response 409**: version desactualizada o conflicto de concurrencia.
- **Response 422**: la cotizacion no tiene insumos minimos para calcular ninguna ubicacion.

#### Integracion con GET /v1/quotes/{folio}/state
- **Ownership**: el contrato canonico de `/state` vive en `SPEC-006 Estado y Progreso de Cotizacion`.
- **Aporte de esta spec**: cuando existe resultado financiero vigente, `GET /state` expone solo un resumen compacto:
  ```json
  {
    "resultadoFinanciero": {
      "primaNeta": 125000.00,
      "primaComercial": 148000.00,
      "ubicacionesCalculadas": 1
    }
  }
  ```

### Diseno Frontend

#### Componentes nuevos
| Componente | Archivo | Props principales | Descripcion |
|------------|---------|------------------|-------------|
| `FinancialResultCard` | `components/FinancialResultCard.tsx` | `result` | Tarjeta con primaNeta, primaComercial y estado del calculo |
| `PrimasPorUbicacionList` | `components/PrimasPorUbicacionList.tsx` | `items` | Desglose de primas por ubicacion |
| `CalculateQuoteAction` | `components/CalculateQuoteAction.tsx` | `loading, error, onCalculate` | Boton y estado para ejecutar el calculo |
| `ValidationAlertList` | `components/ValidationAlertList.tsx` | `alerts` | Reutilizado para alertas del calculo |
| `IdempotencyNotice` | `components/IdempotencyNotice.tsx` | `message, variant` | Reutilizado para mensajes informativos y errores |

#### Páginas nuevas
| Pagina | Archivo | Ruta | Protegida |
|--------|---------|------|-----------|
| `QuoteStatePage` | `pages/QuoteStatePage.tsx` | `/quotes/:folio/state` | no |

#### Hooks y State
| Hook | Archivo | Retorna | Descripcion |
|------|---------|---------|-------------|
| `useQuoteCalculation` | `hooks/useQuoteCalculation.ts` | `{ calculate, loading, error, result, reset }` | Ejecuta el calculo y conserva el resultado |
| `useQuoteState` | `hooks/useQuoteState.ts` | `{ state, loading, error, refresh }` | Se amplia para consumir el resultado financiero calculado |
| `useFinancialResultSummary` | `hooks/useFinancialResultSummary.ts` | `{ primaNeta, primaComercial, primasPorUbicacion }` | Deriva metadatos de render para la UI |

#### Services (llamadas API)
| Funcion | Archivo | Endpoint |
|---------|---------|----------|
| `calculateQuote(folio, payload)` | `services/quoteCalculationService.ts` | `POST /v1/quotes/{folio}/calculate` |

### Arquitectura y Dependencias
- Backend nuevo en `plataforma-danos-back` bajo la capa hexagonal existente: controller -> application -> domain -> infrastructure.
- La operacion debe leer datos generales, layout, ubicaciones y opciones de cobertura antes de calcular, usando la referencia core para validar tarifas y factores.
- La persistencia debe actualizar los totales vigentes en `cotizaciones_danos` y los detalles por ubicacion en una transaccion unica.
- El estado del folio debe reflejar `CALCULADA` cuando el resultado financiero se consolida con exito, aun si existen ubicaciones con alertas.
- La SPA debe incorporar un accionador de calculo en la pantalla de estado y mostrar el resultado financiero sin requerir una pagina aparte.
- `POST /calculate` devuelve el detalle tecnico completo; `GET /state` solo consume el resumen compacto definido en `SPEC-006`.
- La respuesta del backend debe seguir el envelope `data` y los errores deben mapearse a Problem Details, compatible con el manejo ya existente en la app.

### Notas de Implementacion
> El calculo es una operacion de lectura amplia y escritura acotada. Debe ser transaccional, determinista y tolerante a ubicaciones incompletas, pero no debe intentar corregir por si mismo los datos de entrada. Si no existe ninguna ubicacion calculable, el backend debe rechazar la operacion en lugar de inventar un resultado. Cada nueva ejecucion sobrescribe el resultado vigente anterior y deja un unico snapshot util para el MVP.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada item (`[x]`) al completarlo.
> El Orchestrator monitorea este checklist para determinar el progreso.

### Backend

#### Implementacion
- [ ] Crear request/response DTOs para ejecucion de calculo y resultado financiero
- [ ] Implementar entidad o agregado de resultado financiero y primas por ubicacion
- [ ] Implementar caso de uso de calculo del folio por `numeroFolio`
- [ ] Implementar lectura de insumos tecnicos desde cotizacion, ubicaciones y catalogos
- [ ] Implementar persistencia transaccional del resultado financiero
- [ ] Implementar controller `/v1/quotes/{folio}/calculate`
- [ ] Actualizar la proyeccion resumida de `/state` definida en `SPEC-006` para exponer `resultadoFinanciero`
- [ ] Documentar OpenAPI del contrato

#### Tests Backend
- [ ] Caso de uso happy path de calculo con ubicaciones validas
- [ ] Caso de uso con ubicaciones incompletas y alertas vigentes
- [ ] Caso de uso con numeroFolio inexistente
- [ ] Caso de uso con version desactualizada o conflicto de concurrencia
- [ ] Caso de uso con ninguna ubicacion calculable
- [ ] Controller con respuesta `200` y envelope `data`
- [ ] Controller con Problem Details ante error relevante

### Frontend

#### Implementacion
- [ ] Crear servicio Axios para ejecutar el calculo del folio
- [ ] Crear hook para administrar la ejecucion y el estado del resultado
- [ ] Implementar componente de resumen financiero y desglose por ubicacion
- [ ] Integrar accion de calculo en la pantalla de estado del folio
- [ ] Mostrar alertas y bloqueos sin perder el resumen funcional
- [ ] Mantener mensajes de error y estado con envelope `data` y Problem Details

#### Tests Frontend
- [ ] Componente principal renderiza primaNeta, primaComercial y primasPorUbicacion
- [ ] Componente dispara el calculo con el estado actual del folio
- [ ] Hook maneja carga exitosa y resultado parcial
- [ ] Hook maneja error de folio inexistente o version desactualizada
- [ ] Pagina integra consulta de estado y resultado financiero

### QA
- [ ] Ejecutar skill `/gherkin-case-generator` -> criterios CRITERIO-1.1, 1.2, 1.3, 2.1, 2.2, 2.3
- [ ] Ejecutar skill `/risk-identifier` -> clasificacion ASD de riesgos para calculo financiero y persistencia transaccional
- [ ] Revisar cobertura de tests contra criterios de aceptacion
- [ ] Validar que todas las reglas de negocio estan cubiertas
- [ ] Actualizar estado spec: `status: IMPLEMENTED`

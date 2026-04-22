---
id: SPEC-008
status: APPROVED
feature: calculo-de-prima-y-resultado-financiero
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-001", "SPEC-002", "SPEC-003", "SPEC-004", "SPEC-005", "SPEC-006", "SPEC-007"]
---

# Spec: Calculo de Prima y Resultado Financiero

> **Estado:** `APPROVED` -> aprobar con `status: APPROVED` antes de iniciar implementacion.
> **Ciclo de vida:** DRAFT -> APPROVED -> IN_PROGRESS -> IMPLEMENTED -> DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripcion
Esta funcionalidad permite ejecutar el calculo tecnico y comercial de una cotizacion para obtener `primaNeta`, `primaComercial` y el desglose de `primasPorUbicacion`. El calculo debe leer el agregado completo, usar los insumos tecnicos ya persistidos, resolver la configuracion activa de `calculationParameters` y las matrices del fixture disponibles para el MVP (`tariffs`, `fireTariffs`, `catTariffs`), tolerar ubicaciones incompletas con alertas claras y guardar el resultado financiero sin sobrescribir las demas secciones del folio. Las rutas `fhmTariffs` y `electronicEquipmentFactors` quedan contempladas como extensiones condicionadas a que el folio exponga llaves tecnicas adicionales sin romper este contrato.

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
  Y resuelve la configuracion activa de calculo y las matrices vigentes del fixture habilitadas para el MVP
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
2. Deben usarse la configuracion activa expuesta por `GET /v1/calculation-parameters/active`, ademas de tarifas y factores tecnicos aprobados para el reto; toda resolucion debe respetar la vigencia `vigenciaDesde`/`vigenciaHasta` del fixture.
3. Las rutas tecnicas obligatorias del MVP son las que el backend logre resolver con el snapshot actual del folio usando solo llaves ya modeladas: `giroCode|zonaCode|garantiaCode` sobre `tariffs`, `giroCode|tipoConstructivo|nivelTarifario` sobre `fireTariffs` y `zonaTev|garantiaCode` sobre `catTariffs`.
4. Las rutas `zonaFhm|grupo` sobre `fhmTariffs` y `clase|nivelTarifario` sobre `electronicEquipmentFactors` se consideran extensiones opcionales. Si el folio no expone `grupo`, `clase` o equivalentes documentados, el backend debe registrar alerta informativa y omitir esos componentes sin bloquear el resto del calculo.
5. La formula comercial del MVP asociada a `CALC-2026-CORE` version `1.0.0` es: `primaComercialUbicacion = round(primaNetaUbicacion + (primaNetaUbicacion * 0.12) + (primaNetaUbicacion * 0.05), 2)` con redondeo `HALF_UP`; `primaComercial` del folio es la suma de `primaComercialUbicacion` de las ubicaciones calculadas.
6. Los porcentajes `0.12` para `RECARGO_ADMINISTRACION` y `0.05` para `MARGEN_COMERCIAL` son el supuesto controlado del MVP mientras el fixture solo expone metadatos de `calculationParameters`; deben quedar trazados en la respuesta mediante `componentesComerciales`.
7. El resultado financiero debe persistir `primaNeta`, `primaComercial`, `primasPorUbicacion`, `estadoCalculo`, `calculatedAt` y `calculationParameterVersion` en una misma operacion logica.
8. Persistir el resultado financiero no debe sobrescribir datos generales, layout, ubicaciones ni opcionesCobertura.
9. Una ubicacion no debe calcularse si no tiene codigo postal valido, `giro.claveIncendio`, garantias seleccionadas o si ninguna ruta tecnica obligatoria de sus garantias puede resolverse con la informacion persistida.
10. Una ubicacion incompleta genera alerta, pero no debe impedir calcular las demas.
11. El desglose financiero debe contemplar, como minimo, un arbol `ubicacion -> garantia -> componente`; cada componente tecnico debe conservar `fuente`, `lookupKey`, `rate`, `factor` y `monto` para trazabilidad contra el fixture.
12. La ejecucion del calculo sobrescribe el resultado vigente anterior, incrementa `version` y actualiza `fechaUltimaActualizacion`; no se conserva historial en el MVP.
13. El calculo debe ser determinista respecto al estado persistido que consume y a la `calculationParameterVersion` usada.
14. `POST /calculate` devuelve el desglose tecnico completo; `GET /state` solo expone un resumen compacto del resultado vigente.
15. `estadoCalculo` se limita a `CALCULADO`, `PARCIAL` o `RECHAZADO`: `CALCULADO` cuando todas las ubicaciones elegibles del snapshot fueron calculadas, `PARCIAL` cuando se calcula solo un subconjunto y `RECHAZADO` cuando no existe ninguna ubicacion calculable.
16. Si el folio no tiene garantias activas seleccionadas o no existe ninguna ubicacion calculable, el backend debe responder `422` con Problem Details y no persistir un resultado artificial.
17. Los componentes del reto sin llave tecnica suficiente en el fixture actual, como `CAT FHM`, `equipo electronico` dependiente de `clase`, `Extension de cobertura`, `Remocion de escombros`, `Gastos extraordinarios`, `Dinero y valores` y `Anuncios luminosos`, deben declararse como pendientes de especializacion o tratarse como labels derivados sin lookup dedicado, pero no deben bloquear el escenario minimo del MVP.

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
| `calculationParameterVersion` | string | si | no vacio | Version del `calculationParameters` usada por el calculo vigente |
| `primasPorUbicacion` | array | si | puede estar vacio si no hay ubicaciones calculables | Desglose del calculo por ubicacion |
| `indiceUbicacion` | integer | si por item | unico dentro del folio | Ubicacion calculada |
| `ubicacionCalculable` | boolean | si por item | derivado | Indica si la ubicacion entro al calculo |
| `primaNetaUbicacion` | decimal | si por item | precision monetaria COP | Prima neta de la ubicacion |
| `primaComercialUbicacion` | decimal | si por item | precision monetaria COP | Prima comercial de la ubicacion |
| `garantiasCalculadas` | array | si por item | serializable | Garantias efectivamente usadas por ubicacion |
| `garantiaCode` | string | si por item | id canonico | Garantia calculada |
| `primaGarantia` | decimal | si por item | precision monetaria COP | Prima consolidada de la garantia |
| `componentes` | array | si por item | serializable | Componentes tecnicos usados para llegar al resultado |
| `componentesComerciales` | array | si por item | serializable | Recargos o margenes comerciales aplicados sobre la prima neta de la ubicacion |
| `porcentaje` | decimal | no por componente comercial | precision porcentual | Porcentaje aplicado sobre la base comercial |
| `base` | decimal | si por componente comercial | precision monetaria COP | Base usada para calcular el recargo comercial |
| `fuente` | string | si por componente | enum: `tariffs`, `fireTariffs`, `catTariffs`, `fhmTariffs`, `electronicEquipmentFactors` | Origen del insumo tecnico usado por el componente |
| `lookupKey` | string | no por componente | nullable | Clave usada para resolver el componente contra el fixture |
| `rate` | decimal | no por componente | nullable | Tasa tecnica usada para el componente cuando aplica |
| `factor` | decimal | no por componente | nullable | Factor tecnico usado para el componente cuando aplica |
| `monto` | decimal | si por componente | precision monetaria COP | Monto aportado por el componente |
| `alertas` | array | no | serializable | Alertas informativas o bloqueantes del calculo |
| `estadoCalculo` | string | si | enum: `CALCULADO`, `PARCIAL`, `RECHAZADO` | Resultado operativo del calculo |
| `calculatedAt` | datetime | si | auto-actualizado | Momento en que se consolidó el resultado |

#### Indices / Constraints
- Foreign key desde `cotizacion_primas_por_ubicacion.cotizacion_id` hacia `cotizaciones_danos.id` con borrado en cascada.
- Constraint unico sobre `cotizacion_id + indiceUbicacion` para evitar duplicidad en el desglose vigente por ubicacion.
- Indice por `estadoCalculo` para consultar resultados calculados, parciales o rechazados.
- Constraint monetario para que los montos se almacenen con escala 2 y redondeo `HALF_UP`.

### Coberturas y Componentes Soportados en MVP

| Garantia / componente visible | Soporte MVP | Origen tecnico principal | Notas |
|-------------------------------|-------------|--------------------------|-------|
| `Incendio edificios` | si | `GAR-INC-ED` + `tariffs` / `fireTariffs` | componente base obligatorio del reto |
| `Incendio contenidos` | si | `GAR-INC-CONT` + `tariffs` | componente base obligatorio del reto |
| `CAT TEV` | si | `catTariffs` con `zonaTev` valida | se muestra como componente derivado dentro de la garantia afectada |
| `Robo` | si | `GAR-ROBO` + `tariffs` cuando exista combinacion vigente | si no existe lookup se reporta alerta y se omite |
| `Vidrios` | si | `GAR-VID` + `tariffs` cuando exista combinacion vigente | no bloquea otras garantias |
| `Perdida de rentas` / `BI` | si | `GAR-INC-LUC` o `GAR-INT` + `tariffs` cuando exista combinacion vigente | se acepta mostrarlos como labels de negocio derivados de la garantia seleccionada |
| `CAT FHM` | opcional | `fhmTariffs` | requiere `grupo` o equivalente; si falta, queda diferido |
| `Equipo electronico` | opcional | `GAR-EL` + `electronicEquipmentFactors` | requiere `clase` o equivalente; si falta, queda diferido |
| `Extension de cobertura`, `Remocion de escombros`, `Gastos extraordinarios`, `Dinero y valores`, `Anuncios luminosos` | diferido | sin lookup dedicado en fixture actual | pueden presentarse despues como subcomponentes derivados sin romper el contrato |

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
      "estadoCalculo": "PARCIAL",
      "primaNeta": 60000.00,
      "primaComercial": 70200.00,
      "calculationParameterVersion": "1.0.0",
      "primasPorUbicacion": [
        {
          "indiceUbicacion": 1,
          "ubicacionCalculable": true,
          "primaNetaUbicacion": 60000.00,
          "primaComercialUbicacion": 70200.00,
          "garantiasCalculadas": [
            {
              "garantiaCode": "GAR-INC-ED",
              "primaGarantia": 60000.00,
              "componentes": [
                {
                  "tipo": "base",
                  "fuente": "tariffs",
                  "lookupKey": "GIRO-001|ZTEV-1|GAR-INC-ED",
                  "rate": 0.015,
                  "factor": 1.20,
                  "monto": 40000.00
                },
                {
                  "tipo": "factor_zona",
                  "fuente": "catTariffs",
                  "lookupKey": "ZTEV-1|GAR-INC-ED",
                  "rate": 0.006,
                  "factor": 1.02,
                  "monto": 20000.00
                }
              ]
            }
          ],
          "componentesComerciales": [
            {
              "tipo": "RECARGO_ADMINISTRACION",
              "porcentaje": 0.12,
              "base": 60000.00,
              "monto": 7200.00
            },
            {
              "tipo": "MARGEN_COMERCIAL",
              "porcentaje": 0.05,
              "base": 60000.00,
              "monto": 3000.00
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
          "componentesComerciales": [],
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
      "calculatedAt": "2026-04-21T00:00:00Z",
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
      "primaNeta": 60000.00,
      "primaComercial": 70200.00,
      "ubicacionesCalculadas": 1,
      "ubicacionesNoCalculables": 1,
      "estadoCalculo": "PARCIAL",
      "calculatedAt": "2026-04-21T00:00:00Z",
      "calculationParameterVersion": "1.0.0"
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
- El snapshot actual del folio ya expone `tipoConstructivo`, `nivel`, `zonaTev` y `zonaFhm`; por eso el primer corte obligatorio se limita a `tariffs`, `fireTariffs` y `catTariffs`.
- Si una ruta tecnica adicional requiere llaves no modeladas todavia, como `grupo` o `clase`, el backend debe registrar alerta y omitir ese componente en el MVP en lugar de inventar equivalencias.
- La persistencia debe actualizar los totales vigentes en `cotizaciones_danos` y los detalles por ubicacion en una transaccion unica.
- El estado del folio debe reflejar `CALCULADA` cuando el resultado financiero se consolida con exito, aun si existen ubicaciones con alertas.
- La SPA debe incorporar un accionador de calculo en la pantalla de estado y mostrar el resultado financiero sin requerir una pagina aparte.
- `POST /calculate` devuelve el detalle tecnico completo; `GET /state` solo consume el resumen compacto definido en `SPEC-006`.
- La respuesta del backend debe seguir el envelope `data` y los errores deben mapearse a Problem Details, compatible con el manejo ya existente en la app.

### Notas de Implementacion
> El calculo es una operacion de lectura amplia y escritura acotada. Debe ser transaccional, determinista y tolerante a ubicaciones incompletas, pero no debe intentar corregir por si mismo los datos de entrada. Si no existe ninguna ubicacion calculable, el backend debe rechazar la operacion en lugar de inventar un resultado. Cada nueva ejecucion sobrescribe el resultado vigente anterior y deja un unico snapshot util para el MVP. La trazabilidad minima del resultado debe permitir reconstruir que filas del fixture participaron realmente, cuales componentes comerciales se agregaron y cuales componentes quedaron diferidos por falta de llaves tecnicas.

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

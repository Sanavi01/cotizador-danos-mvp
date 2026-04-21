---
id: SPEC-002
status: APPROVED
feature: catalogos-y-validaciones-core
created: 2026-04-20
updated: 2026-04-20
author: spec-generator
version: "1.0"
related-specs: ["SPEC-001"]
---

# Spec: Catalogos y Validaciones Core

> **Estado:** `DRAFT` -> aprobar con `status: APPROVED` antes de iniciar implementacion.
> **Ciclo de vida:** DRAFT -> APPROVED -> IN_PROGRESS -> IMPLEMENTED -> DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripcion
Esta funcionalidad define el contrato de una referencia core separada dentro del monorepo, implementada como mock/stub con fixtures en memoria. El backend de cotizacion consume ese contrato, mientras el modulo core lo expone, para resolver catalogos, validaciones territoriales y datos tecnicos reutilizables durante la captura y el calculo de una cotizacion.

### Requerimiento de Negocio
Fuente principal: `.github/requirements/catalogos-y-validaciones-core.md`.

Resumen del requerimiento base:
- Consultar suscriptores, agentes, giros y codigos postales validos.
- Validar codigos postales y enriquecer la direccion territorial.
- Exponer catalogos de clasificacion de riesgo y garantias.
- Generar folios desde la referencia core cuando aplique.
- Resolver tarifas y factores tecnicos requeridos por el calculo.
- Implementar consumo y exposicion de los endpoints mock en esta entrega.
- Mantener contrato documentado con datos versionados dentro del repositorio.

### Historias de Usuario

#### HU-01: Consultar catalogos operativos durante la captura

```
Como:        Usuario del cotizador
Quiero:      consultar suscriptores, agentes, giros y codigos postales validos
Para:        diligenciar la cotizacion con datos consistentes y reutilizables

Prioridad:   Alta
Estimacion:  M
Dependencias: Folios e Idempotencia
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Consultar catalogos base
  Dado que el usuario esta capturando una cotizacion
  Cuando necesita seleccionar un suscriptor, un agente, un giro o un codigo postal
  Entonces el sistema consulta la fuente de referencia aprobada
  Y retorna informacion suficiente para completar la captura
```

**Error Path**
```gherkin
CRITERIO-1.2: Validar un codigo postal no reconocido
  Dado que el usuario ingresa un codigo postal no reconocido
  Cuando el sistema intenta validarlo
  Entonces la respuesta devuelve 200 OK con valido false
  Y la ubicacion registra alertas con severidad adecuada sin bloquear el folio
```

### HU-02: Resolver datos tecnicos para el calculo

```
Como:        Sistema de cotizacion
Quiero:      consumir parametros, tarifas y factores tecnicos desde la referencia core
Para:        calcular la prima de forma consistente y trazable

Prioridad:   Alta
Estimacion:  L
Dependencias: Gestion de Ubicaciones, Opciones de Cobertura, HU-01
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-02

**Happy Path**
```gherkin
CRITERIO-2.1: Obtener insumos tecnicos para una ubicacion calculable
  Dado que una ubicacion cuenta con datos minimos para ser evaluada
  Cuando el backend ejecuta el calculo tecnico
  Entonces consulta la referencia core o su stub documentado
  Y obtiene catalogos, tarifas y factores aplicables a la ubicacion
```

**Error Path**
```gherkin
CRITERIO-2.2: Rechazar un calculo sin tarifa o factor tecnico
  Dado que la referencia core no devuelve una tarifa o factor tecnico requerido
  Cuando el backend intenta evaluar la ubicacion
  Entonces la ubicacion queda marcada como no calculable
  Y el sistema registra una alerta tecnica sin bloquear el folio completo
```

**Edge Case**
```gherkin
CRITERIO-2.3: Operar con un stub documentado
  Dado que no existe una integracion real disponible
  Cuando la solucion usa un stub o mock server
  Entonces el contrato de la referencia queda documentado
  Y los datos de prueba permanecen versionados dentro del repositorio
```

### Reglas de Negocio
1. La referencia core debe existir como modulo separado en el monorepo, implementado como mock/stub con fixtures en memoria.
2. El contrato consumido debe quedar documentado aunque la implementacion sea simulada.
3. La captura y el calculo deben depender de catalogos consistentes, no de valores hardcodeados en la UI.
4. La validacion territorial y tecnica debe ocurrir con los datos de referencia aprobados.
5. La validacion de codigo postal debe enriquecer la ubicacion y, si falla, registrar alertas con severidad `Info`, `Warning` o `Error` sin bloquear el folio completo.
6. Las respuestas exitosas deben usar envelope `data`; los errores funcionales deben usar Problem Details.
7. `GET /v1/folios` permanece en el contrato del core para la generacion secuencial de folios.
8. Los catalogos son de solo lectura; no forman parte del alcance administrar altas o cambios de catalogos.
9. Los giros deben incluir `claveIncendio` porque se requiere para el calculo tecnico.
10. La vigencia de tarifas se resuelve por fecha actual dentro del rango `vigenciaDesde` y `vigenciaHasta`.
11. Los estados globales de cotizacion se limitan a `BORRADOR`, `EN_CAPTURA`, `LISTA_PARA_CALCULO` y `CALCULADA`.
12. El endpoint de estado puede complementar el estado global con `TieneAlertas`, `SeccionesCompletadas`, `UbicacionesCalculables`, `UbicacionesIncompletas`, `Version` y `FechaUltimaActualizacion`.
13. La moneda oficial del dominio es `COP`; todos los calculos usan `BigDecimal`, persistencia y respuestas con 2 decimales, y redondeo `HALF_UP`.
14. El dataset versionado del mock debe ser medio realista: 5 suscriptores, 12 agentes, 20 giros con `claveIncendio`, 4 clasificaciones de riesgo, 14 garantias, 60 codigos postales, 1 configuracion activa, matrices de tarifas por giro/zona/nivel y 2 o 3 cotizaciones semilla.

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripcion |
|---------|---------|---------|-------------|
| `ReferenciaCore` | modulo `plataforma-core-ohs` / fixtures en memoria | nueva | fachada de referencia para catalogos, validaciones y tarifas |
| `Cotizacion` | tabla `cotizaciones_danos` | sin cambio estructural | agregado principal del dominio en el backend de cotizacion |
| `Ubicacion` | tabla `cotizaciones_danos` | modificada conceptualmente | consume validacion territorial, estado de validacion y alertas |
| `CatalogoSuscriptor` | JSON en memoria | nueva | catalogo de solo lectura para captura |
| `CatalogoAgente` | JSON en memoria | nueva | catalogo de solo lectura para captura |
| `GiroComercial` | JSON en memoria | nueva | catalogo de linea de negocio con `claveIncendio` |
| `CodigoPostal` | JSON en memoria | nueva | relacion territorial y validacion postal |
| `ClasificacionRiesgo` | JSON en memoria | nueva | catalogo tecnico de riesgo |
| `GarantiaCatalogo` | JSON en memoria | nueva | catalogo tecnico de garantias |
| `TarifaTecnica` | JSON en memoria | nueva | tasas y factores tecnicos para el calculo |

#### Campos del modelo
| Campo | Tipo | Obligatorio | Validacion | Descripcion |
|-------|------|-------------|------------|-------------|
| `codigo` | string | si | unico, no vacio | identificador de catalogo |
| `nombre` | string | si | maximo 120 caracteres | descripcion legible para UI |
| `activo` | boolean | si | solo items activos por defecto | determina si el item se ofrece en captura |
| `claveIncendio` | string | si en giro | no vacio | clave tecnica necesaria para el calculo en giros |
| `zipCode` | string | si | formato local valido | codigo postal consultado o validado |
| `municipio` | string | si | no vacio | municipio asociado al CP |
| `estado` | string | si | no vacio | estado o departamento asociado al CP |
| `coloniaBarrio` | string | no | no vacio si se resuelve | detalle territorial de la direccion |
| `zona_tev` | string | no | no vacio si se resuelve | zona tecnica para uno de los caminos de calculo |
| `zona_fhm` | string | no | no vacio si se resuelve | zona tecnica para el camino FHM |
| `alertas` | array | si | lista serializable | alertas de validacion o enriquecimiento |
| `tariffKey` | string | si | unico por combinacion tecnica | clave compuesta de giro, zona y garantia |
| `giroCode` | string | si | debe existir en catalogo de giro | referencia al giro |
| `zonaCode` | string | si | debe existir en la zona tecnica aplicable | referencia a la zona tecnica |
| `garantiaCode` | string | si | debe existir en catalogo de garantias | referencia a la garantia |
| `rate` | decimal | si | precision definida por el dominio | tasa tecnica devuelta por la referencia |
| `factor` | decimal | si | precision definida por el dominio | factor tecnico aplicado al calculo |
| `moneda` | string | si | COP para este reto | moneda de la tarifa |
| `vigenciaDesde` | datetime | si | no posterior a `vigenciaHasta` | inicio de aplicabilidad |
| `vigenciaHasta` | datetime | si | no anterior a `vigenciaDesde` | fin de aplicabilidad |

#### Indices / Constraints
- Unicidad por `codigo` en suscriptores, agentes, giros, riesgos y garantias.
- Indice por `zipCode` para resolucion rapida de la validacion territorial.
- Indice por `tariffKey` para lookup tecnico de tarifas y factores.
- Constraint de vigencia `vigenciaDesde <= vigenciaHasta`.
- Constraint semantico para que solo un catalogo activo por clave sea consumido por defecto.
- Los fixtures versionados deben incluir un CP valido, un agente existente y un giro con `claveIncendio` para los escenarios de aceptacion.

### API Endpoints

> La referencia core vive como modulo mock separado y expone el contrato que consume `plataforma-danos-back`. Todos los endpoints usan envelope `data`.

#### GET /v1/subscribers
- **Descripcion**: lista suscriptores disponibles para la captura.
- **Auth requerida**: no / sesion demo si aplica.
- **Response 200**:
  ```json
  {
    "data": [
      { "codigo": "SUS-001", "nombre": "Suscriptor Norte", "activo": true }
    ]
  }
  ```
- **Notas**: catalogo de solo lectura.

#### GET /v1/agents
- **Descripcion**: lista agentes operativos disponibles.
- **Auth requerida**: no / sesion demo si aplica.
- **Response 200**:
  ```json
  {
    "data": [
      { "codigo": "AG-102", "nombre": "Agente Centro", "activo": true }
    ]
  }
  ```
- **Notas**: catalogo de solo lectura.

#### GET /v1/business-lines
- **Descripcion**: lista giros o lineas de negocio autorizadas.
- **Auth requerida**: no / sesion demo si aplica.
- **Response 200**:
  ```json
  {
    "data": [
      { "codigo": "GIRO-001", "nombre": "Oficinas", "activo": true, "claveIncendio": "INC-OFI" }
    ]
  }
  ```
- **Notas**: catalogo de solo lectura; `claveIncendio` es obligatoria para el giro.

#### GET /v1/zip-codes/{zipCode}
- **Descripcion**: consulta un codigo postal y devuelve el enriquecimiento territorial.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `zipCode`: codigo postal a resolver.
- **Response 200**:
  ```json
  {
    "data": {
      "zipCode": "110111",
      "valido": true,
      "municipio": "Bogota D.C.",
      "estado": "Bogota D.C.",
      "coloniaBarrio": "Chapinero",
      "zona_tev": "ZTEV-2",
      "zona_fhm": "ZFHM-1",
      "alertas": []
    }
  }
  ```
- **Response 200 cuando no se reconoce el CP**:
  ```json
  {
    "data": {
      "zipCode": "999999",
      "valido": false,
      "municipio": null,
      "estado": null,
      "coloniaBarrio": null,
      "zona_tev": null,
      "zona_fhm": null,
      "alertas": [
        {
          "codigo": "ZIP_NO_RECONOCIDO",
          "mensaje": "El codigo postal no esta registrado en la referencia core.",
          "severidad": "Warning"
        }
      ]
    }
  }
  ```
- **Notas**: no se usa `404` para un CP no reconocido; la UI maneja `valido: false`.

#### POST /v1/zip-codes/validate
- **Descripcion**: valida un codigo postal y retorna el resultado de negocio.
- **Auth requerida**: no / sesion demo si aplica.
- **Request Body**:
  ```json
  {
    "zipCode": "110111"
  }
  ```
- **Response 200**:
  ```json
  {
    "data": {
      "zipCode": "110111",
      "valido": true,
      "municipio": "Bogota D.C.",
      "estado": "Bogota D.C.",
      "coloniaBarrio": "Chapinero",
      "zona_tev": "ZTEV-2",
      "zona_fhm": "ZFHM-1",
      "alertas": []
    }
  }
  ```
- **Response 200 para CP no reconocido**:
  ```json
  {
    "data": {
      "zipCode": "999999",
      "valido": false,
      "municipio": null,
      "estado": null,
      "coloniaBarrio": null,
      "zona_tev": null,
      "zona_fhm": null,
      "alertas": [
        {
          "codigo": "ZIP_NO_RECONOCIDO",
          "mensaje": "El codigo postal no esta registrado en la referencia core.",
          "severidad": "Error"
        }
      ]
    }
  }
  ```
- **Response 400**: body malformado o faltante.
- **Notas**: la validacion territorial registra alertas, pero no bloquea el folio completo.

#### GET /v1/folios
- **Descripcion**: expone la secuencia o estado de folios administrado por la referencia core.
- **Auth requerida**: no / sesion demo si aplica.
- **Response 200**:
  ```json
  {
    "data": {
      "nextNumeroFolio": "1000001",
      "fuente": "mock",
      "vigente": true
    }
  }
  ```
- **Notas**: permanece en el core mock para la generacion secuencial de folios.

#### GET /v1/catalogs/risk-classification
- **Descripcion**: lista la clasificacion de riesgo aprobada.
- **Auth requerida**: no / sesion demo si aplica.
- **Response 200**:
  ```json
  {
    "data": [
      { "codigo": "RISK-A", "nombre": "Riesgo alto", "activo": true }
    ]
  }
  ```

#### GET /v1/catalogs/guarantees
- **Descripcion**: lista garantias disponibles para la configuracion de cobertura.
- **Auth requerida**: no / sesion demo si aplica.
- **Response 200**:
  ```json
  {
    "data": [
      { "codigo": "GAR-INC-ED", "nombre": "Incendio edificios", "activo": true }
    ]
  }
  ```

#### GET /v1/tariffs/{tariffKey}
- **Descripcion**: resuelve la tarifa tecnica por clave compuesta de giro, zona tecnica y garantia.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `tariffKey`: clave tecnica compuesta `giroCode|zonaCode|garantiaCode`.
- **Response 200**:
  ```json
  {
    "data": {
      "tariffKey": "GIRO-001|ZTEV-2|GAR-INC-ED",
      "giroCode": "GIRO-001",
      "zonaCode": "ZTEV-2",
      "garantiaCode": "GAR-INC-ED",
      "rate": 0.015,
      "factor": 1.2,
      "moneda": "COP",
      "vigenciaDesde": "2026-01-01T00:00:00Z",
      "vigenciaHasta": "2026-12-31T23:59:59Z"
    }
  }
  ```
- **Response 404**: no existe tarifa vigente para la combinacion solicitada.
- **Notas**: la tarifa vigente se selecciona por fecha actual dentro del rango de vigencia.

#### PUT /v1/tariffs/{tariffKey}
- **Descripcion**: contrato opcional para publicar o actualizar una tarifa tecnica en el mock.
- **Auth requerida**: no / sesion demo si aplica.
- **Estado**: opcional / contract-only para esta entrega.
- **Request Body**:
  ```json
  {
    "giroCode": "GIRO-001",
    "zonaCode": "ZTEV-2",
    "garantiaCode": "GAR-INC-ED",
    "rate": 0.015,
    "factor": 1.2,
    "moneda": "COP",
    "vigenciaDesde": "2026-01-01T00:00:00Z",
    "vigenciaHasta": "2026-12-31T23:59:59Z"
  }
  ```
- **Response 200**:
  ```json
  { "data": { "tariffKey": "GIRO-001|ZTEV-2|GAR-INC-ED" } }
  ```
- **Response 400**: payload invalido o vigencia inconsistente.
- **Notas**: no es parte del foco funcional; puede omitirse en la primera implementacion.

### Diseno Frontend

#### Componentes nuevos
| Componente | Archivo | Props principales | Descripcion |
|------------|---------|------------------|-------------|
| `CatalogLookupPanel` | `components/CatalogLookupPanel.tsx` | `title, items, loading, error, filterValue, onFilterChange` | muestra suscriptores, agentes y giros con filtro por nombre |
| `ZipCodeValidationCard` | `components/ZipCodeValidationCard.tsx` | `value, validation, onValidate` | valida un codigo postal y presenta el enriquecimiento territorial |
| `TariffPreviewCard` | `components/TariffPreviewCard.tsx` | `tariff, loading, error` | previsualiza tarifa y factores tecnicos para una ubicacion |
| `ValidationAlertList` | `components/ValidationAlertList.tsx` | `alerts` | visualiza alertas con severidad Info, Warning o Error |

#### PaginAS afectadas
| Pagina | Archivo | Ruta | Protegida |
|--------|---------|------|-----------|
| `FolioPage` | `pages/FolioPage.tsx` | `/cotizador` | no |
| `QuoteStatePage` | `pages/QuoteStatePage.tsx` | `/quotes/:folio/state` | no |
| `CatalogosCorePage` | - | - | no aplica; la consulta se integra en el flujo de la cotizacion |

#### Hooks y State
| Hook | Archivo | Retorna | Descripcion |
|------|---------|---------|-------------|
| `useCoreCatalogs` | `hooks/useCoreCatalogs.ts` | `{ subscribers, agents, businessLines, riskClassifications, guarantees, loading, error, reload }` | carga y refresca catalogos de referencia con filtro local por nombre |
| `useZipCodeValidation` | `hooks/useZipCodeValidation.ts` | `{ validation, loading, error, validateZipCode }` | valida codigos postales y expone alertas |
| `useTariffLookup` | `hooks/useTariffLookup.ts` | `{ tariff, loading, error, lookupTariff }` | resuelve tarifas y factores tecnicos |

#### Services (llamadas API)
| Funcion | Archivo | Endpoint |
|---------|---------|----------|
| `listSubscribers()` | `services/referenceCoreService.ts` | `GET /v1/subscribers` |
| `listAgents()` | `services/referenceCoreService.ts` | `GET /v1/agents` |
| `listBusinessLines()` | `services/referenceCoreService.ts` | `GET /v1/business-lines` |
| `getZipCode(zipCode)` | `services/referenceCoreService.ts` | `GET /v1/zip-codes/{zipCode}` |
| `validateZipCode(payload)` | `services/referenceCoreService.ts` | `POST /v1/zip-codes/validate` |
| `listRiskClassification()` | `services/referenceCoreService.ts` | `GET /v1/catalogs/risk-classification` |
| `listGuarantees()` | `services/referenceCoreService.ts` | `GET /v1/catalogs/guarantees` |
| `getTariff(tariffKey)` | `services/referenceCoreService.ts` | `GET /v1/tariffs/{tariffKey}` |
| `getFolioSequence()` | `services/referenceCoreService.ts` | `GET /v1/folios` |

### Arquitectura y Dependencias
- La referencia core se implementa como modulo separado `plataforma-core-ohs` con mock/stub en memoria y fixtures JSON versionadas.
- `plataforma-danos-back` consume ese contrato mediante un cliente o adaptador HTTP y mantiene su propia persistencia de cotizacion con JPA, PostgreSQL y Flyway.
- El core mock no requiere nuevas tablas; la fuente de verdad es el fixture versionado y el contrato OpenAPI.
- La UI no expone una pagina independiente del core; integra los widgets de consulta dentro del flujo de cotizacion y reutiliza los mismos servicios en futuras pantallas de ubicaciones.
- La documentacion prioritaria de este contrato es OpenAPI y Markdown en el README.

### Notas de Implementacion
> El foco tecnico es mantener un contrato estable, reutilizable y sin hardcodeo en la UI. Los escenarios de aceptacion deben contar con fixtures suficientes para un CP valido, un CP invalido, un agente existente y un giro con `claveIncendio`.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada item (`[x]`) al completarlo.
> El Orchestrator monitorea este checklist para determinar el progreso.

### Backend

#### Implementacion
- [x] Definir DTOs de catalogos, validacion postal y tarifa tecnica
- [x] Definir puertos de salida para la referencia core mock y el consumo desde cotizaciones
- [x] Implementar el modulo `plataforma-core-ohs` con fixtures JSON cargados en memoria
- [ ] Implementar el cliente o adaptador de consumo en `plataforma-danos-back`
- [x] Implementar controller(s) mock `/v1/subscribers`, `/v1/agents`, `/v1/business-lines`, `/v1/zip-codes`, `/v1/catalogs`, `/v1/folios` y documentar OpenAPI
- [x] Documentar respuestas exitosas con envelope `data` y errores con Problem Details
- [x] Agregar fixtures versionados para suscriptores, agentes, giros, CPs, garantias y tarifas
- [x] Incluir al menos un CP valido, un CP invalido, un agente existente y un giro con `claveIncendio`
- [x] Mantener `GET /v1/folios` en el core mock como contrato de secuencia

#### Tests Backend
- [x] Caso feliz de catalogo base por tipo de referencia
- [x] Caso feliz de validacion y enriquecimiento territorial de codigo postal
- [x] Caso de CP no reconocido con `valido: false` y alertas
- [x] Caso feliz de lookup de tarifa tecnica
- [x] Controller o facade retorna `data` en 200
- [x] Controller o facade retorna Problem Details en error funcional
- [x] Contrato o test de integracion del mock core

### Frontend

#### Implementacion
- [ ] Crear servicios Axios para catalogos, zip codes y tarifas
- [ ] Crear hooks para carga de catalogos, validacion postal y lookup tecnico
- [ ] Implementar componentes de UI para busqueda, validacion y alertas no bloqueantes
- [ ] Integrar los componentes en el flujo de cotizacion, no en una pagina separada del core
- [ ] Agregar filtro local por nombre para mejorar la consulta de catalogos
- [ ] Reutilizar la capa en futuras pantallas de ubicaciones

#### Tests Frontend
- [ ] Componente de catalogo renderiza items y estados de carga
- [ ] Componente de validacion postal muestra alerta cuando el codigo es invalido
- [ ] Hook carga catalogos exitosamente
- [ ] Hook devuelve error y estado vacio cuando falla la referencia core
- [ ] Pagina integra el flujo principal y conserva el contrato de datos

### QA
- [ ] Ejecutar coverage unitario minimo del 80 por ciento
- [ ] Ejecutar skill `/gherkin-case-generator` -> criterios CRITERIO-1.1, 1.2, 2.1, 2.2, 2.3
- [ ] Ejecutar skill `/risk-identifier` -> clasificacion ASD de riesgos por mock, CP y tarifas tecnicas
- [ ] Revisar cobertura de tests contra criterios de aceptacion
- [ ] Validar que todas las reglas de negocio estan cubiertas
- [ ] Definir integracion critica de folio creado, CP validado y calculo por ubicacion
- [ ] Actualizar estado spec: `status: IMPLEMENTED`

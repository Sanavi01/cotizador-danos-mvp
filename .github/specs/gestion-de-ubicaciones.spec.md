---
id: SPEC-005
status: APPROVED
feature: gestion-de-ubicaciones
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-001", "SPEC-002", "SPEC-004"]
---

# Spec: Gestion de Ubicaciones

> **Estado:** `APPROVED`.
> **Ciclo de vida:** DRAFT -> APPROVED -> IN_PROGRESS -> IMPLEMENTED -> DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripcion
Esta funcionalidad permite registrar, consultar y editar ubicaciones de riesgo dentro de una cotizacion sin sobrescribir otras secciones del agregado. La seccion soporta dos operaciones complementarias: reemplazo total del snapshot vigente mediante `PUT` y edicion puntual por `indice` mediante `PATCH`. El modelo debe admitir captura progresiva con shape fijo, derivar `estadoValidacion` y `alertasBloqueantes` en backend, y exponer un resumen operativo util para navegacion, progreso y calculo tecnico.

### Requerimiento de Negocio
Fuente principal: `.github/requirements/gestion-de-ubicaciones.md`.

Resumen del requerimiento base:
- Consultar la lista de ubicaciones de un folio.
- Guardar una o varias ubicaciones dentro de la misma cotizacion.
- Editar una ubicacion puntual sin sobrescribir las demas.
- Consultar un resumen de ubicaciones para apoyo de navegacion y seguimiento.
- Mantener `alertasBloqueantes` y `estadoValidacion` por ubicacion.
- Responder con envelope `data` en exito y Problem Details en error.

### Historias de Usuario

#### HU-01: Registrar una o varias ubicaciones

```
Como:        usuario del cotizador
Quiero:      capturar una o varias ubicaciones en un mismo folio
Para:        representar correctamente los riesgos asegurables de la cotizacion

Prioridad:   Alta
Estimacion:  L
Dependencias: Configuracion de Layout de Ubicaciones, Catalogos y Validaciones Core
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Guardar el snapshot vigente de ubicaciones en un folio
  Dado que la cotizacion ya tiene numeroFolio y configuracionLayout
  Cuando el usuario envia la coleccion completa vigente de ubicaciones
  Entonces el sistema reemplaza solo la seccion `ubicaciones` dentro del agregado cotizacion
  Y conserva un indice unico por ubicacion
  Y actualiza version y fechaUltimaActualizacion
```

**Happy Path**
```gherkin
CRITERIO-1.2: Consultar las ubicaciones existentes
  Dado que la cotizacion ya tiene ubicaciones guardadas
  Cuando el usuario consulta la seccion de ubicaciones
  Entonces el sistema devuelve la lista actual de ubicaciones del folio
  Y la UI permite revisar el detalle de cada ubicacion
```

**Edge Case**
```gherkin
CRITERIO-1.3: Guardar ubicaciones parciales
  Dado que una o mas ubicaciones cumplen solo una parte de las reglas de calculo
  Cuando el usuario guarda la informacion disponible
  Entonces el sistema persiste las ubicaciones parciales
  Y marca alertasBloqueantes y estadoValidacion por ubicacion
```

#### HU-02: Editar una ubicacion puntual

```
Como:        usuario del cotizador
Quiero:      modificar una ubicacion especifica
Para:        corregir informacion sin afectar el resto de ubicaciones del folio

Prioridad:   Alta
Estimacion:  M
Dependencias: Configuracion de Layout de Ubicaciones
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-02

**Happy Path**
```gherkin
CRITERIO-2.1: Editar una ubicacion por indice
  Dado que la cotizacion tiene varias ubicaciones registradas
  Cuando el usuario actualiza una ubicacion puntual por su indice
  Entonces el sistema modifica solo esa ubicacion
  Y mantiene intactas las otras ubicaciones del folio
```

**Error Path**
```gherkin
CRITERIO-2.2: Intentar editar una ubicacion inexistente
  Dado que el indice solicitado no existe dentro del folio
  Cuando el usuario intenta actualizarlo
  Entonces el sistema responde con error funcional de no encontrado
```

**Edge Case**
```gherkin
CRITERIO-2.3: Rechazar una edicion con version desactualizada
  Dado que la cotizacion ya fue modificada por otro usuario o proceso
  Cuando el cliente intenta editar una ubicacion con una version antigua
  Entonces el sistema rechaza la actualizacion
  Y conserva el ultimo estado persistido
```

#### HU-03: Guardar una ubicacion incompleta sin bloquear el folio completo

```
Como:        usuario del cotizador
Quiero:      poder guardar una ubicacion aun cuando falten datos para calcularla
Para:        continuar con la captura del resto del folio

Prioridad:   Alta
Estimacion:  M
Dependencias: Configuracion de Layout de Ubicaciones, Catalogos y Validaciones Core
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-03

**Happy Path**
```gherkin
CRITERIO-3.1: Guardar una ubicacion incompleta
  Dado que una ubicacion tiene datos minimos de captura pero no cumple todas las reglas de calculo
  Cuando el usuario guarda la informacion
  Entonces el sistema persiste la ubicacion
  Y marca alertasBloqueantes y estadoValidacion para esa ubicacion
  Y no impide registrar o editar las demas ubicaciones del folio
```

**Edge Case**
```gherkin
CRITERIO-3.2: Recuperar el resumen de ubicaciones con alertas
  Dado que el folio contiene ubicaciones completas e incompletas
  Cuando el usuario consulta el resumen de ubicaciones
  Entonces el sistema devuelve el conteo de ubicaciones calculables e incompletas
  Y expone las alertas vigentes por ubicacion
```

### Reglas de Negocio
1. Cada ubicacion se identifica por `indice` unico dentro de la cotizacion y siempre debe quedar dentro del rango definido por `configuracionLayout`.
2. `PUT /v1/quotes/{folio}/locations` reemplaza la coleccion completa de la seccion `ubicaciones`; el cliente debe enviar el snapshot vigente completo y los elementos omitidos se consideran eliminados.
3. `PATCH /v1/quotes/{folio}/locations/{indice}` actualiza solo la ubicacion solicitada y no altera las demas.
4. Las ubicaciones pueden guardarse parcialmente, pero el read model siempre debe devolver el shape completo del dominio con campos faltantes en `null`.
5. `estadoValidacion` es derivado por backend y se limita a `EMPTY`, `INCOMPLETE`, `INVALID`, `VALID` y `CALCULABLE`.
6. `INCOMPLETE` significa faltan datos requeridos; `INVALID` significa existen datos incorrectos; `VALID` significa estructura completa; `CALCULABLE` significa estructura valida y elegibilidad tecnica resuelta.
7. Un `codigoPostal` invalido puede persistirse, pero deja la ubicacion en `INVALID` y genera `alertasBloqueantes` de ambito ubicacion.
8. `alertasBloqueantes` puede impedir el calculo de una ubicacion puntual, pero no bloquea el folio completo.
9. `garantias[]` existe en el read model de ubicacion como proyeccion derivada de `opcionesCobertura`; el snapshot persistido para calculo vive en el resultado financiero.
10. Toda actualizacion valida incrementa `version` y actualiza `fechaUltimaActualizacion`.
11. La consulta de resumen debe servir para navegacion operativa y para el estado/progreso del folio, midiendo progreso contra los slots esperados del layout.
12. Las respuestas exitosas deben usar envelope `data`; los errores deben publicarse como Problem Details.

---

## 2. DISENO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripcion |
|---------|---------|---------|-------------|
| `Cotizacion` | tabla `cotizaciones_danos` | modificada | agregado principal; conserva `numeroFolio`, `estadoCotizacion`, `version` y `fechaUltimaActualizacion` |
| `UbicacionCotizacion` | tabla `cotizacion_ubicaciones` | nueva | representa una ubicacion de riesgo dentro del folio |
| `UbicacionDetalle` | JSONB o subestructura de `UbicacionCotizacion` | nueva | datos capturados de la ubicacion segun el layout y los catalogos aprobados |
| `AlertaBloqueante` | JSONB o subestructura de `UbicacionCotizacion` | nueva | alertas de validacion o calculo asociadas a una ubicacion |

#### Campos del modelo
| Campo | Tipo | Obligatorio | Validacion | Descripcion |
|-------|------|-------------|------------|-------------|
| `numeroFolio` | string | si | unico, no vacio | Identificador funcional de la cotizacion |
| `indice` | integer | si | unico dentro de la cotizacion, mayor o igual a 1 | Posicion logica de la ubicacion |
| `version` | long | si | control optimista | Version del agregado raiz |
| `fechaUltimaActualizacion` | datetime | si | auto-actualizado | Marca de la ultima modificacion logica |
| `estadoValidacion` | string | si | enum: `EMPTY`, `INCOMPLETE`, `INVALID`, `VALID`, `CALCULABLE` | Resultado operativo derivado de la ubicacion |
| `alertasBloqueantes` | array | si | serializable, puede estar vacio | Alertas que impiden el calculo de la ubicacion puntual |
| `nombreUbicacion` | string | no | requerido junto con `codigoPostal` para guardar draft | Nombre visible de la ubicacion |
| `direccion` | string | no | libre, puede ser `null` | Direccion de la ubicacion |
| `codigoPostal` | string | no | puede persistirse invalido; impacta `estadoValidacion` | Codigo postal capturado |
| `estado` | string | no | derivado o capturado | Division territorial de la ubicacion |
| `municipio` | string | no | derivado o capturado | Municipio asociado |
| `colonia` | string | no | derivado o capturado | Colonia o barrio |
| `ciudad` | string | no | derivado o capturado | Ciudad de la ubicacion |
| `tipoConstructivo` | string | no | codigo canonico preferido del core (`MAMP`, `MIX`, `MET`); puede persistirse otro valor documentado si requiere homologacion | Tipo constructivo del riesgo |
| `nivel` | integer | no | mayor o igual a 1 si aplica; representa el piso real persistido. Las bandas tecnicas `BAS`, `MED`, `ALT` del fixture son referencia de UX y tarifa, no el campo persistido | Nivel o piso de la ubicacion |
| `anioConstruccion` | integer | no | rango razonable | Anio de construccion |
| `giro` | object | no | puede ser `null` en draft | Giro tecnico de la ubicacion |
| `giro.codigo` | string | no | debe existir en catalogo cuando se informa | Codigo del giro |
| `giro.nombre` | string | no | eco de lectura | Nombre del giro |
| `giro.claveIncendio` | string | no | obligatorio para `CALCULABLE` | Clave tecnica requerida por calculo |
| `garantias` | array | si | read model derivado; `[]` cuando no hay proyeccion | Garantias aplicables a la ubicacion |
| `garantiaCode` | string | no por item | id canonico de garantia | Identificador de garantia derivada |
| `zonaCatastrofica` | object | no | derivado de CP valido si aplica | Zonas tecnicas para calculo |
| `zonaCatastrofica.zonaTev` | string | no | derivado | Zona tecnica TEV |
| `zonaCatastrofica.zonaFhm` | string | no | derivado | Zona tecnica FHM |
| `createdAt` | datetime | si | auto-actualizado | Fecha de creacion del registro |
| `updatedAt` | datetime | si | auto-actualizado | Fecha de ultima actualizacion del registro |

#### Indices / Constraints
- Constraint unico sobre `cotizacion_id + indice` para garantizar un indice unico por ubicacion dentro del folio.
- Foreign key desde `cotizacion_ubicaciones.cotizacion_id` hacia `cotizaciones_danos.id` con borrado en cascada.
- Indice por `cotizacion_id` para consultar la lista completa de ubicaciones de una cotizacion.
- Indice por `estado_validacion` si se requiere resumen operativo o filtrado de ubicaciones incompletas.
- Constraint de no nulos para `indice` y `estado_validacion`.
- Constraint semantico para impedir indices duplicados o menores a 1.
- Constraint semantico para impedir indices fuera del rango vigente definido por `configuracionLayout`.

### API Endpoints

#### GET /v1/quotes/{folio}/locations
- **Descripcion**: consulta la lista de ubicaciones de una cotizacion.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `folio`: numeroFolio de la cotizacion.
- **Response 200**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "version": 3,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "ubicaciones": [
        {
          "indice": 1,
          "nombreUbicacion": "Planta principal",
          "direccion": "Calle 100 # 10-10",
          "codigoPostal": "110111",
          "estado": "Cundinamarca",
          "municipio": "Bogota D.C.",
          "colonia": "Chapinero",
          "ciudad": "Bogota D.C.",
          "tipoConstructivo": "MAMP",
          "nivel": 1,
          "anioConstruccion": 2018,
          "giro": {
            "codigo": "GIRO-001",
            "nombre": "Oficinas",
            "claveIncendio": "INC-OFI"
          },
          "garantias": [
            {
              "garantiaCode": "GAR-INC-ED",
              "origen": "GLOBAL",
              "tariffablePreview": true
            }
          ],
          "zonaCatastrofica": {
            "zonaTev": "ZTEV-1",
            "zonaFhm": "ZFHM-1"
          },
          "estadoValidacion": "CALCULABLE",
          "alertasBloqueantes": [],
          "createdAt": "2026-04-21T00:00:00Z",
          "updatedAt": "2026-04-21T00:00:00Z"
        },
        {
          "indice": 2,
          "nombreUbicacion": "Bodega secundaria",
          "direccion": null,
          "codigoPostal": "000000",
          "estado": null,
          "municipio": null,
          "colonia": null,
          "ciudad": null,
          "tipoConstructivo": null,
          "nivel": null,
          "anioConstruccion": null,
          "giro": null,
          "garantias": [],
          "zonaCatastrofica": null,
          "estadoValidacion": "INVALID",
          "alertasBloqueantes": [
            {
              "codigo": "UBICACION_SIN_ZIP",
              "mensaje": "La ubicacion no tiene codigo postal valido.",
              "severidad": "Warning"
            }
          ],
          "createdAt": "2026-04-21T00:00:00Z",
          "updatedAt": "2026-04-21T00:00:00Z"
        }
      ]
    }
  }
  ```
- **Response 200 cuando no hay ubicaciones**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "version": 3,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "ubicaciones": []
    }
  }
  ```
- **Response 404**: numeroFolio inexistente.

#### PUT /v1/quotes/{folio}/locations
- **Descripcion**: reemplaza el snapshot completo de la coleccion de ubicaciones del folio sin modificar otras secciones del agregado.
- **Semantica MVP**: el cliente debe enviar la coleccion completa vigente; cualquier `indice` omitido se considera eliminado dentro de la seccion `ubicaciones`.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `folio`: numeroFolio de la cotizacion.
- **Request Body**:
  ```json
  {
    "version": 3,
    "ubicaciones": [
      {
        "indice": 1,
        "nombreUbicacion": "Planta principal",
        "direccion": "Calle 100 # 10-10",
        "codigoPostal": "110111",
        "estado": "Cundinamarca",
        "municipio": "Bogota D.C.",
        "colonia": "Chapinero",
        "ciudad": "Bogota D.C.",
        "tipoConstructivo": "MAMP",
        "nivel": 1,
        "anioConstruccion": 2018,
        "giro": {
          "codigo": "GIRO-001",
          "nombre": "Oficinas",
          "claveIncendio": "INC-OFI"
        },
        "zonaCatastrofica": {
          "zonaTev": "ZTEV-1",
          "zonaFhm": "ZFHM-1"
        }
      },
      {
        "indice": 2,
        "nombreUbicacion": "Bodega secundaria",
        "direccion": null,
        "codigoPostal": "000000",
        "estado": null,
        "municipio": null,
        "colonia": null,
        "ciudad": null,
        "tipoConstructivo": null,
        "nivel": null,
        "anioConstruccion": null,
        "giro": null,
        "zonaCatastrofica": null
      }
    ]
  }
  ```
- **Response 200**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "version": 4,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "ubicaciones": [
        {
          "indice": 1,
          "nombreUbicacion": "Planta principal",
          "direccion": "Calle 100 # 10-10",
          "codigoPostal": "110111",
          "estado": "Cundinamarca",
          "municipio": "Bogota D.C.",
          "colonia": "Chapinero",
          "ciudad": "Bogota D.C.",
          "tipoConstructivo": "MAMP",
          "nivel": 1,
          "anioConstruccion": 2018,
          "giro": {
            "codigo": "GIRO-001",
            "nombre": "Oficinas",
            "claveIncendio": "INC-OFI"
          },
          "garantias": [
            {
              "garantiaCode": "GAR-INC-ED",
              "origen": "GLOBAL",
              "tariffablePreview": true
            }
          ],
          "zonaCatastrofica": {
            "zonaTev": "ZTEV-1",
            "zonaFhm": "ZFHM-1"
          },
          "estadoValidacion": "CALCULABLE",
          "alertasBloqueantes": [],
          "createdAt": "2026-04-21T00:00:00Z",
          "updatedAt": "2026-04-21T00:00:00Z"
        }
      ]
    }
  }
  ```
- **Response 400**: payload mal formado o con indices repetidos dentro del mismo request.
- **Response 404**: numeroFolio inexistente.
- **Response 409**: version desactualizada o conflicto de concurrencia.
- **Response 422**: ubicaciones fuera del rango del layout, snapshot incompleto respecto al layout o reglas de negocio invalidas.

#### PATCH /v1/quotes/{folio}/locations/{indice}
- **Descripcion**: actualiza parcialmente una ubicacion puntual identificada por indice.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `folio`: numeroFolio de la cotizacion.
  - `indice`: indice unico de la ubicacion dentro del folio.
- **Request Body**:
  ```json
  {
    "version": 3,
    "changes": {
      "codigoPostal": "050002",
      "tipoConstructivo": "MIX",
      "giro": {
        "codigo": "GIRO-002",
        "nombre": "Bodegas",
        "claveIncendio": "INC-BOD"
      }
    }
  }
  ```
- **Response 200**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "version": 4,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "ubicacion": {
        "indice": 2,
        "nombreUbicacion": "Bodega secundaria",
        "direccion": null,
        "codigoPostal": "050002",
        "estado": "Antioquia",
        "municipio": "Medellin",
        "colonia": "El Poblado",
        "ciudad": "Medellin",
        "tipoConstructivo": "MIX",
        "nivel": null,
        "anioConstruccion": null,
        "giro": {
          "codigo": "GIRO-002",
          "nombre": "Bodegas",
          "claveIncendio": "INC-BOD"
        },
        "garantias": [],
        "zonaCatastrofica": {
          "zonaTev": "ZTEV-2",
          "zonaFhm": "ZFHM-1"
        },
        "estadoValidacion": "VALID",
        "alertasBloqueantes": []
      }
    }
  }
  ```
- **Response 404**: numeroFolio inexistente o `indice` inexistente.
- **Response 409**: version desactualizada o conflicto de concurrencia.
- **Response 422**: regla tecnica o semantica incumplida por la ubicacion.

#### GET /v1/quotes/{folio}/locations/summary
- **Descripcion**: consulta un resumen operativo de las ubicaciones de la cotizacion.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `folio`: numeroFolio de la cotizacion.
- **Response 200**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "totalEsperado": 3,
      "totalActual": 2,
      "calculables": 1,
      "incompletas": 0,
      "invalidas": 1,
      "conAlertas": 1,
      "resumenPorIndice": [
        {
          "indice": 1,
          "slotEsperado": true,
          "estadoValidacion": "CALCULABLE",
          "tieneAlertasBloqueantes": false
        },
        {
          "indice": 2,
          "slotEsperado": true,
          "estadoValidacion": "INVALID",
          "tieneAlertasBloqueantes": true
        },
        {
          "indice": 3,
          "slotEsperado": true,
          "estadoValidacion": "EMPTY",
          "tieneAlertasBloqueantes": false
        }
      ]
    }
  }
  ```
- **Response 404**: numeroFolio inexistente.

### Diseno Frontend

#### Componentes nuevos
| Componente | Archivo | Props principales | Descripcion |
|------------|---------|------------------|-------------|
| `LocationsList` | `components/LocationsList.tsx` | `locations, onEdit, onSelect` | Lista las ubicaciones registradas y su estado |
| `LocationForm` | `components/LocationForm.tsx` | `value, onChange, onSubmit, loading, businessLines, zipCodeValidation, onZipCodeSelect` | Formulario para crear o editar una ubicacion |
| `LocationSummaryCard` | `components/LocationSummaryCard.tsx` | `summary` | Tarjeta con conteos, alertas y estado operativo |
| `ValidationAlertList` | `components/ValidationAlertList.tsx` | `alerts` | Reutilizado para mostrar alertasBloqueantes |
| `IdempotencyNotice` | `components/IdempotencyNotice.tsx` | `message, variant` | Reutilizado para mensajes de carga o error |

#### Páginas nuevas
| Pagina | Archivo | Ruta | Protegida |
|--------|---------|------|-----------|
| `LocationsPage` | `pages/LocationsPage.tsx` | `/quotes/:folio/locations` | no |
| `LocationDetailPage` | `pages/LocationDetailPage.tsx` | `/quotes/:folio/locations/:indice` | no |

#### Hooks y State
| Hook | Archivo | Retorna | Descripcion |
|------|---------|---------|-------------|
| `useLocations` | `hooks/useLocations.ts` | `{ locations, loading, saving, error, loadLocations, saveLocations, updateLocation, reset }` | Orquesta consulta, guardado masivo y edicion puntual |
| `useLocationSummary` | `hooks/useLocationSummary.ts` | `{ summary, loading, error, refresh }` | Consulta el resumen operativo de ubicaciones |
| `useLocationEditor` | `hooks/useLocationEditor.ts` | `{ location, loading, saving, error, loadLocation, saveLocation }` | Administra la edicion de una ubicacion por indice |

#### Services (llamadas API)
| Funcion | Archivo | Endpoint |
|---------|---------|----------|
| `getLocations(folio)` | `services/quoteLocationsService.ts` | `GET /v1/quotes/{folio}/locations` |
| `updateLocations(folio, payload)` | `services/quoteLocationsService.ts` | `PUT /v1/quotes/{folio}/locations` |
| `updateLocation(folio, indice, payload)` | `services/quoteLocationsService.ts` | `PATCH /v1/quotes/{folio}/locations/{indice}` |
| `getLocationsSummary(folio)` | `services/quoteLocationsService.ts` | `GET /v1/quotes/{folio}/locations/summary` |

### Arquitectura y Dependencias
- Backend nuevo en `plataforma-danos-back` bajo la capa hexagonal existente: controller -> application -> domain -> infrastructure.
- La persistencia debe modelar cada ubicacion como una entidad independiente dentro del agregado `cotizacion` y usar `indice` unico por folio.
- La seccion depende de la configuracionLayout para validar la estructura esperada y el orden de captura.
- Las validaciones por ubicacion deben consumir la referencia core y derivar en backend `alertasBloqueantes` y `estadoValidacion`.
- La respuesta de estado de cotizacion debe poder reflejar las ubicaciones esperadas por layout, incluyendo slots faltantes normalizados como `EMPTY`.
- `garantias[]` se expone en el read model como proyeccion derivada de la seccion global de coberturas; no se edita por ubicacion en esta capability.
- La SPA debe agregar una vista de lista, detalle y edicion puntual para la captura de ubicaciones.
- La captura frontend debe priorizar seleccion guiada desde catalogos y recomendaciones del fixture antes que entrada manual para `codigoPostal`, `giro` y referencias tecnicas reutilizables.
- La respuesta del backend debe seguir el envelope `data` y los errores deben mapearse a Problem Details, compatible con el manejo ya existente en la app.

### Notas de Implementacion
> La ubicacion debe poder guardarse aun cuando no este lista para calculo. El contrato separa la persistencia funcional del estado derivado para que el usuario pueda avanzar por el folio sin perder trabajo. `PUT` reemplaza el snapshot completo de la seccion `ubicaciones`; `PATCH` modifica solo una ubicacion. El backend deriva `estadoValidacion` y `alertasBloqueantes` a partir de los datos persistidos.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada item (`[x]`) al completarlo.
> El Orchestrator monitorea este checklist para determinar el progreso.

### Backend

#### Implementacion
- [x] Crear request/response DTOs para lista, guardado masivo, edicion puntual y resumen de ubicaciones
- [x] Implementar entidad de dominio para ubicaciones, alertas y estado de validacion
- [x] Implementar caso de uso de consulta de ubicaciones por `numeroFolio`
- [x] Implementar caso de uso de guardado masivo con validacion de layout y catalogos
- [x] Implementar caso de uso de edicion puntual por `indice`
- [x] Implementar caso de uso de resumen operativo de ubicaciones
- [x] Implementar adaptador JPA y migracion Flyway para `cotizacion_ubicaciones`
- [x] Implementar controller `/v1/quotes/{folio}/locations` y `/summary`
- [x] Documentar OpenAPI del contrato

#### Tests Backend
- [x] Caso de uso happy path de consulta de ubicaciones
- [x] Caso de uso happy path de guardado de una o varias ubicaciones
- [x] Caso de uso con edicion puntual por indice
- [x] Caso de uso con ubicacion inexistente
- [x] Caso de uso con ubicacion incompleta y alertasBloqueantes
- [x] Caso de uso con conflicto de version desactualizada
- [x] Controller con respuesta `200` y envelope `data`
- [x] Controller con Problem Details ante error relevante

### Frontend

#### Implementacion
- [x] Crear servicio Axios para consultar, guardar, editar y resumir ubicaciones
- [x] Crear hooks para lista, detalle y resumen operativo
- [x] Implementar lista de ubicaciones y formulario de edicion
- [x] Implementar pagina de detalle por indice y registrar ruta nueva
- [x] Integrar alertasBloqueantes y estadoValidacion en la UI
- [x] Mantener mensajes de error y estado con envelope `data` y Problem Details

#### Tests Frontend
- [x] Componente principal renderiza lista de ubicaciones
- [x] Componente dispara guardado masivo y edicion puntual
- [x] Hook maneja carga exitosa y lista vacia
- [x] Hook maneja error de folio inexistente o conflicto de version
- [x] Pagina integra consulta, edicion y resumen

### QA
- [ ] Ejecutar skill `/gherkin-case-generator` -> criterios CRITERIO-1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 3.2
- [ ] Ejecutar skill `/risk-identifier` -> clasificacion ASD de riesgos para guardado parcial y edicion por indice
- [ ] Revisar cobertura de tests contra criterios de aceptacion
- [ ] Validar que todas las reglas de negocio estan cubiertas
- [ ] Actualizar estado spec: `status: IMPLEMENTED`

---
id: SPEC-004
status: APPROVED
feature: configuracion-de-layout-de-ubicaciones
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-001", "SPEC-003"]
---

# Spec: Configuracion de Layout de Ubicaciones

> **Estado:** `DRAFT` -> aprobar con `status: APPROVED` antes de iniciar implementacion.
> **Ciclo de vida:** DRAFT -> APPROVED -> IN_PROGRESS -> IMPLEMENTED -> DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripcion
Esta funcionalidad permite consultar, capturar y actualizar la configuracionLayout de una cotizacion para preparar la captura ordenada de una o multiples ubicaciones de riesgo. La seccion debe conservar version y fechaUltimaActualizacion, permitir reabrir la configuracion existente y evitar que un cambio de layout afecte otras secciones del agregado.

### Requerimiento de Negocio
Fuente principal: `.github/requirements/configuracion-de-layout-de-ubicaciones.md`.

Resumen del requerimiento base:
- Consultar la configuracion actual del layout de ubicaciones.
- Guardar o actualizar la configuracionLayout de la cotizacion.
- Preparar el folio para la captura ordenada de una o multiples ubicaciones.
- Mantener trazabilidad de version y fechaUltimaActualizacion al modificar el layout.
- Responder con envelope `data` en exito y Problem Details en error.

### Historias de Usuario

#### HU-01: Configurar la estructura de ubicaciones del folio

```
Como:        usuario del cotizador
Quiero:      definir el layout de ubicaciones antes de capturarlas
Para:        organizar correctamente la distribucion del riesgo dentro de la cotizacion

Prioridad:   Alta
Estimacion:  M
Dependencias: Datos Generales de Cotizacion
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Guardar el layout de ubicaciones
  Dado que la cotizacion ya cuenta con numeroFolio
  Cuando el usuario define la configuracionLayout de ubicaciones
  Entonces el sistema persiste la configuracion del layout sin sobrescribir otras secciones del agregado
  Y deja disponible el folio para registrar una o multiples ubicaciones
  Y actualiza version y fechaUltimaActualizacion
```

**Happy Path**
```gherkin
CRITERIO-1.2: Consultar el layout previamente configurado
  Dado que la cotizacion ya tiene configuracionLayout guardada
  Cuando el usuario vuelve a abrir la seccion
  Entonces el sistema devuelve la misma configuracion persistida
  Y la UI la presenta para revision o edicion
```

**Edge Case**
```gherkin
CRITERIO-1.3: Abrir una cotizacion existente sin layout configurado
  Dado que existe una cotizacion valida pero aun no se ha guardado la configuracionLayout
  Cuando el usuario consulta la seccion de layout por numeroFolio
  Entonces el sistema responde con el folio y un layout vacio o inicializable
  Y permite iniciar la configuracion sin crear un segundo folio
```

#### HU-02: Evitar cambios inconsistentes de layout

```
Como:        sistema de cotizacion
Quiero:      controlar modificaciones del layout con versionado optimista
Para:        evitar perdida silenciosa de trabajo concurrente sobre el folio

Prioridad:   Alta
Estimacion:  S
Dependencias: Folios e Idempotencia
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-02

**Error Path**
```gherkin
CRITERIO-2.1: Detectar conflicto de version al editar layout
  Dado que dos usuarios o procesos intentan modificar el mismo folio
  Cuando uno de ellos guarda con una version desactualizada
  Entonces el sistema rechaza la actualizacion
  Y informa un conflicto funcional al cliente en formato Problem Details
```

**Error Path**
```gherkin
CRITERIO-2.2: Rechazar la consulta o guardado de un folio inexistente
  Dado que el numeroFolio no corresponde a una cotizacion existente
  Cuando se intenta consultar o guardar la configuracionLayout
  Entonces el sistema responde con error funcional de no encontrado
```

**Edge Case**
```gherkin
CRITERIO-2.3: Mantener la coherencia del estado del folio
  Dado que la cotizacion tenia estado BORRADOR y ya se guardo un layout valido
  Cuando el sistema consolida la actualizacion del agregado
  Entonces el estado global puede reflejar EN_CAPTURA
  Y el resumen del folio muestra la seccion de layout como completada
```

### Reglas de Negocio
1. `configuracionLayout` describe la forma en que se capturaran y distribuiran las ubicaciones del folio.
2. Una cotizacion puede operar con una o multiples ubicaciones.
3. Los cambios sobre el layout deben respetar versionado optimista.
4. Guardar el layout no debe sobrescribir datos generales, ubicaciones, coberturas ni resultados financieros.
5. La seccion de layout debe persistirse y consultarse de forma independiente, pero seguir perteneciendo al agregado `cotizacion`.
6. Cuando el layout es valido, la actualizacion debe incrementar `version` y actualizar `fechaUltimaActualizacion`.
7. La consulta de la seccion debe poder devolver una configuracion vacia o inicializable con shape fijo y la `version` real del agregado si la cotizacion existe pero aun no tiene layout.
8. Las respuestas exitosas deben usar envelope `data`; los errores deben publicarse como Problem Details.
9. La actualizacion del layout debe ser atomica y no dejar una configuracion parcial visible.
10. `cantidadUbicaciones` define el total esperado de slots para progreso, captura y validacion de rangos de `indice`.
11. No se deben permitir ubicaciones persistidas fuera del rango definido por el layout vigente.
12. El layout debe ser consumible por la futura captura de ubicaciones sin requerir re-trabajo del contrato principal.

---

## 2. DISENO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripcion |
|---------|---------|---------|-------------|
| `Cotizacion` | tabla `cotizaciones_danos` | modificada | agregado principal; conserva `numeroFolio`, `estadoCotizacion`, `version` y `fechaUltimaActualizacion` |
| `ConfiguracionLayout` | tabla `cotizacion_layout_ubicaciones` | nueva | seccion que define la cantidad y orden de captura de ubicaciones |
| `LayoutUbicacionSlot` | JSONB o subestructura de `ConfiguracionLayout` | nueva | detalles de cada ubicacion prevista en el layout |

#### Campos del modelo
| Campo | Tipo | Obligatorio | Validacion | Descripcion |
|-------|------|-------------|------------|-------------|
| `numeroFolio` | string | si | unico, no vacio | Identificador funcional de la cotizacion |
| `version` | long | si | control optimista | Version global del agregado sincronizada con el root |
| `fechaUltimaActualizacion` | datetime | si | auto-actualizado | Marca de la ultima modificacion logica |
| `modoCaptura` | string | si | valores permitidos: `UNICA`, `MULTIPLE` | Define si la cotizacion se organiza para una o varias ubicaciones |
| `cantidadUbicaciones` | integer | si | mayor o igual a 1 | Numero de ubicaciones previstas por el layout |
| `ubicaciones` | array | no | indices unicos, orden estable | Lista de posiciones o slots que estructuran la captura |
| `indice` | integer | si en cada slot | unico dentro de la cotizacion | Posicion logica de la ubicacion |
| `ordenCaptura` | integer | si en cada slot | mayor o igual a 1 | Orden en que la UI presenta o captura la ubicacion |

#### Indices / Constraints
- Constraint unico sobre `cotizacion_id` para garantizar una sola configuracionLayout por cotizacion.
- Foreign key desde `cotizacion_layout_ubicaciones.cotizacion_id` hacia `cotizaciones_danos.id` con borrado en cascada.
- Constraint de integridad para asegurar `cantidadUbicaciones >= 1`.
- Constraint de consistencia para asegurar que `modoCaptura = UNICA` implique `cantidadUbicaciones = 1`.
- Constraint de consistencia para asegurar que `modoCaptura = MULTIPLE` permita `cantidadUbicaciones > 1`.
- Constraint de unicidad por `indice` dentro de la configuracionLayout para no duplicar posiciones.

### API Endpoints

#### GET /v1/quotes/{folio}/locations/layout
- **Descripcion**: consulta la configuracion actual del layout de ubicaciones de una cotizacion.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `folio`: numeroFolio de la cotizacion.
- **Response 200**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "version": 1,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "configuracionLayout": {
        "modoCaptura": "MULTIPLE",
        "cantidadUbicaciones": 3,
        "ubicaciones": [
          { "indice": 1, "ordenCaptura": 1 },
          { "indice": 2, "ordenCaptura": 2 },
          { "indice": 3, "ordenCaptura": 3 }
        ]
      }
    }
  }
  ```
- **Response 200 cuando aun no existe layout**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "version": 3,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "configuracionLayout": {
        "modoCaptura": null,
        "cantidadUbicaciones": null,
        "ubicaciones": []
      }
    }
  }
  ```
- **Response 404**: numeroFolio inexistente.

#### PUT /v1/quotes/{folio}/locations/layout
- **Descripcion**: crea o actualiza la configuracionLayout sin modificar otras secciones del agregado.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `folio`: numeroFolio de la cotizacion.
- **Request Body**:
  ```json
  {
    "version": 1,
    "configuracionLayout": {
      "modoCaptura": "MULTIPLE",
      "cantidadUbicaciones": 3,
      "ubicaciones": [
        { "indice": 1, "ordenCaptura": 1 },
        { "indice": 2, "ordenCaptura": 2 },
        { "indice": 3, "ordenCaptura": 3 }
      ]
    }
  }
  ```
- **Response 200**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "version": 2,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "configuracionLayout": {
        "modoCaptura": "MULTIPLE",
        "cantidadUbicaciones": 3,
        "ubicaciones": [
          { "indice": 1, "ordenCaptura": 1 },
          { "indice": 2, "ordenCaptura": 2 },
          { "indice": 3, "ordenCaptura": 3 }
        ]
      }
    }
  }
  ```
- **Response 400**: payload incompleto o inconsistente.
- **Response 404**: numeroFolio inexistente.
- **Response 409**: version desactualizada o conflicto de concurrencia.
- **Response 422**: cantidadUbicaciones, modoCaptura o indices invalidados por reglas de negocio.
- **Regla adicional del contrato**: si el layout reduce `cantidadUbicaciones`, primero debe resolverse cualquier ubicacion persistida fuera del nuevo rango o la operacion se rechaza con `422`.

### Diseno Frontend

#### Componentes nuevos
| Componente | Archivo | Props principales | Descripcion |
|------------|---------|------------------|-------------|
| `LocationsLayoutForm` | `components/LocationsLayoutForm.tsx` | `value, onChange, onSubmit, loading` | Formulario principal para definir el layout |
| `LocationsLayoutSummaryCard` | `components/LocationsLayoutSummaryCard.tsx` | `layout, version, updatedAt` | Resumen del layout configurado y su trazabilidad |
| `LocationsLayoutPreview` | `components/LocationsLayoutPreview.tsx` | `slots, mode` | Vista previa de las ubicaciones previstas por el layout |
| `ValidationAlertList` | `components/ValidationAlertList.tsx` | `alerts` | Reutilizado para mostrar errores o advertencias de validacion |
| `IdempotencyNotice` | `components/IdempotencyNotice.tsx` | `message, variant` | Reutilizado para estados informativos y errores generales |

#### Páginas nuevas
| Pagina | Archivo | Ruta | Protegida |
|--------|---------|------|-----------|
| `LocationsLayoutPage` | `pages/LocationsLayoutPage.tsx` | `/quotes/:folio/locations/layout` | no |

#### Hooks y State
| Hook | Archivo | Retorna | Descripcion |
|------|---------|---------|-------------|
| `useLocationsLayout` | `hooks/useLocationsLayout.ts` | `{ layout, loading, saving, error, loadLayout, saveLayout, reset }` | Orquesta la consulta y el guardado del layout |
| `useLocationsLayoutPreview` | `hooks/useLocationsLayoutPreview.ts` | `{ slots, mode, canAddSlot }` | Calcula una vista previa de la estructura para la UI |

#### Services (llamadas API)
| Funcion | Archivo | Endpoint |
|---------|---------|----------|
| `getLocationsLayout(folio)` | `services/quoteLayoutService.ts` | `GET /v1/quotes/{folio}/locations/layout` |
| `updateLocationsLayout(folio, payload)` | `services/quoteLayoutService.ts` | `PUT /v1/quotes/{folio}/locations/layout` |

### Arquitectura y Dependencias
- Backend nuevo en `plataforma-danos-back` bajo la capa hexagonal existente: controller -> application -> domain -> infrastructure.
- La seccion de layout debe convivir con el agregado `Cotizacion` y usar control de concurrencia optimista sobre la version del folio.
- Se requiere una migracion Flyway nueva para la tabla `cotizacion_layout_ubicaciones` y su relacion con `cotizaciones_danos`.
- La respuesta de estado de cotizacion debe poder considerar el layout como seccion `COMPLETED` cuando exista configuracion persistida valida con `cantidadUbicaciones >= 1` y slots consistentes.
- La SPA debe agregar una ruta de edicion del layout y, si aplica, un acceso desde la vista de estado del folio para continuar la captura.
- La respuesta del backend debe seguir el envelope `data` y los errores deben mapearse a Problem Details, compatible con el manejo ya existente en la app.

### Notas de Implementacion
> El layout funciona como contrato previo a la captura de ubicaciones. La primera version debe mantener el modelo simple, con una cantidad de ubicaciones y su orden de captura, para dejar preparada la evolucion hacia la gestion detallada de ubicaciones sin rehacer el contrato principal. Si el layout no existe, el progreso de ubicaciones del folio debe considerarse `INCOMPLETE`.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada item (`[x]`) al completarlo.
> El Orchestrator monitorea este checklist para determinar el progreso.

### Backend

#### Implementacion
- [x] Crear request/response DTOs para consulta y actualizacion de layout
- [x] Implementar entidad de dominio para configuracionLayout y sus slots
- [x] Implementar caso de uso de consulta del layout por `numeroFolio`
- [x] Implementar caso de uso de actualizacion con versionado optimista
- [x] Implementar adaptador JPA y migracion Flyway para `cotizacion_layout_ubicaciones`
- [x] Implementar controller `/v1/quotes/{folio}/locations/layout`
- [x] Documentar OpenAPI del contrato

#### Tests Backend
- [x] Caso de uso happy path de consulta de layout
- [x] Caso de uso happy path de actualizacion valida
- [x] Caso de uso con conflicto de version desactualizada
- [x] Caso de uso con `numeroFolio` inexistente
- [x] Controller con respuesta `200` y envelope `data`
- [x] Controller con Problem Details ante error relevante

### Frontend

#### Implementacion
- [x] Crear servicio Axios para consultar y guardar la configuracionLayout
- [x] Crear hook para administrar carga, guardado y estado del layout
- [x] Implementar formulario y componentes de vista previa de ubicaciones
- [x] Implementar pagina de edicion y registrar ruta nueva
- [x] Integrar acceso desde el flujo de cotizacion y estado del folio
- [x] Mantener mensajes de error y estado con envelope `data` y Problem Details

#### Tests Frontend
- [x] Componente principal renderiza el layout existente
- [x] Componente dispara guardado con la version actual
- [x] Hook maneja carga exitosa y layout vacio
- [x] Hook maneja error de folio inexistente o conflicto de version
- [x] Pagina integra consulta, edicion y guardado

### QA
- [ ] Ejecutar skill `/gherkin-case-generator` -> criterios CRITERIO-1.1, 1.2, 1.3, 2.1, 2.2, 2.3
- [ ] Ejecutar skill `/risk-identifier` -> clasificacion ASD de riesgos para persistencia parcial y versionado
- [ ] Revisar cobertura de tests contra criterios de aceptacion
- [ ] Validar que todas las reglas de negocio estan cubiertas
- [ ] Actualizar estado spec: `status: IMPLEMENTED`

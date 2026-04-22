---
id: SPEC-007
status: APPROVED
feature: opciones-de-cobertura
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-002", "SPEC-005", "SPEC-006"]
---

# Spec: Opciones de Cobertura

> **Estado:** `DRAFT` -> aprobar con `status: APPROVED` antes de iniciar implementacion.
> **Ciclo de vida:** DRAFT -> APPROVED -> IN_PROGRESS -> IMPLEMENTED -> DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripcion
Esta funcionalidad permite consultar y actualizar las `opcionesCobertura` globales de una cotizacion para controlar que garantias y terminos participan en el calculo. La seccion debe permanecer separada del resto del agregado, conservar `version` y `fechaUltimaActualizacion`, y exponer tanto la configuracion global editable como una proyeccion derivada por ubicacion util para la UI antes del calculo. La proyeccion debe dejar visible el origen tecnico preliminar que el backend intentaria resolver contra las matrices del fixture (`tariffs`, `fireTariffs`, `catTariffs`, `fhmTariffs`, `electronicEquipmentFactors`) sin reemplazar el calculo final.

### Requerimiento de Negocio
Fuente principal: `.github/requirements/opciones-de-cobertura.md`.

Resumen del requerimiento base:
- Consultar las opciones de cobertura vigentes del folio.
- Guardar o actualizar la configuracion de coberturas del folio.
- Mantener separada esta seccion del resto del agregado.
- Preparar la informacion para el calculo tecnico y comercial.
- Responder con envelope `data` en exito y Problem Details en error.

### Historias de Usuario

#### HU-01: Configurar opciones de cobertura del folio

```
Como:        usuario del cotizador
Quiero:      seleccionar y actualizar opciones de cobertura
Para:        controlar que garantias y terminos participan en el calculo

Prioridad:   Alta
Estimacion:  M
Dependencias: Catalogos y Validaciones Core, Gestion de Ubicaciones
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Guardar opciones de cobertura validas
  Dado que existe una cotizacion identificada por numeroFolio
  Cuando el usuario registra o modifica las opciones de cobertura
  Entonces el sistema persiste la seccion opcionesCobertura
  Y la deja disponible para el calculo posterior
  Y actualiza version y fechaUltimaActualizacion
```

**Happy Path**
```gherkin
CRITERIO-1.2: Consultar opciones de cobertura existentes
  Dado que la cotizacion ya tiene opcionesCobertura guardadas
  Cuando el usuario abre la seccion correspondiente
  Entonces el sistema devuelve la configuracion vigente del folio
  Y la UI la presenta para revision o edicion
```

**Edge Case**
```gherkin
CRITERIO-1.3: Abrir una cotizacion existente sin opcionesCobertura capturadas
  Dado que existe una cotizacion valida pero aun no se ha guardado la seccion de cobertura
  Cuando el usuario consulta las opciones de cobertura por numeroFolio
  Entonces el sistema responde con el folio y una configuracion vacia o inicializable
  Y permite iniciar la captura sin crear un segundo folio
```

#### HU-02: Rechazar opciones incompatibles o inconsistentes

```
Como:        sistema de cotizacion
Quiero:      validar las coberturas solicitadas contra el contexto del folio
Para:        evitar calculos tecnicos con terminos invalidos

Prioridad:   Alta
Estimacion:  S
Dependencias: Catalogos y Validaciones Core
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-02

**Error Path**
```gherkin
CRITERIO-2.1: Rechazar una opcion de cobertura no soportada
  Dado que el usuario intenta guardar una cobertura no disponible en el catalogo aprobado
  Cuando el backend valida la solicitud
  Entonces la operacion es rechazada
  Y el sistema responde con un error funcional entendible en formato Problem Details
```

**Error Path**
```gherkin
CRITERIO-2.2: Rechazar una cotizacion inexistente
  Dado que el numeroFolio no corresponde a una cotizacion existente
  Cuando se intenta consultar o guardar opciones de cobertura
  Entonces el sistema responde con error funcional de no encontrado
```

**Edge Case**
```gherkin
CRITERIO-2.3: Rechazar un cambio con version desactualizada
  Dado que la cotizacion fue actualizada por otro usuario o proceso
  Cuando el cliente intenta guardar una version antigua de opcionesCobertura
  Entonces el sistema rechaza la actualizacion
  Y conserva el ultimo estado persistido
```

### Reglas de Negocio
1. `opcionesCobertura` es una seccion global del agregado y debe quedar versionada de forma independiente del resto de secciones.
2. El identificador canonico de garantia es `garantiaCode` y debe alinearse con el contrato del core.
3. Las opciones guardadas deben ser consistentes con el catalogo aprobado de garantias.
4. Guardar coberturas no debe sobrescribir datos generales, layout, ubicaciones ni resultados financieros.
5. Los cambios sobre coberturas deben reflejarse en el siguiente calculo del folio.
6. La consulta de la seccion debe poder devolver una configuracion vacia con shape fijo y la `version` real del agregado si la cotizacion existe pero aun no tiene coberturas.
7. La respuesta debe incluir `projectionPerLocation` como vista derivada de solo lectura; no se edita por ubicacion en esta capability.
8. Si una garantia del catalogo no existe o esta inactiva, el guardado debe ser rechazado.
9. Una garantia solo puede marcarse como `tariffablePreview = true` cuando existe en catalogo, esta seleccionada y el backend logra resolver al menos una ruta tecnica obligatoria del MVP con la informacion ya persistida del folio: `giroCode|zonaCode|garantiaCode` sobre `tariffs`, `giroCode|tipoConstructivo|nivelTarifario` sobre `fireTariffs` o `zonaTev|garantiaCode` sobre `catTariffs`.
10. Las rutas `fhmTariffs` y `electronicEquipmentFactors` son optativas en esta capability. Si faltan `grupo`, `clase` o equivalentes documentados, la proyeccion debe devolver `tariffablePreview = false`, `fuenteTecnicaPreview = UNRESOLVED` y explicar el motivo en `motivosNoTarifable`; el endpoint no debe inventar defaults tecnicos.
11. Si no existe un catalogo formal para `terminos`, estos se tratan como valores documentados libres asociados a la garantia.
12. Toda actualizacion valida incrementa `version` y actualiza `fechaUltimaActualizacion`.
13. Las respuestas exitosas deben usar envelope `data`; los errores deben publicarse como Problem Details.
14. `projectionPerLocation` es orientativa: no persiste montos ni reemplaza la elegibilidad final definida por `POST /calculate`.

---

## 2. DISENO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripcion |
|---------|---------|---------|-------------|
| `Cotizacion` | tabla `cotizaciones_danos` | modificada | agregado principal; conserva `numeroFolio`, `estadoCotizacion`, `version` y `fechaUltimaActualizacion` |
| `OpcionesCobertura` | tabla `cotizacion_opciones_cobertura` | nueva | seccion persistida con la configuracion de garantias aplicadas al folio |
| `GarantiaSeleccionada` | JSONB o subestructura de `OpcionesCobertura` | nueva | elemento global de cobertura que indica que garantia participa en el calculo |
| `TerminoCobertura` | JSONB o subestructura de `OpcionesCobertura` | nueva | ajustes o terminos asociados a una garantia seleccionada |
| `ProjectionPerLocation` | DTO de aplicacion | nueva | vista derivada por ubicacion usada por la UI antes del calculo |

#### Campos del modelo
| Campo | Tipo | Obligatorio | Validacion | Descripcion |
|-------|------|-------------|------------|-------------|
| `numeroFolio` | string | si | unico, no vacio | Identificador funcional de la cotizacion |
| `version` | long | si | control optimista | Version del agregado raiz |
| `fechaUltimaActualizacion` | datetime | si | auto-actualizado | Marca de la ultima modificacion logica |
| `garantiasSeleccionadas` | array | si | ids canonicos unicos, puede estar vacio | Garantias globales que participan en el calculo |
| `garantiaCode` | string | si por item | debe existir en catalogo aprobado | Identificador canonico de la garantia |
| `terminos` | array | no | valores consistentes con la garantia | Ajustes o terminos adicionales por garantia |
| `observaciones` | string | no | longitud controlada | Nota opcional de la configuracion |
| `projectionPerLocation` | array | si en lectura | derivado, solo lectura | Proyeccion de garantias aplicables por ubicacion |
| `garantiasDerivadas` | array | si por item | derivado desde opciones globales | Garantias visibles por ubicacion antes del calculo |
| `calculablePreview` | boolean | si por item | derivado | Indica si la ubicacion podria ser calculable con la configuracion actual |
| `tariffablePreview` | boolean | si por garantia derivada | derivado | Indica si el backend encontro una ruta tecnica preliminar para la garantia |
| `fuenteTecnicaPreview` | string | si por garantia derivada | enum: `CORE_TARIFF`, `FIRE_TARIFF`, `CAT_TARIFF`, `FHM_TARIFF`, `ELECTRONIC_FACTOR`, `UNRESOLVED` | Fuente del fixture usada para la previsualizacion |
| `lookupKeyPreview` | string | no por garantia derivada | nullable | Clave compuesta resuelta contra el fixture cuando aplica |
| `motivosNoTarifable` | array | si por garantia derivada | serializable | Explica por que la garantia no puede previsualizarse como tarifable |
| `createdAt` | datetime | si | auto-actualizado | Fecha de creacion del registro |
| `updatedAt` | datetime | si | auto-actualizado | Fecha de ultima actualizacion del registro |

#### Indices / Constraints
- Constraint unico sobre `cotizacion_id` para garantizar una sola configuracion de coberturas por cotizacion.
- Foreign key desde `cotizacion_opciones_cobertura.cotizacion_id` hacia `cotizaciones_danos.id` con borrado en cascada.
- Constraint de unicidad por `garantiaCode` dentro de la configuracion para evitar duplicados.
- Constraint de no nulos para `numeroFolio`, `version`, `fechaUltimaActualizacion` y las garantias seleccionadas cuando la seccion existe.
- Constraint semantico para impedir guardar garantias fuera del catalogo aprobado o con estados inconsistentes.

### API Endpoints

#### GET /v1/quotes/{folio}/coverage-options
- **Descripcion**: consulta la configuracion vigente de opciones de cobertura de una cotizacion.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `folio`: numeroFolio de la cotizacion.
- **Response 200**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "version": 2,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "opcionesCobertura": {
        "garantiasSeleccionadas": [
          { "garantiaCode": "GAR-INC-ED", "terminos": [] },
          { "garantiaCode": "GAR-ROBO", "terminos": [] }
        ],
        "observaciones": "Cobertura base para el analisis inicial"
      },
      "projectionPerLocation": [
        {
          "indice": 1,
          "garantiasDerivadas": [
            {
              "garantiaCode": "GAR-INC-ED",
              "tariffablePreview": true,
              "fuenteTecnicaPreview": "CORE_TARIFF",
              "lookupKeyPreview": "GIRO-001|ZTEV-1|GAR-INC-ED",
              "motivosNoTarifable": []
            },
            {
              "garantiaCode": "GAR-ROBO",
              "tariffablePreview": false,
              "fuenteTecnicaPreview": "UNRESOLVED",
              "lookupKeyPreview": null,
              "motivosNoTarifable": ["No existe tarifa vigente para la combinacion actual de la ubicacion."]
            }
          ],
          "calculablePreview": true
        },
        {
          "indice": 2,
          "garantiasDerivadas": [
            {
              "garantiaCode": "GAR-INC-ED",
              "tariffablePreview": false,
              "fuenteTecnicaPreview": "UNRESOLVED",
              "lookupKeyPreview": null,
              "motivosNoTarifable": ["La ubicacion no tiene zona TEV resoluble."]
            },
            {
              "garantiaCode": "GAR-ROBO",
              "tariffablePreview": false,
              "fuenteTecnicaPreview": "UNRESOLVED",
              "lookupKeyPreview": null,
              "motivosNoTarifable": ["La ubicacion no tiene giro o codigo postal valido para previsualizar tarifas."]
            }
          ],
          "calculablePreview": false
        }
      ]
    }
  }
  ```
- **Response 200 cuando aun no existe configuracion**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "version": 3,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "opcionesCobertura": {
        "garantiasSeleccionadas": [],
        "observaciones": null
      },
      "projectionPerLocation": []
    }
  }
  ```
- **Response 404**: numeroFolio inexistente.

#### PUT /v1/quotes/{folio}/coverage-options
- **Descripcion**: crea o actualiza la configuracion de opciones de cobertura sin modificar otras secciones del agregado.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `folio`: numeroFolio de la cotizacion.
- **Request Body**:
  ```json
  {
    "version": 2,
    "opcionesCobertura": {
      "garantiasSeleccionadas": [
        { "garantiaCode": "GAR-INC-ED", "terminos": [] },
        { "garantiaCode": "GAR-ROBO", "terminos": [] }
      ],
      "observaciones": "Cobertura base para el analisis inicial"
    }
  }
  ```
- **Response 200**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "version": 3,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "opcionesCobertura": {
        "garantiasSeleccionadas": [
          { "garantiaCode": "GAR-INC-ED", "terminos": [] },
          { "garantiaCode": "GAR-ROBO", "terminos": [] }
        ],
        "observaciones": "Cobertura base para el analisis inicial"
      },
      "projectionPerLocation": [
        {
          "indice": 1,
          "garantiasDerivadas": [
            {
              "garantiaCode": "GAR-INC-ED",
              "tariffablePreview": true,
              "fuenteTecnicaPreview": "CORE_TARIFF",
              "lookupKeyPreview": "GIRO-001|ZTEV-1|GAR-INC-ED",
              "motivosNoTarifable": []
            },
            {
              "garantiaCode": "GAR-ROBO",
              "tariffablePreview": false,
              "fuenteTecnicaPreview": "UNRESOLVED",
              "lookupKeyPreview": null,
              "motivosNoTarifable": ["No existe tarifa vigente para la combinacion actual de la ubicacion."]
            }
          ],
          "calculablePreview": true
        }
      ]
    }
  }
  ```
- **Response 400**: payload mal formado o con duplicidad de garantias en el request.
- **Response 404**: numeroFolio inexistente.
- **Response 409**: version desactualizada o conflicto de concurrencia.
- **Response 422**: garantia no soportada, inactiva o inconsistente con el catalogo aprobado.

### Diseno Frontend

#### Componentes nuevos
| Componente | Archivo | Props principales | Descripcion |
|------------|---------|------------------|-------------|
| `CoverageOptionsForm` | `components/CoverageOptionsForm.tsx` | `value, guarantees, onChange, onSubmit, loading` | Formulario principal para editar las coberturas |
| `CoverageOptionsSummaryCard` | `components/CoverageOptionsSummaryCard.tsx` | `options, version, updatedAt` | Resumen de la configuracion vigente |
| `GuaranteeSelectorPanel` | `components/GuaranteeSelectorPanel.tsx` | `items, selected, onToggle` | Selector visual de garantias desde el catalogo core |
| `ValidationAlertList` | `components/ValidationAlertList.tsx` | `alerts` | Reutilizado para mensajes de validacion o rechazo |
| `IdempotencyNotice` | `components/IdempotencyNotice.tsx` | `message, variant` | Reutilizado para mensajes informativos o error |

#### Páginas nuevas
| Pagina | Archivo | Ruta | Protegida |
|--------|---------|------|-----------|
| `CoverageOptionsPage` | `pages/CoverageOptionsPage.tsx` | `/quotes/:folio/coverage-options` | no |

#### Hooks y State
| Hook | Archivo | Retorna | Descripcion |
|------|---------|---------|-------------|
| `useCoverageOptions` | `hooks/useCoverageOptions.ts` | `{ options, loading, saving, error, loadOptions, saveOptions, reset }` | Orquesta la consulta y el guardado de coberturas |
| `useCoverageCatalogs` | `hooks/useCoverageCatalogs.ts` | `{ guarantees, loading, error, reload }` | Reutiliza el catalogo de garantias aprobado por el core |

#### Services (llamadas API)
| Funcion | Archivo | Endpoint |
|---------|---------|----------|
| `getCoverageOptions(folio)` | `services/quoteCoverageOptionsService.ts` | `GET /v1/quotes/{folio}/coverage-options` |
| `updateCoverageOptions(folio, payload)` | `services/quoteCoverageOptionsService.ts` | `PUT /v1/quotes/{folio}/coverage-options` |

### Arquitectura y Dependencias
- Backend nuevo en `plataforma-danos-back` bajo la capa hexagonal existente: controller -> application -> domain -> infrastructure.
- La persistencia debe modelar una seccion independiente del agregado `Cotizacion` con versionado optimista.
- La validacion debe apoyarse en el catalogo de garantias del core para impedir coberturas inexistentes o inactivas.
- El resultado de esta seccion debe ser consumido por el futuro calculo tecnico y por el resumen de estado del folio.
- La seccion debe alimentar el read model derivado `garantias[]` de ubicaciones y la vista `projectionPerLocation`, sin convertir esa proyeccion en fuente primaria editable.
- La proyeccion debe dejar trazable que clave preliminar del fixture pudo resolverse y por que una garantia queda sin preview, para mantener coherencia con `SPEC-008`.
- La SPA debe agregar una pagina de edicion y, si aplica, un acceso desde la vista de progreso o calculo.
- La respuesta del backend debe seguir el envelope `data` y los errores deben mapearse a Problem Details, compatible con el manejo ya existente en la app.

### Notas de Implementacion
> La cobertura no debe tratarse como una lista libre. El contrato debe validar garantias contra el catalogo core, preservar la version del agregado y dejar una fotografia clara de lo que participara en el siguiente calculo. La edicion ocurre solo a nivel global; la vista por ubicacion es derivada y de solo lectura antes del calculo. El corte MVP prioriza las rutas que ya pueden resolverse con `giro`, `tipoConstructivo`, `nivel` y `zonaTev`; si una ruta depende de llaves aun no modeladas en el folio actual, la proyeccion debe marcarla como `UNRESOLVED` en lugar de asumir equivalencias.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada item (`[x]`) al completarlo.
> El Orchestrator monitorea este checklist para determinar el progreso.

### Backend

#### Implementacion
- [x] Crear request/response DTOs para consulta y actualizacion de opciones de cobertura
- [x] Implementar entidad de dominio para opcionesCobertura y garantias seleccionadas
- [x] Implementar caso de uso de consulta de opciones de cobertura por `numeroFolio`
- [x] Implementar caso de uso de actualizacion con validacion contra catalogo aprobado
- [x] Implementar adaptador JPA y migracion Flyway para `cotizacion_opciones_cobertura`
- [x] Implementar controller `/v1/quotes/{folio}/coverage-options`
- [x] Documentar OpenAPI del contrato

#### Tests Backend
- [x] Caso de uso happy path de consulta de coberturas
- [x] Caso de uso happy path de actualizacion valida
- [x] Caso de uso con garantia no soportada o inactiva
- [x] Caso de uso con numeroFolio inexistente
- [x] Caso de uso con conflicto de version desactualizada
- [x] Controller con respuesta `200` y envelope `data`
- [x] Controller con Problem Details ante error relevante

### Frontend

#### Implementacion
- [x] Crear servicio Axios para consultar y guardar opciones de cobertura
- [x] Crear hook para administrar carga, guardado y errores
- [x] Implementar formulario y componentes de selector de garantias
- [x] Implementar pagina de edicion y registrar ruta nueva
- [x] Integrar el catalogo de garantias desde la referencia core
- [x] Mantener mensajes de error y estado con envelope `data` y Problem Details

#### Tests Frontend
- [x] Componente principal renderiza coberturas existentes
- [x] Componente dispara guardado con la version actual
- [x] Hook maneja carga exitosa y configuracion vacia
- [x] Hook maneja error de folio inexistente o validacion invalida
- [x] Pagina integra consulta, edicion y resumen

### QA
- [ ] Ejecutar skill `/gherkin-case-generator` -> criterios CRITERIO-1.1, 1.2, 1.3, 2.1, 2.2, 2.3
- [ ] Ejecutar skill `/risk-identifier` -> clasificacion ASD de riesgos para validacion de catalogos y versionado
- [ ] Revisar cobertura de tests contra criterios de aceptacion
- [ ] Validar que todas las reglas de negocio estan cubiertas
- [ ] Actualizar estado spec: `status: IMPLEMENTED`

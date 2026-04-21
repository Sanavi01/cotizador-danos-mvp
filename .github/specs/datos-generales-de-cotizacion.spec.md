---
id: SPEC-003
status: DRAFT
feature: datos-generales-de-cotizacion
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-001", "SPEC-002"]
---

# Spec: Datos Generales de Cotizacion

> **Estado:** `DRAFT` -> aprobar con `status: APPROVED` antes de iniciar implementacion.
> **Ciclo de vida:** DRAFT -> APPROVED -> IN_PROGRESS -> IMPLEMENTED -> DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripcion
Esta funcionalidad permite consultar, capturar y actualizar la seccion general de una cotizacion sin sobrescribir el resto del agregado. La pantalla de datos generales debe permitir retomar una cotizacion existente, validar referencias de negocio contra la referencia core y conservar version y trazabilidad en cada edicion valida.

### Requerimiento de Negocio
Fuente principal: `.github/requirements/datos-generales-de-cotizacion.md`.

Resumen del requerimiento base:
- Consultar la seccion general de una cotizacion por `numeroFolio`.
- Capturar y actualizar datos del asegurado y de conduccion comercial.
- Resolver y validar `codigoAgente`, `clasificacionRiesgo` y `tipoNegocio` contra catalogos aprobados.
- Mantener `version` y `fechaUltimaActualizacion` al editar la seccion.
- Responder con envelope `data` en exito y Problem Details en error.

### Historias de Usuario

#### HU-01: Consultar y guardar los datos generales

```
Como:        usuario del cotizador
Quiero:      capturar y actualizar los datos generales de una cotizacion
Para:        dejar listo el folio para continuar con las siguientes secciones

Prioridad:   Alta
Estimacion:  M
Dependencias: Folios e Idempotencia, Catalogos y Validaciones Core
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Guardar datos generales de forma exitosa
  Dado que existe una cotizacion identificada por numeroFolio
  Cuando el usuario registra o actualiza los datos generales validos
  Entonces el sistema persiste la seccion general sin sobrescribir el resto del agregado
  Y mantiene intactas las secciones de layout, ubicaciones, coberturas y resultados financieros
  Y actualiza version y fechaUltimaActualizacion
```

**Happy Path**
```gherkin
CRITERIO-1.2: Consultar datos generales existentes
  Dado que la cotizacion ya tiene informacion general guardada
  Cuando el usuario abre la pantalla de datos generales
  Entonces el sistema devuelve la informacion previamente persistida
  Y la UI la presenta para revision o edicion
```

**Edge Case**
```gherkin
CRITERIO-1.3: Abrir una cotizacion existente sin seccion general capturada
  Dado que existe una cotizacion valida pero aun no se ha guardado la seccion general
  Cuando el usuario consulta los datos generales por numeroFolio
  Entonces el sistema responde con el folio y la estructura vacia de captura
  Y permite iniciar la edicion sin crear un segundo folio
```

#### HU-02: Rechazar datos generales inconsistentes

```
Como:        sistema de cotizacion
Quiero:      validar los datos generales obligatorios y sus referencias
Para:        evitar que el folio avance con informacion inconsistente

Prioridad:   Alta
Estimacion:  M
Dependencias: Catalogos y Validaciones Core
Capa:        Backend / Frontend / Ambas
```

#### Criterios de Aceptacion - HU-02

**Error Path**
```gherkin
CRITERIO-2.1: Rechazar un codigoAgente invalido
  Dado que el usuario envia un codigoAgente inexistente o inconsistente
  Cuando el backend intenta guardar la seccion
  Entonces la operacion es rechazada
  Y el sistema responde con Problem Details
```

**Error Path**
```gherkin
CRITERIO-2.2: Rechazar una cotizacion inexistente
  Dado que el numeroFolio no corresponde a una cotizacion existente
  Cuando se intenta consultar o guardar datos generales
  Entonces el sistema responde con error funcional de no encontrado
  Y la UI no permite continuar la captura sobre un folio inexistente
```

**Edge Case**
```gherkin
CRITERIO-2.3: Rechazar cambios con version desactualizada
  Dado que dos usuarios editaron la misma cotizacion y uno guardo primero
  Cuando el segundo intenta persistir la seccion con una version antigua
  Entonces el sistema rechaza la operacion con conflicto de concurrencia
  Y conserva la ultima version persistida en la base de datos
```

### Reglas de Negocio
1. `cotizacion` es el agregado principal y no debe ser sobrescrito por completo al guardar una sola seccion.
2. Toda actualizacion valida incrementa `version` y actualiza `fechaUltimaActualizacion`.
3. `codigoAgente`, `clasificacionRiesgo` y `tipoNegocio` deben validarse contra los catalogos aprobados por la referencia core.
4. `tipoNegocio` es un dato global del folio y no conduce el calculo tecnico; el driver tecnico para calculo vive en `ubicacion.giro.claveIncendio`.
5. `datosAsegurado` y `datosConduccion` deben persistirse de forma atomica junto con la cotizacion.
6. Si el `numeroFolio` no existe, la consulta o el guardado deben responder con `404` en formato Problem Details.
7. Si una referencia catalogo no existe o esta inactiva, el guardado debe responder con Problem Details y no debe modificar la cotizacion.
8. Las respuestas exitosas deben usar envelope `data`; los errores deben publicarse como Problem Details.
9. La consulta inicial de la seccion general debe poder devolver una estructura vacia con shape fijo y la `version` real del agregado para una cotizacion existente sin datos capturados.
10. Si no existe un catalogo formal para `tipoDocumento`, el campo se trata como valor documentado libre y no como referencia obligatoria del core.
11. El cambio de esta seccion no debe recalcular primas ni modificar otras secciones del agregado.

---

## 2. DISENO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripcion |
|---------|---------|---------|-------------|
| `Cotizacion` | tabla `cotizaciones_danos` | modificada | agregado principal; conserva `numeroFolio`, `estadoCotizacion`, `version` y `fechaUltimaActualizacion` |
| `DatosGeneralesCotizacion` | tabla `cotizacion_datos_generales` | nueva | seccion persistida con datos del asegurado y conduccion comercial |
| `DatosAsegurado` | objeto embebido / subestructura de `DatosGeneralesCotizacion` | nueva | informacion de captura del asegurado |
| `DatosConduccion` | objeto embebido / subestructura de `DatosGeneralesCotizacion` | nueva | informacion comercial con `codigoAgente`, `clasificacionRiesgo` y `tipoNegocio` |

#### Campos del modelo
| Campo | Tipo | Obligatorio | Validacion | Descripcion |
|-------|------|-------------|------------|-------------|
| `numeroFolio` | string | si | unico, no vacio | Identificador funcional de la cotizacion |
| `version` | long | si | control optimista | Version del agregado raiz |
| `fechaUltimaActualizacion` | datetime | si | auto-actualizado | Marca de la ultima modificacion logica |
| `datosAsegurado` | objeto | si | campos requeridos por la forma de captura | Bloque de identificacion y contacto del asegurado |
| `tipoDocumento` | string | si | valor documentado, no vacio | Tipo de documento del asegurado |
| `numeroDocumento` | string | si | no vacio, formato valido | Documento del asegurado |
| `nombreORazonSocial` | string | si | maximo definido por negocio | Nombre completo o razon social del asegurado |
| `correoElectronico` | string | no | formato email | Correo de contacto del asegurado |
| `telefono` | string | no | longitud y formato telefonico | Telefono de contacto del asegurado |
| `datosConduccion` | objeto | si | campos requeridos por la forma de captura | Bloque comercial asociado a la cotizacion |
| `codigoAgente` | string | si | debe existir en catalogo de agentes | Agente comercial asignado |
| `clasificacionRiesgo` | string | si | debe existir en catalogo de riesgo | Clasificacion tecnica del riesgo |
| `tipoNegocio` | string | si | debe existir en catalogo de giros / business lines | Tipo de negocio o giro de la cotizacion |
| `createdAt` | datetime | si | auto-actualizado | Fecha de creacion de la seccion |
| `updatedAt` | datetime | si | auto-actualizado | Fecha de ultima actualizacion de la seccion |

#### Indices / Constraints
- Constraint unico sobre `cotizacion_id` en `cotizacion_datos_generales` para garantizar una sola seccion general por cotizacion.
- Foreign key desde `cotizacion_datos_generales.cotizacion_id` hacia `cotizaciones_danos.id` con borrado en cascada.
- Indice unico sobre `numero_folio` en `cotizaciones_danos` se reutiliza para la busqueda principal.
- Indices de apoyo en `codigo_agente`, `clasificacion_riesgo` y `tipo_negocio` solo si se requiere auditoria o filtros operativos posteriores.
- Constraint de no nulos para los campos funcionalmente obligatorios del bloque comercial.

### API Endpoints

#### GET /v1/quotes/{folio}/general-info
- **Descripcion**: consulta la seccion general de una cotizacion por `numeroFolio`.
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
      "datosAsegurado": {
        "tipoDocumento": "NIT",
        "numeroDocumento": "900123456",
        "nombreORazonSocial": "ACME SAS",
        "correoElectronico": "contacto@acme.com",
        "telefono": "6015550101"
      },
      "datosConduccion": {
        "codigoAgente": "AG-102",
        "clasificacionRiesgo": "RISK-A",
        "tipoNegocio": "GIRO-001"
      }
    }
  }
  ```
- **Response 200 cuando la seccion aun no existe**:
  ```json
  {
    "data": {
      "numeroFolio": "1000001",
      "version": 3,
      "fechaUltimaActualizacion": "2026-04-21T00:00:00Z",
      "datosAsegurado": {
        "tipoDocumento": null,
        "numeroDocumento": null,
        "nombreORazonSocial": null,
        "correoElectronico": null,
        "telefono": null
      },
      "datosConduccion": {
        "codigoAgente": null,
        "clasificacionRiesgo": null,
        "tipoNegocio": null
      }
    }
  }
  ```
- **Response 404**: numeroFolio inexistente.

#### PUT /v1/quotes/{folio}/general-info
- **Descripcion**: crea o actualiza la seccion general sin alterar otras secciones del agregado.
- **Auth requerida**: no / sesion demo si aplica.
- **Path Parameters**:
  - `folio`: numeroFolio de la cotizacion.
- **Request Body**:
  ```json
  {
    "version": 1,
    "datosAsegurado": {
      "tipoDocumento": "NIT",
      "numeroDocumento": "900123456",
      "nombreORazonSocial": "ACME SAS",
      "correoElectronico": "contacto@acme.com",
      "telefono": "6015550101"
    },
    "datosConduccion": {
      "codigoAgente": "AG-102",
      "clasificacionRiesgo": "RISK-A",
      "tipoNegocio": "GIRO-001"
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
      "datosAsegurado": {
        "tipoDocumento": "NIT",
        "numeroDocumento": "900123456",
        "nombreORazonSocial": "ACME SAS",
        "correoElectronico": "contacto@acme.com",
        "telefono": "6015550101"
      },
      "datosConduccion": {
        "codigoAgente": "AG-102",
        "clasificacionRiesgo": "RISK-A",
        "tipoNegocio": "GIRO-001"
      }
    }
  }
  ```
- **Response 400**: payload incompleto o con campos de formato invalido.
- **Response 404**: numeroFolio inexistente.
- **Response 409**: version desactualizada o conflicto de concurrencia.
- **Response 422**: codigoAgente, clasificacionRiesgo o tipoNegocio no validos contra la referencia core.

### Diseno Frontend

#### Componentes nuevos
| Componente | Archivo | Props principales | Descripcion |
|------------|---------|------------------|-------------|
| `GeneralInfoForm` | `components/GeneralInfoForm.tsx` | `value, catalogs, onChange, onSubmit, loading` | Formulario principal para editar datos generales |
| `GeneralInfoSummaryCard` | `components/GeneralInfoSummaryCard.tsx` | `quote, version, updatedAt` | Resumen de folio y trazabilidad de la seccion |
| `GeneralInfoCatalogPanel` | `components/GeneralInfoCatalogPanel.tsx` | `agents, riskClassifications, businessLines` | Selector visual o panel de apoyo para catalogos del bloque comercial |
| `ValidationAlertList` | `components/ValidationAlertList.tsx` | `alerts` | Reutilizado para mostrar errores o advertencias de validacion |
| `IdempotencyNotice` | `components/IdempotencyNotice.tsx` | `message, variant` | Reutilizado para estados informativos y errores generales |

#### Páginas nuevas
| Pagina | Archivo | Ruta | Protegida |
|--------|---------|------|-----------|
| `GeneralInfoPage` | `pages/GeneralInfoPage.tsx` | `/quotes/:folio/general-info` | no |

#### Hooks y State
| Hook | Archivo | Retorna | Descripcion |
|------|---------|---------|-------------|
| `useGeneralInfo` | `hooks/useGeneralInfo.ts` | `{ generalInfo, loading, saving, error, loadGeneralInfo, saveGeneralInfo, reset }` | Orquesta la consulta y el guardado de la seccion |
| `useGeneralInfoCatalogs` | `hooks/useGeneralInfoCatalogs.ts` | `{ agents, riskClassifications, businessLines, loading, error, reload }` | Reutiliza la referencia core para poblar la captura |

#### Services (llamadas API)
| Funcion | Archivo | Endpoint |
|---------|---------|----------|
| `getGeneralInfo(folio)` | `services/quoteGeneralInfoService.ts` | `GET /v1/quotes/{folio}/general-info` |
| `updateGeneralInfo(folio, payload)` | `services/quoteGeneralInfoService.ts` | `PUT /v1/quotes/{folio}/general-info` |

### Arquitectura y Dependencias
- Backend nuevo en `plataforma-danos-back` bajo la capa hexagonal existente: controller -> application -> domain -> infrastructure.
- Se requiere un cliente o adaptador hacia la referencia core para validar agentes, clasificacion de riesgo y tipo de negocio.
- Se requiere una migracion Flyway nueva para la tabla de seccion general y su llave foranea con `cotizaciones_danos`.
- El endpoint de estado de cotizacion debe poder reflejar `datos generales` como seccion completada cuando exista la informacion persistida.
- La SPA debe agregar una ruta de edicion y, si aplica, un acceso desde la vista de estado del folio para retomar la captura.
- La respuesta del backend debe seguir el envelope `data` y los errores deben mapearse a Problem Details, compatible con el manejo ya existente en la app.
- La seccion cuenta como `COMPLETED` para el resumen de estado cuando `datosAsegurado` y `datosConduccion` tienen sus campos obligatorios presentes y validos.

### Notas de Implementacion
> La actualizacion de la seccion debe ser atomica: primero validar el folio y las referencias de negocio, luego persistir la seccion y finalmente actualizar la version y la marca temporal del agregado raiz. La UI debe conservar los valores en memoria al cambiar entre consulta y edicion para evitar perder contexto durante un error de validacion.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada item (`[x]`) al completarlo.
> El Orchestrator monitorea este checklist para determinar el progreso.

### Backend

#### Implementacion
- [ ] Crear request/response DTOs para consulta y actualizacion de datos generales
- [ ] Implementar entidad de dominio para la seccion general y sus value objects
- [ ] Implementar caso de uso de consulta de datos generales por `numeroFolio`
- [ ] Implementar caso de uso de actualizacion con validacion contra referencia core
- [ ] Implementar adaptador JPA y migracion Flyway para `cotizacion_datos_generales`
- [ ] Implementar controller `/v1/quotes/{folio}/general-info`
- [ ] Documentar OpenAPI del contrato

#### Tests Backend
- [ ] Caso de uso happy path de consulta de seccion general
- [ ] Caso de uso happy path de actualizacion valida
- [ ] Caso de uso con `codigoAgente` invalido o catalogo inconsistente
- [ ] Caso de uso con `numeroFolio` inexistente
- [ ] Caso de uso con conflicto de version desactualizada
- [ ] Controller con respuesta `200` y envelope `data`
- [ ] Controller con Problem Details ante error relevante

### Frontend

#### Implementacion
- [ ] Crear servicio Axios para consultar y guardar la seccion general
- [ ] Crear hook para administrar carga, guardado y errores
- [ ] Implementar formulario y componentes de soporte para asegurado y conduccion comercial
- [ ] Implementar pagina de edicion y registrar ruta nueva
- [ ] Reutilizar catalogos core para poblar los campos comerciales
- [ ] Mantener mensajes de error y estado con envelope `data` y Problem Details

#### Tests Frontend
- [ ] Componente principal renderiza datos existentes
- [ ] Componente dispara guardado con la version actual
- [ ] Hook maneja carga exitosa y seccion vacia
- [ ] Hook maneja error de folio inexistente o validacion invalida
- [ ] Pagina integra consulta, edicion y guardado

### QA
- [ ] Ejecutar skill `/gherkin-case-generator` -> criterios CRITERIO-1.1, 1.2, 1.3, 2.1, 2.2, 2.3
- [ ] Ejecutar skill `/risk-identifier` -> clasificacion ASD de riesgos para persistencia parcial y validacion de catlogos
- [ ] Revisar cobertura de tests contra criterios de aceptacion
- [ ] Validar que todas las reglas de negocio estan cubiertas
- [ ] Actualizar estado spec: `status: IMPLEMENTED`

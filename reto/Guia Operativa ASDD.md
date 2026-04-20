# Guia Operativa ASDD para el Cotizador de Danos

Este documento sirve como referencia rapida para avanzar feature por feature usando la metodologia ASDD del repositorio.

## Objetivo

Usar un flujo estable para:

- partir de un requerimiento formal
- generar una spec tecnica correcta
- implementar backend y frontend con base en la spec
- generar pruebas y artefactos QA
- cerrar la spec sin dejar trabajo a medias

## Regla Principal

No se implementa codigo sin una spec en estado `APPROVED`.

Flujo oficial:

```text
requirements/<feature>.md
  -> /generate-spec
  -> specs/<feature>.spec.md
  -> APPROVED
  -> implementacion
  -> tests
  -> QA
  -> spec en IMPLEMENTED
```

## Orden recomendado de trabajo

Por logica de dependencias y buenas practicas, este es el orden sugerido:

1. `folios-e-idempotencia`
2. `catalogos-y-validaciones-core`
3. `datos-generales-de-cotizacion`
4. `configuracion-de-layout-de-ubicaciones`
5. `gestion-de-ubicaciones`
6. `opciones-de-cobertura`
7. `estado-y-progreso-de-cotizacion`
8. `calculo-de-prima-y-resultado-financiero`

## Estrategia de ramas

No crear una rama por requerimiento desde el backlog.

Usar ramas cuando la unidad de trabajo ya sea estable:

- para generar o ajustar una spec: `spec/<feature>`
- para implementar un feature aprobado: `feature/<feature>`
- para correcciones puntuales: `fix/<feature>-<ajuste>`
- para cambios metodologicos o estructurales: `chore/<tema>`

Ejemplos:

- `spec/folios-e-idempotencia`
- `feature/folios-e-idempotencia`
- `feature/datos-generales-de-cotizacion`
- `fix/quote-state-conflict`

## Seleccion de modelo

La plantilla ASDD del repositorio no debe forzar un modelo especifico al ejecutar prompts o agentes.

Regla recomendada:

- el usuario decide el modelo activo
- los prompts definen flujo y responsabilidades, no el modelo
- si un agente declara `model:` en su frontmatter, debe removerse salvo que exista una justificacion tecnica explicita y acordada

## Flujo recomendado por feature

### Fase 1. Seleccionar el requerimiento

Tomar un archivo de `.github/requirements/` ya listo para spec.

Ejemplo inicial recomendado:

- `.github/requirements/folios-e-idempotencia.md`

### Fase 2. Generar la spec

Skill o comando principal:

```text
/generate-spec <feature>
```

Ejemplo:

```text
/generate-spec folios-e-idempotencia
```

Prompt recomendado si quieres pedir mayor precision:

```text
Genera la spec para folios-e-idempotencia usando .github/requirements/folios-e-idempotencia.md.
Quiero contrato completo para POST /v1/folios y GET /v1/quotes/{folio}/state,
envelope data, Problem Details, header Idempotency-Key, reglas de negocio,
modelos afectados y checklist backend, frontend y QA.
```

### Fase 3. Revisar la spec en DRAFT

Antes de aprobar, validar como minimo:

- historias de usuario completas
- criterios Gherkin utiles y verificables
- endpoints y contratos tecnicos claros
- errores esperados claros
- cambios de modelo de datos claros
- tareas backend, frontend y QA accionables

### Fase 4. Aprobar la spec

Cambiar la spec a `APPROVED` solo cuando ya no haya dudas de alcance ni contrato.

Estado esperado:

```text
DRAFT -> APPROVED
```

### Fase 5. Implementar backend y frontend

Opcion recomendada al inicio: flujo manual y controlado.

Comandos:

```text
/implement-backend <feature>
/implement-frontend <feature>
```

Ejemplo:

```text
/implement-backend folios-e-idempotencia
/implement-frontend folios-e-idempotencia
```

Usar tambien `Database Agent` si la spec incluye cambios de persistencia o migraciones.

### Fase 6. Generar pruebas

Skill principal:

```text
/unit-testing <feature>
```

Ejemplos:

```text
/unit-testing folios-e-idempotencia
/unit-testing folios-e-idempotencia backend
/unit-testing folios-e-idempotencia frontend
```

Cobertura esperada:

- happy path
- errores de negocio
- validaciones de entrada
- conflictos de version o idempotencia cuando apliquen

### Fase 7. Ejecutar QA

Skills recomendadas:

```text
/gherkin-case-generator <feature>
/risk-identifier <feature>
```

Solo si aplica:

```text
/performance-analyzer <feature>
/automation-flow-proposer <feature>
```

Salida esperada:

- Gherkin util para regresion
- matriz de riesgos
- plan de performance si la spec tiene SLA
- propuesta de automatizacion si se quiere priorizar E2E o API

### Fase 8. Cerrar correctamente la spec

La spec solo debe pasar a `IMPLEMENTED` cuando:

- el codigo ya existe
- los tests existen y cubren lo relevante
- QA ya fue generado
- el comportamiento real coincide con el contrato aprobado

Transicion ideal:

```text
APPROVED -> IN_PROGRESS -> IMPLEMENTED
```

## Cuando usar /asdd-orchestrate

Usar esta skill cuando la spec ya esta madura y el equipo ya tiene patron estable para ejecutar las fases sin tanta supervision manual.

Comando:

```text
/asdd-orchestrate <feature>
```

Ejemplo:

```text
/asdd-orchestrate folios-e-idempotencia
```

Recomendacion practica:

- primeros features: flujo manual
- despues de estabilizar el patron: flujo orquestado

## Contratos: que va en requirement y que va en spec

En `.github/requirements/`:

- ruta esperada
- intencion del endpoint
- headers obligatorios por negocio
- notas funcionales de request/response si hacen falta para quitar ambiguedad

En `.github/specs/`:

- request body exacto
- response body exacto
- headers tecnicos
- codigos HTTP
- Problem Details
- validaciones
- ejemplos JSON

Regla rapida:

- si aun es necesidad de negocio, va en requirement
- si ya define el intercambio tecnico exacto, va en spec

## Checklist corto antes de avanzar al siguiente feature

- el requerimiento existe y esta claro
- la spec fue generada
- la spec fue aprobada
- backend implementado segun la spec
- frontend implementado segun la spec
- tests generados
- QA generado
- spec cerrada en IMPLEMENTED
- rama integrada correctamente

## Secuencia minima sugerida para el primer feature

```text
1. Crear rama: spec/folios-e-idempotencia
2. Ejecutar: /generate-spec folios-e-idempotencia
3. Revisar la spec
4. Aprobar la spec
5. Crear rama: feature/folios-e-idempotencia
6. Ejecutar: /implement-backend folios-e-idempotencia
7. Ejecutar: /implement-frontend folios-e-idempotencia
8. Ejecutar: /unit-testing folios-e-idempotencia
9. Ejecutar: /gherkin-case-generator folios-e-idempotencia
10. Ejecutar: /risk-identifier folios-e-idempotencia
11. Validar entregables
12. Marcar la spec como IMPLEMENTED
```

## Nota final

Si hay duda entre acelerar o mantener orden, priorizar orden.

En este reto, una spec estable vale mas que implementar rapido sobre una definicion incompleta, porque backend, frontend, tests y QA dependen directamente de ella.
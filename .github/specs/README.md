# Specs — Fuente de Verdad del Proyecto ASDD

Este directorio contiene las especificaciones técnicas de cada funcionalidad. Son la fuente de verdad para todos los agentes de desarrollo.

## Ciclo de Vida

```
DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED
```

| Estado | Quién | Condición |
|--------|-------|-----------|
| `DRAFT` | spec-generator | Spec generada, pendiente de revisión humana |
| `APPROVED` | Usuario / Tech Lead | Revisada y aprobada — verde para implementar |
| `IN_PROGRESS` | orchestrator | Implementación en curso |
| `IMPLEMENTED` | orchestrator | Código + tests + QA completos |
| `DEPRECATED` | Usuario | Descartada o reemplazada por otra spec |

> **Regla:** Sin `status: APPROVED` en el frontmatter → ningún agente implementa código.

## Convención de Nombres

```
.github/specs/<nombre-feature-en-kebab-case>.spec.md
```

## Índice de Specs

| ID | Feature | Archivo | Estado | Fecha |
|----|---------|---------|--------|-------|
| — | *(sin specs aún)* | — | — | — |

> Actualizar esta tabla cada vez que se crea o cambia el estado de una spec.

## Requerimientos pendientes de spec

Los siguientes requerimientos están en `.github/requirements/` listos para convertirse en spec:

| Requerimiento | Archivo | Acción |
|---------------|---------|--------|
| Folios e Idempotencia | `.github/requirements/folios-e-idempotencia.md` | `/generate-spec folios-e-idempotencia` |
| Catalogos y Validaciones Core | `.github/requirements/catalogos-y-validaciones-core.md` | `/generate-spec catalogos-y-validaciones-core` |
| Datos Generales de Cotizacion | `.github/requirements/datos-generales-de-cotizacion.md` | `/generate-spec datos-generales-de-cotizacion` |
| Configuracion de Layout de Ubicaciones | `.github/requirements/configuracion-de-layout-de-ubicaciones.md` | `/generate-spec configuracion-de-layout-de-ubicaciones` |
| Gestion de Ubicaciones | `.github/requirements/gestion-de-ubicaciones.md` | `/generate-spec gestion-de-ubicaciones` |
| Estado y Progreso de Cotizacion | `.github/requirements/estado-y-progreso-de-cotizacion.md` | `/generate-spec estado-y-progreso-de-cotizacion` |
| Opciones de Cobertura | `.github/requirements/opciones-de-cobertura.md` | `/generate-spec opciones-de-cobertura` |
| Calculo de Prima y Resultado Financiero | `.github/requirements/calculo-de-prima-y-resultado-financiero.md` | `/generate-spec calculo-de-prima-y-resultado-financiero` |

## Cómo crear una spec nueva

**Opción 1 — Desde un requerimiento existente:**
```
/generate-spec folios-e-idempotencia
```

**Opción 2 — Desde cero:**
```
/generate-spec
> Descripción del feature: ...
```

**Opción 3 — Orquestación completa (spec → implementación → tests → QA):**
```
/asdd-orchestrate
> Feature: nombre del feature
```

## Frontmatter requerido en toda spec

```yaml
---
id: SPEC-001
status: DRAFT
feature: nombre-del-feature
created: YYYY-MM-DD
updated: YYYY-MM-DD
author: spec-generator
version: "1.0"
related-specs: []
---
```

## Template

Ver `.github/skills/generate-spec/spec-template.md`

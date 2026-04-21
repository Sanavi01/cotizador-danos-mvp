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
| SPEC-001 | Folios e Idempotencia | [folios-e-idempotencia.spec.md](folios-e-idempotencia.spec.md) | APPROVED | 2026-04-20 |
| SPEC-002 | Catalogos y Validaciones Core | [catalogos-y-validaciones-core.spec.md](catalogos-y-validaciones-core.spec.md) | APPROVED | 2026-04-20 |
| SPEC-003 | Datos Generales de Cotizacion | [datos-generales-de-cotizacion.spec.md](datos-generales-de-cotizacion.spec.md) | APPROVED | 2026-04-21 |
| SPEC-004 | Configuracion de Layout de Ubicaciones | [configuracion-de-layout-de-ubicaciones.spec.md](configuracion-de-layout-de-ubicaciones.spec.md) | APPROVED | 2026-04-21 |
| SPEC-005 | Gestion de Ubicaciones | [gestion-de-ubicaciones.spec.md](gestion-de-ubicaciones.spec.md) | APPROVED | 2026-04-21 |
| SPEC-006 | Estado y Progreso de Cotizacion | [estado-y-progreso-de-cotizacion.spec.md](estado-y-progreso-de-cotizacion.spec.md) | DRAFT | 2026-04-21 |
| SPEC-007 | Opciones de Cobertura | [opciones-de-cobertura.spec.md](opciones-de-cobertura.spec.md) | DRAFT | 2026-04-21 |
| SPEC-008 | Calculo de Prima y Resultado Financiero | [calculo-de-prima-y-resultado-financiero.spec.md](calculo-de-prima-y-resultado-financiero.spec.md) | DRAFT | 2026-04-21 |

> Actualizar esta tabla cada vez que se crea o cambia el estado de una spec.

## Specs en revisión

Las siguientes specs existen pero todavía no tienen implementación completa:

| Feature | Archivo | Estado | Siguiente paso |
|---------|---------|--------|----------------|
| Estado y Progreso de Cotizacion | [estado-y-progreso-de-cotizacion.spec.md](estado-y-progreso-de-cotizacion.spec.md) | DRAFT | Aprobar para implementación |
| Opciones de Cobertura | [opciones-de-cobertura.spec.md](opciones-de-cobertura.spec.md) | DRAFT | Aprobar para implementación |
| Calculo de Prima y Resultado Financiero | [calculo-de-prima-y-resultado-financiero.spec.md](calculo-de-prima-y-resultado-financiero.spec.md) | DRAFT | Aprobar para implementación |

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

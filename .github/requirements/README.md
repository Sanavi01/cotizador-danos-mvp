# Requirements — Requerimientos de Negocio

Este directorio conserva los requerimientos base del dominio. Cuando ya existe una spec, la tabla enlaza la spec correspondiente para mantener trazabilidad entre negocio y contrato técnico.

## ¿Qué es un Requerimiento?

Un requerimiento es un documento que describe **qué necesita el negocio**, antes de que el `Spec Generator` lo convierta en una spec técnica ASDD. Es la entrada al pipeline ASDD.

## Contratos en esta fase

En `requirements/` sí conviene dejar **contratos preliminares**, pero no el contrato técnico final.

- En el requerimiento: ruta esperada, intención del endpoint, headers obligatorios si ya son una regla de negocio y notas funcionales sobre payload/response cuando sean necesarias para evitar ambigüedad.
- En la spec: contrato canónico completo con request body, response body, códigos HTTP, Problem Details, headers, validaciones, campos obligatorios y ejemplos JSON.

Regla práctica:

- Si el contrato aún expresa una necesidad de negocio, va en el requerimiento.
- Si ya define el shape exacto del intercambio técnico, va en la spec.

## Lifecycle

```
requirements/<feature>.md  →  /generate-spec  →  specs/<feature>.spec.md
  (requerimiento de negocio)     (Spec Generator)    (especificación técnica)
```

## Cómo Usar

1. Crear un archivo `<feature>.md` en este directorio con la descripción del requerimiento
2. Incluir, cuando aplique, contratos preliminares suficientes para evitar ambigüedad funcional
3. Ejecutar `/generate-spec` o usar `@Spec Generator` en Copilot Chat
4. Una vez generada la spec en `.github/specs/`, el requerimiento puede archivarse o eliminarse

## Convención de Nombres

```
.github/requirements/<nombre-feature-kebab-case>.md
```

## Requerimientos con spec

| Feature | Requerimiento | Spec | Estado |
|---------|---------------|------|--------|
| Folios e Idempotencia | `folios-e-idempotencia.md` | [folios-e-idempotencia.spec.md](../specs/folios-e-idempotencia.spec.md) | APPROVED |
| Catalogos y Validaciones Core | `catalogos-y-validaciones-core.md` | [catalogos-y-validaciones-core.spec.md](../specs/catalogos-y-validaciones-core.spec.md) | APPROVED |
| Datos Generales de Cotizacion | `datos-generales-de-cotizacion.md` | [datos-generales-de-cotizacion.spec.md](../specs/datos-generales-de-cotizacion.spec.md) | APPROVED |
| Configuracion de Layout de Ubicaciones | `configuracion-de-layout-de-ubicaciones.md` | [configuracion-de-layout-de-ubicaciones.spec.md](../specs/configuracion-de-layout-de-ubicaciones.spec.md) | APPROVED |
| Gestion de Ubicaciones | `gestion-de-ubicaciones.md` | [gestion-de-ubicaciones.spec.md](../specs/gestion-de-ubicaciones.spec.md) | APPROVED |
| Estado y Progreso de Cotizacion | `estado-y-progreso-de-cotizacion.md` | [estado-y-progreso-de-cotizacion.spec.md](../specs/estado-y-progreso-de-cotizacion.spec.md) | DRAFT |
| Opciones de Cobertura | `opciones-de-cobertura.md` | [opciones-de-cobertura.spec.md](../specs/opciones-de-cobertura.spec.md) | DRAFT |
| Calculo de Prima y Resultado Financiero | `calculo-de-prima-y-resultado-financiero.md` | [calculo-de-prima-y-resultado-financiero.spec.md](../specs/calculo-de-prima-y-resultado-financiero.spec.md) | DRAFT |

> Actualiza esta tabla cuando cambie el estado de una spec o se agregue un requerimiento nuevo.

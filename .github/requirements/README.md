# Requirements — Requerimientos de Negocio

Este directorio contiene los requerimientos de negocio que están **listos para ser especificados** pero aún no tienen una spec generada.

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

## Requerimientos Pendientes

| Feature | Archivo | Estado |
|---------|---------|--------|
| Folios e Idempotencia | `folios-e-idempotencia.md` | LISTO PARA SPEC |
| Catalogos y Validaciones Core | `catalogos-y-validaciones-core.md` | LISTO PARA SPEC |
| Datos Generales de Cotizacion | `datos-generales-de-cotizacion.md` | LISTO PARA SPEC |
| Configuracion de Layout de Ubicaciones | `configuracion-de-layout-de-ubicaciones.md` | LISTO PARA SPEC |
| Gestion de Ubicaciones | `gestion-de-ubicaciones.md` | LISTO PARA SPEC |
| Estado y Progreso de Cotizacion | `estado-y-progreso-de-cotizacion.md` | LISTO PARA SPEC |
| Opciones de Cobertura | `opciones-de-cobertura.md` | LISTO PARA SPEC |
| Calculo de Prima y Resultado Financiero | `calculo-de-prima-y-resultado-financiero.md` | LISTO PARA SPEC |

> Actualiza esta tabla al agregar o procesar requerimientos.

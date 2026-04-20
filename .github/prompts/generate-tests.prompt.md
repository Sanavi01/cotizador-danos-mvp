---
name: generate-tests
description: Genera pruebas unitarias para backend (JUnit/Mockito) y/o frontend (Vitest) en paralelo, basadas en la spec ASDD y el código implementado.
argument-hint: "<nombre-feature> [--backend] [--frontend] (por defecto genera ambos en paralelo)"
agent: Orchestrator
tools:
  - edit/createFile
  - edit/editFiles
  - read/readFile
  - search/listDirectory
  - search
  - execute/runInTerminal
---

Genera pruebas unitarias completas para el feature especificado.

**Feature**: ${input:featureName:nombre del feature en kebab-case}
**Scope**: ${input:scope:backend, frontend, o ambos en paralelo (default)}

## Pasos obligatorios:

1. **Lee la spec** en `.github/specs/${input:featureName:nombre-feature}.spec.md` — sección "Plan de Pruebas Unitarias".
2. **Si scope es "ambos"**: lanza en paralelo `Test Engineer Backend` + `Test Engineer Frontend`.
3. **Si scope es "backend"**: delega a `Test Engineer Backend`:
   - `plataforma-danos-back/src/test/java/.../${input:featureName:feature}UseCaseTest.java`
   - `plataforma-danos-back/src/test/java/.../${input:featureName:feature}RepositoryAdapterTest.java`
   - `plataforma-danos-back/src/test/java/.../${input:featureName:feature}ControllerTest.java`
4. **Si scope es "frontend"**: delega a `Test Engineer Frontend`:
   - `cotizador-danos-web/src/__tests__/components/[Feature].test.jsx`
   - `cotizador-danos-web/src/__tests__/hooks/use[Feature].test.js`
   - `cotizador-danos-web/src/__tests__/pages/[Feature]Page.test.jsx`
5. **Verifica** que los tests corren:
   - Backend: Gradle test del módulo `plataforma-danos-back`
   - Frontend: `vitest run` del módulo `cotizador-danos-web`

## Cobertura obligatoria por test:
- ✅ Happy path (flujo exitoso)
- ❌ Error path (excepciones, errores de red, datos inválidos)
- 🔲 Edge cases (campos vacíos, duplicados, permisos)

## Restricciones:
- Cada test debe ser independiente (no compartir estado).
- Mockear SIEMPRE las dependencias externas (DB, API, core mock) cuando el alcance del test lo requiera.
- Para backend: usar JUnit 5 + Mockito + Spring Boot Test según el nivel.
- Para frontend: usar `vitest` + `@testing-library/react`.

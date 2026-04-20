# Copilot Instructions

## ASDD Workflow (Agent Spec Software Development)

Este repositorio sigue el flujo **ASDD**: toda funcionalidad nueva se ejecuta en cuatro fases orquestadas por agentes especializados.

```
[Orchestrator] → [Spec Generator] → [Backend ∥ Frontend ∥ DB] → [Tests BE ∥ Tests FE] → [QA] → [Doc]
```

### Fases del flujo ASDD
1. **Spec**: El agente `spec-generator` genera la spec en `.github/specs/<feature>.spec.md`.
2. **Implementación (paralelo)**: `backend-developer` + `frontend-developer` + `database-agent` (si hay cambios de DB).
3. **Tests (paralelo)**: `test-engineer-backend` + `test-engineer-frontend`.
4. **QA**: `qa-agent` genera estrategia, Gherkin, riesgos y análisis de performance.
5. **Doc (opcional)**: `documentation-agent` genera README updates, API docs y ADRs.

### Skills disponibles (slash commands):
- `/asdd-orchestrate` — orquesta el flujo completo ASDD o consulta estado
- `/generate-spec` — genera spec técnica en `.github/specs/`
- `/implement-backend` — implementa feature completo en el backend
- `/implement-frontend` — implementa feature completo en el frontend
- `/unit-testing` — genera suite de tests (backend + frontend)
- `/gherkin-case-generator` — casos Given-When-Then + datos de prueba
- `/risk-identifier` — clasificación de riesgos ASD (Alto/Medio/Bajo)
- `/automation-flow-proposer` — propuesta de automatización con ROI
- `/performance-analyzer` — planificación de pruebas de performance

### Requerimientos y Specs
- Los requerimientos de negocio viven en `.github/requirements/`. Son la entrada al pipeline ASDD.
- Las specs técnicas viven en `.github/specs/`. Cada spec es la fuente de verdad para implementar.
- Antes de implementar cualquier desarrollo, debe existir una spec aprobada en `.github/specs/`.
- Flujo: `requirements/<feature>.md` → `/generate-spec` → `specs/<feature>.spec.md` (APPROVED)

---

## Mapa de Archivos ASDD

### Agentes
| Agente | Fase | Ruta |
|---|---|---|
| Orchestrator | Entry point | `.github/agents/orchestrator.agent.md` |
| Spec Generator | Fase 1 | `.github/agents/spec-generator.agent.md` |
| Backend Developer | Fase 2 | `.github/agents/backend-developer.agent.md` |
| Frontend Developer | Fase 2 | `.github/agents/frontend-developer.agent.md` |
| Database Agent | Fase 2 | `.github/agents/database.agent.md` |
| Test Engineer Backend | Fase 3 | `.github/agents/test-engineer-backend.agent.md` |
| Test Engineer Frontend | Fase 3 | `.github/agents/test-engineer-frontend.agent.md` |
| QA Agent | Fase 4 | `.github/agents/qa.agent.md` |
| Documentation Agent | Fase 5 | `.github/agents/documentation.agent.md` |

### Skills
| Skill | Agente | Ruta |
|---|---|---|
| `/asdd-orchestrate` | Orchestrator | `.github/skills/asdd-orchestrate/SKILL.md` |
| `/generate-spec` | Spec Generator | `.github/skills/generate-spec/SKILL.md` |
| `/implement-backend` | Backend Developer | `.github/skills/implement-backend/SKILL.md` |
| `/implement-frontend` | Frontend Developer | `.github/skills/implement-frontend/SKILL.md` |
| `/unit-testing` | Test Engineer Backend + Frontend | `.github/skills/unit-testing/SKILL.md` |
| `/gherkin-case-generator` | QA Agent | `.github/skills/gherkin-case-generator/SKILL.md` |
| `/risk-identifier` | QA Agent | `.github/skills/risk-identifier/SKILL.md` |
| `/automation-flow-proposer` | QA Agent | `.github/skills/automation-flow-proposer/SKILL.md` |
| `/performance-analyzer` | QA Agent | `.github/skills/performance-analyzer/SKILL.md` |

### Instructions (path-scoped)
| Scope | Ruta | Se aplica a |
|---|---|---|
| Backend | `.github/instructions/backend.instructions.md` | `plataforma-danos-back/src/main/java/**/*.java` |
| Frontend | `.github/instructions/frontend.instructions.md` | `cotizador-danos-web/src/**/*.{js,jsx}` |
| Tests | `.github/instructions/tests.instructions.md` | `plataforma-danos-back/src/test/**/*.java` · `cotizador-danos-web/src/__tests__/**/*.{js,jsx}` · `automatizacion/**` |

### Lineamientos y Contexto
| Documento | Ruta |
|---|---|
| Lineamientos de Desarrollo | `.github/docs/lineamientos/dev-guidelines.md` |
| Lineamientos QA | `.github/docs/lineamientos/qa-guidelines.md` |
| Stack + Arquitectura + Naming | `.github/instructions/backend.instructions.md` |
| Stack Frontend + Naming | `.github/instructions/frontend.instructions.md` |

### Lineamientos generales para todos los agentes
- **Reglas de Oro**: ver `.github/AGENTS.md` — rigen TODAS las interacciones.
- **Specs activas**: `.github/specs/` — consultar siempre antes de implementar.

---

## Reglas de Oro

> Principio rector: todas las contribuciones de la IA deben ser seguras, transparentes, con propósito definido y alineadas con las instrucciones explícitas del usuario.

### I. Integridad del Código y del Sistema
- **No código no autorizado**: no escribir, generar ni sugerir código nuevo a menos que el usuario lo solicite explícitamente.
- **No modificaciones no autorizadas**: no modificar, refactorizar ni eliminar código, archivos o estructuras existentes sin aprobación explícita.
- **Preservar la lógica existente**: respetar los patrones arquitectónicos, el estilo de codificación y la lógica operativa existentes del proyecto.

### II. Clarificación de Requisitos
- **Clarificación obligatoria**: si la solicitud es ambigua, incompleta o poco clara, detenerse y solicitar clarificación antes de proceder.
- **No realizar suposiciones**: basar todas las acciones estrictamente en información explícita provista por el usuario.

### III. Transparencia Operativa
- **Explicar antes de actuar**: antes de cualquier acción, explicar qué se hará y posibles implicaciones.
- **Detención ante la incertidumbre**: si surge inseguridad o conflicto con estas reglas, detenerse y consultar al usuario.
- **Acciones orientadas a un propósito**: cada acción debe ser directamente relevante para la solicitud explícita.

---

## Diccionario de Dominio

Términos canónicos a usar en specs, código y mensajes:

| Término | Definición | Sinónimos rechazados |
|---------|-----------|---------------------|
| **Cotización** (`quote`) | Agregado principal de negocio identificado por `numeroFolio` | Caso, trámite |
| **Número de Folio** (`numeroFolio`) | Identificador único secuencial de la cotización | ID, consecutivo suelto |
| **Estado de Cotización** (`estadoCotizacion`) | Estado global de la cotización | Etapa, status |
| **Ubicación** (`location`) | Riesgo asegurable individual dentro de la cotización | Sede, predio |
| **Índice de Ubicación** (`indice`) | Posición única de la ubicación dentro de la cotización | ID técnico, correlativo externo |
| **Layout de Ubicaciones** (`configuracionLayout`) | Configuración de captura y distribución de ubicaciones | Maquetación, estructura |
| **Opciones de Cobertura** (`opcionesCobertura`) | Configuración de coberturas aplicables a la cotización | Términos, amparos |
| **Giro** (`giro`) | Actividad económica o línea de negocio del riesgo | Rubro, actividad |
| **Clave de Incendio** (`claveIncendio`) | Clave técnica requerida para tarifa de incendio | Clave técnica genérica |
| **Garantía** (`garantia`) | Componente tarifable incluido en el cálculo | Cobertura suelta, ítem |
| **Prima Neta** (`primaNeta`) | Resultado técnico consolidado antes de recargos comerciales | Prima técnica total |
| **Prima Comercial** (`primaComercial`) | Resultado final comercial de la cotización | Prima final |
| **Primas por Ubicación** (`primasPorUbicacion`) | Desglose financiero individual por ubicación | Detalle parcial |
| **Versión** (`version`) | Control de concurrencia optimista del agregado | Revisón, lock manual |
| **Fecha de Última Actualización** (`fechaUltimaActualizacion`) | Marca temporal de la última modificación lógica | Fecha modificación |
| **Alerta Bloqueante** (`alertaBloqueante`) | Regla que impide calcular una ubicación específica | Warning genérico |
| **Estado de Validación** (`estadoValidacion`) | Resultado de completitud o calculabilidad de la ubicación | Checklist, estatus libre |

**Reglas:** `numeroFolio` identifica la cotización. `cotizacion` es el agregado principal. Los timestamps del dominio deben mantenerse consistentes con el contrato del proyecto. Las ubicaciones pueden guardarse parcialmente, pero solo se calculan si cumplen las reglas bloqueantes definidas en la spec. Los errores del backend deben usar Problem Details y las respuestas exitosas deben usar envelope `data`.

---

## Decisiones Base del Reto

- Monorepo con módulos `cotizador-danos-web`, `plataforma-danos-back`, `plataforma-core-ohs`, `automatizacion` y `docs/`.
- Backend objetivo: Java 21 + Spring Boot 4.0.5 + Gradle + Spring Data JPA + Hibernate + Flyway + PostgreSQL.
- Frontend objetivo: React + Vite con sesión demo simulada.
- Servicio core de referencia: mock dentro del monorepo.
- API pública bajo `/v1/...`.
- Idempotencia obligatoria en `POST /v1/folios` mediante `Idempotency-Key`.
- Moneda COP con cálculo en `BigDecimal`, 2 decimales y redondeo `HALF_UP`.
- Estados globales sugeridos de cotización: `BORRADOR`, `EN_CAPTURA`, `LISTA_PARA_CALCULO`, `CALCULADA`.

---

## Project Overview

> Ver `README.md` en la raíz del proyecto.

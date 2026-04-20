# ASDD — Agent Spec-Driven Development

Framework de desarrollo asistido por IA que transforma requerimientos en código funcional mediante agentes especializados orquestados. Garantiza calidad y trazabilidad a través de especificaciones técnicas aprobadas antes de cualquier implementación.

```
Requerimiento → Spec → [Backend ∥ Frontend ∥ DB] → [Tests BE ∥ Tests FE] → QA → Docs
```

---

## Compatibilidad

| Herramienta | Configuración | Carpeta de agentes |
|-------------|---------------|--------------------|
| **Claude Code CLI** | `.claude/settings.json` | `.claude/agents/` |
| **GitHub Copilot** | `.github/copilot-instructions.md` | `.github/agents/` |

Ambas herramientas comparten el mismo flujo, las mismas specs y los mismos lineamientos. Solo difiere la carpeta de entrada de los agentes.

---

## Instalación

### Claude Code CLI

1. Instala Claude Code: https://claude.ai/code
2. Autentícate con tu cuenta Anthropic
3. Clona este repositorio en tu proyecto
4. Copia `.claude/` a la raíz de tu proyecto

```bash
cp -r .claude/ /tu-proyecto/.claude/
cp -r .github/ /tu-proyecto/.github/
```

### GitHub Copilot

1. Instala la extensión **GitHub Copilot Chat** en VS Code
2. Activa el uso de instruction files en tu settings.json de VS Code:

```json
{
  "github.copilot.chat.codeGeneration.useInstructionFiles": true
}
```

3. Copia `.github/` a la raíz de tu proyecto

---

## Flujo de trabajo

### Opción A — Orquestación automática completa

```
/asdd-orchestrate nombre-feature
```

El Orchestrator gestiona todo: genera la spec, espera aprobación, ejecuta fases en paralelo y reporta el estado al final.

### Opción B — Control manual paso a paso

```bash
# 1. Generar especificación técnica
/generate-spec nombre-feature

# 2. Revisar y aprobar la spec generada en .github/specs/<feature>.spec.md
#    Cambiar el campo:  status: DRAFT  →  status: APPROVED

# 3. Implementar backend y frontend (se pueden ejecutar en paralelo)
/implement-backend nombre-feature
/implement-frontend nombre-feature

# 4. Generar tests
/unit-testing nombre-feature

# 5. Análisis QA
/gherkin-case-generator
/risk-identifier
```

> **Regla de Oro**: Ningún agente escribe código si la spec no tiene `status: APPROVED`.

---

## Skills disponibles

| Comando | Qué hace |
|---------|----------|
| `/asdd-orchestrate` | Orquesta el flujo ASDD completo |
| `/generate-spec` | Genera spec técnica en `.github/specs/` |
| `/implement-backend` | Implementa el backend según la spec aprobada |
| `/implement-frontend` | Implementa el frontend según la spec aprobada |
| `/unit-testing` | Genera tests unitarios e integración |
| `/gherkin-case-generator` | Genera escenarios Given-When-Then y datos de prueba |
| `/risk-identifier` | Clasifica riesgos de calidad (Alto / Medio / Bajo) |
| `/automation-flow-proposer` | Propone flujos a automatizar con análisis de ROI |
| `/performance-analyzer` | Define estrategia de performance testing con k6 |

---

## Agentes disponibles

| Agente | Fase | Responsabilidad |
|--------|------|-----------------|
| `orchestrator` | Entry point | Coordina el flujo completo |
| `spec-generator` | 1 | Genera especificaciones técnicas |
| `backend-developer` | 2 | Rutas, servicios, repositorios |
| `frontend-developer` | 2 | Páginas, componentes, hooks |
| `database-agent` | 2 | Modelos, migrations, seeders |
| `test-engineer-backend` | 3 | Tests unitarios e integración backend |
| `test-engineer-frontend` | 3 | Tests unitarios y e2e frontend |
| `qa-agent` | 4 | Estrategia QA, Gherkin, riesgos, performance |
| `documentation-agent` | 5 | README, API docs, ADRs |

**Claude Code**: invoca agentes con `@nombre-agente` o con skills `/comando`
**GitHub Copilot**: usa `@nombre-agente` en el chat o los prompts en `.github/prompts/`

---

## Ciclo de vida de una spec

```
DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED
```

Las specs viven en `.github/specs/<feature>.spec.md`. Solo pasan a implementación cuando el usuario las aprueba manualmente cambiando el campo `status`.

---

## Estructura del repositorio

```
.
├── .claude/                        ← Configuración Claude Code CLI
│   ├── settings.json               ← Modelo, permisos, hooks
│   ├── agents/                     ← Sub-agentes Claude Code
│   ├── skills/                     ← Skills invocables con /comando
│   ├── rules/                      ← Reglas automáticas por tipo de archivo
│   ├── hooks/                      ← Scripts pre/post edit
│   └── docs/lineamientos/          ← Dev guidelines y QA guidelines
│
├── .github/                        ← Configuración GitHub Copilot
│   ├── copilot-instructions.md     ← Instrucciones globales + diccionario de dominio
│   ├── AGENTS.md                   ← Reglas de Oro para todos los agentes
│   ├── agents/                     ← Agentes Copilot
│   ├── skills/                     ← Skills portables
│   ├── instructions/               ← Instrucciones por scope (backend, frontend, tests)
│   ├── prompts/                    ← Prompts rápidos reutilizables
│   ├── requirements/               ← Requerimientos de entrada (input)
│   └── specs/                      ← Especificaciones técnicas (output de fase 1)
```

---

## Ejemplo completo

```bash
# 1. Escribe el requerimiento
echo "El usuario debe poder convertir monedas en tiempo real" \
  > .github/requirements/conversiones.md

# 2. Genera la spec
/generate-spec conversiones

# 3. Abre .github/specs/conversiones.spec.md, revisa y cambia:
#    status: DRAFT  →  status: APPROVED

# 4. Orquesta la implementación
/asdd-orchestrate conversiones

# → Backend implementado
# → Frontend implementado
# → Tests generados
# → Análisis QA completado
```

---

## Documentación interna

- `.github/README.md` — Guía detallada para GitHub Copilot
- `.claude/README.md` — Guía detallada para Claude Code CLI
- `.github/AGENTS.md` — Reglas de Oro y lineamientos de todos los agentes
- `.github/specs/README.md` — Convenciones y ciclo de vida de specs

---
name: frontend-task
description: Implementa una funcionalidad en el frontend React/Vite basada en una spec ASDD aprobada.
argument-hint: "<nombre-feature> (debe existir .github/specs/<nombre-feature>.spec.md)"
agent: Frontend Developer
tools:
  - edit/createFile
  - edit/editFiles
  - read/readFile
  - search/listDirectory
  - search
  - execute/runInTerminal
---

Implementa el frontend para el feature especificado, siguiendo la spec aprobada.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

## Pasos obligatorios:

1. **Lee la spec** en `.github/specs/${input:featureName:nombre-feature}.spec.md` — si no existe, detente e informa al usuario.
2. **Revisa el código existente** en `cotizador-danos-web/src/` para entender patrones actuales.
3. **Implementa en orden**:
  - `cotizador-danos-web/src/services/` — servicio con llamadas a API
  - `cotizador-danos-web/src/hooks/` o `context/` — flujo y sesión demo
  - `cotizador-danos-web/src/components/` — componentes reutilizables
  - `cotizador-danos-web/src/pages/` — página + CSS Module
4. **Registra la ruta** en `cotizador-danos-web/src/App.jsx`.
5. **Verifica** el build del módulo correspondiente.

## Restricciones:
- USAR CSS Modules exclusivamente — sin frameworks CSS globales.
- El estado de sesión demo debe venir del hook/store aprobado del proyecto.
- Las variables de entorno deben usar prefijo `VITE_`.
- Consumir endpoints `/v1/...` y leer respuesta desde `data`.

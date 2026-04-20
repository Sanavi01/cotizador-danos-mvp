---
name: backend-task
description: Implementa una funcionalidad en el backend Spring Boot basada en una spec ASDD aprobada.
argument-hint: "<nombre-feature> (debe existir .github/specs/<nombre-feature>.spec.md)"
agent: Backend Developer
tools:
  - edit/createFile
  - edit/editFiles
  - read/readFile
  - search/listDirectory
  - search
  - execute/runInTerminal
---

Implementa el backend para el feature especificado, siguiendo la spec aprobada.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

## Pasos obligatorios:

1. **Lee la spec** en `.github/specs/${input:featureName:nombre-feature}.spec.md` — si no existe, detente e informa al usuario.
2. **Revisa el código existente** en `plataforma-danos-back/` para entender patrones actuales.
3. **Implementa en orden**:
  - DTOs/controller de entrada
  - dominio + casos de uso
  - adaptadores JPA/clients
  - controller Spring Boot
4. **Registra o expone** el nuevo endpoint dentro del módulo Spring correspondiente.
5. **Verifica** compilación o tests del módulo con Gradle.

## Restricciones:
- Sigue la arquitectura `controller → application → domain → infrastructure`.
- NO inyectar repositorios JPA directamente en controllers.
- Si cambia persistencia, agrega migración Flyway.

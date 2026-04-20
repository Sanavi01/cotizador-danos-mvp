---
name: implement-frontend
description: Implementa un feature completo en el frontend. Requiere spec con status APPROVED en .github/specs/.
argument-hint: "<nombre-feature>"
---

# Implement Frontend

## Prerequisitos
1. Leer spec: `.github/specs/<feature>.spec.md` — sección 2.3 (componentes, páginas, hooks)
2. Leer stack: `.github/instructions/frontend.instructions.md`
3. Leer arquitectura: `.github/instructions/frontend.instructions.md`

## Orden de implementación
```
services → hooks/state → components → pages/views → registrar ruta
```

| Capa | Responsabilidad |
|------|-----------------|
| **Services** | Llamadas HTTP al backend — sin estado, sin lógica de negocio |
| **Hooks / State** | Estado local, efectos, acciones — consume services |
| **Components** | UI reutilizable — recibe props, emite eventos |
| **Pages / Views** | Composición final — layout + rutas |

## Patrones obligatorios
- Sesión demo: consumir SÓLO desde el hook/store de sesión aprobado por el proyecto
- Variables de entorno: URL del API siempre desde variables de entorno, nunca hardcodeada
- Endpoints del reto bajo `/v1/...` y respuestas con envelope `data`
- Estilos: usar ÚNICAMENTE el sistema de estilos aprobado en el proyecto (ver contexto)

Ver patrones específicos en `.github/instructions/frontend.instructions.md`.

## Restricciones
- Solo directorio `cotizador-danos-web/`. No tocar backend.
- No generar tests (responsabilidad de `test-engineer-frontend`).

---
name: implement-backend
description: Implementa un feature completo en el backend. Requiere spec con status APPROVED en .github/specs/.
argument-hint: "<nombre-feature>"
---

# Implement Backend

## Prerequisitos
1. Leer spec: `.github/specs/<feature>.spec.md` — sección 2 (modelos, endpoints)
2. Leer stack: `.github/instructions/backend.instructions.md`
3. Leer arquitectura: `.github/instructions/backend.instructions.md`

## Orden de implementación
```
request/response DTOs → domain model → use cases → adapters → controllers → registrar/OpenAPI
```

| Capa | Responsabilidad |
|------|-----------------|
| **DTOs** | Contratos HTTP de entrada y salida |
| **Domain / Ports** | Agregado, value objects y puertos |
| **Use Cases** | Lógica de negocio y orquestación |
| **Adapters** | JPA, clientes HTTP y mapeadores |
| **Controllers** | Parsing HTTP + DI + delegar al caso de uso |

## Patrón de DI (obligatorio en controllers)
- Inyectar dependencias por constructor gestionadas por Spring.
- El controller recibe casos de uso, nunca repositorios JPA directamente.
- Los adaptadores externos deben quedar detrás de puertos o clientes claramente encapsulados.

Ver patrones específicos del stack en `.github/instructions/backend.instructions.md`.

## Reglas
- Seguir `.github/instructions/backend.instructions.md` para arquitectura, versionado optimista, rutas `/v1/...`, Flyway y Problem Details.

## Restricciones
- Solo directorio `plataforma-danos-back/`. No tocar frontend.
- No generar tests (responsabilidad de `test-engineer-backend`).

---
applyTo: "plataforma-danos-back/src/main/java/**/*.java"
---

> **Scope**: Se aplica a proyectos con capa backend Java. Si el proyecto usa un stack diferente, adaptar esta instrucción al stack real antes de implementar.

# Instrucciones para Archivos de Backend (Java/Spring Boot)

## Arquitectura objetivo

Siempre sigue una arquitectura **hexagonal/clean** con separación explícita entre entrada, aplicación, dominio e infraestructura:

```
controllers → application → domain → infrastructure
```

- **`entrypoints/controller`**: Solo parseo HTTP, validación de entrada, mapeo request/response y delegación al caso de uso.
- **`application/usecase`**: Orquesta reglas de negocio y transacciones del caso de uso.
- **`domain/model`**: Entidades, value objects, puertos y reglas puras del dominio.
- **`infrastructure/persistence|client|config`**: JPA, Flyway, clientes HTTP, configuración y adaptadores externos.

## Wiring de Dependencias (patrón obligatorio)

```java
@RestController
@RequestMapping("/v1/quotes")
public class QuoteController {

    private final UpdateGeneralInfoUseCase updateGeneralInfoUseCase;

    public QuoteController(UpdateGeneralInfoUseCase updateGeneralInfoUseCase) {
        this.updateGeneralInfoUseCase = updateGeneralInfoUseCase;
    }

    @PutMapping("/{folio}/general-info")
    public ResponseEntity<ApiResponse<QuoteGeneralInfoResponse>> updateGeneralInfo(
            @PathVariable String folio,
            @Valid @RequestBody UpdateGeneralInfoRequest request) {
        return ResponseEntity.ok(ApiResponse.of(updateGeneralInfoUseCase.handle(folio, request)));
    }
}
```

NUNCA instanciar manualmente repositorios o casos de uso dentro del controller.
NUNCA mezclar lógica de dominio dentro de controllers o entities JPA.

## Convenciones de Código

- Java 21 como baseline del proyecto.
- Preferir paquetes por capability y capa, evitando paquetes genéricos enormes.
- Entidades de dominio con nombres del negocio: `Cotizacion`, `Ubicacion`, `Garantia`, `PrimaPorUbicacion`.
- Controllers bajo `/v1/...`, nunca `/api/v1/...` para este reto.
- Respuestas exitosas con envelope `data`; errores con Problem Details.
- Persistencia con Spring Data JPA + Hibernate sobre PostgreSQL.
- Versionado optimista con `@Version` en la entidad persistida de `Cotizacion`.
- Migraciones con Flyway; no depender de `ddl-auto` como mecanismo principal.
- Cálculos monetarios siempre con `BigDecimal`, escala 2 y `RoundingMode.HALF_UP`.
- HTTP clients externos con timeout explícito y configuración centralizada.

## Nuevas Rutas / Controladores

Para agregar un nuevo endpoint:
1. Crear request/response DTOs en la capa de entrada si aplica.
2. Crear o reutilizar el caso de uso en la capa de aplicación.
3. Exponer controller bajo `/v1/...`.
4. Documentar el contrato en OpenAPI.
5. Si cambia esquema, agregar migración Flyway.

> Ver `README.md` para la estructura de carpetas específica del proyecto.

## Nunca hacer

- Lógica de negocio en controllers.
- Acceder a repositorios JPA directamente desde controllers.
- Hacer cálculo técnico dentro de mapeadores HTTP.
- Usar `double` o `float` para dinero.
- Saltarse Flyway creando tablas manualmente fuera del repositorio.
- Acoplar el dominio a clases de infraestructura o a clients HTTP.

---

> Para estándares de código limpio, SOLID, nombrado, API REST, seguridad y observabilidad, ver `.github/docs/lineamientos/dev-guidelines.md`.

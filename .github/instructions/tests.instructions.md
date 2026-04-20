---
applyTo: "plataforma-danos-back/src/test/**/*.java,cotizador-danos-web/src/__tests__/**/*.{js,jsx},automatizacion/**/*"
---

> **Scope**: Las reglas de backend aplican a proyectos con tests en Java/Spring; las de frontend a JS/JSX; y las de automatización al módulo E2E. Mantener independencia, aislamiento y cobertura ≥ 80% en lógica de negocio.

# Instrucciones para Archivos de Pruebas Unitarias

## Principios

- **Independencia**: cada test es 100% independiente — sin estado compartido entre tests.
- **Aislamiento**: mockear SIEMPRE dependencias externas no cubiertas por el alcance del test (DB, APIs REST, sistema de archivos, servicios del core).
- **Claridad**: nombre del test debe describir la función bajo prueba y el escenario (qué pasa cuando X).
- **Cobertura**: cubrir happy path, error path y edge cases para cada unidad.

## Backend (JUnit 5 + Mockito + Spring Boot Test)

### Estructura de archivos
```
plataforma-danos-back/src/test/java/
  .../application/
  .../infrastructure/
  .../entrypoints/
```

### Convenciones
- Nombre: `[metodo]_[escenario]` (ej: `createQuote_returnsConflictWhenVersionMismatch`).
- Casos de uso con JUnit 5 + Mockito.
- Controllers con `@WebMvcTest` o `MockMvc`.
- Integración selectiva con `@SpringBootTest` y preferiblemente Testcontainers para PostgreSQL cuando aporte valor real.

```java
@ExtendWith(MockitoExtension.class)
class CreateFolioUseCaseTest {

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Test
    void createFolio_returnsExistingQuoteWhenIdempotencyKeyAlreadyUsed() {
        // GIVEN
        // WHEN
        // THEN
    }
}
```

## Frontend (Vitest + Testing Library)

### Estructura de archivos
```
cotizador-danos-web/src/__tests__/
  [ComponentName].test.jsx
  use[HookName].test.js
```

### Convenciones
- Nombre del describe: nombre del componente/hook.
- Nombre del it/test: `[verbo] [qué hace] [condición]` (ej: `renders login button when unauthenticated`).
- Usar `vi.mock()` para mockear módulos externos y servicios HTTP.
- Siempre limpiar mocks con `beforeEach(() => vi.clearAllMocks())`.

```jsx
// Ejemplo mínimo de test de componente
describe('LoginPage', () => {
  it('renders email input', () => {
    render(<LoginPage />);
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
  });
});
```

## Nunca hacer

- Tests que dependen del orden de ejecución.
- Llamadas reales a servicios externos no controlados por el test.
- `console.log` permanentes en tests.
- Lógica condicional dentro de un test (if/else).
- Usar `sleep` para sincronización temporal (cero tests "flaky").

## Automatización E2E (Serenity BDD + Screenplay)

- El módulo `automatizacion/` debe cubrir al menos 3 flujos críticos justificando su ROI.
- Mantener datos desacoplados del código y ambientes configurables.
- Priorizar los flujos: creación/apertura de folio, captura de ubicaciones, cálculo con ubicación incompleta.
- La automatización debe apuntar al sistema desplegado localmente, nunca a ambientes manuales del usuario.

---

> Para quality gates, pirámide de testing, TDD, CDC y nomenclatura Gherkin, ver `.github/docs/lineamientos/dev-guidelines.md` §7 y `.github/docs/lineamientos/qa-guidelines.md`.

### Estructura AAA obligatoria
```text
# GIVEN — preparar datos y contexto
# WHEN  — ejecutar la acción bajo prueba
# THEN  — verificar el resultado esperado
```

### DoR de Automatización
Antes de automatizar un flujo, verificar:
- [ ] Caso ejecutado exitosamente en manual sin bugs críticos
- [ ] Caso de prueba detallado con datos identificados
- [ ] Viabilidad técnica comprobada
- [ ] Ambiente estable disponible
- [ ] Aprobación del equipo

### DoD de Automatización
Un script finaliza cuando:
- [ ] Código revisado por pares (pull request review)
- [ ] Datos desacoplados del código
- [ ] Integrado al pipeline de CI
- [ ] Con documentación y trazabilidad hacia la HU

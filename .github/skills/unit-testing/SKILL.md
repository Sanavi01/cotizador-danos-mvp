---
name: unit-testing
description: Genera tests unitarios e integración para backend y/o frontend. Lee la spec y el código implementado. Requiere spec APPROVED e implementación completa.
argument-hint: "<nombre-feature> [backend|frontend|ambos]"
---

# Unit Testing

## Definition of Done — verificar al completar

- [ ] Cobertura ≥ 80% en lógica de negocio (quality gate bloqueante)
- [ ] Tests aislados — sin dependencia a servicios externos no controlados
- [ ] Escenario feliz + errores de negocio + validaciones de entrada cubiertos
- [ ] Los cambios no rompen contratos existentes del módulo

## Prerequisito — Lee en paralelo

```
.github/specs/<feature>.spec.md        (criterios de aceptación)
código implementado en plataforma-danos-back/ y/o cotizador-danos-web/
.github/instructions/backend.instructions.md   (JUnit + Mockito + Spring Boot Test)
.github/instructions/frontend.instructions.md  (Vitest + Testing Library)
```

## Output por scope

### Backend → `plataforma-danos-back/src/test/java/`

| Archivo | Cubre |
|---------|-------|
| `.../entrypoints/*ControllerTest.java` | Endpoints: 200/201, 404, 409, 422 |
| `.../application/*UseCaseTest.java` | Lógica: happy path + errores de negocio |
| `.../infrastructure/*RepositoryAdapterTest.java` | Adaptadores críticos y mapeos |

### Frontend → `cotizador-danos-web/src/__tests__/`

| Archivo | Cubre |
|---------|-------|
| `components/<Feature>.test.jsx` | Render + interacciones (click, submit) |
| `hooks/use<Feature>.test.js` | Estado inicial + respuesta API + error handling |
| `pages/<Feature>Page.test.jsx` | Render completo con providers |

## Patrones core

```java
@ExtendWith(MockitoExtension.class)
class UpdateGeneralInfoUseCaseTest {

    @Mock
    private CotizacionRepository repository;

    @Test
    void handle_updatesGeneralInfoWhenVersionMatches() {
        // GIVEN
        // WHEN
        // THEN
    }
}
```

```js
// Frontend — mock service + renderHook (Vitest + Testing Library)
vi.mock('../../services/featureService');
getFeatures.mockResolvedValue([{ uid: '1' }]);
const { result } = renderHook(() => useFeature());
await waitFor(() => expect(result.current.data).toHaveLength(1));
```

## Restricciones

- Solo `tests/` o `__tests__/`. No modificar código fuente.
- Nunca depender de servicios externos no controlados por el alcance del test.
- Cobertura mínima ≥ 80% en lógica de negocio.

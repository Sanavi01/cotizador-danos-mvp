"""
patterns.py — Referencia conceptual para el agente backend.
No se ejecuta directamente; sirve para recordar el patrón objetivo del reto.

Patrón recomendado:

1. Controller Spring Boot expone `/v1/...`
2. Controller delega a un Use Case
3. Use Case trabaja con puertos del dominio
4. Adaptadores JPA/HTTP implementan los puertos
5. Errores HTTP con Problem Details y éxitos con envelope `data`

Ejemplo esquemático:

// Controller
@RestController
@RequestMapping("/v1/quotes")
class QuoteController {
    private final UpdateGeneralInfoUseCase useCase;
}

// Use case
class UpdateGeneralInfoUseCase {
    private final CotizacionRepository repository;
    private final Clock clock;
}

// Domain aggregate
class Cotizacion {
    private String numeroFolio;
    private EstadoCotizacion estadoCotizacion;
    private Long version;
}

// Persistence adapter
class JpaCotizacionRepositoryAdapter implements CotizacionRepository {
}
"""

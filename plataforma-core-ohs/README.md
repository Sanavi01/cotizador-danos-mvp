# Plataforma Core OHS

Modulo mock/stub de referencia para el reto de cotizacion de danos. Expone catalogos, validacion territorial, secuencia de folios y tarifas tecnicas con fixtures en memoria.

## Decisiones de dominio

Estas reglas quedan fijadas como convenciones del proyecto para mantener trazabilidad y evitar ambiguedad en la implementacion:

- Estados globales de cotizacion: `BORRADOR`, `EN_CAPTURA`, `LISTA_PARA_CALCULO`, `CALCULADA`.
- El endpoint de estado puede complementar el estado global con `TieneAlertas`, `SeccionesCompletadas`, `UbicacionesCalculables`, `UbicacionesIncompletas`, `Version` y `FechaUltimaActualizacion`.
- La moneda oficial del dominio es `COP`.
- Todo calculo monetario usa `BigDecimal`.
- Persistencia y respuestas API con 2 decimales.
- Redondeo `HALF_UP`.
- Formato visual esperado en frontend: `es-CO`.

## Dataset versionado

El fixture se mantiene con un volumen medio realista para pruebas de contrato y consumo:

- 5 suscriptores.
- 12 agentes.
- 20 giros con `claveIncendio`.
- 4 clasificaciones de riesgo.
- 14 garantias alineadas con el reto.
- 60 codigos postales distribuidos en varias ciudades y zonas tecnicas.
- 1 configuracion activa de calculo.
- Tarifas de incendio por giro, tipo constructivo y nivel.
- Tarifas CAT por zona TEV y cobertura aplicable.
- Tarifa FHM por zona FHM y grupo.
- Factores de equipo electronico por clase y nivel.
- 3 cotizaciones semilla, incluyendo una calculada y una incompleta.

## Fixture principal

El dataset vive en [src/main/resources/fixtures/reference-core-fixtures.json](src/main/resources/fixtures/reference-core-fixtures.json).

## Contrato expuesto

El modulo expone endpoints REST bajo `/v1/...` para:

- Catalogos base de suscriptores, agentes, giros, clasificacion de riesgo y garantias.
- Validacion y consulta de codigos postales.
- Secuencia de folios.
- Lookup de tarifas tecnicas.

## Notas de implementacion

- El estado de la informacion se resuelve en memoria a partir de fixtures versionadas.
- Los catalogos son de solo lectura.
- La validacion postal no usa `404` para codigos no reconocidos; retorna `200` con `valido: false`.
- Los errores funcionales se devuelven como Problem Details.

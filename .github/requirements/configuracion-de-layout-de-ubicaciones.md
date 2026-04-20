# Requerimiento: Configuracion de Layout de Ubicaciones

## Objetivo

Permitir definir la configuracionLayout del folio para organizar la captura de una o multiples ubicaciones de riesgo.

## Alcance

- Consultar la configuracion actual del layout de ubicaciones.
- Guardar o actualizar la configuracionLayout de la cotizacion.
- Preparar el folio para la captura ordenada de una o varias ubicaciones.
- Conservar trazabilidad de version y fechaUltimaActualizacion al modificar el layout.

## Historias de Usuario

### HU-01: Configurar la estructura de ubicaciones del folio

Como: usuario del cotizador
Quiero: definir el layout de ubicaciones antes de capturarlas
Para: organizar correctamente la distribucion del riesgo dentro de la cotizacion

Prioridad: Alta
Dependencias: datos-generales-de-cotizacion

#### Criterios de aceptacion

```gherkin
Escenario: Guardar el layout de ubicaciones
  Dado que la cotizacion ya cuenta con numeroFolio
  Cuando el usuario define la configuracionLayout de ubicaciones
  Entonces el sistema persiste la configuracion del layout
  Y deja disponible el folio para registrar una o varias ubicaciones
  Y actualiza version y fechaUltimaActualizacion

Escenario: Consultar el layout previamente configurado
  Dado que la cotizacion ya tiene configuracionLayout guardada
  Cuando el usuario vuelve a abrir la seccion
  Entonces el sistema devuelve la misma configuracion persistida
```

### HU-02: Evitar cambios inconsistentes de layout

Como: sistema de cotizacion
Quiero: controlar modificaciones del layout con versionado optimista
Para: evitar perdida silenciosa de trabajo concurrente sobre el folio

Prioridad: Alta
Dependencias: folios-e-idempotencia

#### Criterios de aceptacion

```gherkin
Escenario: Detectar conflicto de version al editar layout
  Dado que dos usuarios o procesos intentan modificar el mismo folio
  Cuando uno de ellos guarda con una version desactualizada
  Entonces el sistema rechaza la actualizacion
  Y informa un conflicto funcional al cliente
```

## Contratos esperados

- GET /v1/quotes/{folio}/locations/layout
- PUT /v1/quotes/{folio}/locations/layout

## Reglas de negocio

- configuracionLayout describe la forma en que se capturaran y distribuiran las ubicaciones del folio.
- Una cotizacion puede operar con una o multiples ubicaciones.
- Los cambios sobre el layout deben respetar versionado optimista.
- Guardar el layout no debe sobrescribir otras secciones del agregado.

## Fuera de alcance inicial

- Edicion detallada del contenido de cada ubicacion.
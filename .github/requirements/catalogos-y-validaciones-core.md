# Requerimiento: Catalogos y Validaciones Core

## Objetivo

Disponer de un servicio de referencia, real o simulado, para resolver catalogos comerciales y tecnicos necesarios durante la captura y el calculo de una cotizacion.

## Alcance

- Consultar suscriptores, agentes, giros y codigos postales.
- Validar codigos postales y enriquecer direccion territorial.
- Exponer catalogos de clasificacion de riesgo y garantias.
- Generar folios desde la referencia core cuando aplique.
- Resolver tarifas y factores tecnicos requeridos por el calculo.

## Historias de Usuario

### HU-01: Consultar catalogos operativos durante la captura

Como: usuario del cotizador
Quiero: consultar suscriptores, agentes, giros y codigos postales validos
Para: diligenciar la cotizacion con datos consistentes y reutilizables

Prioridad: Alta
Dependencias: folios-e-idempotencia

#### Criterios de aceptacion

```gherkin
Escenario: Consultar catalogos base
  Dado que el usuario esta capturando una cotizacion
  Cuando necesita seleccionar un suscriptor, un agente, un giro o un codigo postal
  Entonces el sistema consulta la fuente de referencia aprobada
  Y retorna informacion suficiente para completar la captura

Escenario: Validar un codigo postal invalido
  Dado que el usuario ingresa un codigo postal no reconocido
  Cuando el sistema intenta validarlo
  Entonces la validacion falla
  Y la ubicacion queda marcada con alerta bloqueante
```

### HU-02: Resolver datos tecnicos para el calculo

Como: sistema de cotizacion
Quiero: consumir parametros, tarifas y factores tecnicos desde la referencia core
Para: calcular la prima de forma consistente y trazable

Prioridad: Alta
Dependencias: gestion-de-ubicaciones, opciones-de-cobertura

#### Criterios de aceptacion

```gherkin
Escenario: Obtener insumos tecnicos para una ubicacion calculable
  Dado que una ubicacion cuenta con datos minimos para ser evaluada
  Cuando el backend ejecuta el calculo tecnico
  Entonces consulta la referencia core o su stub documentado
  Y obtiene catalogos, tarifas y factores aplicables a la ubicacion

Escenario: Operar con un stub documentado
  Dado que no existe una integracion real disponible
  Cuando la solucion usa un stub o mock server
  Entonces el contrato de la referencia queda documentado
  Y los datos de prueba permanecen versionados dentro del repositorio
```

## Contratos esperados

- GET /v1/subscribers
- GET /v1/agents
- GET /v1/business-lines
- GET /v1/zip-codes/{zipCode}
- POST /v1/zip-codes/validate
- GET /v1/folios
- GET /v1/catalogs/risk-classification
- GET /v1/catalogs/guarantees
- GET /v1/tariffs/...
- PUT /v1/tariffs/...

## Reglas de negocio

- La referencia core puede implementarse como servicio real, stub, mock server o fixtures versionados.
- El contrato consumido debe quedar documentado aunque la implementacion sea simulada.
- La captura y el calculo deben depender de catalogos consistentes, no de valores hardcodeados en la UI.
- La validacion territorial y tecnica debe ocurrir con los datos de referencia aprobados.

## Fuera de alcance inicial

- Sincronizacion bidireccional con sistemas corporativos reales fuera del reto.
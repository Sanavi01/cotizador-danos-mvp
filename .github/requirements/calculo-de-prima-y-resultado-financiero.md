# Requerimiento: Calculo de Prima y Resultado Financiero

## Objetivo

Permitir ejecutar el calculo tecnico y comercial de una cotizacion, obteniendo primaNeta, primaComercial y primasPorUbicacion con alertas claras para riesgos incompletos.

## Alcance

- Ejecutar el calculo del folio completo por numeroFolio.
- Leer la cotizacion, parametros globales, tarifas y factores tecnicos requeridos.
- Determinar que ubicaciones son calculables y cuales permanecen incompletas.
- Calcular y consolidar la prima por ubicacion, la primaNeta total y la primaComercial total.
- Persistir el resultado financiero sin sobrescribir otras secciones del agregado.
- Presentar alertas para ubicaciones incompletas sin bloquear completamente el folio.

## Historias de Usuario

### HU-01: Calcular la prima del folio

Como: usuario del cotizador
Quiero: ejecutar el calculo del folio
Para: conocer la primaNeta, la primaComercial y el desglose por ubicacion

Prioridad: Alta
Dependencias: datos-generales-de-cotizacion, gestion-de-ubicaciones, opciones-de-cobertura, catalogos-y-validaciones-core

#### Criterios de aceptacion

```gherkin
Escenario: Calcular el folio con una o varias ubicaciones validas
  Dado que la cotizacion cuenta con datos suficientes para ejecutar el calculo
  Cuando el usuario solicita calcular el folio
  Entonces el backend lee la cotizacion completa y sus insumos tecnicos
  Y calcula la prima por cada ubicacion calculable
  Y consolida primaNeta y primaComercial del folio
  Y persiste el resultado financiero en una misma operacion logica

Escenario: Consultar el resultado financiero despues del calculo
  Dado que el folio ya fue calculado
  Cuando la UI consulta el estado funcional correspondiente
  Entonces el usuario puede ver primaNeta, primaComercial y primasPorUbicacion
```

### HU-02: Continuar el calculo con ubicaciones incompletas

Como: usuario del cotizador
Quiero: obtener resultado para las ubicaciones validas aunque existan ubicaciones incompletas
Para: no perder el avance comercial del folio por una sola ubicacion pendiente

Prioridad: Alta
Dependencias: gestion-de-ubicaciones, estado-y-progreso-de-cotizacion

#### Criterios de aceptacion

```gherkin
Escenario: Calcular solo las ubicaciones elegibles
  Dado que el folio tiene una ubicacion completa y otra incompleta
  Cuando el usuario ejecuta el calculo
  Entonces el sistema calcula la ubicacion elegible
  Y registra alerta para la ubicacion incompleta
  Y no bloquea completamente el resultado del folio

Escenario: Marcar una ubicacion como no calculable
  Dado que una ubicacion no tiene codigo postal valido, giro.claveIncendio o garantias tarifables
  Cuando el backend evalua la elegibilidad tecnica
  Entonces la ubicacion se marca como no calculable
  Y queda reportada con alertaBloqueante y estadoValidacion correspondiente
```

## Contratos esperados

- POST /v1/quotes/{folio}/calculate

## Reglas de negocio

- El calculo debe leer la cotizacion completa por numeroFolio.
- Deben usarse parametros globales de calculo, tarifas y factores tecnicos aprobados para el reto.
- El resultado financiero debe persistir primaNeta, primaComercial y primasPorUbicacion en una misma operacion logica.
- Persistir el resultado financiero no debe sobrescribir datos generales, layout, ubicaciones ni opcionesCobertura.
- Una ubicacion no debe calcularse si no tiene codigo postal valido, giro.claveIncendio o garantias tarifables.
- Una ubicacion incompleta genera alerta, pero no debe impedir calcular las demas.
- El desglose financiero debe contemplar, como minimo, los componentes tecnicos obligatorios definidos en el reto.

## Fuera de alcance inicial

- Formula actuarial corporativa exacta cuando no haya sido entregada por el reto.
- Emision final de documentos comerciales o de poliza.
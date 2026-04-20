# Requerimiento: Folios e Idempotencia

## Objetivo

Permitir que el usuario cree un nuevo numeroFolio y retome una cotizacion existente sin generar duplicados cuando el cliente reintenta la operacion.

## Alcance

- Crear folios secuenciales para nuevas cotizaciones.
- Exigir idempotencia en la creacion de folios.
- Permitir que la SPA abra un folio ya existente para continuar la captura.
- Dejar trazable la relacion entre solicitud, numeroFolio y cotizacion creada.

## Historias de Usuario

### HU-01: Crear un folio nuevo

Como: usuario del cotizador
Quiero: solicitar un numeroFolio nuevo
Para: iniciar la captura de una nueva cotizacion

Prioridad: Alta
Dependencias: catalogos-y-validaciones-core

#### Criterios de aceptacion

```gherkin
Escenario: Crear un folio nuevo de forma exitosa
  Dado que el usuario inicia una nueva cotizacion
  Cuando solicita la creacion del folio con una llave de idempotencia valida
  Entonces el sistema genera un numeroFolio secuencial unico
  Y crea la cotizacion en estado BORRADOR
  Y retorna el resultado en un envelope data

Escenario: Reintentar la creacion con la misma llave de idempotencia
  Dado que ya existe una solicitud previa exitosa con la misma llave de idempotencia
  Cuando el cliente reintenta la operacion
  Entonces el sistema no crea un segundo numeroFolio
  Y retorna la misma respuesta funcional de la solicitud original
```

### HU-02: Abrir un folio existente

Como: usuario del cotizador
Quiero: ingresar un numeroFolio ya creado
Para: continuar una cotizacion sin perder el avance previo

Prioridad: Alta
Dependencias: estado-y-progreso-de-cotizacion

#### Criterios de aceptacion

```gherkin
Escenario: Abrir un folio existente
  Dado que existe una cotizacion asociada a un numeroFolio
  Cuando el usuario consulta el folio desde la SPA
  Entonces el sistema devuelve el estado actual de la cotizacion
  Y permite continuar el flujo desde la ultima informacion persistida

Escenario: Consultar un folio inexistente
  Dado que el numeroFolio no existe en el sistema
  Cuando el usuario intenta abrirlo
  Entonces el sistema responde con un error funcional en formato Problem Details
```

## Contratos esperados

- POST /v1/folios
- GET /v1/quotes/{folio}/state

## Reglas de negocio

- numeroFolio es el identificador principal y debe ser secuencial y unico.
- POST /v1/folios requiere el header Idempotency-Key.
- Una misma llave de idempotencia no debe producir mas de una cotizacion efectiva.
- La cotizacion creada inicia en estado BORRADOR.
- Las respuestas exitosas deben usar envelope data y los errores deben usar Problem Details.

## Fuera de alcance inicial

- Emision de poliza.
- Flujo de autenticacion real.
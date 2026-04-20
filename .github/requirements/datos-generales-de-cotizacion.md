# Requerimiento: Datos Generales de Cotizacion

## Objetivo

Permitir la captura, consulta y actualizacion de la informacion general de una cotizacion sin afectar otras secciones del agregado.

## Alcance

- Consultar la seccion general de una cotizacion por numeroFolio.
- Capturar y actualizar datos del asegurado y de conduccion comercial.
- Resolver y validar datos relacionados con codigoAgente, clasificacionRiesgo y tipoNegocio.
- Mantener version y fechaUltimaActualizacion al editar la seccion.

## Historias de Usuario

### HU-01: Consultar y guardar los datos generales

Como: usuario del cotizador
Quiero: capturar y actualizar los datos generales de una cotizacion
Para: dejar listo el folio para continuar con las siguientes secciones

Prioridad: Alta
Dependencias: folios-e-idempotencia, catalogos-y-validaciones-core

#### Criterios de aceptacion

```gherkin
Escenario: Guardar datos generales de forma exitosa
  Dado que existe una cotizacion identificada por numeroFolio
  Cuando el usuario registra o actualiza los datos generales validos
  Entonces el sistema persiste la seccion general
  Y mantiene intactas las secciones de layout, ubicaciones, coberturas y resultados financieros
  Y actualiza version y fechaUltimaActualizacion

Escenario: Consultar los datos generales existentes
  Dado que la cotizacion ya tiene informacion general guardada
  Cuando el usuario abre la pantalla de datos generales
  Entonces el sistema devuelve la informacion previamente persistida
  Y la UI la presenta para revision o edicion
```

### HU-02: Rechazar datos generales inconsistentes

Como: sistema de cotizacion
Quiero: validar los datos generales obligatorios y sus referencias
Para: evitar que el folio avance con informacion inconsistente

Prioridad: Alta
Dependencias: catalogos-y-validaciones-core

#### Criterios de aceptacion

```gherkin
Escenario: Rechazar un codigoAgente invalido
  Dado que el usuario envia un codigoAgente inexistente o inconsistente
  Cuando el backend intenta guardar la seccion
  Entonces la operacion es rechazada
  Y el sistema responde con Problem Details

Escenario: Rechazar una cotizacion inexistente
  Dado que el numeroFolio no corresponde a una cotizacion existente
  Cuando se intenta consultar o guardar datos generales
  Entonces el sistema responde con error funcional de no encontrado
```

## Contratos esperados

- GET /v1/quotes/{folio}/general-info
- PUT /v1/quotes/{folio}/general-info

## Reglas de negocio

- cotizacion es el agregado principal y no debe ser sobrescrito por completo al guardar una sola seccion.
- Toda actualizacion valida incrementa version y actualiza fechaUltimaActualizacion.
- datosAsegurado, datosConduccion.codigoAgente, clasificacionRiesgo y tipoNegocio deben quedar consistentes con los catalogos aprobados.
- Las respuestas exitosas deben quedar bajo data y los errores deben publicarse como Problem Details.

## Fuera de alcance inicial

- Calculo de prima.
- Emision documental posterior a la cotizacion.
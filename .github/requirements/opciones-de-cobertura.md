# Requerimiento: Opciones de Cobertura

## Objetivo

Permitir consultar y actualizar las opcionesCobertura de una cotizacion para controlar los terminos que seran considerados durante el calculo.

## Alcance

- Consultar las opciones de cobertura vigentes del folio.
- Guardar o actualizar la configuracion de coberturas del folio.
- Mantener separada esta seccion del resto del agregado.
- Preparar la informacion para el calculo tecnico y comercial.

## Historias de Usuario

### HU-01: Configurar opciones de cobertura del folio

Como: usuario del cotizador
Quiero: seleccionar y actualizar opciones de cobertura
Para: controlar que garantias y terminos participan en el calculo

Prioridad: Alta
Dependencias: catalogos-y-validaciones-core, gestion-de-ubicaciones

#### Criterios de aceptacion

```gherkin
Escenario: Guardar opciones de cobertura validas
  Dado que existe una cotizacion identificada por numeroFolio
  Cuando el usuario registra o modifica las opciones de cobertura
  Entonces el sistema persiste la seccion opcionesCobertura
  Y la deja disponible para el calculo posterior
  Y actualiza version y fechaUltimaActualizacion

Escenario: Consultar opciones de cobertura existentes
  Dado que la cotizacion ya tiene opcionesCobertura guardadas
  Cuando el usuario abre la seccion correspondiente
  Entonces el sistema devuelve la configuracion vigente del folio
```

### HU-02: Rechazar opciones incompatibles o inconsistentes

Como: sistema de cotizacion
Quiero: validar las coberturas solicitadas contra el contexto del folio
Para: evitar calculos tecnicos con terminos invalidos

Prioridad: Alta
Dependencias: catalogos-y-validaciones-core

#### Criterios de aceptacion

```gherkin
Escenario: Rechazar una opcion de cobertura no soportada
  Dado que el usuario intenta guardar una cobertura no disponible en el catalogo aprobado
  Cuando el backend valida la solicitud
  Entonces la operacion es rechazada
  Y el sistema responde con un error funcional entendible
```

## Contratos esperados

- GET /v1/quotes/{folio}/coverage-options
- PUT /v1/quotes/{folio}/coverage-options

## Reglas de negocio

- opcionesCobertura debe quedar versionada como una seccion independiente del agregado.
- Las opciones guardadas deben ser consistentes con las garantias y catalogos aprobados.
- Guardar coberturas no debe sobrescribir otras secciones de la cotizacion.
- Los cambios sobre coberturas deben reflejarse en el siguiente calculo del folio.

## Fuera de alcance inicial

- Emision de clausulados o documentos comerciales finales.
# Requerimiento: Estado y Progreso de Cotizacion

## Objetivo

Permitir consultar el estadoCotizacion y el progreso operativo del folio para orientar al usuario sobre que secciones estan completas, cuales faltan y que ubicaciones ya pueden calcularse.

## Alcance

- Consultar el estado global de la cotizacion.
- Exponer el avance por seccion funcional.
- Informar ubicaciones calculables, ubicaciones incompletas y alertas vigentes.
- Exponer version y fechaUltimaActualizacion para soporte de concurrencia y trazabilidad.

## Historias de Usuario

### HU-01: Visualizar el progreso del folio

Como: usuario del cotizador
Quiero: consultar el estado y progreso del folio
Para: saber que me falta capturar antes de ejecutar el calculo

Prioridad: Alta
Dependencias: datos-generales-de-cotizacion, configuracion-de-layout-de-ubicaciones, gestion-de-ubicaciones, opciones-de-cobertura

#### Criterios de aceptacion

```gherkin
Escenario: Consultar el estado consolidado de la cotizacion
  Dado que existe una cotizacion con informacion parcial o completa
  Cuando el usuario consulta el estado del folio
  Entonces el sistema devuelve estadoCotizacion
  Y reporta las secciones completadas o pendientes
  Y resume ubicaciones calculables, ubicaciones incompletas y alertas vigentes

Escenario: Reflejar la ultima actualizacion del folio
  Dado que la cotizacion ha recibido cambios funcionales
  Cuando se consulta el estado
  Entonces la respuesta incluye version y fechaUltimaActualizacion actuales
```

### HU-02: Reflejar transiciones de estado coherentes

Como: sistema de cotizacion
Quiero: actualizar el estado global segun el avance real del folio
Para: guiar la operacion y el calculo de forma consistente

Prioridad: Alta
Dependencias: calculo-de-prima-y-resultado-financiero

#### Criterios de aceptacion

```gherkin
Escenario: Pasar de BORRADOR a EN_CAPTURA
  Dado que ya existe un numeroFolio creado
  Cuando el usuario empieza a registrar informacion funcional del folio
  Entonces el estado global deja de ser BORRADOR y refleja captura en progreso

Escenario: Marcar el folio como listo o calculado
  Dado que el folio cumple las condiciones necesarias para calcular o ya fue calculado
  Cuando el sistema consolida el avance real del flujo
  Entonces el estado global se actualiza a LISTA_PARA_CALCULO o CALCULADA segun corresponda
```

## Contratos esperados

- GET /v1/quotes/{folio}/state

## Reglas de negocio

- Los estados globales sugeridos del folio son BORRADOR, EN_CAPTURA, LISTA_PARA_CALCULO y CALCULADA.
- El estado debe derivarse del avance real del agregado y no de un marcador aislado sin trazabilidad.
- version y fechaUltimaActualizacion deben ser visibles en la consulta de estado.
- Una ubicacion incompleta debe impactar el resumen de progreso sin bloquear el resto del folio.

## Fuera de alcance inicial

- Dashboards analiticos ajenos al flujo de cotizacion.
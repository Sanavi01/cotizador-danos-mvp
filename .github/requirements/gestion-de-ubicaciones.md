# Requerimiento: Gestion de Ubicaciones

## Objetivo

Permitir registrar, consultar y editar ubicaciones de riesgo dentro de una cotizacion, admitiendo guardado parcial y validacion individual por ubicacion.

## Alcance

- Consultar la lista de ubicaciones de un folio.
- Guardar una o varias ubicaciones dentro de la misma cotizacion.
- Editar una ubicacion puntual sin sobrescribir las demas.
- Consultar un resumen de ubicaciones para apoyo de navegacion y seguimiento.
- Mantener alertasBloqueantes y estadoValidacion por ubicacion.

## Historias de Usuario

### HU-01: Registrar una o varias ubicaciones

Como: usuario del cotizador
Quiero: capturar una o varias ubicaciones en un mismo folio
Para: representar correctamente los riesgos asegurables de la cotizacion

Prioridad: Alta
Dependencias: configuracion-de-layout-de-ubicaciones, catalogos-y-validaciones-core

#### Criterios de aceptacion

```gherkin
Escenario: Guardar varias ubicaciones en un folio
  Dado que la cotizacion ya tiene numeroFolio y configuracionLayout
  Cuando el usuario envia una o varias ubicaciones validas
  Entonces el sistema las persiste dentro del agregado cotizacion
  Y conserva un indice unico por ubicacion
  Y actualiza version y fechaUltimaActualizacion

Escenario: Consultar las ubicaciones existentes
  Dado que la cotizacion ya tiene ubicaciones guardadas
  Cuando el usuario consulta la seccion de ubicaciones
  Entonces el sistema devuelve la lista actual de ubicaciones del folio
```

### HU-02: Editar una ubicacion puntual

Como: usuario del cotizador
Quiero: modificar una ubicacion especifica
Para: corregir informacion sin afectar el resto de ubicaciones del folio

Prioridad: Alta
Dependencias: configuracion-de-layout-de-ubicaciones

#### Criterios de aceptacion

```gherkin
Escenario: Editar una ubicacion por indice
  Dado que la cotizacion tiene varias ubicaciones registradas
  Cuando el usuario actualiza una ubicacion puntual por su indice
  Entonces el sistema modifica solo esa ubicacion
  Y mantiene intactas las otras ubicaciones del folio

Escenario: Intentar editar una ubicacion inexistente
  Dado que el indice solicitado no existe dentro del folio
  Cuando el usuario intenta actualizarlo
  Entonces el sistema responde con error funcional de no encontrado
```

### HU-03: Guardar una ubicacion incompleta sin bloquear el folio completo

Como: usuario del cotizador
Quiero: poder guardar una ubicacion aun cuando falten datos para calcularla
Para: continuar con la captura del resto del folio

Prioridad: Alta
Dependencias: estado-y-progreso-de-cotizacion

#### Criterios de aceptacion

```gherkin
Escenario: Guardar una ubicacion incompleta
  Dado que una ubicacion tiene datos minimos de captura pero no cumple todas las reglas de calculo
  Cuando el usuario guarda la informacion
  Entonces el sistema persiste la ubicacion
  Y marca alertasBloqueantes y estadoValidacion para esa ubicacion
  Y no impide registrar o editar las demas ubicaciones del folio
```

## Contratos esperados

- GET /v1/quotes/{folio}/locations
- PUT /v1/quotes/{folio}/locations
- PATCH /v1/quotes/{folio}/locations/{indice}
- GET /v1/quotes/{folio}/locations/summary

## Reglas de negocio

- Cada ubicacion se identifica por indice unico dentro de la cotizacion.
- Las ubicaciones pueden guardarse parcialmente.
- Una ubicacion incompleta debe reflejar alertasBloqueantes y estadoValidacion.
- El guardado o edicion de ubicaciones no debe sobrescribir layout, datos generales, coberturas ni resultados financieros.
- Toda actualizacion valida incrementa version y actualiza fechaUltimaActualizacion.

## Fuera de alcance inicial

- Calculo tecnico de prima.
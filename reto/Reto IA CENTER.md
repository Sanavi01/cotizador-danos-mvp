# **Reto Técnico:**

## **Objetivo**

Construir una solución funcional para un cotizador de daños que permita capturar un folio, registrar información general, administrar ubicaciones de riesgo, calcular la prima neta/comercial y mostrar el resultado en una interfaz web.

El reto debe evaluar capacidades de:

* diseño y construcción de backend  
* construcción de frontend  
* integración entre servicios  
* modelado de datos  
* manejo de reglas de negocio  
* calidad de código  
* pruebas unitarias y automatizadas  
* documentación técnica y operativa

## **Contexto del negocio**

La solución representa un cotizador de seguros de daños compuesto por tres bloques:

* **cotizador-danos-web**: SPA para captura y consulta.  
* **plataforma-danos-back**: backend principal que administra la cotización.  
* **plataforma-core-ohs**: servicio de referencia con catálogos, tarifas, agentes, códigos postales y folios.

El flujo esperado es:

1. El usuario crea o recupera un folio.  
2. Captura datos generales de la cotización.  
3. Configura el layout y registra una o múltiples ubicaciones.  
4. El backend consulta catálogos y tarifas técnicas.  
5. El backend calcula la prima por ubicación y la prima total.  
6. El frontend presenta alertas, estados y desglose financiero.

## **Alcance funcional obligatorio**

La solución debe cubrir como mínimo las siguientes capacidades.

### **Backend**

Implementar un backend que:

* cree folios con idempotencia  
* consulte y guarde datos generales de una cotización  
* consulte y guarde la configuración del layout de ubicaciones  
* registre, consulte y edite ubicaciones  
* consulte el estado de la cotización  
* consulte y guarde opciones de cobertura  
* ejecute el cálculo de prima neta y prima comercial  
* persista el resultado financiero sin sobrescribir otras secciones de la cotización  
* maneje versionado optimista en operaciones de edición

**Endpoints mínimos esperados:**

* POST /v1/folios  
* GET /v1/quotes/{folio}/general-info  
* PUT /v1/quotes/{folio}/general-info  
* GET /v1/quotes/{folio}/locations/layout  
* PUT /v1/quotes/{folio}/locations/layout  
* GET /v1/quotes/{folio}/locations  
* PUT /v1/quotes/{folio}/locations  
* PATCH /v1/quotes/{folio}/locations/{índice}  
* GET /v1/quotes/{folio}/locations/summary  
* GET /v1/quotes/{folio}/state  
* GET /v1/quotes/{folio}/coverage-options  
* PUT /v1/quotes/{folio}/coverage-options  
* POST /v1/quotes/{folio}/calculate

### **Frontend**

Implementar una SPA que permita:

* crear o abrir un folio  
* capturar datos generales  
* consultar suscriptores, agentes, giros y códigos postales  
* capturar una o varias ubicaciones  
* editar una ubicación puntual  
* visualizar el progreso y estado del folio  
* configurar opciones de cobertura  
* ejecutar el cálculo  
* mostrar la prima neta, la prima comercial y el desglose por ubicación  
* mostrar alertas de ubicaciones incompletas sin bloquear completamente el folio

**Rutas funcionales mínimas sugeridas:**

* /cotizador  
* /quotes/{folio}/general-info  
* /quotes/{folio}/locations  
* /quotes/{folio}/technical-info  
* /quotes/{folio}/terms-and-conditions

## **Reglas de negocio obligatorias**

La implementación debe respetar estas reglas:

* la cotización se identifica por **numeroFolio**  
* el backend debe persistir la cotización como agregado principal  
* las escrituras deben hacerse por actualización parcial  
* al editar secciones funcionales, debe incrementarse la **versión**  
* debe actualizarse **fechaUltimaActualizacion**  
* el cálculo debe guardar **primaNeta**, **primaComercial** y **primasPorUbicacion** en una misma operación lógica  
* si una ubicación está incompleta, esta ubicación genera alerta, pero no debe impedir calcular las demás  
* una ubicación no debe calcularse si no tiene código postal válido, **giro.claveIncendio** o garantías tarifables

## **Dominio mínimo esperado**

### **Cotización**

La cotización debe contemplar, como mínimo:

* numeroFolio  
* estadoCotizacion  
* datosAsegurado  
* datosConduccion.codigoAgente  
* clasificacionRiesgo  
* tipoNegocio  
* configuracionLayout  
* opcionesCobertura  
* ubicaciones\[\]  
* primaNeta  
* primaComercial  
* primasPorUbicacion\[\]  
* versión  
* metadatos

### **Ubicación**

Cada ubicación debe incluir al menos:

* índice  
* nombreUbicacion  
* direccion  
* codigoPostal  
* estado  
* municipio  
* colonia  
* ciudad  
* tipoConstructivo  
* nivel  
* anioConstruccion  
* giro  
* giro.claveIncendio  
* garantías\[\]  
* zonaCatastrofica  
* alertasBloqueantes  
* estadoValidacion

## **Integración con servicios de referencia**

El backend debe consumir o simular las siguientes capacidades del servicio core:

* catálogo de suscriptores  
* consulta de agente por clave  
* consulta de giros  
* validación y consulta de código postal  
* generación secuencial de folio  
* consulta de catálogos de clasificación de riesgo y garantías  
* consulta de tarifas y factores técnicos

**Endpoints de referencia del servicio core:**

* GET /v1/subscribers  
* GET /v1/agents  
* GET /v1/business-lines  
* GET /v1/zip-codes/{zipCode}  
* POST /v1/zip-codes/validate  
* GET /v1/folios  
* GET /v1/catalogs/risk-classification  
* GET /v1/catalogs/guarantees  
* GET|PUT /v1/tariffs/...

Si no se implementa un servicio real adicional, se acepta un stub, mock server o fixtures versionados siempre que el contrato quede documentado.

## **Cálculo técnico mínimo**

El cálculo de prima debe:

1. Leer la cotización completa por folio.  
2. Leer parámetros globales de cálculo.  
3. Resolver datos técnicos requeridos por ubicación.  
4. Determinar si cada ubicación es calculable o incompleta.  
5. Calcular prima por ubicación.  
6. Consolidar prima neta total.  
7. Derivar prima comercial total.  
8. Persistir el resultado financiero.

**Componentes técnicos que el reto debe contemplar en el desglose:**

* Incendio edificios  
* Incendio contenidos  
* Extensión de cobertura  
* CAT TEV  
* CAT FHM  
* Remoción de escombros  
* Gastos extraordinarios  
* pérdida de rentas  
* BI  
* equipo electrónico  
* robo  
* dinero y valores  
* vidrios  
* anuncios luminosos

No es obligatorio replicar exactamente una fórmula actuarial real si no fue entregada, pero sí debe existir una lógica consistente, trazable y documentada.

## **Datos técnicos y colecciones de referencia**

La solución debe contemplar como mínimo estas fuentes de datos:

* **cotizaciones\_danos**  
* **parametros\_calculo**  
* **tarifas\_incendio**  
* **tarifas\_cat**  
* **tarifa\_fhm**  
* **factores\_equipo\_electronico**  
* **catalogo\_cp\_zonas**  
* **dim\_zona\_tev**  
* **dim\_zona\_fhm**

**Uso esperado por colección:**

* **cotizaciones\_danos**: captura operativa, ubicaciones, coberturas y resultado financiero.  
* **parametros\_calculo**: parámetros globales para convertir prima técnica a comercial.  
* **tarifas\_incendio**: tasas base y metadatos técnicos por giro.  
* **tarifas\_cat**: factores CAT por zona.  
* **tarifa\_fhm**: cuotas FHM por grupo, zona y condición.  
* **factores\_equipo\_electronico**: factor técnico por clase y nivel de zona.  
* **catalogo\_cp\_zonas**: relación entre código postal, zona CAT y nivel técnico.  
* **dim\_zona\_tev y dim\_zona\_fhm**: catálogos de apoyo para normalización.

## **Requerimientos de pruebas**

### **Pruebas unitarias**

Las pruebas unitarias deben cubrir como mínimo el 80% del código.

Incluir pruebas unitarias para:

* casos de uso del backend  
* validaciones de negocio  
* cálculo de prima  
* repositorios o adaptadores críticos con mocks  
* componentes o hooks clave del frontend  
* transformaciones o mapeos relevantes

### **Pruebas automatizadas**

Las pruebas automatizadas requieren como mínimo 3 flujos que el participante considere críticos. Se debe justificar el porqué de la elección.

Incluir pruebas automatizadas para:

* endpoints principales del backend  
* flujo de creación y actualización de folio  
* captura y edición de ubicaciones  
* ejecución del cálculo  
* manejo de ubicaciones incompletas  
* flujo principal del frontend

Se acepta cualquiera de estas estrategias:

* pruebas de integración backend  
* contract tests  
* pruebas end to end  
* combinación de las anteriores

La entrega debe explicar claramente qué cubre cada suite y cómo ejecutarla.

## **Documentación requerida**

La entrega debe incluir como mínimo:

* Descripción de arquitectura  
* Decisiones técnicas relevantes  
* Instrucciones de instalación y ejecución  
* Variables de entorno necesarias  
* contratos API  
* modelo de datos principal  
* Explicación de la lógica de cálculo implementada  
* Estrategia de pruebas  
* Supuestos y limitaciones

## **Entregables esperados**

**Condición obligatoria:** Uso obligatorio de la metodología ASSD.

El participante debe entregar:

* Todos los Specs generados de la metodología ASSD  
* Un video en YouTube de máximo 10 minutos en modo oculto (un video en modo privado causa descalificación). El video debe cubrir los temas de la sección criterios de evaluación y la evidencia funcional del aplicativo.  
* Repositorio de código fuente \- GitLab Sofka  
* Pruebas unitarias  
* Pruebas automatizadas  
* Archivo **README.md** principal  
* Colección de requests o documentación equivalente de APIs  
* Scripts de arranque local  
* Fixtures, mocks o semillas de datos

**Opcional:**

* docker-compose.yml  
* pipeline CI  
* colección Postman o Bruno  
* cobertura de pruebas

## **Criterios de evaluación**

Se evaluará:

* claridad del modelado del dominio  
* separación entre capas y responsabilidades  
* calidad del código  
* consistencia de APIs y manejo de errores  
* experiencia de usuario en frontend  
* cobertura y calidad de pruebas  
* argumentación de los flujos automatizados  
* trazabilidad del cálculo  
* calidad de la documentación  
* facilidad de ejecución local

## **Restricciones y supuestos sugeridos**

Para mantener el reto acotado, se recomienda indicar a los participantes:

* pueden usar stubs o mocks para integraciones externas no previstas  
* pueden simplificar autenticación si no forma parte del objetivo  
* deben priorizar claridad y trazabilidad sobre complejidad innecesaria  
* cualquier fórmula simplificada debe estar documentada  
* cualquier dato no entregado debe resolverse con supuestos explícitos

## **Lista de insumos a proporcionar**

Para que el reto sea resoluble de forma justa, se recomienda entregar a los participantes los siguientes insumos:

* documento funcional del reto  
* documento técnico base del dominio y arquitectura  
* contrato esperado de APIs backend y core  
* ejemplos de request/response por endpoint  
* catálogo de garantías  
* catálogo de clasificación de riesgo  
* catálogo de suscriptores  
* catálogo de agentes o regla de consulta de agentes  
* catálogo de giros con **claveIncendio**  
* catálogo o dataset de códigos postales y zonas  
* dataset mínimo de **parametros\_calculo**  
* dataset mínimo de **tarifas\_incendio**  
* dataset mínimo de **tarifas\_cat**  
* dataset mínimo de **tarifa\_fhm**  
* dataset mínimo de **factores\_equipo\_electronico**  
* ejemplos de documentos de **cotizaciones\_danos**  
* reglas de negocio obligatorias  
* criterios de aceptación  
* criterios de evaluación  
* definición de qué flujos deben quedar demostrables  
* instrucciones de entrega  
* fecha límite y formato de entrega

## **Escenario de aceptación sugerido**

La solución debe permitir demostrar este escenario mínimo:

1. Crear un folio nuevo.  
2. Capturar datos generales.  
3. Definir layout de ubicaciones.  
4. Registrar al menos dos ubicaciones.  
5. Dejar una ubicación completa y una incompleta.  
6. Configurar opciones de cobertura.  
7. Ejecutar el cálculo.  
8. Ver prima calculada para la ubicación válida.  
9. Ver alerta para la ubicación incompleta.  
10. Consultar el estado final del folio.

## **Resultado esperado del reto**

Al finalizar, el participante debe entregar una solución ejecutable y documentada que demuestre capacidad para construir un flujo integral de cotización con backend, frontend, persistencia, integración, pruebas y documentación técnica.

* .


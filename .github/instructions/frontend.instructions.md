---
applyTo: "cotizador-danos-web/src/**/*.{js,jsx}"
---

> **Scope**: Se aplica a proyectos con capa frontend. Si el proyecto es backend-only, este archivo no tiene efecto. Si el frontend usa otro framework (Vue, Angular, Svelte, etc.), adaptar las convenciones de componentes, rutas y estado al stack real.

# Instrucciones para Archivos de Frontend (React/Vite)

## Convenciones Obligatorias

- **CSS**: SIEMPRE usar CSS Modules (`*.module.css`) — NUNCA clases CSS globales ni frameworks como Tailwind/Bootstrap.
- **Nombres**: PascalCase para componentes y páginas (`.jsx`), camelCase para hooks (`.js`) y servicios (`.js`).
- **Sesión demo**: mantener una única fuente de verdad para la sesión simulada (`useDemoSession`, contexto o store equivalente) — nunca crear estado paralelo.
- **Env vars**: SIEMPRE con prefijo `VITE_` para que Vite las exponga.
- **Contrato API**: consumir rutas `/v1/...` y envelope `data`.

## Estructura de Archivos

```
src/
  context/                ← sesión demo y estado transversal
  hooks/                  ← hooks por capability
  services/               ← axios clients y servicios por módulo
  components/ProtectedRoute.jsx
  pages/                  ← PageName.jsx + PageName.module.css
```

## Llamadas a la API Backend

Usar siempre **Axios** (no `fetch`). Las llamadas van en `services/`, nunca directamente en componentes o páginas.

```js
// services/featureService.js
import axios from 'axios';
const API_BASE = import.meta.env.VITE_API_URL;

export async function getQuoteState(folio) {
  const res = await axios.get(`${API_BASE}/v1/quotes/${folio}/state`);
  return res.data.data;
}

export async function createFolio(payload, idempotencyKey) {
  const res = await axios.post(`${API_BASE}/v1/folios`, payload, {
    headers: { 'Idempotency-Key': idempotencyKey }
  });
  return res.data.data;
}
```

## Rutas (React Router v6)

Las rutas se registran en `src/App.jsx`:
```jsx
<Route path="/cotizador" element={<CotizadorPage />} />
<Route path="/quotes/:folio/general-info" element={<ProtectedRoute><GeneralInfoPage /></ProtectedRoute>} />
```

## Componentes

- Un componente por archivo.
- Props tipadas con JSDoc si son complejas.
- No lógica de negocio en los componentes — delegar a hooks o servicios.
- La UI debe mostrar progreso, alertas por ubicación y desglose financiero sin bloquear el resto del folio.

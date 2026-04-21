import { createApiClient, executeWithRetry, type ApiEnvelope } from './httpClient'

export type LocationsLayoutMode = 'UNICA' | 'MULTIPLE'

export interface LocationsLayoutSlot {
  indice: number
  ordenCaptura: number
}

export interface LocationsLayoutConfiguration {
  modoCaptura: LocationsLayoutMode | null
  cantidadUbicaciones: number | null
  ubicaciones: LocationsLayoutSlot[]
}

interface ApiLocationsLayoutConfiguration {
  modoCaptura: LocationsLayoutMode | null
  cantidadUbicaciones: number | null
  ubicaciones: LocationsLayoutSlot[] | null
}

interface ApiLocationsLayoutResponse {
  numeroFolio: string
  version: number
  fechaUltimaActualizacion: string
  configuracionLayout: ApiLocationsLayoutConfiguration
}

export interface LocationsLayoutRecord {
  numeroFolio: string
  version: number
  fechaUltimaActualizacion: string
  configuracionLayout: LocationsLayoutConfiguration
}

export interface LocationsLayoutPayload {
  version: number
  configuracionLayout: {
    modoCaptura: LocationsLayoutMode
    cantidadUbicaciones: number
    ubicaciones: LocationsLayoutSlot[]
  }
}

function normalizeSlots(slots: LocationsLayoutSlot[] | null | undefined): LocationsLayoutSlot[] {
  if (!Array.isArray(slots)) {
    return []
  }

  return slots.map((slot) => ({
    indice: slot.indice,
    ordenCaptura: slot.ordenCaptura,
  }))
}

function normalizeLayoutConfiguration(configuration: ApiLocationsLayoutConfiguration): LocationsLayoutConfiguration {
  return {
    modoCaptura: configuration.modoCaptura,
    cantidadUbicaciones: configuration.cantidadUbicaciones,
    ubicaciones: normalizeSlots(configuration.ubicaciones),
  }
}

function normalizeLayoutResponse(response: ApiLocationsLayoutResponse): LocationsLayoutRecord {
  return {
    numeroFolio: response.numeroFolio,
    version: response.version,
    fechaUltimaActualizacion: response.fechaUltimaActualizacion,
    configuracionLayout: normalizeLayoutConfiguration(response.configuracionLayout),
  }
}

function createLayoutClient() {
  return createApiClient('VITE_API_URL', 'Define VITE_API_URL para conectar con el backend principal.')
}

function getLayoutEnvelope<T>(request: () => Promise<{ data: ApiEnvelope<T> }>): Promise<T> {
  return executeWithRetry(request).then((response) => response.data.data)
}

export function createEmptyLocationsLayout(folio: string): LocationsLayoutRecord {
  return {
    numeroFolio: folio,
    version: 0,
    fechaUltimaActualizacion: '',
    configuracionLayout: {
      modoCaptura: null,
      cantidadUbicaciones: null,
      ubicaciones: [],
    },
  }
}

export async function getLocationsLayout(folio: string): Promise<LocationsLayoutRecord> {
  const response = await getLayoutEnvelope(() =>
    createLayoutClient().get<ApiEnvelope<ApiLocationsLayoutResponse>>(
      `/v1/quotes/${encodeURIComponent(folio)}/locations/layout`,
    ),
  )

  return normalizeLayoutResponse(response)
}

export async function updateLocationsLayout(
  folio: string,
  payload: LocationsLayoutPayload,
): Promise<LocationsLayoutRecord> {
  const response = await getLayoutEnvelope(() =>
    createLayoutClient().put<ApiEnvelope<ApiLocationsLayoutResponse>>(
      `/v1/quotes/${encodeURIComponent(folio)}/locations/layout`,
      payload,
    ),
  )

  return normalizeLayoutResponse(response)
}
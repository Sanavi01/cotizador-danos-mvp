import { createApiClient, executeWithRetry, type ApiEnvelope } from './httpClient'
import type { ValidationAlert } from './referenceCoreService'

export type LocationValidationState = 'EMPTY' | 'INCOMPLETE' | 'INVALID' | 'VALID' | 'CALCULABLE'

export interface LocationGiro {
  codigo: string | null
  nombre: string | null
  claveIncendio: string | null
}

export interface LocationGarantia {
  garantiaCode: string | null
  origen: string | null
  tariffablePreview: boolean | null
}

export interface LocationZonaCatastrofica {
  zonaTev: string | null
  zonaFhm: string | null
}

export interface LocationRecord {
  indice: number
  nombreUbicacion: string | null
  direccion: string | null
  codigoPostal: string | null
  estado: string | null
  municipio: string | null
  colonia: string | null
  ciudad: string | null
  tipoConstructivo: string | null
  nivel: number | null
  anioConstruccion: number | null
  giro: LocationGiro | null
  garantias: LocationGarantia[]
  zonaCatastrofica: LocationZonaCatastrofica | null
  estadoValidacion: LocationValidationState
  alertasBloqueantes: ValidationAlert[]
  createdAt: string | null
  updatedAt: string | null
}

export interface LocationSummaryByIndex {
  indice: number
  slotEsperado: boolean
  estadoValidacion: LocationValidationState
  tieneAlertasBloqueantes: boolean
}

export interface LocationSummaryRecord {
  numeroFolio: string
  totalEsperado: number
  totalActual: number
  calculables: number
  incompletas: number
  invalidas: number
  conAlertas: number
  resumenPorIndice: LocationSummaryByIndex[]
}

export interface LocationCollectionRecord {
  numeroFolio: string
  version: number
  fechaUltimaActualizacion: string
  ubicaciones: LocationRecord[]
}

export interface LocationDraft {
  indice: number
  nombreUbicacion: string | null
  direccion: string | null
  codigoPostal: string | null
  estado: string | null
  municipio: string | null
  colonia: string | null
  ciudad: string | null
  tipoConstructivo: string | null
  nivel: number | null
  anioConstruccion: number | null
  giro: LocationGiro | null
  zonaCatastrofica: LocationZonaCatastrofica | null
}

export interface LocationChanges {
  nombreUbicacion?: string | null
  direccion?: string | null
  codigoPostal?: string | null
  estado?: string | null
  municipio?: string | null
  colonia?: string | null
  ciudad?: string | null
  tipoConstructivo?: string | null
  nivel?: number | null
  anioConstruccion?: number | null
  giro?: LocationGiro | null
  zonaCatastrofica?: LocationZonaCatastrofica | null
}

export interface UpdateLocationsPayload {
  version: number
  ubicaciones: LocationDraft[]
}

export interface UpdateLocationPayload {
  version: number
  changes: LocationChanges
}

interface ApiLocationGiro {
  codigo: string | null
  nombre: string | null
  claveIncendio: string | null
}

interface ApiLocationGarantia {
  garantiaCode: string | null
  origen: string | null
  tariffablePreview: boolean | null
}

interface ApiLocationZonaCatastrofica {
  zonaTev: string | null
  zonaFhm: string | null
}

interface ApiLocationRecord {
  indice: number
  nombreUbicacion: string | null
  direccion: string | null
  codigoPostal: string | null
  estado: string | null
  municipio: string | null
  colonia: string | null
  ciudad: string | null
  tipoConstructivo: string | null
  nivel: number | null
  anioConstruccion: number | null
  giro: ApiLocationGiro | null
  garantias: ApiLocationGarantia[] | null
  zonaCatastrofica: ApiLocationZonaCatastrofica | null
  estadoValidacion: LocationValidationState | null
  alertasBloqueantes: ValidationAlert[] | null
  createdAt: string | null
  updatedAt: string | null
}

interface ApiLocationCollectionResponse {
  numeroFolio: string
  version: number
  fechaUltimaActualizacion: string
  ubicaciones: ApiLocationRecord[] | null
}

interface ApiLocationMutationResponse {
  numeroFolio: string
  version: number
  fechaUltimaActualizacion: string
  ubicacion: ApiLocationRecord
}

interface ApiLocationSummaryByIndex {
  indice: number
  slotEsperado: boolean
  estadoValidacion: LocationValidationState | null
  tieneAlertasBloqueantes: boolean
}

interface ApiLocationSummaryResponse {
  numeroFolio: string
  totalEsperado: number
  totalActual: number
  calculables: number
  incompletas: number
  invalidas: number
  conAlertas: number
  resumenPorIndice: ApiLocationSummaryByIndex[] | null
}

function createLocationsClient() {
  return createApiClient('VITE_API_URL', 'Define VITE_API_URL para conectar con el backend principal.')
}

function getEnvelopeData<T>(request: () => Promise<{ data: ApiEnvelope<T> }>): Promise<T> {
  return executeWithRetry(request).then((response) => response.data.data)
}

function normalizeString(value: string | null | undefined): string | null {
  return value ?? null
}

function normalizeNumber(value: number | null | undefined): number | null {
  return typeof value === 'number' && Number.isFinite(value) ? value : null
}

function normalizeGiro(giro: ApiLocationGiro | null | undefined): LocationGiro | null {
  if (!giro) {
    return null
  }

  return {
    codigo: normalizeString(giro.codigo),
    nombre: normalizeString(giro.nombre),
    claveIncendio: normalizeString(giro.claveIncendio),
  }
}

function normalizeGarantias(garantias: ApiLocationGarantia[] | null | undefined): LocationGarantia[] {
  if (!Array.isArray(garantias)) {
    return []
  }

  return garantias.map((garantia) => ({
    garantiaCode: normalizeString(garantia.garantiaCode),
    origen: normalizeString(garantia.origen),
    tariffablePreview: Boolean(garantia.tariffablePreview),
  }))
}

function normalizeZonaCatastrofica(
  zonaCatastrofica: ApiLocationZonaCatastrofica | null | undefined,
): LocationZonaCatastrofica | null {
  if (!zonaCatastrofica) {
    return null
  }

  return {
    zonaTev: normalizeString(zonaCatastrofica.zonaTev),
    zonaFhm: normalizeString(zonaCatastrofica.zonaFhm),
  }
}

function normalizeEstadoValidacion(value: LocationValidationState | null | undefined): LocationValidationState {
  return value ?? 'EMPTY'
}

function normalizeAlerts(alerts: ValidationAlert[] | null | undefined): ValidationAlert[] {
  if (!Array.isArray(alerts)) {
    return []
  }

  return alerts.map((alert) => ({
    codigo: alert.codigo,
    mensaje: alert.mensaje,
    severidad: alert.severidad,
  }))
}

function normalizeLocation(location: ApiLocationRecord): LocationRecord {
  return {
    indice: location.indice,
    nombreUbicacion: normalizeString(location.nombreUbicacion),
    direccion: normalizeString(location.direccion),
    codigoPostal: normalizeString(location.codigoPostal),
    estado: normalizeString(location.estado),
    municipio: normalizeString(location.municipio),
    colonia: normalizeString(location.colonia),
    ciudad: normalizeString(location.ciudad),
    tipoConstructivo: normalizeString(location.tipoConstructivo),
    nivel: normalizeNumber(location.nivel),
    anioConstruccion: normalizeNumber(location.anioConstruccion),
    giro: normalizeGiro(location.giro),
    garantias: normalizeGarantias(location.garantias),
    zonaCatastrofica: normalizeZonaCatastrofica(location.zonaCatastrofica),
    estadoValidacion: normalizeEstadoValidacion(location.estadoValidacion),
    alertasBloqueantes: normalizeAlerts(location.alertasBloqueantes),
    createdAt: normalizeString(location.createdAt),
    updatedAt: normalizeString(location.updatedAt),
  }
}

function normalizeCollectionResponse(response: ApiLocationCollectionResponse): LocationCollectionRecord {
  return {
    numeroFolio: response.numeroFolio,
    version: response.version,
    fechaUltimaActualizacion: response.fechaUltimaActualizacion,
    ubicaciones: Array.isArray(response.ubicaciones) ? response.ubicaciones.map(normalizeLocation) : [],
  }
}

function normalizeMutationResponse(response: ApiLocationMutationResponse) {
  return {
    numeroFolio: response.numeroFolio,
    version: response.version,
    fechaUltimaActualizacion: response.fechaUltimaActualizacion,
    ubicacion: normalizeLocation(response.ubicacion),
  }
}

function normalizeSummaryResponse(response: ApiLocationSummaryResponse): LocationSummaryRecord {
  return {
    numeroFolio: response.numeroFolio,
    totalEsperado: response.totalEsperado,
    totalActual: response.totalActual,
    calculables: response.calculables,
    incompletas: response.incompletas,
    invalidas: response.invalidas,
    conAlertas: response.conAlertas,
    resumenPorIndice: Array.isArray(response.resumenPorIndice)
      ? response.resumenPorIndice.map((item) => ({
          indice: item.indice,
          slotEsperado: item.slotEsperado,
          estadoValidacion: normalizeEstadoValidacion(item.estadoValidacion),
          tieneAlertasBloqueantes: item.tieneAlertasBloqueantes,
        }))
      : [],
  }
}

export function createEmptyLocation(indice: number): LocationRecord {
  return {
    indice,
    nombreUbicacion: null,
    direccion: null,
    codigoPostal: null,
    estado: null,
    municipio: null,
    colonia: null,
    ciudad: null,
    tipoConstructivo: null,
    nivel: null,
    anioConstruccion: null,
    giro: null,
    garantias: [],
    zonaCatastrofica: null,
    estadoValidacion: 'EMPTY',
    alertasBloqueantes: [],
    createdAt: null,
    updatedAt: null,
  }
}

export function toLocationDraft(location: LocationRecord): LocationDraft {
  return {
    indice: location.indice,
    nombreUbicacion: location.nombreUbicacion,
    direccion: location.direccion,
    codigoPostal: location.codigoPostal,
    estado: location.estado,
    municipio: location.municipio,
    colonia: location.colonia,
    ciudad: location.ciudad,
    tipoConstructivo: location.tipoConstructivo,
    nivel: location.nivel,
    anioConstruccion: location.anioConstruccion,
    giro: location.giro,
    zonaCatastrofica: location.zonaCatastrofica,
  }
}

export async function getLocations(folio: string): Promise<LocationCollectionRecord> {
  const response = await getEnvelopeData(() =>
    createLocationsClient().get<ApiEnvelope<ApiLocationCollectionResponse>>(`/v1/quotes/${encodeURIComponent(folio)}/locations`),
  )

  return normalizeCollectionResponse(response)
}

export async function updateLocations(folio: string, payload: UpdateLocationsPayload): Promise<LocationCollectionRecord> {
  const response = await getEnvelopeData(() =>
    createLocationsClient().put<ApiEnvelope<ApiLocationCollectionResponse>>(
      `/v1/quotes/${encodeURIComponent(folio)}/locations`,
      payload,
    ),
  )

  return normalizeCollectionResponse(response)
}

export async function updateLocation(
  folio: string,
  indice: number,
  payload: UpdateLocationPayload,
): Promise<ReturnType<typeof normalizeMutationResponse>> {
  const response = await getEnvelopeData(() =>
    createLocationsClient().patch<ApiEnvelope<ApiLocationMutationResponse>>(
      `/v1/quotes/${encodeURIComponent(folio)}/locations/${encodeURIComponent(String(indice))}`,
      payload,
    ),
  )

  return normalizeMutationResponse(response)
}

export async function getLocationsSummary(folio: string): Promise<LocationSummaryRecord> {
  const response = await getEnvelopeData(() =>
    createLocationsClient().get<ApiEnvelope<ApiLocationSummaryResponse>>(
      `/v1/quotes/${encodeURIComponent(folio)}/locations/summary`,
    ),
  )

  return normalizeSummaryResponse(response)
}
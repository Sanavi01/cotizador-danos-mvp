import { createApiClient, executeWithRetry, type ApiEnvelope } from './httpClient'

export interface ReferenceCatalogItem {
  codigo: string
  nombre: string
  activo: boolean
  claveIncendio?: string
}

export interface ValidationAlert {
  codigo: string
  mensaje: string
  severidad: 'Info' | 'Warning' | 'Error'
}

export interface ZipCodeValidation {
  zipCode: string
  valido: boolean
  municipio: string | null
  estado: string | null
  coloniaBarrio: string | null
  zona_tev: string | null
  zona_fhm: string | null
  alertas: ValidationAlert[]
}

export interface TariffDetails {
  tariffKey: string
  giroCode: string
  zonaCode: string
  garantiaCode: string
  rate: number
  factor: number
  moneda: 'COP' | string
  vigenciaDesde: string
  vigenciaHasta: string
}

export interface FolioSequenceResponse {
  nextNumeroFolio: string
  fuente: string
  vigente: boolean
}

export interface ZipCodeValidationPayload {
  zipCode: string
}

async function getEnvelopeData<T>(request: () => Promise<{ data: ApiEnvelope<T> }>): Promise<T> {
  const response = await executeWithRetry(request)
  return response.data.data
}

export async function listSubscribers(): Promise<ReferenceCatalogItem[]> {
  return getEnvelopeData(() =>
    createApiClient('VITE_REFERENCE_CORE_API_URL', 'Define VITE_REFERENCE_CORE_API_URL para conectar con el mock core.')
      .get<ApiEnvelope<ReferenceCatalogItem[]>>('/v1/subscribers'),
  )
}

export async function listAgents(): Promise<ReferenceCatalogItem[]> {
  return getEnvelopeData(() =>
    createApiClient('VITE_REFERENCE_CORE_API_URL', 'Define VITE_REFERENCE_CORE_API_URL para conectar con el mock core.')
      .get<ApiEnvelope<ReferenceCatalogItem[]>>('/v1/agents'),
  )
}

export async function listBusinessLines(): Promise<ReferenceCatalogItem[]> {
  return getEnvelopeData(() =>
    createApiClient('VITE_REFERENCE_CORE_API_URL', 'Define VITE_REFERENCE_CORE_API_URL para conectar con el mock core.')
      .get<ApiEnvelope<ReferenceCatalogItem[]>>('/v1/business-lines'),
  )
}

export async function getZipCode(zipCode: string): Promise<ZipCodeValidation> {
  return getEnvelopeData(() =>
    createApiClient('VITE_REFERENCE_CORE_API_URL', 'Define VITE_REFERENCE_CORE_API_URL para conectar con el mock core.')
      .get<ApiEnvelope<ZipCodeValidation>>(`/v1/zip-codes/${encodeURIComponent(zipCode)}`),
  )
}

export async function validateZipCode(payload: ZipCodeValidationPayload): Promise<ZipCodeValidation> {
  return getEnvelopeData(() =>
    createApiClient('VITE_REFERENCE_CORE_API_URL', 'Define VITE_REFERENCE_CORE_API_URL para conectar con el mock core.')
      .post<ApiEnvelope<ZipCodeValidation>>('/v1/zip-codes/validate', payload),
  )
}

export async function listRiskClassification(): Promise<ReferenceCatalogItem[]> {
  return getEnvelopeData(() =>
    createApiClient('VITE_REFERENCE_CORE_API_URL', 'Define VITE_REFERENCE_CORE_API_URL para conectar con el mock core.')
      .get<ApiEnvelope<ReferenceCatalogItem[]>>('/v1/catalogs/risk-classification'),
  )
}

export async function listGuarantees(): Promise<ReferenceCatalogItem[]> {
  return getEnvelopeData(() =>
    createApiClient('VITE_REFERENCE_CORE_API_URL', 'Define VITE_REFERENCE_CORE_API_URL para conectar con el mock core.')
      .get<ApiEnvelope<ReferenceCatalogItem[]>>('/v1/catalogs/guarantees'),
  )
}

export async function getTariff(tariffKey: string): Promise<TariffDetails> {
  return getEnvelopeData(() =>
    createApiClient('VITE_REFERENCE_CORE_API_URL', 'Define VITE_REFERENCE_CORE_API_URL para conectar con el mock core.')
      .get<ApiEnvelope<TariffDetails>>(`/v1/tariffs/${encodeURIComponent(tariffKey)}`),
  )
}

export async function getFolioSequence(): Promise<FolioSequenceResponse> {
  return getEnvelopeData(() =>
    createApiClient('VITE_REFERENCE_CORE_API_URL', 'Define VITE_REFERENCE_CORE_API_URL para conectar con el mock core.')
      .get<ApiEnvelope<FolioSequenceResponse>>('/v1/folios'),
  )
}

import { createApiClient, executeWithRetry, type ApiEnvelope } from './httpClient'

export type CoverageTechnicalSourcePreview =
  | 'CORE_TARIFF'
  | 'FIRE_TARIFF'
  | 'CAT_TARIFF'
  | 'FHM_TARIFF'
  | 'ELECTRONIC_FACTOR'
  | 'UNRESOLVED'

export interface CoverageSelection {
  garantiaCode: string
  terminos: string[]
}

export interface CoverageOptionsConfiguration {
  garantiasSeleccionadas: CoverageSelection[]
  observaciones: string | null
}

export interface CoverageGuaranteeProjection {
  garantiaCode: string
  tariffablePreview: boolean
  fuenteTecnicaPreview: CoverageTechnicalSourcePreview
  lookupKeyPreview: string | null
  motivosNoTarifable: string[]
}

export interface CoverageLocationProjection {
  indice: number
  garantiasDerivadas: CoverageGuaranteeProjection[]
  calculablePreview: boolean
}

interface ApiCoverageSelection {
  garantiaCode?: string | null
  terminos?: string[] | null
}

interface ApiCoverageOptionsConfiguration {
  garantiasSeleccionadas?: ApiCoverageSelection[] | null
  observaciones?: string | null
}

interface ApiCoverageGuaranteeProjection {
  garantiaCode?: string | null
  tariffablePreview?: boolean | null
  fuenteTecnicaPreview?: string | null
  lookupKeyPreview?: string | null
  motivosNoTarifable?: string[] | null
}

interface ApiCoverageLocationProjection {
  indice?: number | null
  garantiasDerivadas?: ApiCoverageGuaranteeProjection[] | null
  calculablePreview?: boolean | null
}

interface ApiCoverageOptionsResponse {
  numeroFolio: string
  version: number
  fechaUltimaActualizacion: string
  opcionesCobertura?: ApiCoverageOptionsConfiguration | null
  projectionPerLocation?: ApiCoverageLocationProjection[] | null
}

export interface CoverageOptionsRecord {
  numeroFolio: string
  version: number
  fechaUltimaActualizacion: string
  opcionesCobertura: CoverageOptionsConfiguration
  projectionPerLocation: CoverageLocationProjection[]
}

export interface CoverageOptionsPayload {
  version: number
  opcionesCobertura: CoverageOptionsConfiguration
}

const allowedTechnicalSources: CoverageTechnicalSourcePreview[] = [
  'CORE_TARIFF',
  'FIRE_TARIFF',
  'CAT_TARIFF',
  'FHM_TARIFF',
  'ELECTRONIC_FACTOR',
  'UNRESOLVED',
]

function createCoverageClient() {
  return createApiClient('VITE_API_URL', 'Define VITE_API_URL para conectar con el backend principal.')
}

function getCoverageEnvelopeData<T>(request: () => Promise<{ data: ApiEnvelope<T> }>): Promise<T> {
  return executeWithRetry(request).then((response) => response.data.data)
}

function normalizeOptionalText(value: string | null | undefined): string | null {
  if (typeof value !== 'string') {
    return null
  }

  const trimmedValue = value.trim()

  return trimmedValue.length > 0 ? trimmedValue : null
}

function normalizeTerminos(terminos: string[] | null | undefined): string[] {
  if (!Array.isArray(terminos)) {
    return []
  }

  return terminos.map((termino) => termino.trim()).filter((termino) => termino.length > 0)
}

function normalizeSelection(selection: ApiCoverageSelection): CoverageSelection {
  return {
    garantiaCode: typeof selection.garantiaCode === 'string' ? selection.garantiaCode.trim() : '',
    terminos: normalizeTerminos(selection.terminos),
  }
}

function normalizeCoverageConfiguration(configuration: ApiCoverageOptionsConfiguration | null | undefined): CoverageOptionsConfiguration {
  if (!configuration) {
    return {
      garantiasSeleccionadas: [],
      observaciones: null,
    }
  }

  return {
    garantiasSeleccionadas: Array.isArray(configuration.garantiasSeleccionadas)
      ? configuration.garantiasSeleccionadas.map(normalizeSelection)
      : [],
    observaciones: normalizeOptionalText(configuration.observaciones),
  }
}

function normalizeTechnicalSourcePreview(value: string | null | undefined): CoverageTechnicalSourcePreview {
  if (typeof value === 'string' && allowedTechnicalSources.includes(value as CoverageTechnicalSourcePreview)) {
    return value as CoverageTechnicalSourcePreview
  }

  return 'UNRESOLVED'
}

function normalizeGuaranteeProjection(projection: ApiCoverageGuaranteeProjection | null | undefined): CoverageGuaranteeProjection {
  return {
    garantiaCode: typeof projection?.garantiaCode === 'string' ? projection.garantiaCode.trim() : '',
    tariffablePreview: Boolean(projection?.tariffablePreview),
    fuenteTecnicaPreview: normalizeTechnicalSourcePreview(projection?.fuenteTecnicaPreview),
    lookupKeyPreview: normalizeOptionalText(projection?.lookupKeyPreview),
    motivosNoTarifable: Array.isArray(projection?.motivosNoTarifable)
      ? projection.motivosNoTarifable.map((reason) => reason.trim()).filter((reason) => reason.length > 0)
      : [],
  }
}

function normalizeLocationProjection(projection: ApiCoverageLocationProjection | null | undefined): CoverageLocationProjection {
  return {
    indice: typeof projection?.indice === 'number' && Number.isFinite(projection.indice) ? projection.indice : 0,
    garantiasDerivadas: Array.isArray(projection?.garantiasDerivadas)
      ? projection.garantiasDerivadas.map(normalizeGuaranteeProjection)
      : [],
    calculablePreview: Boolean(projection?.calculablePreview),
  }
}

function normalizeCoverageResponse(response: ApiCoverageOptionsResponse): CoverageOptionsRecord {
  return {
    numeroFolio: response.numeroFolio,
    version: response.version,
    fechaUltimaActualizacion: response.fechaUltimaActualizacion,
    opcionesCobertura: normalizeCoverageConfiguration(response.opcionesCobertura),
    projectionPerLocation: Array.isArray(response.projectionPerLocation)
      ? response.projectionPerLocation.map(normalizeLocationProjection)
      : [],
  }
}

export function createEmptyCoverageOptions(folio: string): CoverageOptionsRecord {
  return {
    numeroFolio: folio,
    version: 0,
    fechaUltimaActualizacion: '',
    opcionesCobertura: {
      garantiasSeleccionadas: [],
      observaciones: null,
    },
    projectionPerLocation: [],
  }
}

export async function getCoverageOptions(folio: string): Promise<CoverageOptionsRecord> {
  const response = await getCoverageEnvelopeData(() =>
    createCoverageClient().get<ApiEnvelope<ApiCoverageOptionsResponse>>(
      `/v1/quotes/${encodeURIComponent(folio)}/coverage-options`,
    ),
  )

  return normalizeCoverageResponse(response)
}

export async function updateCoverageOptions(
  folio: string,
  payload: CoverageOptionsPayload,
): Promise<CoverageOptionsRecord> {
  const response = await getCoverageEnvelopeData(() =>
    createCoverageClient().put<ApiEnvelope<ApiCoverageOptionsResponse>>(
      `/v1/quotes/${encodeURIComponent(folio)}/coverage-options`,
      payload,
    ),
  )

  return normalizeCoverageResponse(response)
}
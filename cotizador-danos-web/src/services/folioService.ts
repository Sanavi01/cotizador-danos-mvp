import { createApiClient, executeWithRetry, type ApiEnvelope } from './httpClient'
import type { ValidationAlert } from './referenceCoreService'

export interface CreateFolioPayload {
  origin: string
}

export interface CreateFolioResponse {
  numeroFolio: string
  estadoCotizacion: string
  version: number
  fechaUltimaActualizacion: string
}

export type QuoteProgressStatus = 'COMPLETED' | 'INCOMPLETE'

export interface QuoteProgressSummary {
  datosGenerales: QuoteProgressStatus
  layoutUbicaciones: QuoteProgressStatus
  ubicaciones: QuoteProgressStatus
  opcionesCobertura: QuoteProgressStatus
}

export interface QuoteLocationsSummary {
  totalEsperado: number
  totalActual: number
  calculables: number
  incompletas: number
  invalidas: number
  conAlertas: number
}

export interface QuoteFinancialSummary {
  primaNeta: number
  primaComercial: number
  ubicacionesCalculadas: number
  ubicacionesNoCalculables: number
  estadoCalculo: 'CALCULADO' | 'PARCIAL' | 'RECHAZADO'
  calculatedAt: string
  calculationParameterVersion: string
}

export interface QuoteStateResponse {
  numeroFolio: string
  estadoCotizacion: string
  version: number
  fechaUltimaActualizacion: string
  progreso: QuoteProgressSummary
  resumenUbicaciones: QuoteLocationsSummary
  tieneAlertas: boolean
  alertasVigentes: ValidationAlert[]
  readyToCalculate: boolean
  resultadoFinanciero: QuoteFinancialSummary | null
}

export async function createFolio(
  payload: CreateFolioPayload,
  idempotencyKey: string,
): Promise<CreateFolioResponse> {
  const client = createApiClient('VITE_API_URL', 'Define VITE_API_URL para conectar con el backend principal.')

  const response = await executeWithRetry(() =>
    client.post<ApiEnvelope<CreateFolioResponse>>('/v1/folios', payload, {
      headers: {
        'Idempotency-Key': idempotencyKey,
      },
    }),
  )

  return response.data.data
}

export async function getQuoteState(folio: string): Promise<QuoteStateResponse> {
  const client = createApiClient('VITE_API_URL', 'Define VITE_API_URL para conectar con el backend principal.')

  const response = await executeWithRetry(() =>
    client.get<ApiEnvelope<QuoteStateResponse>>(`/v1/quotes/${encodeURIComponent(folio)}/state`),
  )

  return response.data.data
}
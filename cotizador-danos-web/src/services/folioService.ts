import { createApiClient, executeWithRetry, type ApiEnvelope } from './httpClient'

export interface CreateFolioPayload {
  origin: string
}

export interface CreateFolioResponse {
  numeroFolio: string
  estadoCotizacion: string
  version: number
  fechaUltimaActualizacion: string
}

export interface QuoteStateResponse {
  numeroFolio: string
  estadoCotizacion: string
  tieneAlertas: boolean
  seccionesCompletadas: string[]
  ubicacionesCalculables: number
  ubicacionesIncompletas: number
  version: number
  fechaUltimaActualizacion: string
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
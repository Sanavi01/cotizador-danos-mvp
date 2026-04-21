import axios, { type AxiosInstance } from 'axios'

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

interface ApiEnvelope<T> {
  data: T
}

interface ProblemDetails {
  title?: string
  detail?: string
}

const apiTimeoutMs = 10000
const retryDelayMs = 250
const retryAttempts = 2

function getApiBaseUrl(): string {
  const apiBaseUrl = import.meta.env.VITE_API_URL as string | undefined

  if (!apiBaseUrl) {
    throw new Error('Define VITE_API_URL para conectar con el backend.')
  }

  return apiBaseUrl
}

function createClient(): AxiosInstance {
  return axios.create({
    baseURL: getApiBaseUrl(),
    timeout: apiTimeoutMs,
    headers: {
      'Content-Type': 'application/json',
    },
  })
}

function getProblemMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const responseData = error.response?.data as ProblemDetails | undefined

    return responseData?.detail ?? responseData?.title ?? error.message ?? 'No fue posible completar la operación.'
  }

  if (error instanceof Error) {
    return error.message
  }

  return 'No fue posible completar la operación.'
}

function isRetryableError(error: unknown): boolean {
  if (!axios.isAxiosError(error)) {
    return true
  }

  const status = error.response?.status

  return status === undefined || status >= 500 || status === 429
}

async function executeWithRetry<T>(operation: () => Promise<T>): Promise<T> {
  let lastError: unknown

  for (let attempt = 1; attempt <= retryAttempts; attempt += 1) {
    try {
      return await operation()
    } catch (error) {
      lastError = error

      if (attempt === retryAttempts || !isRetryableError(error)) {
        break
      }

      await new Promise((resolve) => setTimeout(resolve, retryDelayMs * attempt))
    }
  }

  throw new Error(getProblemMessage(lastError))
}

export async function createFolio(
  payload: CreateFolioPayload,
  idempotencyKey: string,
): Promise<CreateFolioResponse> {
  const client = createClient()

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
  const client = createClient()

  const response = await executeWithRetry(() =>
    client.get<ApiEnvelope<QuoteStateResponse>>(`/v1/quotes/${encodeURIComponent(folio)}/state`),
  )

  return response.data.data
}
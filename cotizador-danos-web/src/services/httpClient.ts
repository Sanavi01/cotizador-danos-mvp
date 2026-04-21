import axios, { type AxiosInstance } from 'axios'

export interface ApiEnvelope<T> {
  data: T
}

export interface ProblemDetails {
  title?: string
  detail?: string
}

const apiTimeoutMs = 10000
const retryDelayMs = 250
const retryAttempts = 2

type ApiUrlKey = 'VITE_API_URL' | 'VITE_REFERENCE_CORE_API_URL'

export function hasApiBaseUrl(key: ApiUrlKey): boolean {
  return Boolean(import.meta.env[key])
}

export function getApiBaseUrl(key: ApiUrlKey, errorMessage: string): string {
  const apiBaseUrl = import.meta.env[key] as string | undefined

  if (!apiBaseUrl) {
    throw new Error(errorMessage)
  }

  return apiBaseUrl
}

export function createApiClient(key: ApiUrlKey, errorMessage: string): AxiosInstance {
  return axios.create({
    baseURL: getApiBaseUrl(key, errorMessage),
    timeout: apiTimeoutMs,
    headers: {
      'Content-Type': 'application/json',
    },
  })
}

export function getProblemMessage(error: unknown): string {
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

export async function executeWithRetry<T>(operation: () => Promise<T>): Promise<T> {
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

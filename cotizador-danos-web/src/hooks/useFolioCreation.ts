import { useState } from 'react'
import {
  createFolio,
  type CreateFolioPayload,
  type CreateFolioResponse,
} from '../services/folioService'

export function useFolioCreation() {
  const [folio, setFolio] = useState<CreateFolioResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function submitFolio(
    payload: CreateFolioPayload,
    idempotencyKey: string,
  ): Promise<CreateFolioResponse> {
    setLoading(true)
    setError(null)

    try {
      const createdFolio = await createFolio(payload, idempotencyKey)
      setFolio(createdFolio)
      return createdFolio
    } catch (submissionError) {
      const message = submissionError instanceof Error ? submissionError.message : 'No fue posible crear el folio.'
      setError(message)
      throw submissionError instanceof Error ? submissionError : new Error(message)
    } finally {
      setLoading(false)
    }
  }

  function reset() {
    setFolio(null)
    setError(null)
  }

  return {
    createFolio: submitFolio,
    loading,
    error,
    folio,
    reset,
  }
}
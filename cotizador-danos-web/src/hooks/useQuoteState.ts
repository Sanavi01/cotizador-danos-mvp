import { useEffect, useState } from 'react'
import { getQuoteState, type QuoteStateResponse } from '../services/folioService'

export function useQuoteState(folio: string | undefined) {
  const [state, setState] = useState<QuoteStateResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function loadState(currentFolio: string) {
    return getQuoteState(currentFolio)
  }

  async function refresh() {
    if (!folio) {
      setState(null)
      setError('El número de folio es obligatorio.')
      return
    }

    setLoading(true)
    setError(null)

    try {
      const quoteState = await loadState(folio)
      setState(quoteState)
    } catch (stateError) {
      const message = stateError instanceof Error ? stateError.message : 'No fue posible consultar el estado.'
      setError(message)
      setState(null)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let cancelled = false

    async function loadInitialState() {
      if (!folio) {
        setState(null)
        setError('El número de folio es obligatorio.')
        return
      }

      setLoading(true)
      setError(null)

      try {
        const quoteState = await loadState(folio)

        if (!cancelled) {
          setState(quoteState)
        }
      } catch (stateError) {
        if (!cancelled) {
          const message = stateError instanceof Error ? stateError.message : 'No fue posible consultar el estado.'
          setError(message)
          setState(null)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void loadInitialState()

    return () => {
      cancelled = true
    }
  }, [folio])

  return {
    state,
    loading,
    error,
    refresh,
  }
}
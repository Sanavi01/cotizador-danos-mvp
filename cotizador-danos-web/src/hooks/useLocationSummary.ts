import { useEffect, useState } from 'react'
import { getProblemMessage, hasApiBaseUrl } from '../services/httpClient'
import { getLocationsSummary, type LocationSummaryRecord } from '../services/quoteLocationsService'

interface LocationSummaryState {
  summary: LocationSummaryRecord | null
  loading: boolean
  error: string | null
}

const initialLocationSummaryState: LocationSummaryState = {
  summary: null,
  loading: false,
  error: null,
}

export function useLocationSummary(folio: string | undefined) {
  const [state, setState] = useState<LocationSummaryState>({
    ...initialLocationSummaryState,
    loading: true,
  })

  useEffect(() => {
    let cancelled = false

    async function loadInitialSummary() {
      if (!folio) {
        if (!cancelled) {
          setState({
            ...initialLocationSummaryState,
            loading: false,
            error: 'El número de folio es obligatorio.',
          })
        }

        return
      }

      if (!hasApiBaseUrl('VITE_API_URL')) {
        if (!cancelled) {
          setState({
            ...initialLocationSummaryState,
            loading: false,
            error: 'Define VITE_API_URL para consultar el resumen de ubicaciones.',
          })
        }

        return
      }

      setState((currentState) => ({ ...currentState, loading: true, error: null }))

      try {
        const summary = await getLocationsSummary(folio)

        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            summary,
            loading: false,
          }))
        }
      } catch (summaryError) {
        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            loading: false,
            error: getProblemMessage(summaryError),
          }))
        }
      }
    }

    setState({ ...initialLocationSummaryState, loading: true })
    void loadInitialSummary()

    return () => {
      cancelled = true
    }
  }, [folio])

  async function refresh(currentFolio = folio): Promise<LocationSummaryRecord | null> {
    if (!currentFolio) {
      setState((currentState) => ({
        ...currentState,
        summary: null,
        loading: false,
        error: 'El número de folio es obligatorio.',
      }))

      return null
    }

    if (!hasApiBaseUrl('VITE_API_URL')) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: 'Define VITE_API_URL para consultar el resumen de ubicaciones.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, loading: true, error: null }))

    try {
      const summary = await getLocationsSummary(currentFolio)

      setState((currentState) => ({
        ...currentState,
        summary,
        loading: false,
      }))

      return summary
    } catch (summaryError) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: getProblemMessage(summaryError),
      }))

      return null
    }
  }

  return {
    summary: state.summary,
    loading: state.loading,
    error: state.error,
    refresh,
  }
}
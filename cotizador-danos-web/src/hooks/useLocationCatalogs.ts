import { useEffect, useState } from 'react'
import { getProblemMessage, hasApiBaseUrl } from '../services/httpClient'
import { listBusinessLines, type ReferenceCatalogItem } from '../services/referenceCoreService'

interface LocationCatalogsState {
  businessLines: ReferenceCatalogItem[]
  loading: boolean
  error: string | null
}

const initialLocationCatalogsState: LocationCatalogsState = {
  businessLines: [],
  loading: false,
  error: null,
}

export function useLocationCatalogs() {
  const [state, setState] = useState<LocationCatalogsState>({
    ...initialLocationCatalogsState,
    loading: true,
  })

  useEffect(() => {
    let cancelled = false

    async function loadCatalogs() {
      if (!hasApiBaseUrl('VITE_REFERENCE_CORE_API_URL')) {
        if (!cancelled) {
          setState({
            ...initialLocationCatalogsState,
            loading: false,
            error: 'Define VITE_REFERENCE_CORE_API_URL para consultar los giros del mock core.',
          })
        }

        return
      }

      setState((currentState) => ({ ...currentState, loading: true, error: null }))

      try {
        const businessLines = await listBusinessLines()

        if (!cancelled) {
          setState({
            businessLines,
            loading: false,
            error: null,
          })
        }
      } catch (catalogError) {
        if (!cancelled) {
          setState({
            ...initialLocationCatalogsState,
            loading: false,
            error: getProblemMessage(catalogError),
          })
        }
      }
    }

    void loadCatalogs()

    return () => {
      cancelled = true
    }
  }, [])

  return {
    businessLines: state.businessLines,
    loading: state.loading,
    error: state.error,
  }
}
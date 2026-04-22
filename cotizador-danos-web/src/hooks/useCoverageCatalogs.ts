import { useEffect, useState } from 'react'
import { hasApiBaseUrl, getProblemMessage } from '../services/httpClient'
import { listGuarantees, type ReferenceCatalogItem } from '../services/referenceCoreService'

interface CoverageCatalogState {
  guarantees: ReferenceCatalogItem[]
  loading: boolean
  error: string | null
}

const emptyCoverageCatalogState: CoverageCatalogState = {
  guarantees: [],
  loading: false,
  error: null,
}

export function useCoverageCatalogs() {
  const [state, setState] = useState<CoverageCatalogState>({
    ...emptyCoverageCatalogState,
    loading: true,
  })

  async function reload() {
    if (!hasApiBaseUrl('VITE_REFERENCE_CORE_API_URL')) {
      setState({
        ...emptyCoverageCatalogState,
        error: 'Define VITE_REFERENCE_CORE_API_URL para consultar el catálogo de garantías.',
      })

      return
    }

    setState((currentState) => ({ ...currentState, loading: true, error: null }))

    try {
      const guarantees = await listGuarantees()

      setState({
        guarantees,
        loading: false,
        error: null,
      })
    } catch (catalogError) {
      setState({
        ...emptyCoverageCatalogState,
        loading: false,
        error: getProblemMessage(catalogError),
      })
    }
  }

  useEffect(() => {
    let cancelled = false

    async function loadInitialCatalogs() {
      if (!hasApiBaseUrl('VITE_REFERENCE_CORE_API_URL')) {
        if (!cancelled) {
          setState({
            ...emptyCoverageCatalogState,
            loading: false,
            error: 'Define VITE_REFERENCE_CORE_API_URL para consultar el catálogo de garantías.',
          })
        }

        return
      }

      setState((currentState) => ({ ...currentState, loading: true, error: null }))

      try {
        const guarantees = await listGuarantees()

        if (!cancelled) {
          setState({
            guarantees,
            loading: false,
            error: null,
          })
        }
      } catch (catalogError) {
        if (!cancelled) {
          setState({
            ...emptyCoverageCatalogState,
            loading: false,
            error: getProblemMessage(catalogError),
          })
        }
      }
    }

    void loadInitialCatalogs()

    return () => {
      cancelled = true
    }
  }, [])

  return {
    guarantees: state.guarantees,
    loading: state.loading,
    error: state.error,
    reload,
  }
}
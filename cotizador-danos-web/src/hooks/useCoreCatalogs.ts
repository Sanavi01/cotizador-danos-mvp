import { useEffect, useState } from 'react'
import { hasApiBaseUrl, getProblemMessage } from '../services/httpClient'
import {
  listAgents,
  listBusinessLines,
  listGuarantees,
  listRiskClassification,
  listSubscribers,
  type ReferenceCatalogItem,
} from '../services/referenceCoreService'

interface CoreCatalogsState {
  subscribers: ReferenceCatalogItem[]
  agents: ReferenceCatalogItem[]
  businessLines: ReferenceCatalogItem[]
  riskClassifications: ReferenceCatalogItem[]
  guarantees: ReferenceCatalogItem[]
  loading: boolean
  error: string | null
}

const emptyCatalogsState: CoreCatalogsState = {
  subscribers: [],
  agents: [],
  businessLines: [],
  riskClassifications: [],
  guarantees: [],
  loading: false,
  error: null,
}

export function useCoreCatalogs() {
  const [state, setState] = useState<CoreCatalogsState>({
    ...emptyCatalogsState,
    loading: true,
  })

  async function reload() {
    if (!hasApiBaseUrl('VITE_REFERENCE_CORE_API_URL')) {
      setState({
        ...emptyCatalogsState,
        error: 'Define VITE_REFERENCE_CORE_API_URL para consultar la referencia core.',
      })
      return
    }

    setState((currentState) => ({ ...currentState, loading: true, error: null }))

    try {
      const [subscribers, agents, businessLines, riskClassifications, guarantees] = await Promise.all([
        listSubscribers(),
        listAgents(),
        listBusinessLines(),
        listRiskClassification(),
        listGuarantees(),
      ])

      setState({
        subscribers,
        agents,
        businessLines,
        riskClassifications,
        guarantees,
        loading: false,
        error: null,
      })
    } catch (catalogError) {
      setState({
        ...emptyCatalogsState,
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
            ...emptyCatalogsState,
            loading: false,
            error: 'Define VITE_REFERENCE_CORE_API_URL para consultar la referencia core.',
          })
        }

        return
      }

      setState((currentState) => ({ ...currentState, loading: true, error: null }))

      try {
        const [subscribers, agents, businessLines, riskClassifications, guarantees] = await Promise.all([
          listSubscribers(),
          listAgents(),
          listBusinessLines(),
          listRiskClassification(),
          listGuarantees(),
        ])

        if (!cancelled) {
          setState({
            subscribers,
            agents,
            businessLines,
            riskClassifications,
            guarantees,
            loading: false,
            error: null,
          })
        }
      } catch (catalogError) {
        if (!cancelled) {
          setState({
            ...emptyCatalogsState,
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
    subscribers: state.subscribers,
    agents: state.agents,
    businessLines: state.businessLines,
    riskClassifications: state.riskClassifications,
    guarantees: state.guarantees,
    loading: state.loading,
    error: state.error,
    reload,
  }
}

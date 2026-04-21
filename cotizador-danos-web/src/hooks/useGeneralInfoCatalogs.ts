import { useEffect, useState } from 'react'
import { hasApiBaseUrl, getProblemMessage } from '../services/httpClient'
import {
  listAgents,
  listBusinessLines,
  listRiskClassification,
  type ReferenceCatalogItem,
} from '../services/referenceCoreService'

interface GeneralInfoCatalogsState {
  agents: ReferenceCatalogItem[]
  riskClassifications: ReferenceCatalogItem[]
  businessLines: ReferenceCatalogItem[]
  loading: boolean
  error: string | null
}

const initialGeneralInfoCatalogsState: GeneralInfoCatalogsState = {
  agents: [],
  riskClassifications: [],
  businessLines: [],
  loading: false,
  error: null,
}

export function useGeneralInfoCatalogs() {
  const [state, setState] = useState<GeneralInfoCatalogsState>({
    ...initialGeneralInfoCatalogsState,
    loading: true,
  })

  async function loadCatalogs() {
    if (!hasApiBaseUrl('VITE_REFERENCE_CORE_API_URL')) {
      setState({
        ...initialGeneralInfoCatalogsState,
        error: 'Define VITE_REFERENCE_CORE_API_URL para consultar los catálogos de datos generales.',
      })

      return
    }

    setState((currentState) => ({ ...currentState, loading: true, error: null }))

    try {
      const [agents, riskClassifications, businessLines] = await Promise.all([
        listAgents(),
        listRiskClassification(),
        listBusinessLines(),
      ])

      setState({
        agents,
        riskClassifications,
        businessLines,
        loading: false,
        error: null,
      })
    } catch (catalogError) {
      setState({
        ...initialGeneralInfoCatalogsState,
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
            ...initialGeneralInfoCatalogsState,
            loading: false,
            error: 'Define VITE_REFERENCE_CORE_API_URL para consultar los catálogos de datos generales.',
          })
        }

        return
      }

      setState((currentState) => ({ ...currentState, loading: true, error: null }))

      try {
        const [agents, riskClassifications, businessLines] = await Promise.all([
          listAgents(),
          listRiskClassification(),
          listBusinessLines(),
        ])

        if (!cancelled) {
          setState({
            agents,
            riskClassifications,
            businessLines,
            loading: false,
            error: null,
          })
        }
      } catch (catalogError) {
        if (!cancelled) {
          setState({
            ...initialGeneralInfoCatalogsState,
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
    agents: state.agents,
    riskClassifications: state.riskClassifications,
    businessLines: state.businessLines,
    loading: state.loading,
    error: state.error,
    reload: loadCatalogs,
  }
}
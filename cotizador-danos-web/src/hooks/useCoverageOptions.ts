import { useEffect, useState } from 'react'
import { hasApiBaseUrl, getProblemMessage } from '../services/httpClient'
import {
  createEmptyCoverageOptions,
  getCoverageOptions,
  updateCoverageOptions,
  type CoverageOptionsPayload,
  type CoverageOptionsRecord,
} from '../services/quoteCoverageOptionsService'

interface CoverageOptionsState {
  options: CoverageOptionsRecord | null
  loading: boolean
  saving: boolean
  error: string | null
}

const emptyCoverageOptionsState: CoverageOptionsState = {
  options: null,
  loading: false,
  saving: false,
  error: null,
}

export function useCoverageOptions(folio: string | undefined) {
  const [state, setState] = useState<CoverageOptionsState>({
    ...emptyCoverageOptionsState,
    loading: true,
  })

  useEffect(() => {
    let cancelled = false

    async function loadInitialOptions() {
      if (!folio) {
        if (!cancelled) {
          setState({
            ...emptyCoverageOptionsState,
            loading: false,
            error: 'El número de folio es obligatorio.',
          })
        }

        return
      }

      if (!hasApiBaseUrl('VITE_API_URL')) {
        if (!cancelled) {
          setState({
            ...emptyCoverageOptionsState,
            loading: false,
            error: 'Define VITE_API_URL para consultar las opciones de cobertura.',
          })
        }

        return
      }

      setState((currentState) => ({ ...currentState, loading: true, error: null }))

      try {
        const options = await getCoverageOptions(folio)

        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            options,
            loading: false,
          }))
        }
      } catch (coverageError) {
        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            loading: false,
            error: getProblemMessage(coverageError),
          }))
        }
      }
    }

    setState({ ...emptyCoverageOptionsState, loading: true })
    void loadInitialOptions()

    return () => {
      cancelled = true
    }
  }, [folio])

  async function loadOptions(currentFolio = folio): Promise<CoverageOptionsRecord | null> {
    if (!currentFolio) {
      setState((currentState) => ({
        ...currentState,
        options: null,
        loading: false,
        error: 'El número de folio es obligatorio.',
      }))

      return null
    }

    if (!hasApiBaseUrl('VITE_API_URL')) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: 'Define VITE_API_URL para consultar las opciones de cobertura.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, loading: true, error: null }))

    try {
      const options = await getCoverageOptions(currentFolio)

      setState((currentState) => ({
        ...currentState,
        options,
        loading: false,
      }))

      return options
    } catch (coverageError) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: getProblemMessage(coverageError),
      }))

      return null
    }
  }

  async function saveOptions(
    currentFolio: string | undefined,
    payload: CoverageOptionsPayload,
  ): Promise<CoverageOptionsRecord | null> {
    if (!currentFolio) {
      setState((currentState) => ({
        ...currentState,
        saving: false,
        error: 'El número de folio es obligatorio.',
      }))

      return null
    }

    if (!hasApiBaseUrl('VITE_API_URL')) {
      setState((currentState) => ({
        ...currentState,
        saving: false,
        error: 'Define VITE_API_URL para guardar las opciones de cobertura.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, saving: true, error: null }))

    try {
      const options = await updateCoverageOptions(currentFolio, payload)

      setState((currentState) => ({
        ...currentState,
        options,
        saving: false,
      }))

      return options
    } catch (coverageError) {
      setState((currentState) => ({
        ...currentState,
        saving: false,
        error: getProblemMessage(coverageError),
      }))

      return null
    }
  }

  function reset() {
    setState({ ...emptyCoverageOptionsState })
  }

  return {
    options: state.options ?? (folio ? createEmptyCoverageOptions(folio) : null),
    loading: state.loading,
    saving: state.saving,
    error: state.error,
    loadOptions,
    saveOptions,
    reset,
  }
}
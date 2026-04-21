import { useEffect, useState } from 'react'
import { hasApiBaseUrl, getProblemMessage } from '../services/httpClient'
import {
  createEmptyLocationsLayout,
  getLocationsLayout,
  updateLocationsLayout,
  type LocationsLayoutPayload,
  type LocationsLayoutRecord,
} from '../services/quoteLocationsLayoutService'

interface LocationsLayoutState {
  layout: LocationsLayoutRecord | null
  loading: boolean
  saving: boolean
  error: string | null
}

const initialLocationsLayoutState: LocationsLayoutState = {
  layout: null,
  loading: false,
  saving: false,
  error: null,
}

export function useLocationsLayout(folio: string | undefined) {
  const [state, setState] = useState<LocationsLayoutState>({
    ...initialLocationsLayoutState,
    loading: true,
  })

  useEffect(() => {
    let cancelled = false

    async function loadInitialLayout() {
      if (!folio) {
        if (!cancelled) {
          setState({
            ...initialLocationsLayoutState,
            loading: false,
            error: 'El número de folio es obligatorio.',
          })
        }

        return
      }

      if (!hasApiBaseUrl('VITE_API_URL')) {
        if (!cancelled) {
          setState({
            ...initialLocationsLayoutState,
            loading: false,
            error: 'Define VITE_API_URL para consultar el layout de ubicaciones.',
          })
        }

        return
      }

      setState((currentState) => ({ ...currentState, loading: true, error: null }))

      try {
        const layout = await getLocationsLayout(folio)

        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            layout,
            loading: false,
          }))
        }
      } catch (layoutError) {
        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            loading: false,
            error: getProblemMessage(layoutError),
          }))
        }
      }
    }

    setState({ ...initialLocationsLayoutState, loading: true })
    void loadInitialLayout()

    return () => {
      cancelled = true
    }
  }, [folio])

  async function loadLayout(currentFolio = folio): Promise<LocationsLayoutRecord | null> {
    if (!currentFolio) {
      setState((currentState) => ({
        ...currentState,
        layout: null,
        loading: false,
        error: 'El número de folio es obligatorio.',
      }))

      return null
    }

    if (!hasApiBaseUrl('VITE_API_URL')) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: 'Define VITE_API_URL para consultar el layout de ubicaciones.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, loading: true, error: null }))

    try {
      const layout = await getLocationsLayout(currentFolio)

      setState((currentState) => ({
        ...currentState,
        layout,
        loading: false,
      }))

      return layout
    } catch (layoutError) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: getProblemMessage(layoutError),
      }))

      return null
    }
  }

  async function saveLayout(
    currentFolio: string | undefined,
    payload: LocationsLayoutPayload,
  ): Promise<LocationsLayoutRecord | null> {
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
        error: 'Define VITE_API_URL para guardar el layout de ubicaciones.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, saving: true, error: null }))

    try {
      const layout = await updateLocationsLayout(currentFolio, payload)

      setState((currentState) => ({
        ...currentState,
        layout,
        saving: false,
      }))

      return layout
    } catch (layoutError) {
      setState((currentState) => ({
        ...currentState,
        saving: false,
        error: getProblemMessage(layoutError),
      }))

      return null
    }
  }

  function reset() {
    setState({ ...initialLocationsLayoutState })
  }

  return {
    layout: state.layout ?? createEmptyLocationsLayout(folio ?? ''),
    loading: state.loading,
    saving: state.saving,
    error: state.error,
    loadLayout,
    saveLayout,
    reset,
  }
}
import { useEffect, useState } from 'react'
import { getProblemMessage, hasApiBaseUrl } from '../services/httpClient'
import {
  createEmptyLocation,
  getLocations,
  updateLocation,
  updateLocations,
  type LocationCollectionRecord,
  type LocationRecord,
  type UpdateLocationPayload,
  type UpdateLocationsPayload,
} from '../services/quoteLocationsService'

interface LocationsState {
  locations: LocationRecord[]
  version: number
  fechaUltimaActualizacion: string
  loading: boolean
  saving: boolean
  error: string | null
}

const initialLocationsState: LocationsState = {
  locations: [],
  version: 0,
  fechaUltimaActualizacion: '',
  loading: false,
  saving: false,
  error: null,
}

function applyCollection(collection: LocationCollectionRecord): LocationsState {
  return {
    locations: collection.ubicaciones,
    version: collection.version,
    fechaUltimaActualizacion: collection.fechaUltimaActualizacion,
    loading: false,
    saving: false,
    error: null,
  }
}

function mergeUpdatedLocation(currentLocations: LocationRecord[], nextLocation: LocationRecord): LocationRecord[] {
  const existingIndex = currentLocations.findIndex((location) => location.indice === nextLocation.indice)

  if (existingIndex < 0) {
    return [...currentLocations, nextLocation].sort((left, right) => left.indice - right.indice)
  }

  return currentLocations.map((location) => (location.indice === nextLocation.indice ? nextLocation : location))
}

export function useLocations(folio: string | undefined) {
  const [state, setState] = useState<LocationsState>({
    ...initialLocationsState,
    loading: true,
  })

  useEffect(() => {
    let cancelled = false

    async function loadInitialLocations() {
      if (!folio) {
        if (!cancelled) {
          setState({
            ...initialLocationsState,
            loading: false,
            error: 'El número de folio es obligatorio.',
          })
        }

        return
      }

      if (!hasApiBaseUrl('VITE_API_URL')) {
        if (!cancelled) {
          setState({
            ...initialLocationsState,
            loading: false,
            error: 'Define VITE_API_URL para consultar las ubicaciones.',
          })
        }

        return
      }

      setState((currentState) => ({ ...currentState, loading: true, error: null }))

      try {
        const collection = await getLocations(folio)

        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            ...applyCollection(collection),
          }))
        }
      } catch (locationsError) {
        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            loading: false,
            error: getProblemMessage(locationsError),
          }))
        }
      }
    }

    setState({ ...initialLocationsState, loading: true })
    void loadInitialLocations()

    return () => {
      cancelled = true
    }
  }, [folio])

  async function loadLocations(currentFolio = folio): Promise<LocationRecord[] | null> {
    if (!currentFolio) {
      setState((currentState) => ({
        ...currentState,
        locations: [],
        loading: false,
        error: 'El número de folio es obligatorio.',
      }))

      return null
    }

    if (!hasApiBaseUrl('VITE_API_URL')) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: 'Define VITE_API_URL para consultar las ubicaciones.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, loading: true, error: null }))

    try {
      const collection = await getLocations(currentFolio)

      setState((currentState) => ({
        ...currentState,
        ...applyCollection(collection),
      }))

      return collection.ubicaciones
    } catch (locationsError) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: getProblemMessage(locationsError),
      }))

      return null
    }
  }

  async function saveLocations(
    currentFolio: string | undefined,
    payload: UpdateLocationsPayload,
  ): Promise<LocationRecord[] | null> {
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
        error: 'Define VITE_API_URL para guardar las ubicaciones.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, saving: true, error: null }))

    try {
      const collection = await updateLocations(currentFolio, payload)

      setState((currentState) => ({
        ...currentState,
        ...applyCollection(collection),
      }))

      return collection.ubicaciones
    } catch (locationsError) {
      setState((currentState) => ({
        ...currentState,
        saving: false,
        error: getProblemMessage(locationsError),
      }))

      return null
    }
  }

  async function updateLocationByIndice(
    currentFolio: string | undefined,
    indice: number,
    payload: UpdateLocationPayload,
  ): Promise<LocationRecord | null> {
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
        error: 'Define VITE_API_URL para actualizar la ubicación.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, saving: true, error: null }))

    try {
      const mutation = await updateLocation(currentFolio, indice, payload)

      setState((currentState) => ({
        ...currentState,
        locations: mergeUpdatedLocation(currentState.locations, mutation.ubicacion),
        version: mutation.version,
        fechaUltimaActualizacion: mutation.fechaUltimaActualizacion,
        saving: false,
      }))

      return mutation.ubicacion
    } catch (locationsError) {
      setState((currentState) => ({
        ...currentState,
        saving: false,
        error: getProblemMessage(locationsError),
      }))

      return null
    }
  }

  function reset() {
    setState({ ...initialLocationsState })
  }

  return {
    locations: state.locations,
    version: state.version,
    fechaUltimaActualizacion: state.fechaUltimaActualizacion,
    loading: state.loading,
    saving: state.saving,
    error: state.error,
    loadLocations,
    saveLocations,
    updateLocation: updateLocationByIndice,
    reset,
    createEmptyLocation,
  }
}
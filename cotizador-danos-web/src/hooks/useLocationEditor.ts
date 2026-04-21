import { useEffect, useState } from 'react'
import { getProblemMessage, hasApiBaseUrl } from '../services/httpClient'
import { getLocations, updateLocation, type LocationRecord, type UpdateLocationPayload } from '../services/quoteLocationsService'

interface LocationEditorState {
  location: LocationRecord | null
  version: number
  fechaUltimaActualizacion: string
  loading: boolean
  saving: boolean
  error: string | null
}

const initialLocationEditorState: LocationEditorState = {
  location: null,
  version: 0,
  fechaUltimaActualizacion: '',
  loading: false,
  saving: false,
  error: null,
}

function findLocation(locations: LocationRecord[], indice: number): LocationRecord | null {
  return locations.find((location) => location.indice === indice) ?? null
}

export function useLocationEditor(folio: string | undefined, indice: number | undefined) {
  const [state, setState] = useState<LocationEditorState>({
    ...initialLocationEditorState,
    loading: true,
  })

  useEffect(() => {
    let cancelled = false

    async function loadInitialLocation() {
      if (!folio || !indice) {
        if (!cancelled) {
          setState({
            ...initialLocationEditorState,
            loading: false,
            error: 'El número de folio y el índice son obligatorios.',
          })
        }

        return
      }

      if (!hasApiBaseUrl('VITE_API_URL')) {
        if (!cancelled) {
          setState({
            ...initialLocationEditorState,
            loading: false,
            error: 'Define VITE_API_URL para consultar la ubicación.',
          })
        }

        return
      }

      setState((currentState) => ({ ...currentState, loading: true, error: null }))

      try {
        const collection = await getLocations(folio)
        const location = findLocation(collection.ubicaciones, indice)

        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            location,
            version: collection.version,
            fechaUltimaActualizacion: collection.fechaUltimaActualizacion,
            loading: false,
            error: location ? null : `No se encontró la ubicación ${indice} dentro del folio ${folio}.`,
          }))
        }
      } catch (locationError) {
        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            loading: false,
            error: getProblemMessage(locationError),
          }))
        }
      }
    }

    setState({ ...initialLocationEditorState, loading: true })
    void loadInitialLocation()

    return () => {
      cancelled = true
    }
  }, [folio, indice])

  async function loadLocation(currentFolio = folio, currentIndice = indice): Promise<LocationRecord | null> {
    if (!currentFolio || !currentIndice) {
      setState((currentState) => ({
        ...currentState,
        location: null,
        loading: false,
        error: 'El número de folio y el índice son obligatorios.',
      }))

      return null
    }

    if (!hasApiBaseUrl('VITE_API_URL')) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: 'Define VITE_API_URL para consultar la ubicación.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, loading: true, error: null }))

    try {
      const collection = await getLocations(currentFolio)
      const location = findLocation(collection.ubicaciones, currentIndice)

      setState((currentState) => ({
        ...currentState,
        location,
        version: collection.version,
        fechaUltimaActualizacion: collection.fechaUltimaActualizacion,
        loading: false,
        error: location ? null : `No se encontró la ubicación ${currentIndice} dentro del folio ${currentFolio}.`,
      }))

      return location
    } catch (locationError) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: getProblemMessage(locationError),
      }))

      return null
    }
  }

  async function saveLocation(
    currentFolio: string | undefined,
    currentIndice: number | undefined,
    payload: UpdateLocationPayload,
  ): Promise<LocationRecord | null> {
    if (!currentFolio || !currentIndice) {
      setState((currentState) => ({
        ...currentState,
        saving: false,
        error: 'El número de folio y el índice son obligatorios.',
      }))

      return null
    }

    if (!hasApiBaseUrl('VITE_API_URL')) {
      setState((currentState) => ({
        ...currentState,
        saving: false,
        error: 'Define VITE_API_URL para guardar la ubicación.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, saving: true, error: null }))

    try {
      const mutation = await updateLocation(currentFolio, currentIndice, payload)

      setState((currentState) => ({
        ...currentState,
        location: mutation.ubicacion,
        version: mutation.version,
        fechaUltimaActualizacion: mutation.fechaUltimaActualizacion,
        saving: false,
      }))

      return mutation.ubicacion
    } catch (locationError) {
      setState((currentState) => ({
        ...currentState,
        saving: false,
        error: getProblemMessage(locationError),
      }))

      return null
    }
  }

  function reset() {
    setState({ ...initialLocationEditorState })
  }

  return {
    location: state.location,
    version: state.version,
    fechaUltimaActualizacion: state.fechaUltimaActualizacion,
    loading: state.loading,
    saving: state.saving,
    error: state.error,
    loadLocation,
    saveLocation,
    reset,
  }
}
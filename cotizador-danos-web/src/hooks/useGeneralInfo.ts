import { useEffect, useState } from 'react'
import { hasApiBaseUrl, getProblemMessage } from '../services/httpClient'
import {
  getGeneralInfo,
  updateGeneralInfo,
  type GeneralInfoPayload,
  type GeneralInfoRecord,
} from '../services/quoteGeneralInfoService'

interface GeneralInfoState {
  generalInfo: GeneralInfoRecord | null
  loading: boolean
  saving: boolean
  error: string | null
}

const initialGeneralInfoState: GeneralInfoState = {
  generalInfo: null,
  loading: false,
  saving: false,
  error: null,
}

export function useGeneralInfo(folio: string | undefined) {
  const [state, setState] = useState<GeneralInfoState>({
    ...initialGeneralInfoState,
    loading: true,
  })

  useEffect(() => {
    let cancelled = false

    async function loadInitialGeneralInfo() {
      if (!folio) {
        if (!cancelled) {
          setState({
            ...initialGeneralInfoState,
            loading: false,
            error: 'El número de folio es obligatorio.',
          })
        }

        return
      }

      if (!hasApiBaseUrl('VITE_API_URL')) {
        if (!cancelled) {
          setState({
            ...initialGeneralInfoState,
            loading: false,
            error: 'Define VITE_API_URL para consultar los datos generales.',
          })
        }

        return
      }

      setState((currentState) => ({ ...currentState, loading: true, error: null }))

      try {
        const generalInfo = await getGeneralInfo(folio)

        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            generalInfo,
            loading: false,
          }))
        }
      } catch (generalInfoError) {
        if (!cancelled) {
          setState((currentState) => ({
            ...currentState,
            loading: false,
            error: getProblemMessage(generalInfoError),
          }))
        }
      }
    }

    setState({ ...initialGeneralInfoState, loading: true })
    void loadInitialGeneralInfo()

    return () => {
      cancelled = true
    }
  }, [folio])

  async function loadGeneralInfo(currentFolio = folio): Promise<GeneralInfoRecord | null> {
    if (!currentFolio) {
      setState((currentState) => ({
        ...currentState,
        generalInfo: null,
        loading: false,
        error: 'El número de folio es obligatorio.',
      }))

      return null
    }

    if (!hasApiBaseUrl('VITE_API_URL')) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: 'Define VITE_API_URL para consultar los datos generales.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, loading: true, error: null }))

    try {
      const generalInfo = await getGeneralInfo(currentFolio)

      setState((currentState) => ({
        ...currentState,
        generalInfo,
        loading: false,
      }))

      return generalInfo
    } catch (generalInfoError) {
      setState((currentState) => ({
        ...currentState,
        loading: false,
        error: getProblemMessage(generalInfoError),
      }))

      return null
    }
  }

  async function saveGeneralInfo(currentFolio: string | undefined, payload: GeneralInfoPayload): Promise<GeneralInfoRecord | null> {
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
        error: 'Define VITE_API_URL para guardar los datos generales.',
      }))

      return null
    }

    setState((currentState) => ({ ...currentState, saving: true, error: null }))

    try {
      const generalInfo = await updateGeneralInfo(currentFolio, payload)

      setState((currentState) => ({
        ...currentState,
        generalInfo,
        saving: false,
      }))

      return generalInfo
    } catch (generalInfoError) {
      setState((currentState) => ({
        ...currentState,
        saving: false,
        error: getProblemMessage(generalInfoError),
      }))

      return null
    }
  }

  function reset() {
    setState({ ...initialGeneralInfoState })
  }

  return {
    generalInfo: state.generalInfo,
    loading: state.loading,
    saving: state.saving,
    error: state.error,
    loadGeneralInfo,
    saveGeneralInfo,
    reset,
  }
}
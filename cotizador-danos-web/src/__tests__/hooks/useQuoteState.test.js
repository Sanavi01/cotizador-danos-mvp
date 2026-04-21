import '@testing-library/jest-dom/vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useQuoteState } from '../../hooks/useQuoteState'
import { getQuoteState } from '../../services/folioService'

vi.mock('../../services/folioService', () => ({
  getQuoteState: vi.fn(),
}))

const mockedGetQuoteState = vi.mocked(getQuoteState)

describe('useQuoteState', () => {
  beforeEach(() => {
    mockedGetQuoteState.mockReset()
    vi.clearAllMocks()
  })

  it('loads the current quote state on mount and refreshes it on demand', async () => {
    const initialState = {
      numeroFolio: '1000001',
      estadoCotizacion: 'BORRADOR',
      version: 0,
      fechaUltimaActualizacion: '2026-04-20T00:00:00Z',
      progreso: {
        datosGenerales: 'INCOMPLETE',
        layoutUbicaciones: 'INCOMPLETE',
        ubicaciones: 'INCOMPLETE',
        opcionesCobertura: 'INCOMPLETE',
      },
      resumenUbicaciones: {
        totalEsperado: 0,
        totalActual: 0,
        calculables: 0,
        incompletas: 0,
        invalidas: 0,
        conAlertas: 0,
      },
      tieneAlertas: false,
      alertasVigentes: [],
      readyToCalculate: false,
      resultadoFinanciero: null,
    }

    const refreshedState = {
      ...initialState,
      estadoCotizacion: 'EN_CAPTURA',
      version: 1,
      progreso: {
        ...initialState.progreso,
        datosGenerales: 'COMPLETED',
      },
      resumenUbicaciones: {
        ...initialState.resumenUbicaciones,
        totalEsperado: 3,
        totalActual: 2,
        calculables: 1,
      },
    }

    mockedGetQuoteState.mockResolvedValueOnce(initialState).mockResolvedValueOnce(refreshedState)

    const { result } = renderHook(() => useQuoteState('1000001'))

    await waitFor(() => {
      expect(result.current.state).toEqual(initialState)
      expect(result.current.error).toBeNull()
      expect(result.current.loading).toBe(false)
    })

    await result.current.refresh()

    await waitFor(() => {
      expect(result.current.state).toEqual(refreshedState)
    })

    expect(mockedGetQuoteState).toHaveBeenCalledWith('1000001')
    expect(mockedGetQuoteState).toHaveBeenCalledTimes(2)
  })

  it('reports a service error when the folio cannot be loaded', async () => {
    mockedGetQuoteState.mockRejectedValue(new Error('No fue posible consultar el estado.'))

    const { result } = renderHook(() => useQuoteState('9999999'))

    await waitFor(() => {
      expect(result.current.state).toBeNull()
      expect(result.current.error).toBe('No fue posible consultar el estado.')
      expect(result.current.loading).toBe(false)
    })
  })

  it('validates the missing folio input', async () => {
    const { result } = renderHook(() => useQuoteState(undefined))

    await waitFor(() => {
      expect(result.current.state).toBeNull()
      expect(result.current.error).toBe('El número de folio es obligatorio.')
      expect(result.current.loading).toBe(false)
    })
  })
})
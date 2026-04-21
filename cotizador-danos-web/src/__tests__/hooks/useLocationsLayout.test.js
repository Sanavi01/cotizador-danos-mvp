import '@testing-library/jest-dom/vitest'
import { act, renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useLocationsLayout } from '../../hooks/useLocationsLayout'
import { getProblemMessage, hasApiBaseUrl } from '../../services/httpClient'
import {
  createEmptyLocationsLayout,
  getLocationsLayout,
  updateLocationsLayout,
} from '../../services/quoteLocationsLayoutService'

vi.mock('../../services/httpClient', () => ({
  hasApiBaseUrl: vi.fn(),
  getProblemMessage: vi.fn(),
}))

vi.mock('../../services/quoteLocationsLayoutService', () => ({
  createEmptyLocationsLayout: vi.fn((folio) => ({
    numeroFolio: folio,
    version: 0,
    fechaUltimaActualizacion: '',
    configuracionLayout: {
      modoCaptura: null,
      cantidadUbicaciones: null,
      ubicaciones: [] ,
    },
  })),
  getLocationsLayout: vi.fn(),
  updateLocationsLayout: vi.fn(),
}))

const mockedHasApiBaseUrl = vi.mocked(hasApiBaseUrl)
const mockedGetProblemMessage = vi.mocked(getProblemMessage)
const mockedCreateEmptyLocationsLayout = vi.mocked(createEmptyLocationsLayout)
const mockedGetLocationsLayout = vi.mocked(getLocationsLayout)
const mockedUpdateLocationsLayout = vi.mocked(updateLocationsLayout)

const layoutFixture = {
  numeroFolio: '1000013',
  version: 1,
  fechaUltimaActualizacion: '2026-04-21T12:12:00Z',
  configuracionLayout: {
    modoCaptura: 'MULTIPLE',
    cantidadUbicaciones: 5,
    ubicaciones: [
      { indice: 2, ordenCaptura: 1 },
      { indice: 4, ordenCaptura: 2 },
      { indice: 3, ordenCaptura: 3 },
      { indice: 1, ordenCaptura: 4 },
      { indice: 5, ordenCaptura: 5 },
    ],
  },
}

const emptyLayoutFixture = {
  numeroFolio: '1000013',
  version: 3,
  fechaUltimaActualizacion: '2026-04-21T12:12:00Z',
  configuracionLayout: {
    modoCaptura: null,
    cantidadUbicaciones: null,
    ubicaciones: [],
  },
}

const savedLayoutFixture = {
  ...layoutFixture,
  version: 2,
}

describe('useLocationsLayout', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedHasApiBaseUrl.mockReturnValue(true)
    mockedGetProblemMessage.mockReturnValue('No fue posible completar la operación.')
  })

  it('loads the current layout and saves changes with the current version', async () => {
    mockedGetLocationsLayout.mockResolvedValue(layoutFixture)
    mockedUpdateLocationsLayout.mockResolvedValue(savedLayoutFixture)

    const { result } = renderHook(() => useLocationsLayout('1000013'))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.layout).toEqual(layoutFixture)
      expect(result.current.error).toBeNull()
    })

    await act(async () => {
      await result.current.saveLayout('1000013', {
        version: 1,
        configuracionLayout: layoutFixture.configuracionLayout,
      })
    })

    await waitFor(() => {
      expect(result.current.layout).toEqual(savedLayoutFixture)
      expect(result.current.saving).toBe(false)
    })

    expect(mockedGetLocationsLayout).toHaveBeenCalledWith('1000013')
    expect(mockedUpdateLocationsLayout).toHaveBeenCalledWith('1000013', {
      version: 1,
      configuracionLayout: layoutFixture.configuracionLayout,
    })
    expect(mockedCreateEmptyLocationsLayout).toHaveBeenCalledWith('1000013')
  })

  it('loads a blank section shape when the backend returns an empty layout record', async () => {
    mockedGetLocationsLayout.mockResolvedValue(emptyLayoutFixture)

    const { result } = renderHook(() => useLocationsLayout('1000013'))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.layout).toEqual(emptyLayoutFixture)
      expect(result.current.error).toBeNull()
    })
  })

  it('reports a missing folio before calling the service', async () => {
    const { result } = renderHook(() => useLocationsLayout(undefined))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.error).toBe('El número de folio es obligatorio.')
    })

    expect(mockedGetLocationsLayout).not.toHaveBeenCalled()
    expect(mockedUpdateLocationsLayout).not.toHaveBeenCalled()
  })

  it('surfaces backend errors when loading or saving the layout', async () => {
    mockedGetProblemMessage.mockReturnValue('No fue posible consultar el layout.')
    mockedGetLocationsLayout.mockRejectedValueOnce(new Error('Backend down'))

    const { result } = renderHook(() => useLocationsLayout('1000013'))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.error).toBe('No fue posible consultar el layout.')
    })

    mockedGetLocationsLayout.mockResolvedValue(layoutFixture)
    mockedGetProblemMessage.mockReturnValue('No fue posible guardar el layout.')
    mockedUpdateLocationsLayout.mockRejectedValueOnce(new Error('Save down'))

    await act(async () => {
      await result.current.saveLayout('1000013', {
        version: 1,
        configuracionLayout: layoutFixture.configuracionLayout,
      })
    })

    await waitFor(() => {
      expect(result.current.saving).toBe(false)
      expect(result.current.error).toBe('No fue posible guardar el layout.')
    })
  })
})
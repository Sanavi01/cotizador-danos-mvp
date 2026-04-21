import '@testing-library/jest-dom/vitest'
import { act, renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useLocations } from '../../hooks/useLocations'
import { getProblemMessage, hasApiBaseUrl } from '../../services/httpClient'
import { createEmptyLocation, getLocations, updateLocation, updateLocations } from '../../services/quoteLocationsService'

vi.mock('../../services/httpClient', () => ({
  hasApiBaseUrl: vi.fn(),
  getProblemMessage: vi.fn(),
}))

vi.mock('../../services/quoteLocationsService', () => ({
  createEmptyLocation: vi.fn((indice) => ({ indice })),
  getLocations: vi.fn(),
  updateLocations: vi.fn(),
  updateLocation: vi.fn(),
}))

const mockedHasApiBaseUrl = vi.mocked(hasApiBaseUrl)
const mockedGetProblemMessage = vi.mocked(getProblemMessage)
const mockedCreateEmptyLocation = vi.mocked(createEmptyLocation)
const mockedGetLocations = vi.mocked(getLocations)
const mockedUpdateLocations = vi.mocked(updateLocations)
const mockedUpdateLocation = vi.mocked(updateLocation)

describe('useLocations', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedHasApiBaseUrl.mockReturnValue(true)
    mockedGetProblemMessage.mockReturnValue('No fue posible completar la operación.')
  })

  it('loads an empty locations collection without errors', async () => {
    mockedGetLocations.mockResolvedValue({
      numeroFolio: '1000001',
      version: 3,
      fechaUltimaActualizacion: '2026-04-21T00:00:00Z',
      ubicaciones: [],
    })

    const { result } = renderHook(() => useLocations('1000001'))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.error).toBeNull()
      expect(result.current.locations).toEqual([])
      expect(result.current.version).toBe(3)
    })

    expect(mockedGetLocations).toHaveBeenCalledWith('1000001')
    expect(result.current.createEmptyLocation).toBe(mockedCreateEmptyLocation)
  })

  it('surfaces a folio error when the initial load fails', async () => {
    mockedGetLocations.mockRejectedValue(new Error('404'))
    mockedGetProblemMessage.mockReturnValue('No existe una cotización con el folio solicitado.')

    const { result } = renderHook(() => useLocations('9999999'))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.error).toBe('No existe una cotización con el folio solicitado.')
      expect(result.current.locations).toEqual([])
    })
  })

  it('surfaces a version conflict when the bulk save fails', async () => {
    mockedGetLocations.mockResolvedValue({
      numeroFolio: '1000001',
      version: 3,
      fechaUltimaActualizacion: '2026-04-21T00:00:00Z',
      ubicaciones: [],
    })
    mockedUpdateLocations.mockRejectedValue(new Error('409'))
    mockedGetProblemMessage.mockReturnValue('La versión enviada ya no está vigente para el folio.')

    const { result } = renderHook(() => useLocations('1000001'))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    await act(async () => {
      await result.current.saveLocations('1000001', {
        version: 3,
        ubicaciones: [{ indice: 1, nombreUbicacion: 'Planta principal' }],
      })
    })

    await waitFor(() => {
      expect(result.current.saving).toBe(false)
      expect(result.current.error).toBe('La versión enviada ya no está vigente para el folio.')
    })

    expect(mockedUpdateLocations).toHaveBeenCalledWith('1000001', {
      version: 3,
      ubicaciones: [{ indice: 1, nombreUbicacion: 'Planta principal' }],
    })
    expect(mockedUpdateLocation).not.toHaveBeenCalled()
  })
})

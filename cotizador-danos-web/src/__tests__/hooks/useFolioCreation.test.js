import '@testing-library/jest-dom/vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useFolioCreation } from '../../hooks/useFolioCreation'
import { createFolio } from '../../services/folioService'

vi.mock('../../services/folioService', () => ({
  createFolio: vi.fn(),
}))

const mockedCreateFolio = vi.mocked(createFolio)

describe('useFolioCreation', () => {
  beforeEach(() => {
    mockedCreateFolio.mockReset()
  })

  it('stores the folio returned by the service', async () => {
    const createdFolio = {
      numeroFolio: '1000001',
      estadoCotizacion: 'BORRADOR',
      version: 0,
      fechaUltimaActualizacion: '2026-04-20T00:00:00Z',
    }

    mockedCreateFolio.mockResolvedValue(createdFolio)

    const { result } = renderHook(() => useFolioCreation())

    await result.current.createFolio({ origin: 'spa' }, 'key-123')

    await waitFor(() => {
      expect(result.current.folio).toEqual(createdFolio)
      expect(result.current.error).toBeNull()
      expect(result.current.loading).toBe(false)
    })

    expect(mockedCreateFolio).toHaveBeenCalledWith({ origin: 'spa' }, 'key-123')
  })

  it('surfaces creation errors and keeps the current folio empty', async () => {
    mockedCreateFolio.mockRejectedValue(new Error('No fue posible crear el folio.'))

    const { result } = renderHook(() => useFolioCreation())

    await expect(result.current.createFolio({ origin: 'spa' }, 'key-123')).rejects.toThrow(
      'No fue posible crear el folio.',
    )

    await waitFor(() => {
      expect(result.current.folio).toBeNull()
      expect(result.current.error).toBe('No fue posible crear el folio.')
      expect(result.current.loading).toBe(false)
    })
  })
})
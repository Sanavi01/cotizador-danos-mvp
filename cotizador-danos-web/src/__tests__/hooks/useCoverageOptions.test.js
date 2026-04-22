import '@testing-library/jest-dom/vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useCoverageOptions } from '../../hooks/useCoverageOptions'
import { getProblemMessage, hasApiBaseUrl } from '../../services/httpClient'
import {
  createEmptyCoverageOptions,
  getCoverageOptions,
  updateCoverageOptions,
} from '../../services/quoteCoverageOptionsService'

vi.mock('../../services/httpClient', () => ({
  hasApiBaseUrl: vi.fn(),
  getProblemMessage: vi.fn(),
}))

vi.mock('../../services/quoteCoverageOptionsService', () => ({
  createEmptyCoverageOptions: vi.fn(),
  getCoverageOptions: vi.fn(),
  updateCoverageOptions: vi.fn(),
}))

const mockedHasApiBaseUrl = vi.mocked(hasApiBaseUrl)
const mockedGetProblemMessage = vi.mocked(getProblemMessage)
const mockedCreateEmptyCoverageOptions = vi.mocked(createEmptyCoverageOptions)
const mockedGetCoverageOptions = vi.mocked(getCoverageOptions)
const mockedUpdateCoverageOptions = vi.mocked(updateCoverageOptions)

const emptyCoverageOptions = {
  numeroFolio: '1000001',
  version: 0,
  fechaUltimaActualizacion: '',
  opcionesCobertura: {
    garantiasSeleccionadas: [],
    observaciones: null,
  },
  projectionPerLocation: [],
}

const loadedCoverageOptions = {
  numeroFolio: '1000001',
  version: 2,
  fechaUltimaActualizacion: '2026-04-21T10:47:00Z',
  opcionesCobertura: {
    garantiasSeleccionadas: [
      { garantiaCode: 'GAR-INC-ED', terminos: ['  base  ', ''] },
    ],
    observaciones: '  Cobertura base  ',
  },
  projectionPerLocation: [
    {
      indice: 1,
      garantiasDerivadas: [
        {
          garantiaCode: 'GAR-INC-ED',
          tariffablePreview: true,
          fuenteTecnicaPreview: 'CORE_TARIFF',
          lookupKeyPreview: 'GIRO-001|ZTEV-1|GAR-INC-ED',
          motivosNoTarifable: [],
        },
      ],
      calculablePreview: true,
    },
  ],
}

const updatedCoverageOptions = {
  ...loadedCoverageOptions,
  version: 3,
}

describe('useCoverageOptions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedHasApiBaseUrl.mockReturnValue(true)
    mockedGetProblemMessage.mockReturnValue('No fue posible consultar las opciones de cobertura.')
    mockedCreateEmptyCoverageOptions.mockReturnValue(emptyCoverageOptions)
  })

  it('loads options and keeps the folio placeholder until the service resolves', async () => {
    mockedGetCoverageOptions.mockResolvedValue(loadedCoverageOptions)

    const { result } = renderHook(() => useCoverageOptions('1000001'))

    expect(result.current.options).toEqual(emptyCoverageOptions)
    expect(result.current.loading).toBe(true)

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.error).toBeNull()
    expect(result.current.options).toEqual(loadedCoverageOptions)
    expect(mockedCreateEmptyCoverageOptions).toHaveBeenCalledWith('1000001')
    expect(mockedGetCoverageOptions).toHaveBeenCalledWith('1000001')
  })

  it('saves the current payload and updates the record returned by the service', async () => {
    mockedGetCoverageOptions.mockResolvedValue(loadedCoverageOptions)
    mockedUpdateCoverageOptions.mockResolvedValue(updatedCoverageOptions)

    const { result } = renderHook(() => useCoverageOptions('1000001'))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    const saved = await result.current.saveOptions('1000001', {
      version: 2,
      opcionesCobertura: loadedCoverageOptions.opcionesCobertura,
    })

    expect(saved).toEqual(updatedCoverageOptions)
    expect(mockedUpdateCoverageOptions).toHaveBeenCalledWith('1000001', {
      version: 2,
      opcionesCobertura: loadedCoverageOptions.opcionesCobertura,
    })
    await waitFor(() => {
      expect(result.current.options).toEqual(updatedCoverageOptions)
      expect(result.current.saving).toBe(false)
      expect(result.current.error).toBeNull()
    })
  })

  it('surfaces an error when the folio is missing', async () => {
    const { result } = renderHook(() => useCoverageOptions(undefined))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.options).toBeNull()
    expect(result.current.error).toBe('El número de folio es obligatorio.')
    expect(mockedGetCoverageOptions).not.toHaveBeenCalled()
    expect(mockedUpdateCoverageOptions).not.toHaveBeenCalled()
  })
})
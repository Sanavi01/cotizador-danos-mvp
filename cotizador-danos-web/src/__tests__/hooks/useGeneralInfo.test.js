import '@testing-library/jest-dom/vitest'
import { renderHook, waitFor, act } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useGeneralInfo } from '../../hooks/useGeneralInfo'
import { getProblemMessage, hasApiBaseUrl } from '../../services/httpClient'
import { getGeneralInfo, updateGeneralInfo } from '../../services/quoteGeneralInfoService'

vi.mock('../../services/httpClient', () => ({
  hasApiBaseUrl: vi.fn(),
  getProblemMessage: vi.fn(),
}))

vi.mock('../../services/quoteGeneralInfoService', () => ({
  getGeneralInfo: vi.fn(),
  updateGeneralInfo: vi.fn(),
}))

const mockedHasApiBaseUrl = vi.mocked(hasApiBaseUrl)
const mockedGetProblemMessage = vi.mocked(getProblemMessage)
const mockedGetGeneralInfo = vi.mocked(getGeneralInfo)
const mockedUpdateGeneralInfo = vi.mocked(updateGeneralInfo)

const generalInfoFixture = {
  numeroFolio: '1000011',
  version: 3,
  fechaUltimaActualizacion: '2026-04-21T10:47:00Z',
  datosAsegurado: {
    tipoDocumento: 'NIT',
    numeroDocumento: '900123456',
    nombreORazonSocial: 'ACME SAS',
    correoElectronico: 'contacto@acme.com',
    telefono: '6015550101',
  },
  datosConduccion: {
    codigoAgente: 'AG-101',
    clasificacionRiesgo: 'RISK-A',
    tipoNegocio: 'GIRO-001',
  },
}

const emptyGeneralInfoFixture = {
  numeroFolio: '1000011',
  version: 3,
  fechaUltimaActualizacion: '2026-04-21T10:47:00Z',
  datosAsegurado: {
    tipoDocumento: '',
    numeroDocumento: '',
    nombreORazonSocial: '',
    correoElectronico: '',
    telefono: '',
  },
  datosConduccion: {
    codigoAgente: '',
    clasificacionRiesgo: '',
    tipoNegocio: '',
  },
}

const savedGeneralInfoFixture = {
  ...generalInfoFixture,
  version: 4,
  datosConduccion: {
    ...generalInfoFixture.datosConduccion,
    codigoAgente: 'AG-102',
  },
}

describe('useGeneralInfo', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedHasApiBaseUrl.mockReturnValue(true)
    mockedGetProblemMessage.mockReturnValue('No fue posible completar la operación.')
  })

  it('loads the current general info and saves changes with the current version', async () => {
    mockedGetGeneralInfo.mockResolvedValue(generalInfoFixture)
    mockedUpdateGeneralInfo.mockResolvedValue(savedGeneralInfoFixture)

    const { result } = renderHook(() => useGeneralInfo('1000011'))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.generalInfo).toEqual(generalInfoFixture)
    })

    await act(async () => {
      await result.current.saveGeneralInfo('1000011', {
        version: 3,
        datosAsegurado: generalInfoFixture.datosAsegurado,
        datosConduccion: {
          ...generalInfoFixture.datosConduccion,
          codigoAgente: 'AG-102',
        },
      })
    })

    await waitFor(() => {
      expect(result.current.generalInfo).toEqual(savedGeneralInfoFixture)
      expect(result.current.saving).toBe(false)
    })

    expect(mockedGetGeneralInfo).toHaveBeenCalledWith('1000011')
    expect(mockedUpdateGeneralInfo).toHaveBeenCalledWith('1000011', {
      version: 3,
      datosAsegurado: generalInfoFixture.datosAsegurado,
      datosConduccion: {
        ...generalInfoFixture.datosConduccion,
        codigoAgente: 'AG-102',
      },
    })
  })

  it('loads a blank section shape when the backend returns an empty general info record', async () => {
    mockedGetGeneralInfo.mockResolvedValue(emptyGeneralInfoFixture)

    const { result } = renderHook(() => useGeneralInfo('1000011'))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.generalInfo).toEqual(emptyGeneralInfoFixture)
      expect(result.current.error).toBeNull()
    })
  })

  it('reports a missing folio before calling the service', async () => {
    const { result } = renderHook(() => useGeneralInfo(undefined))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.error).toBe('El número de folio es obligatorio.')
    })

    expect(mockedGetGeneralInfo).not.toHaveBeenCalled()
    expect(mockedUpdateGeneralInfo).not.toHaveBeenCalled()
  })

  it('surfaces backend errors when loading or saving the section', async () => {
    mockedGetProblemMessage.mockReturnValue('No fue posible consultar los datos generales.')
    mockedGetGeneralInfo.mockRejectedValue(new Error('Backend down'))

    const { result } = renderHook(() => useGeneralInfo('1000011'))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.error).toBe('No fue posible consultar los datos generales.')
    })

    mockedGetGeneralInfo.mockResolvedValue(generalInfoFixture)
    mockedGetProblemMessage.mockReturnValue('No fue posible guardar los datos generales.')
    mockedUpdateGeneralInfo.mockRejectedValue(new Error('Save down'))

    await act(async () => {
      await result.current.saveGeneralInfo('1000011', {
        version: 3,
        datosAsegurado: generalInfoFixture.datosAsegurado,
        datosConduccion: generalInfoFixture.datosConduccion,
      })
    })

    await waitFor(() => {
      expect(result.current.error).toBe('No fue posible guardar los datos generales.')
      expect(result.current.saving).toBe(false)
    })
  })
})
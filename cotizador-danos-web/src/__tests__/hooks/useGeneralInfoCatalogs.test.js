import '@testing-library/jest-dom/vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useGeneralInfoCatalogs } from '../../hooks/useGeneralInfoCatalogs'
import { getProblemMessage, hasApiBaseUrl } from '../../services/httpClient'
import { listAgents, listBusinessLines, listRiskClassification } from '../../services/referenceCoreService'

vi.mock('../../services/httpClient', () => ({
  hasApiBaseUrl: vi.fn(),
  getProblemMessage: vi.fn(),
}))

vi.mock('../../services/referenceCoreService', () => ({
  listAgents: vi.fn(),
  listBusinessLines: vi.fn(),
  listRiskClassification: vi.fn(),
}))

const mockedHasApiBaseUrl = vi.mocked(hasApiBaseUrl)
const mockedGetProblemMessage = vi.mocked(getProblemMessage)
const mockedListAgents = vi.mocked(listAgents)
const mockedListBusinessLines = vi.mocked(listBusinessLines)
const mockedListRiskClassification = vi.mocked(listRiskClassification)

const initialCatalogs = {
  agents: [{ codigo: 'AG-101', nombre: 'Agente Sabana', activo: true }],
  riskClassifications: [{ codigo: 'RISK-A', nombre: 'Riesgo alto', activo: true }],
  businessLines: [{ codigo: 'GIRO-001', nombre: 'Oficinas', activo: true, claveIncendio: 'INC-OFI' }],
}

const refreshedCatalogs = {
  agents: [{ codigo: 'AG-102', nombre: 'Agente Centro', activo: true }],
  riskClassifications: [{ codigo: 'RISK-B', nombre: 'Riesgo medio', activo: true }],
  businessLines: [{ codigo: 'GIRO-002', nombre: 'Bodegas', activo: true, claveIncendio: 'INC-BOD' }],
}

describe('useGeneralInfoCatalogs', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedHasApiBaseUrl.mockReturnValue(true)
    mockedGetProblemMessage.mockReturnValue('No fue posible consultar los catálogos de datos generales.')
  })

  it('loads the catalogs on mount', async () => {
    mockedListAgents.mockResolvedValue(initialCatalogs.agents)
    mockedListRiskClassification.mockResolvedValue(initialCatalogs.riskClassifications)
    mockedListBusinessLines.mockResolvedValue(initialCatalogs.businessLines)

    const { result } = renderHook(() => useGeneralInfoCatalogs())

    expect(result.current.loading).toBe(true)

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.error).toBeNull()
    expect(result.current.agents).toEqual(initialCatalogs.agents)
    expect(result.current.riskClassifications).toEqual(initialCatalogs.riskClassifications)
    expect(result.current.businessLines).toEqual(initialCatalogs.businessLines)
    expect(mockedListAgents).toHaveBeenCalledTimes(1)
    expect(mockedListRiskClassification).toHaveBeenCalledTimes(1)
    expect(mockedListBusinessLines).toHaveBeenCalledTimes(1)
  })

  it('reloads the catalogs when requested', async () => {
    mockedListAgents.mockResolvedValueOnce(initialCatalogs.agents).mockResolvedValueOnce(refreshedCatalogs.agents)
    mockedListRiskClassification
      .mockResolvedValueOnce(initialCatalogs.riskClassifications)
      .mockResolvedValueOnce(refreshedCatalogs.riskClassifications)
    mockedListBusinessLines.mockResolvedValueOnce(initialCatalogs.businessLines).mockResolvedValueOnce(refreshedCatalogs.businessLines)

    const { result } = renderHook(() => useGeneralInfoCatalogs())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    await result.current.reload()

    await waitFor(() => {
      expect(result.current.agents).toEqual(refreshedCatalogs.agents)
      expect(result.current.riskClassifications).toEqual(refreshedCatalogs.riskClassifications)
      expect(result.current.businessLines).toEqual(refreshedCatalogs.businessLines)
    })
  })

  it('returns an error when the reference core is unavailable', async () => {
    mockedHasApiBaseUrl.mockReturnValue(false)

    const { result } = renderHook(() => useGeneralInfoCatalogs())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.error).toBe('Define VITE_REFERENCE_CORE_API_URL para consultar los catálogos de datos generales.')
    })

    expect(mockedListAgents).not.toHaveBeenCalled()
    expect(mockedListRiskClassification).not.toHaveBeenCalled()
    expect(mockedListBusinessLines).not.toHaveBeenCalled()
  })

  it('surfaces service errors from the reference core', async () => {
    mockedListAgents.mockRejectedValue(new Error('Core down'))
    mockedListRiskClassification.mockResolvedValue(initialCatalogs.riskClassifications)
    mockedListBusinessLines.mockResolvedValue(initialCatalogs.businessLines)
    mockedGetProblemMessage.mockReturnValue('No fue posible consultar los catálogos de datos generales.')

    const { result } = renderHook(() => useGeneralInfoCatalogs())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.error).toBe('No fue posible consultar los catálogos de datos generales.')
      expect(result.current.agents).toEqual([])
      expect(result.current.riskClassifications).toEqual([])
      expect(result.current.businessLines).toEqual([])
    })
  })
})
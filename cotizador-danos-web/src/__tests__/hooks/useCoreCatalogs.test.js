import '@testing-library/jest-dom/vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useCoreCatalogs } from '../../hooks/useCoreCatalogs'
import { getProblemMessage, hasApiBaseUrl } from '../../services/httpClient'
import {
  listAgents,
  listBusinessLines,
  listGuarantees,
  listRiskClassification,
  listSubscribers,
} from '../../services/referenceCoreService'

vi.mock('../../services/httpClient', () => ({
  hasApiBaseUrl: vi.fn(),
  getProblemMessage: vi.fn(),
}))

vi.mock('../../services/referenceCoreService', () => ({
  listSubscribers: vi.fn(),
  listAgents: vi.fn(),
  listBusinessLines: vi.fn(),
  listRiskClassification: vi.fn(),
  listGuarantees: vi.fn(),
}))

const mockedHasApiBaseUrl = vi.mocked(hasApiBaseUrl)
const mockedGetProblemMessage = vi.mocked(getProblemMessage)
const mockedListSubscribers = vi.mocked(listSubscribers)
const mockedListAgents = vi.mocked(listAgents)
const mockedListBusinessLines = vi.mocked(listBusinessLines)
const mockedListRiskClassification = vi.mocked(listRiskClassification)
const mockedListGuarantees = vi.mocked(listGuarantees)

const referenceFixtures = {
  subscribers: [{ codigo: 'SUS-001', nombre: 'Suscriptor Norte', activo: true }],
  agents: [{ codigo: 'AG-102', nombre: 'Agente Centro', activo: true }],
  businessLines: [{ codigo: 'GIRO-001', nombre: 'Oficinas', activo: true, claveIncendio: 'INC-OFI' }],
  riskClassifications: [{ codigo: 'RISK-A', nombre: 'Riesgo alto', activo: true }],
  guarantees: [{ codigo: 'GAR-INC-ED', nombre: 'Incendio edificios', activo: true }],
}

describe('useCoreCatalogs', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedHasApiBaseUrl.mockReturnValue(true)
    mockedGetProblemMessage.mockReturnValue('No fue posible consultar la referencia core.')
  })

  it('loads catalogs on mount', async () => {
    mockedListSubscribers.mockResolvedValue(referenceFixtures.subscribers)
    mockedListAgents.mockResolvedValue(referenceFixtures.agents)
    mockedListBusinessLines.mockResolvedValue(referenceFixtures.businessLines)
    mockedListRiskClassification.mockResolvedValue(referenceFixtures.riskClassifications)
    mockedListGuarantees.mockResolvedValue(referenceFixtures.guarantees)

    const { result } = renderHook(() => useCoreCatalogs())

    expect(result.current.loading).toBe(true)

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.error).toBeNull()
    expect(result.current.subscribers).toEqual(referenceFixtures.subscribers)
    expect(result.current.agents).toEqual(referenceFixtures.agents)
    expect(result.current.businessLines).toEqual(referenceFixtures.businessLines)
    expect(result.current.riskClassifications).toEqual(referenceFixtures.riskClassifications)
    expect(result.current.guarantees).toEqual(referenceFixtures.guarantees)
    expect(mockedListSubscribers).toHaveBeenCalledTimes(1)
    expect(mockedListAgents).toHaveBeenCalledTimes(1)
    expect(mockedListBusinessLines).toHaveBeenCalledTimes(1)
    expect(mockedListRiskClassification).toHaveBeenCalledTimes(1)
    expect(mockedListGuarantees).toHaveBeenCalledTimes(1)
  })

  it('returns an empty state when the reference core fails', async () => {
    mockedListSubscribers.mockRejectedValue(new Error('Core down'))
    mockedListAgents.mockResolvedValue(referenceFixtures.agents)
    mockedListBusinessLines.mockResolvedValue(referenceFixtures.businessLines)
    mockedListRiskClassification.mockResolvedValue(referenceFixtures.riskClassifications)
    mockedListGuarantees.mockResolvedValue(referenceFixtures.guarantees)

    const { result } = renderHook(() => useCoreCatalogs())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.subscribers).toEqual([])
    expect(result.current.agents).toEqual([])
    expect(result.current.businessLines).toEqual([])
    expect(result.current.riskClassifications).toEqual([])
    expect(result.current.guarantees).toEqual([])
    expect(result.current.error).toBe('No fue posible consultar la referencia core.')
  })
})
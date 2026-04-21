import '@testing-library/jest-dom/vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { FolioPage } from '../../pages/FolioPage'

const navigateMock = vi.fn()
const createFolioMock = vi.fn().mockResolvedValue({
  numeroFolio: '1000001',
  estadoCotizacion: 'BORRADOR',
  version: 0,
  fechaUltimaActualizacion: '2026-04-20T00:00:00Z',
})
const resetMock = vi.fn()
const reloadCatalogsMock = vi.fn()
const validateZipCodeMock = vi.fn()
const lookupTariffMock = vi.fn()

vi.mock('../../hooks/useCoreCatalogs', () => ({
  useCoreCatalogs: () => ({
    subscribers: [{ codigo: 'SUS-001', nombre: 'Suscriptor Norte', activo: true }],
    agents: [{ codigo: 'AG-102', nombre: 'Agente Centro', activo: true }],
    businessLines: [{ codigo: 'GIRO-001', nombre: 'Oficinas', activo: true, claveIncendio: 'INC-OFI' }],
    riskClassifications: [{ codigo: 'RISK-A', nombre: 'Riesgo alto', activo: true }],
    guarantees: [{ codigo: 'GAR-INC-ED', nombre: 'Incendio edificios', activo: true }],
    loading: false,
    error: null,
    reload: reloadCatalogsMock,
  }),
}))

vi.mock('../../hooks/useZipCodeValidation', () => ({
  useZipCodeValidation: () => ({
    validation: null,
    loading: false,
    error: null,
    validateZipCode: validateZipCodeMock,
  }),
}))

vi.mock('../../hooks/useTariffLookup', () => ({
  useTariffLookup: () => ({
    tariff: null,
    loading: false,
    error: null,
    lookupTariff: lookupTariffMock,
  }),
}))

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')

  return {
    ...actual,
    useNavigate: () => navigateMock,
  }
})

vi.mock('../../hooks/useFolioCreation', () => ({
  useFolioCreation: () => ({
    createFolio: createFolioMock,
    loading: false,
    error: null,
    folio: null,
    reset: resetMock,
  }),
}))

describe('FolioPage', () => {
  it('renders the core catalog flow and wires the main actions', () => {
    render(<FolioPage />)

    expect(screen.getByText('SUS-001')).toBeInTheDocument()
    expect(screen.getByText('AG-102')).toBeInTheDocument()
    expect(screen.getByText('GIRO-001')).toBeInTheDocument()
    expect(screen.getByText('RISK-A')).toBeInTheDocument()
    expect(screen.getByText('GAR-INC-ED')).toBeInTheDocument()

    const originInput = screen.getByLabelText(/origen/i)
    fireEvent.change(originInput, { target: { value: '  web  ' } })

    fireEvent.click(screen.getByRole('button', { name: /crear folio/i }))

    expect(createFolioMock).toHaveBeenCalledWith(
      { origin: 'web' },
      expect.any(String),
    )

    fireEvent.change(screen.getByLabelText(/^zip$/i), { target: { value: '110111' } })
    fireEvent.click(screen.getByRole('button', { name: /validar zip/i }))

    expect(validateZipCodeMock).toHaveBeenCalledWith('110111')

    fireEvent.click(screen.getByRole('button', { name: /usar ejemplo/i }))

    expect(lookupTariffMock).toHaveBeenCalledWith('GIRO-001|ZTEV-2|GAR-INC-ED')

    fireEvent.click(screen.getByRole('button', { name: /refrescar cat/i }))

    expect(reloadCatalogsMock).toHaveBeenCalled()

    fireEvent.click(screen.getByRole('button', { name: /nueva llave/i }))

    expect(resetMock).toHaveBeenCalled()
    expect(navigateMock).not.toHaveBeenCalled()
  })
})
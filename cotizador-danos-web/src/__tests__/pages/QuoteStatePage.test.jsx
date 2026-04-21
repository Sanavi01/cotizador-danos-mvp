import '@testing-library/jest-dom/vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { QuoteStatePage } from '../../pages/QuoteStatePage'

const navigateMock = vi.fn()
const refreshMock = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')

  return {
    ...actual,
    useNavigate: () => navigateMock,
    useParams: () => ({ folio: '1000001' }),
  }
})

vi.mock('../../hooks/useQuoteState', () => ({
  useQuoteState: () => ({
    state: {
      numeroFolio: '1000001',
      estadoCotizacion: 'BORRADOR',
      tieneAlertas: false,
      seccionesCompletadas: ['datos generales'],
      ubicacionesCalculables: 1,
      ubicacionesIncompletas: 0,
      version: 0,
      fechaUltimaActualizacion: '2026-04-20T00:00:00Z',
    },
    loading: false,
    error: null,
    refresh: refreshMock,
  }),
}))

describe('QuoteStatePage', () => {
  it('renders the state summary and exposes navigation controls', () => {
    render(<QuoteStatePage />)

    expect(screen.getByText('Estado de cotización')).toBeInTheDocument()
    expect(screen.getByText('BORRADOR')).toBeInTheDocument()
    expect(screen.getByText('datos generales')).toBeInTheDocument()
    expect(screen.getByText('Ubicaciones')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: /actualizar/i }))
    fireEvent.click(screen.getByRole('button', { name: /volver/i }))

    expect(refreshMock).toHaveBeenCalled()
    expect(navigateMock).toHaveBeenCalledWith('/cotizador')
  })
})
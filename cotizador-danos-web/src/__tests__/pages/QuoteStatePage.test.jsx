import '@testing-library/jest-dom/vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
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
      estadoCotizacion: 'EN_CAPTURA',
      version: 4,
      fechaUltimaActualizacion: '2026-04-20T00:00:00Z',
      progreso: {
        datosGenerales: 'COMPLETED',
        layoutUbicaciones: 'COMPLETED',
        ubicaciones: 'INCOMPLETE',
        opcionesCobertura: 'COMPLETED',
      },
      resumenUbicaciones: {
        totalEsperado: 3,
        totalActual: 2,
        calculables: 1,
        incompletas: 0,
        invalidas: 1,
        conAlertas: 1,
      },
      tieneAlertas: true,
      alertasVigentes: [
        {
          codigo: 'UBICACION_SIN_ZIP',
          mensaje: 'La ubicacion no tiene codigo postal valido.',
          severidad: 'Warning',
        },
      ],
      readyToCalculate: false,
      resultadoFinanciero: {
        primaNeta: 60000,
        primaComercial: 70200,
        ubicacionesCalculadas: 1,
        ubicacionesNoCalculables: 2,
        estadoCalculo: 'PARCIAL',
        calculatedAt: '2026-04-21T00:00:00Z',
        calculationParameterVersion: '1.0.0',
      },
    },
    loading: false,
    error: null,
    refresh: refreshMock,
  }),
}))

describe('QuoteStatePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders the state summary and exposes navigation controls', () => {
    render(<QuoteStatePage />)

    expect(screen.getByText('Estado de cotización')).toBeInTheDocument()
    expect(screen.getByText('EN_CAPTURA')).toBeInTheDocument()
    expect(screen.getAllByText('Datos generales')).toHaveLength(2)
    expect(screen.getAllByText('Layout de ubicaciones')).toHaveLength(2)
    expect(screen.getByText('Ubicaciones consolidadas')).toBeInTheDocument()
    expect(screen.getByText('UBICACION_SIN_ZIP')).toBeInTheDocument()
    expect(screen.getByText('PARCIAL')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: /actualizar/i }))
    fireEvent.click(screen.getByRole('button', { name: 'Datos generales' }))
    fireEvent.click(screen.getByRole('button', { name: 'Layout de ubicaciones' }))
    fireEvent.click(screen.getByRole('button', { name: 'Ubicaciones' }))
    fireEvent.click(screen.getByRole('button', { name: 'Volver' }))

    expect(refreshMock).toHaveBeenCalled()
    expect(navigateMock).toHaveBeenCalledWith('/quotes/1000001/general-info')
    expect(navigateMock).toHaveBeenCalledWith('/quotes/1000001/locations/layout')
    expect(navigateMock).toHaveBeenCalledWith('/quotes/1000001/locations')
    expect(navigateMock).toHaveBeenCalledWith('/cotizador')
  })
})
import '@testing-library/jest-dom/vitest'
import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { QuoteProgressCard } from '../../components/QuoteProgressCard'

describe('QuoteProgressCard', () => {
  it('renders the consolidated quote progress, alerts and financial snapshot', () => {
    render(
      <QuoteProgressCard
        state={{
          numeroFolio: '1000001',
          estadoCotizacion: 'CALCULADA',
          version: 5,
          fechaUltimaActualizacion: '2026-04-21T00:00:00Z',
          progreso: {
            datosGenerales: 'COMPLETED',
            layoutUbicaciones: 'COMPLETED',
            ubicaciones: 'INCOMPLETE',
            opcionesCobertura: 'COMPLETED',
          },
          resumenUbicaciones: {
            totalEsperado: 3,
            totalActual: 3,
            calculables: 1,
            incompletas: 1,
            invalidas: 1,
            conAlertas: 2,
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
        }}
      />,
    )

    expect(screen.getByText('Resumen de trazabilidad')).toBeInTheDocument()
    expect(screen.getByText('1000001')).toBeInTheDocument()
    expect(screen.getByText('CALCULADA')).toBeInTheDocument()
    expect(screen.getByText('Aún en captura')).toBeInTheDocument()
    expect(screen.getByText('Datos generales')).toBeInTheDocument()
    expect(screen.getByText('Layout de ubicaciones')).toBeInTheDocument()
    expect(screen.getByText('Ubicaciones')).toBeInTheDocument()
    expect(screen.getByText('Opciones de cobertura')).toBeInTheDocument()
    expect(screen.getByText('UBICACION_SIN_ZIP')).toBeInTheDocument()
    expect(screen.getByText('La ubicacion no tiene codigo postal valido.')).toBeInTheDocument()
    expect(screen.getByText('PARCIAL')).toBeInTheDocument()
    expect(screen.getByText('1.0.0')).toBeInTheDocument()
  })

  it('renders the empty snapshot message when no state is available', () => {
    render(<QuoteProgressCard state={null} />)

    expect(screen.getByText('Consultando el progreso del folio.')).toBeInTheDocument()
    expect(
      screen.getByText('El backend devolverá aquí el snapshot operativo con estado, progreso, alertas y resultado financiero vigente.'),
    ).toBeInTheDocument()
  })
})
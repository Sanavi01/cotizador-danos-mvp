import '@testing-library/jest-dom/vitest'
import { renderHook } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { useQuoteProgress } from '../../hooks/useQuoteProgress'

describe('useQuoteProgress', () => {
  it('returns incomplete fallback values when no state is provided', () => {
    const { result } = renderHook(() => useQuoteProgress(null))

    expect(result.current.progress).toEqual({
      datosGenerales: 'INCOMPLETE',
      layoutUbicaciones: 'INCOMPLETE',
      ubicaciones: 'INCOMPLETE',
      opcionesCobertura: 'INCOMPLETE',
    })
    expect(result.current.summary).toEqual({
      totalEsperado: 0,
      totalActual: 0,
      calculables: 0,
      incompletas: 0,
      invalidas: 0,
      conAlertas: 0,
    })
    expect(result.current.alerts).toEqual([])
    expect(result.current.financialSummary).toBeNull()
    expect(result.current.readyToCalculate).toBe(false)
    expect(result.current.sections).toHaveLength(4)
  })

  it('derives progress summary, alerts and financial data from the current state', () => {
    const state = {
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
    }

    const { result } = renderHook(() => useQuoteProgress(state))

    expect(result.current.progress).toEqual(state.progreso)
    expect(result.current.summary).toEqual(state.resumenUbicaciones)
    expect(result.current.alerts).toEqual(state.alertasVigentes)
    expect(result.current.financialSummary).toEqual(state.resultadoFinanciero)
    expect(result.current.readyToCalculate).toBe(false)
    expect(result.current.sections.map((section) => section.label)).toEqual([
      'Datos generales',
      'Layout de ubicaciones',
      'Ubicaciones',
      'Opciones de cobertura',
    ])
  })
})
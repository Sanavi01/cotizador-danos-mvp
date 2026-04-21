import type {
  QuoteFinancialSummary,
  QuoteLocationsSummary,
  QuoteProgressStatus,
  QuoteProgressSummary,
  QuoteStateResponse,
} from '../services/folioService'

export interface QuoteProgressSection {
  key: keyof QuoteProgressSummary
  label: string
  status: QuoteProgressStatus
}

const emptyProgress: QuoteProgressSummary = {
  datosGenerales: 'INCOMPLETE',
  layoutUbicaciones: 'INCOMPLETE',
  ubicaciones: 'INCOMPLETE',
  opcionesCobertura: 'INCOMPLETE',
}

const emptySummary: QuoteLocationsSummary = {
  totalEsperado: 0,
  totalActual: 0,
  calculables: 0,
  incompletas: 0,
  invalidas: 0,
  conAlertas: 0,
}

export function useQuoteProgress(state: QuoteStateResponse | null) {
  const progress = state?.progreso ?? emptyProgress
  const summary = state?.resumenUbicaciones ?? emptySummary
  const alerts = state?.alertasVigentes ?? []
  const financialSummary: QuoteFinancialSummary | null = state?.resultadoFinanciero ?? null
  const readyToCalculate = state?.readyToCalculate ?? false

  const sections: QuoteProgressSection[] = [
    { key: 'datosGenerales', label: 'Datos generales', status: progress.datosGenerales },
    { key: 'layoutUbicaciones', label: 'Layout de ubicaciones', status: progress.layoutUbicaciones },
    { key: 'ubicaciones', label: 'Ubicaciones', status: progress.ubicaciones },
    { key: 'opcionesCobertura', label: 'Opciones de cobertura', status: progress.opcionesCobertura },
  ]

  return {
    progress,
    summary,
    alerts,
    financialSummary,
    readyToCalculate,
    sections,
  }
}
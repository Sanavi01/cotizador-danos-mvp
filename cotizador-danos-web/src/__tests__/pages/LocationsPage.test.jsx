import '@testing-library/jest-dom/vitest'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { LocationsPage } from '../../pages/LocationsPage'

const navigateMock = vi.fn()
const loadLocationsMock = vi.fn()
const saveLocationsMock = vi.fn()
const createEmptyLocationMock = vi.fn()
const refreshSummaryMock = vi.fn()
const validateZipCodeMock = vi.fn()
const resetZipCodeValidationMock = vi.fn()

let locationsState
let summaryState
let locationCatalogsState
let zipCodeValidationState

function createLocation(indice, overrides = {}) {
  return {
    indice,
    nombreUbicacion: `Ubicación ${indice}`,
    direccion: `Calle ${indice}`,
    codigoPostal: '110111',
    estado: 'Bogota D.C.',
    municipio: 'Bogota',
    colonia: 'Chapinero',
    ciudad: 'Bogota',
    tipoConstructivo: 'CONCRETO',
    nivel: 1,
    anioConstruccion: 2018,
    giro: {
      codigo: 'GIRO-001',
      nombre: 'Manufactura ligera',
      claveIncendio: 'CI-001',
    },
    garantias: [],
    zonaCatastrofica: {
      zonaTev: 'Z-TEV-01',
      zonaFhm: 'Z-FHM-01',
    },
    estadoValidacion: 'CALCULABLE',
    alertasBloqueantes: [],
    createdAt: '2026-04-21T00:00:00Z',
    updatedAt: '2026-04-21T00:00:00Z',
    ...overrides,
  }
}

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')

  return {
    ...actual,
    useNavigate: () => navigateMock,
    useParams: () => ({ folio: '1000001' }),
  }
})

vi.mock('../../hooks/useLocations', () => ({
  useLocations: () => locationsState,
}))

vi.mock('../../hooks/useLocationSummary', () => ({
  useLocationSummary: () => summaryState,
}))

vi.mock('../../hooks/useLocationCatalogs', () => ({
  useLocationCatalogs: () => locationCatalogsState,
}))

vi.mock('../../hooks/useZipCodeValidation', () => ({
  useZipCodeValidation: () => zipCodeValidationState,
}))

afterEach(() => {
  cleanup()
})

describe('LocationsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    locationsState = {
      locations: [createLocation(1)],
      version: 3,
      fechaUltimaActualizacion: '2026-04-21T00:00:00Z',
      loading: false,
      saving: false,
      error: null,
      loadLocations: loadLocationsMock,
      saveLocations: saveLocationsMock,
      updateLocation: vi.fn(),
      reset: vi.fn(),
      createEmptyLocation: createEmptyLocationMock,
    }

    summaryState = {
      summary: {
        numeroFolio: '1000001',
        totalEsperado: 2,
        totalActual: 1,
        calculables: 1,
        incompletas: 0,
        invalidas: 0,
        conAlertas: 0,
        resumenPorIndice: [
          { indice: 1, slotEsperado: true, estadoValidacion: 'CALCULABLE', tieneAlertasBloqueantes: false },
          { indice: 2, slotEsperado: true, estadoValidacion: 'EMPTY', tieneAlertasBloqueantes: false },
        ],
      },
      loading: false,
      error: null,
      refresh: refreshSummaryMock,
    }

    locationCatalogsState = {
      businessLines: [],
      loading: false,
      error: null,
    }

    zipCodeValidationState = {
      validation: null,
      loading: false,
      error: null,
      validateZipCode: validateZipCodeMock,
      reset: resetZipCodeValidationMock,
    }
  })

  it('renders the list, summary and sends the full snapshot when saving', async () => {
    const user = userEvent.setup()
    saveLocationsMock.mockResolvedValue([
      createLocation(1, {
        nombreUbicacion: 'Ubicación principal ajustada',
      }),
    ])

    render(<LocationsPage />)

    expect(screen.getByText('Ubicaciones de cotización')).toBeInTheDocument()
    expect(screen.getByText('Lista de ubicaciones')).toBeInTheDocument()
    expect(screen.getByText('Resumen operativo')).toBeInTheDocument()
    expect(await screen.findByDisplayValue('Ubicación 1')).toBeInTheDocument()

    await user.clear(screen.getByLabelText(/nombre de ubicación/i))
    await user.type(screen.getByLabelText(/nombre de ubicación/i), 'Ubicación principal ajustada')
    await user.click(screen.getByRole('button', { name: /guardar snapshot completo/i }))

    await waitFor(() => {
      expect(saveLocationsMock).toHaveBeenCalledWith(
        '1000001',
        {
          version: 3,
          ubicaciones: [
            expect.objectContaining({
              indice: 1,
              nombreUbicacion: 'Ubicación principal ajustada',
            }),
          ],
        },
      )
    })
    expect(refreshSummaryMock).toHaveBeenCalledWith('1000001')
  })

  it('starts the first pending slot from the empty-state CTA and the summary empty slot', async () => {
    const user = userEvent.setup()
    const emptySlot = createLocation(1, {
      nombreUbicacion: null,
      direccion: null,
      codigoPostal: null,
      estado: null,
      municipio: null,
      colonia: null,
      ciudad: null,
      tipoConstructivo: null,
      nivel: null,
      anioConstruccion: null,
      giro: null,
      garantias: [],
      zonaCatastrofica: null,
      estadoValidacion: 'EMPTY',
      createdAt: null,
      updatedAt: null,
    })

    locationsState = {
      ...locationsState,
      locations: [],
      version: 0,
    }
    summaryState = {
      ...summaryState,
      summary: {
        ...summaryState.summary,
        totalActual: 0,
        calculables: 0,
        resumenPorIndice: [{ indice: 1, slotEsperado: true, estadoValidacion: 'EMPTY', tieneAlertasBloqueantes: false }],
      },
    }
    createEmptyLocationMock.mockReturnValue(emptySlot)

    render(<LocationsPage />)

    expect(screen.getByText(/todavía no tiene ubicaciones persistidas/i)).toBeInTheDocument()

    await user.click(screen.getAllByRole('button', { name: /capturar ubicación 1/i })[0])

    expect(createEmptyLocationMock).toHaveBeenCalledWith(1)
    expect(await screen.findByLabelText(/nombre de ubicación/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /guardar snapshot completo/i })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /#1.*empty.*sin alertas/i }))

    expect(createEmptyLocationMock).toHaveBeenCalledTimes(2)
  })

  it('keeps the unsaved draft selected when opening a new empty slot while another location already exists', async () => {
    const user = userEvent.setup()
    const pendingSlot = createLocation(2, {
      nombreUbicacion: null,
      direccion: null,
      codigoPostal: null,
      estado: null,
      municipio: null,
      colonia: null,
      ciudad: null,
      tipoConstructivo: null,
      nivel: null,
      anioConstruccion: null,
      giro: null,
      garantias: [],
      zonaCatastrofica: null,
      estadoValidacion: 'EMPTY',
      createdAt: null,
      updatedAt: null,
    })

    createEmptyLocationMock.mockReturnValue(pendingSlot)

    render(<LocationsPage />)

    await user.click(screen.getByRole('button', { name: /#2.*empty.*sin alertas/i }))

    expect(createEmptyLocationMock).toHaveBeenCalledWith(2)
    expect(await screen.findByRole('heading', { name: /ubicación 2/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/nombre de ubicación/i)).toHaveValue('')
  })
})

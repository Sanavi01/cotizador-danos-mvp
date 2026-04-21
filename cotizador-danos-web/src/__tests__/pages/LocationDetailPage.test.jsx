import '@testing-library/jest-dom/vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { LocationDetailPage } from '../../pages/LocationDetailPage'

const navigateMock = vi.fn()
const loadLocationMock = vi.fn()
const saveLocationMock = vi.fn()
const validateZipCodeMock = vi.fn()
const resetZipCodeValidationMock = vi.fn()

let editorState
let locationCatalogsState
let zipCodeValidationState

function createLocation(overrides = {}) {
  return {
    indice: 2,
    nombreUbicacion: 'Bodega secundaria',
    direccion: 'Calle 50 # 20-30',
    codigoPostal: '050001',
    estado: 'Antioquia',
    municipio: 'Medellin',
    colonia: null,
    ciudad: 'Medellin',
    tipoConstructivo: 'CONCRETO',
    nivel: 1,
    anioConstruccion: 2018,
    giro: {
      codigo: 'GIRO-002',
      nombre: 'Bodega',
      claveIncendio: 'CI-010',
    },
    garantias: [],
    zonaCatastrofica: {
      zonaTev: 'Z-TEV-02',
      zonaFhm: 'Z-FHM-01',
    },
    estadoValidacion: 'VALID',
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
    useParams: () => ({ folio: '1000001', indice: '2' }),
  }
})

vi.mock('../../hooks/useLocationEditor', () => ({
  useLocationEditor: () => editorState,
}))

vi.mock('../../hooks/useLocationCatalogs', () => ({
  useLocationCatalogs: () => locationCatalogsState,
}))

vi.mock('../../hooks/useZipCodeValidation', () => ({
  useZipCodeValidation: () => zipCodeValidationState,
}))

describe('LocationDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    editorState = {
      location: createLocation(),
      version: 3,
      fechaUltimaActualizacion: '2026-04-21T00:00:00Z',
      loading: false,
      saving: false,
      error: null,
      loadLocation: loadLocationMock,
      saveLocation: saveLocationMock,
      reset: vi.fn(),
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

  it('sends the puntual update payload with the current version', async () => {
    const user = userEvent.setup()
    saveLocationMock.mockResolvedValue(createLocation({ nombreUbicacion: 'Bodega ajustada' }))

    render(<LocationDetailPage />)

    expect(screen.getByText(/detalle de ubicación/i)).toBeInTheDocument()
    expect(await screen.findByDisplayValue('Bodega secundaria')).toBeInTheDocument()

    await user.clear(screen.getByLabelText(/nombre de ubicación/i))
    await user.type(screen.getByLabelText(/nombre de ubicación/i), 'Bodega ajustada')
    await user.click(screen.getByRole('button', { name: /guardar ubicación puntual/i }))

    await waitFor(() => {
      expect(saveLocationMock).toHaveBeenCalledWith(
        '1000001',
        2,
        expect.objectContaining({
          version: 3,
          changes: expect.objectContaining({
            nombreUbicacion: 'Bodega ajustada',
            codigoPostal: '050001',
          }),
        }),
      )
    })
  })
})

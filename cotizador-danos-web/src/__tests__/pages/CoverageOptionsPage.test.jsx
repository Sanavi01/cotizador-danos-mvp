import '@testing-library/jest-dom/vitest'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { CoverageOptionsPage } from '../../pages/CoverageOptionsPage'

const navigateMock = vi.fn()
const reloadCatalogsMock = vi.fn()
const loadOptionsMock = vi.fn()
const saveOptionsMock = vi.fn()

let coverageState
let catalogsState

const initialCoverageOptions = {
  numeroFolio: '1000001',
  version: 2,
  fechaUltimaActualizacion: '2026-04-21T10:47:00Z',
  opcionesCobertura: {
    garantiasSeleccionadas: [{ garantiaCode: 'GAR-INC-ED', terminos: [] }],
    observaciones: 'Cobertura base para el análisis inicial',
  },
  projectionPerLocation: [
    {
      indice: 1,
      garantiasDerivadas: [
        {
          garantiaCode: 'GAR-INC-ED',
          tariffablePreview: true,
          fuenteTecnicaPreview: 'CORE_TARIFF',
          lookupKeyPreview: 'GIRO-001|ZTEV-1|GAR-INC-ED',
          motivosNoTarifable: [],
        },
        {
          garantiaCode: 'GAR-ROBO',
          tariffablePreview: false,
          fuenteTecnicaPreview: 'UNRESOLVED',
          lookupKeyPreview: null,
          motivosNoTarifable: ['No existe tarifa vigente para la combinacion actual de la ubicacion.'],
        },
      ],
      calculablePreview: true,
    },
  ],
}

const savedCoverageOptions = {
  ...initialCoverageOptions,
  version: 3,
}

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')

  return {
    ...actual,
    useNavigate: () => navigateMock,
    useParams: () => ({ folio: '1000001' }),
  }
})

vi.mock('../../hooks/useCoverageOptions', () => ({
  useCoverageOptions: () => coverageState,
}))

vi.mock('../../hooks/useCoverageCatalogs', () => ({
  useCoverageCatalogs: () => catalogsState,
}))

afterEach(() => {
  cleanup()
})

describe('CoverageOptionsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    coverageState = {
      options: initialCoverageOptions,
      loading: false,
      saving: false,
      error: null,
      loadOptions: loadOptionsMock,
      saveOptions: saveOptionsMock,
      reset: vi.fn(),
    }

    catalogsState = {
      guarantees: [
        { codigo: 'GAR-INC-ED', nombre: 'Incendio edificios', activo: true, claveIncendio: 'INC-ED' },
      ],
      loading: false,
      error: null,
      reload: reloadCatalogsMock,
    }

    saveOptionsMock.mockResolvedValue(savedCoverageOptions)
  })

  it('renders the current coverage snapshot, shows the preview projection and saves with the active version', async () => {
    const user = userEvent.setup()

    render(<CoverageOptionsPage />)

    expect(screen.getByRole('heading', { name: '1000001' })).toBeInTheDocument()
    expect(screen.getByText('Configuración vigente')).toBeInTheDocument()
    expect(
      screen.getByText((content, element) => element?.textContent === 'Versión actual 2'),
    ).toBeInTheDocument()
    expect(screen.getByText('Ubicación 1')).toBeInTheDocument()
    expect(screen.getByText('Tarifable')).toBeInTheDocument()
    expect(screen.getByText('No tarifable')).toBeInTheDocument()

    await user.type(screen.getByLabelText(/observaciones/i), ' ajustada')
    await user.click(screen.getByRole('button', { name: /guardar opciones/i }))

    await waitFor(() => {
      expect(saveOptionsMock).toHaveBeenCalledWith('1000001', {
        version: 2,
        opcionesCobertura: {
          garantiasSeleccionadas: [{ garantiaCode: 'GAR-INC-ED', terminos: [] }],
          observaciones: 'Cobertura base para el análisis inicial ajustada',
        },
      })
    })

    expect(await screen.findByText('Las opciones de cobertura se guardaron correctamente.')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /estado del folio/i }))

    expect(navigateMock).toHaveBeenCalledWith('/quotes/1000001/state')
  })
})
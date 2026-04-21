import '@testing-library/jest-dom/vitest'
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { GeneralInfoPage } from '../../pages/GeneralInfoPage'

const navigateMock = vi.fn()
const loadGeneralInfoMock = vi.fn()
const saveGeneralInfoMock = vi.fn()
const reloadCatalogsMock = vi.fn()
let currentGeneralInfo = {
  numeroFolio: '1000011',
  version: 3,
  fechaUltimaActualizacion: '2026-04-21T10:47:00Z',
  datosAsegurado: {
    tipoDocumento: 'NIT',
    numeroDocumento: '900123456',
    nombreORazonSocial: 'ACME SAS',
    correoElectronico: 'contacto@acme.com',
    telefono: '6015550101',
  },
  datosConduccion: {
    codigoAgente: 'AG-101',
    clasificacionRiesgo: 'RISK-A',
    tipoNegocio: 'GIRO-001',
  },
}

const emptyGeneralInfoFixture = {
  numeroFolio: '1000011',
  version: 3,
  fechaUltimaActualizacion: '2026-04-21T10:47:00Z',
  datosAsegurado: {
    tipoDocumento: '',
    numeroDocumento: '',
    nombreORazonSocial: '',
    correoElectronico: '',
    telefono: '',
  },
  datosConduccion: {
    codigoAgente: '',
    clasificacionRiesgo: '',
    tipoNegocio: '',
  },
}

const generalInfoFixture = {
  numeroFolio: '1000011',
  version: 3,
  fechaUltimaActualizacion: '2026-04-21T10:47:00Z',
  datosAsegurado: {
    tipoDocumento: 'NIT',
    numeroDocumento: '900123456',
    nombreORazonSocial: 'ACME SAS',
    correoElectronico: 'contacto@acme.com',
    telefono: '6015550101',
  },
  datosConduccion: {
    codigoAgente: 'AG-101',
    clasificacionRiesgo: 'RISK-A',
    tipoNegocio: 'GIRO-001',
  },
}

const savedGeneralInfoFixture = {
  ...generalInfoFixture,
  version: 4,
  datosAsegurado: {
    ...generalInfoFixture.datosAsegurado,
    nombreORazonSocial: 'ACME LTDA',
  },
}

const catalogs = {
  agents: [{ codigo: 'AG-101', nombre: 'Agente Sabana', activo: true }],
  riskClassifications: [{ codigo: 'RISK-A', nombre: 'Riesgo alto', activo: true }],
  businessLines: [{ codigo: 'GIRO-001', nombre: 'Oficinas', activo: true, claveIncendio: 'INC-OFI' }],
}

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')

  return {
    ...actual,
    useNavigate: () => navigateMock,
    useParams: () => ({ folio: '1000011' }),
  }
})

vi.mock('../../hooks/useGeneralInfo', () => ({
  useGeneralInfo: () => ({
    generalInfo: currentGeneralInfo,
    loading: false,
    saving: false,
    error: null,
    loadGeneralInfo: loadGeneralInfoMock,
    saveGeneralInfo: saveGeneralInfoMock,
    reset: vi.fn(),
  }),
}))

vi.mock('../../hooks/useGeneralInfoCatalogs', () => ({
  useGeneralInfoCatalogs: () => ({
    agents: catalogs.agents,
    riskClassifications: catalogs.riskClassifications,
    businessLines: catalogs.businessLines,
    loading: false,
    error: null,
    reload: reloadCatalogsMock,
  }),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
  currentGeneralInfo = generalInfoFixture
})

describe('GeneralInfoPage', () => {
  it('renders the loaded section and saves the edited payload with the current version', async () => {
    const user = userEvent.setup()

    render(<GeneralInfoPage />)

    expect(await screen.findByText('Datos generales de cotización')).toBeInTheDocument()
    expect(screen.getByText('1000011')).toBeInTheDocument()
    expect(await screen.findByDisplayValue('ACME SAS')).toBeInTheDocument()

    await user.clear(screen.getByPlaceholderText('ACME SAS'))
    await user.type(screen.getByPlaceholderText('ACME SAS'), 'ACME LTDA')
    await user.click(screen.getByRole('button', { name: /guardar datos generales/i }))

    expect(saveGeneralInfoMock).toHaveBeenCalledWith('1000011', {
      version: 3,
      datosAsegurado: {
        tipoDocumento: 'NIT',
        numeroDocumento: '900123456',
        nombreORazonSocial: 'ACME LTDA',
        correoElectronico: 'contacto@acme.com',
        telefono: '6015550101',
      },
      datosConduccion: {
        codigoAgente: 'AG-101',
        clasificacionRiesgo: 'RISK-A',
        tipoNegocio: 'GIRO-001',
      },
    })

    await user.click(screen.getByRole('button', { name: /estado del folio/i }))

    expect(navigateMock).toHaveBeenCalledWith('/quotes/1000011/state')
  })

  it('shows the empty-section notice when the folio exists without general info', () => {
    currentGeneralInfo = emptyGeneralInfoFixture

    render(<GeneralInfoPage />)

    expect(
      screen.getByText('Esta cotización existe, pero aún no tiene datos generales capturados. Completa el formulario para iniciar la edición.'),
    ).toBeInTheDocument()
  })
})
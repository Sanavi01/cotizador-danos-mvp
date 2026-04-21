import '@testing-library/jest-dom/vitest'
import { cleanup, fireEvent, render, screen, within, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { LocationsLayoutPage } from '../../pages/LocationsLayoutPage'

const navigateMock = vi.fn()
const loadLayoutMock = vi.fn()
const saveLayoutMock = vi.fn()

const currentLayout = {
  numeroFolio: '1000013',
  version: 1,
  fechaUltimaActualizacion: '2026-04-21T12:12:00Z',
  configuracionLayout: {
    modoCaptura: 'MULTIPLE',
    cantidadUbicaciones: 5,
    ubicaciones: [
      { indice: 2, ordenCaptura: 1 },
      { indice: 4, ordenCaptura: 2 },
      { indice: 3, ordenCaptura: 3 },
      { indice: 1, ordenCaptura: 4 },
      { indice: 5, ordenCaptura: 5 },
    ],
  },
}

const savedLayout = {
  ...currentLayout,
  version: 2,
}

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')

  return {
    ...actual,
    useNavigate: () => navigateMock,
    useParams: () => ({ folio: '1000013' }),
  }
})

vi.mock('../../hooks/useLocationsLayout', () => ({
  useLocationsLayout: () => ({
    layout: currentLayout,
    loading: false,
    saving: false,
    error: null,
    loadLayout: loadLayoutMock,
    saveLayout: saveLayoutMock,
    reset: vi.fn(),
  }),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('LocationsLayoutPage', () => {
  it('renders the form and the preview with the same capture order', () => {
    render(<LocationsLayoutPage />)

    const formSection = screen.getByRole('heading', { name: /orden de captura previsto/i }).closest('section')
    const previewSection = screen.getByRole('heading', { name: /ubicaciones planificadas/i }).closest('section')

    expect(formSection).not.toBeNull()
    expect(previewSection).not.toBeNull()

    expect(within(formSection).getAllByText(/Ubicación \d+/).map((element) => element.textContent)).toEqual([
      'Ubicación 2',
      'Ubicación 4',
      'Ubicación 3',
      'Ubicación 1',
      'Ubicación 5',
    ])
    expect(within(previewSection).getAllByText(/Ubicación \d+/).map((element) => element.textContent)).toEqual([
      'Ubicación 2',
      'Ubicación 4',
      'Ubicación 3',
      'Ubicación 1',
      'Ubicación 5',
    ])
  })

  it('keeps the form editable after an out-of-range capture order and saves once corrected', async () => {
    saveLayoutMock.mockResolvedValue(savedLayout)

    render(<LocationsLayoutPage />)

    const orderInputs = screen.getAllByLabelText(/orden de captura/i)
    const saveButton = screen.getByRole('button', { name: /guardar layout/i })
    const formElement = saveButton.closest('form')

    fireEvent.change(orderInputs[4], { target: { value: '7' } })
    fireEvent.submit(formElement)

    await waitFor(() => {
      expect(
        screen.getByText('El orden de captura no puede ser mayor que la cantidad de ubicaciones configurada.'),
      ).toBeInTheDocument()
    })
    expect(saveLayoutMock).not.toHaveBeenCalled()

    fireEvent.change(orderInputs[4], { target: { value: '5' } })
    fireEvent.submit(formElement)

    await waitFor(() => {
      expect(saveLayoutMock).toHaveBeenCalledWith('1000013', {
        version: 1,
        configuracionLayout: currentLayout.configuracionLayout,
      })
    })
    expect(screen.getByText('La configuración del layout se guardó correctamente.')).toBeInTheDocument()
    expect(
      screen.getByText((content, element) => element.textContent === 'Versión actual 2'),
    ).toBeInTheDocument()
  })
})
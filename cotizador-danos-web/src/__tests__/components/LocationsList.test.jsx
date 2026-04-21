import '@testing-library/jest-dom/vitest'
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { LocationsList } from '../../components/LocationsList'

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

describe('LocationsList', () => {
  const onEdit = vi.fn()
  const onSelect = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    cleanup()
  })

  it('renders the persisted locations with their current state', () => {
    render(
      <LocationsList
        locations={[
          createLocation(1),
          createLocation(2, {
            estadoValidacion: 'INVALID',
            alertasBloqueantes: [{ codigo: 'UBICACION_SIN_ZIP', mensaje: 'Código postal inválido.', severidad: 'Warning' }],
          }),
        ]}
        onEdit={onEdit}
        onSelect={onSelect}
      />,
    )

    expect(screen.getByText('Lista de ubicaciones')).toBeInTheDocument()
    expect(screen.getAllByText('Ubicación 1')).toHaveLength(2)
    expect(screen.getAllByText('Ubicación 2')).toHaveLength(2)
    expect(screen.getByText('CALCULABLE')).toBeInTheDocument()
    expect(screen.getByText('INVALID')).toBeInTheDocument()
  })

  it('emits selection and detail actions for the requested location', async () => {
    const user = userEvent.setup()
    const location = createLocation(3)

    render(<LocationsList locations={[location]} onEdit={onEdit} onSelect={onSelect} />)

    await user.click(screen.getAllByRole('button', { name: /seleccionar/i })[0])
    await user.click(screen.getAllByRole('button', { name: /abrir detalle/i })[0])

    expect(onSelect).toHaveBeenCalledWith(location)
    expect(onEdit).toHaveBeenCalledWith(location)
  })
})

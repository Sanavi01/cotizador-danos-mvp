import '@testing-library/jest-dom/vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { CatalogLookupPanel } from '../../components/CatalogLookupPanel'

const catalogItems = [
  { codigo: 'SUS-001', nombre: 'Suscriptor Norte', activo: true },
  { codigo: 'SUS-002', nombre: 'Suscriptor Sur', activo: false },
]

describe('CatalogLookupPanel', () => {
  it('renders items and filters them by name', () => {
    const onFilterChange = vi.fn()

    const { rerender } = render(
      <CatalogLookupPanel
        title="Suscriptores"
        items={catalogItems}
        loading={false}
        error={null}
        filterValue=""
        onFilterChange={onFilterChange}
      />,
    )

    expect(screen.getByRole('heading', { name: 'Suscriptores' })).toBeInTheDocument()
    expect(screen.getByText('SUS-001')).toBeInTheDocument()
    expect(screen.getByText('SUS-002')).toBeInTheDocument()
    expect(screen.getByText('Suscriptor Norte')).toBeInTheDocument()
    expect(screen.getByText('Suscriptor Sur')).toBeInTheDocument()

    fireEvent.change(screen.getByLabelText(/filtrar por nombre/i), { target: { value: 'norte' } })

    expect(onFilterChange).toHaveBeenCalledWith('norte')

    rerender(
      <CatalogLookupPanel
        title="Suscriptores"
        items={catalogItems}
        loading={false}
        error={null}
        filterValue="norte"
        onFilterChange={onFilterChange}
      />,
    )

    expect(screen.getByText('SUS-001')).toBeInTheDocument()
    expect(screen.queryByText('SUS-002')).not.toBeInTheDocument()
    expect(screen.queryByText('Suscriptor Sur')).not.toBeInTheDocument()
  })

  it('shows loading and error states instead of the list', () => {
    render(
      <CatalogLookupPanel
        title="Giros"
        items={catalogItems}
        loading={true}
        error="No fue posible cargar el catalogo."
        filterValue=""
        onFilterChange={vi.fn()}
      />,
    )

    expect(screen.getByText('Cargando catálogo...')).toBeInTheDocument()
    expect(screen.getByText('No fue posible cargar el catalogo.')).toBeInTheDocument()
  })
})
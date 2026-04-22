import '@testing-library/jest-dom/vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useState } from 'react'
import { describe, expect, it, vi } from 'vitest'
import { CoverageOptionsForm } from '../../components/CoverageOptionsForm'

const guarantees = [
  { codigo: 'GAR-INC-ED', nombre: 'Incendio edificios', activo: true, claveIncendio: 'INC-ED' },
  { codigo: 'GAR-ROBO', nombre: 'Hurto calificado', activo: false, claveIncendio: 'ROBO' },
]

const initialValue = {
  numeroFolio: '1000001',
  version: 2,
  fechaUltimaActualizacion: '2026-04-21T10:47:00Z',
  opcionesCobertura: {
    garantiasSeleccionadas: [],
    observaciones: null,
  },
  projectionPerLocation: [],
}

function Harness({ onSubmit }) {
  const [value, setValue] = useState(initialValue)

  return (
    <CoverageOptionsForm
      value={value}
      guarantees={guarantees}
      onChange={setValue}
      onSubmit={onSubmit}
      loading={false}
    />
  )
}

describe('CoverageOptionsForm', () => {
  it('renders the selector and submits the current draft after a guarantee is toggled', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn((event) => event.preventDefault())

    render(<Harness onSubmit={onSubmit} />)

    expect(screen.getByText('Garantías y términos de participación')).toBeInTheDocument()
    expect(screen.getByText('Selecciona una garantía del catálogo para empezar a definir los términos que participarán en el siguiente cálculo.')).toBeInTheDocument()

    await user.click(screen.getByRole('checkbox', { name: /incendio edificios/i }))

    expect(screen.getByRole('heading', { name: 'Incendio edificios' })).toBeInTheDocument()

    await user.type(screen.getByPlaceholderText('incendio, robo, daños'), 'incendio, robo')
    await user.type(screen.getByLabelText(/observaciones/i), 'Cobertura base para el análisis inicial')
    await user.click(screen.getByRole('button', { name: /guardar opciones/i }))

    expect(onSubmit).toHaveBeenCalledTimes(1)
    expect(screen.getByDisplayValue('Cobertura base para el análisis inicial')).toBeInTheDocument()
  })
})
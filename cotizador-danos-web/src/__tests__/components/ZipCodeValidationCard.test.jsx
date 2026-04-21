import '@testing-library/jest-dom/vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { ZipCodeValidationCard } from '../../components/ZipCodeValidationCard'

describe('ZipCodeValidationCard', () => {
  it('renders invalid zip alerts and submits the current value', () => {
    const onValidate = vi.fn()
    const onValueChange = vi.fn()

    render(
      <ZipCodeValidationCard
        value="999999"
        validation={{
          zipCode: '999999',
          valido: false,
          municipio: null,
          estado: null,
          coloniaBarrio: null,
          zona_tev: null,
          zona_fhm: null,
          alertas: [
            {
              codigo: 'ZIP_NO_RECONOCIDO',
              mensaje: 'El codigo postal no esta registrado en la referencia core.',
              severidad: 'Warning',
            },
          ],
        }}
        onValidate={onValidate}
        error="No fue posible validar el codigo postal."
        onValueChange={onValueChange}
      />,
    )

    expect(screen.getByText('No reconocido')).toBeInTheDocument()
    expect(screen.getByText('No fue posible validar el codigo postal.')).toBeInTheDocument()
    expect(screen.getByText('ZIP_NO_RECONOCIDO')).toBeInTheDocument()
    expect(screen.getByText('Warning')).toBeInTheDocument()

    fireEvent.change(screen.getByLabelText(/zip/i), { target: { value: '110111' } })
    fireEvent.click(screen.getByRole('button', { name: /validar zip/i }))

    expect(onValueChange).toHaveBeenCalledWith('110111')
    expect(onValidate).toHaveBeenCalledWith('110111')
  })
})
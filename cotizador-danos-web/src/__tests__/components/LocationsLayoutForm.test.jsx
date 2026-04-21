import '@testing-library/jest-dom/vitest'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { useState } from 'react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { LocationsLayoutForm } from '../../components/LocationsLayoutForm'

function createLayoutValue() {
  return {
    numeroFolio: '1000013',
    version: 4,
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
}

function renderForm({ onSubmit = vi.fn(), onChangeSpy = vi.fn() } = {}) {
  function Harness() {
    const [value, setValue] = useState(createLayoutValue())

    return (
      <LocationsLayoutForm
        value={value}
        onChange={(nextValue) => {
          onChangeSpy(nextValue)
          setValue(nextValue)
        }}
        onSubmit={(event) => {
          event.preventDefault()
          onSubmit(value)
        }}
        loading={false}
      />
    )
  }

  return {
    onSubmit,
    onChangeSpy,
    ...render(<Harness />),
  }
}

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('LocationsLayoutForm', () => {
  it('renders the current layout sorted by capture order', () => {
    renderForm()

    expect(screen.getByLabelText(/modo de captura/i)).toHaveValue('MULTIPLE')
    expect(screen.getByLabelText(/cantidad de ubicaciones/i)).toHaveValue(5)
    expect(screen.getAllByText(/Ubicación \d+/).map((element) => element.textContent)).toEqual([
      'Ubicación 2',
      'Ubicación 4',
      'Ubicación 3',
      'Ubicación 1',
      'Ubicación 5',
    ])
    expect(screen.getAllByLabelText(/orden de captura/i).map((input) => input.value)).toEqual(['1', '2', '3', '4', '5'])
  })

  it('updates the draft and submits the current version', async () => {
    const user = userEvent.setup()
    const { onSubmit, onChangeSpy } = renderForm()

    const quantityInput = screen.getByLabelText(/cantidad de ubicaciones/i)
    const saveButton = screen.getByRole('button', { name: /guardar layout/i })
    const formElement = saveButton.closest('form')

    await user.clear(quantityInput)
    await user.type(quantityInput, '6')
    fireEvent.submit(formElement)

    expect(onChangeSpy).toHaveBeenCalled()
    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          version: 4,
          configuracionLayout: expect.objectContaining({
            modoCaptura: 'MULTIPLE',
            cantidadUbicaciones: 6,
            ubicaciones: expect.arrayContaining([expect.objectContaining({ indice: 6, ordenCaptura: 6 })]),
          }),
        }),
      )
    })
  })
})
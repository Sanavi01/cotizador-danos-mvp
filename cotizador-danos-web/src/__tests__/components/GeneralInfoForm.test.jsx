import '@testing-library/jest-dom/vitest'
import { cleanup, render, screen } from '@testing-library/react'
import { useState } from 'react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { GeneralInfoForm } from '../../components/GeneralInfoForm'

const catalogs = {
  agents: [
    { codigo: 'AG-101', nombre: 'Agente Sabana', activo: true },
    { codigo: 'AG-102', nombre: 'Agente Centro', activo: true },
  ],
  riskClassifications: [
    { codigo: 'RISK-A', nombre: 'Riesgo alto', activo: true },
    { codigo: 'RISK-B', nombre: 'Riesgo medio', activo: true },
  ],
  businessLines: [
    { codigo: 'GIRO-001', nombre: 'Oficinas', activo: true, claveIncendio: 'INC-OFI' },
    { codigo: 'GIRO-002', nombre: 'Bodegas', activo: true, claveIncendio: 'INC-BOD' },
  ],
}

function createGeneralInfoValue() {
  return {
    numeroFolio: '1000011',
    version: 4,
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
}

function renderForm({ fieldErrors = {}, onSubmit = vi.fn() } = {}) {
  function Harness() {
    const [value, setValue] = useState(createGeneralInfoValue())

    return (
      <GeneralInfoForm
        value={value}
        catalogs={catalogs}
        fieldErrors={fieldErrors}
        onChange={setValue}
        onSubmit={onSubmit}
        loading={false}
      />
    )
  }

  return {
    onSubmit,
    ...render(<Harness />),
  }
}

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('GeneralInfoForm', () => {
  it('renders the captured values and inline field errors', () => {
    render(
      <GeneralInfoForm
        value={createGeneralInfoValue()}
        catalogs={catalogs}
        fieldErrors={{
          tipoDocumento: { message: 'El tipo de documento es obligatorio.', severity: 'error' },
          codigoAgente: { message: 'El código de agente es obligatorio.', severity: 'error' },
        }}
        onChange={vi.fn()}
        onSubmit={vi.fn()}
        loading={false}
      />,
    )

    expect(screen.getByDisplayValue('NIT')).toBeInTheDocument()
    expect(screen.getByDisplayValue('ACME SAS')).toBeInTheDocument()
    expect(screen.getByText('El tipo de documento es obligatorio.')).toBeInTheDocument()
    expect(screen.getByText('El código de agente es obligatorio.')).toBeInTheDocument()
    expect(screen.getByDisplayValue('NIT')).toHaveAttribute('aria-invalid', 'true')
    expect(screen.getByDisplayValue('AG-101')).toHaveAttribute('aria-invalid', 'true')
  })

  it('submits the trimmed payload with the current version', async () => {
    const user = userEvent.setup()
    const { onSubmit } = renderForm()

    await user.clear(screen.getByPlaceholderText('NIT'))
    await user.type(screen.getByPlaceholderText('NIT'), 'NIT')
    await user.clear(screen.getByPlaceholderText('900123456'))
    await user.type(screen.getByPlaceholderText('900123456'), '900123456')
    await user.clear(screen.getByPlaceholderText('ACME SAS'))
    await user.type(screen.getByPlaceholderText('ACME SAS'), 'ACME SAS')
    await user.clear(screen.getByPlaceholderText('contacto@acme.com'))
    await user.type(screen.getByPlaceholderText('contacto@acme.com'), 'contacto@acme.com')
    await user.clear(screen.getByPlaceholderText('6015550101'))
    await user.type(screen.getByPlaceholderText('6015550101'), '6015550101')

    await user.clear(screen.getByPlaceholderText('AG-102'))
    await user.type(screen.getByPlaceholderText('AG-102'), 'AG-102')
    await user.clear(screen.getByPlaceholderText('RISK-A'))
    await user.type(screen.getByPlaceholderText('RISK-A'), 'RISK-B')
    await user.clear(screen.getByPlaceholderText('GIRO-001'))
    await user.type(screen.getByPlaceholderText('GIRO-001'), 'GIRO-002')

    await user.click(screen.getByRole('button', { name: /guardar datos generales/i }))

    expect(onSubmit).toHaveBeenCalledWith({
      version: 4,
      datosAsegurado: {
        tipoDocumento: 'NIT',
        numeroDocumento: '900123456',
        nombreORazonSocial: 'ACME SAS',
        correoElectronico: 'contacto@acme.com',
        telefono: '6015550101',
      },
      datosConduccion: {
        codigoAgente: 'AG-102',
        clasificacionRiesgo: 'RISK-B',
        tipoNegocio: 'GIRO-002',
      },
    })
  })

  it('opens the catalog selector and applies the selected option', async () => {
    const user = userEvent.setup()

    function Harness() {
      const [value, setValue] = useState(createGeneralInfoValue())

      return (
        <GeneralInfoForm
          value={value}
          catalogs={catalogs}
          fieldErrors={{}}
          onChange={setValue}
          onSubmit={vi.fn()}
          loading={false}
        />
      )
    }

    render(<Harness />)

    await user.click(screen.getByRole('button', { name: /abrir opciones de código de agente/i }))
    await user.click(screen.getByRole('button', { name: /ag-102/i }))

    expect(screen.getByDisplayValue('AG-102')).toBeInTheDocument()
  })
})
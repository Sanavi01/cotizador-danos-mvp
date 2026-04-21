import '@testing-library/jest-dom/vitest'
import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { GeneralInfoCatalogPanel } from '../../components/GeneralInfoCatalogPanel'

describe('GeneralInfoCatalogPanel', () => {
  it('renders the catalog groups and their visible items', () => {
    render(
      <GeneralInfoCatalogPanel
        agents={[
          { codigo: 'AG-101', nombre: 'Agente Sabana', activo: true },
          { codigo: 'AG-102', nombre: 'Agente Centro', activo: true },
        ]}
        riskClassifications={[
          { codigo: 'RISK-A', nombre: 'Riesgo alto', activo: true },
        ]}
        businessLines={[
          { codigo: 'GIRO-001', nombre: 'Oficinas', activo: true, claveIncendio: 'INC-OFI' },
        ]}
      />,
    )

    expect(screen.getByRole('heading', { name: 'Catálogos comerciales' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Agentes' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Clasificaciones de riesgo' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Giros / negocios' })).toBeInTheDocument()
    expect(screen.getByText('Agente Sabana')).toBeInTheDocument()
    expect(screen.getByText('Agente Centro')).toBeInTheDocument()
    expect(screen.getByText('Riesgo alto')).toBeInTheDocument()
    expect(screen.getByText('Oficinas')).toBeInTheDocument()
    expect(screen.getByText('INC-OFI')).toBeInTheDocument()
  })

  it('shows the empty state when there are no catalog entries', () => {
    render(<GeneralInfoCatalogPanel agents={[]} riskClassifications={[]} businessLines={[]} />)

    expect(screen.getAllByText('Sin referencias disponibles.')).toHaveLength(3)
  })
})
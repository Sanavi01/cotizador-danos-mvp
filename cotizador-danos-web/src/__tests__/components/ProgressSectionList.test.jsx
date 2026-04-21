import '@testing-library/jest-dom/vitest'
import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ProgressSectionList } from '../../components/ProgressSectionList'

describe('ProgressSectionList', () => {
  it('renders the four progress sections with their current status', () => {
    render(
      <ProgressSectionList
        progress={{
          datosGenerales: 'COMPLETED',
          layoutUbicaciones: 'COMPLETED',
          ubicaciones: 'INCOMPLETE',
          opcionesCobertura: 'INCOMPLETE',
        }}
      />,
    )

    expect(screen.getByText('Datos generales')).toBeInTheDocument()
    expect(screen.getByText('Layout de ubicaciones')).toBeInTheDocument()
    expect(screen.getByText('Ubicaciones')).toBeInTheDocument()
    expect(screen.getByText('Opciones de cobertura')).toBeInTheDocument()
    expect(screen.getAllByText('COMPLETED')).toHaveLength(2)
    expect(screen.getAllByText('INCOMPLETE')).toHaveLength(2)
  })
})
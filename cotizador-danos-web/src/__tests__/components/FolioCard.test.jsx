import '@testing-library/jest-dom/vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { FolioCard } from '../../components/FolioCard'

describe('FolioCard', () => {
  it('renders the created folio summary and opens the state view', () => {
    const folio = {
      numeroFolio: '1000001',
      estadoCotizacion: 'BORRADOR',
      version: 0,
      fechaUltimaActualizacion: '2026-04-20T00:00:00Z',
    }
    const handleOpen = vi.fn()

    render(<FolioCard folio={folio} onOpen={handleOpen} />)

    expect(screen.getByText('Folio creado')).toBeInTheDocument()
    expect(screen.getByText('1000001')).toBeInTheDocument()
    expect(screen.getByText((content, element) => element.textContent === 'Estado BORRADOR · Versión 0')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /abrir estado/i })).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: /abrir estado/i }))

    expect(handleOpen).toHaveBeenCalledWith('1000001')
  })
})
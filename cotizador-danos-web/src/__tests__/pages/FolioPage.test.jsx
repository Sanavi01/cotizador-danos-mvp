import '@testing-library/jest-dom/vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { FolioPage } from '../../pages/FolioPage'

const navigateMock = vi.fn()
const createFolioMock = vi.fn().mockResolvedValue({
  numeroFolio: '1000001',
  estadoCotizacion: 'BORRADOR',
  version: 0,
  fechaUltimaActualizacion: '2026-04-20T00:00:00Z',
})
const resetMock = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')

  return {
    ...actual,
    useNavigate: () => navigateMock,
  }
})

vi.mock('../../hooks/useFolioCreation', () => ({
  useFolioCreation: () => ({
    createFolio: createFolioMock,
    loading: false,
    error: null,
    folio: null,
    reset: resetMock,
  }),
}))

describe('FolioPage', () => {
  it('submits the idempotent creation flow and rotates the key on demand', () => {
    render(<FolioPage />)

    const originInput = screen.getByLabelText(/origen/i)
    fireEvent.change(originInput, { target: { value: '  web  ' } })

    const noticeBefore = screen.getByText(/Llave activa:/).textContent

    fireEvent.click(screen.getByRole('button', { name: /crear folio/i }))

    expect(createFolioMock).toHaveBeenCalledWith(
      { origin: 'web' },
      expect.any(String),
    )

    fireEvent.click(screen.getByRole('button', { name: /nueva llave/i }))

    expect(resetMock).toHaveBeenCalled()
    expect(screen.getByText(/Llave activa:/).textContent).not.toBe(noticeBefore)
    expect(navigateMock).not.toHaveBeenCalled()
  })
})
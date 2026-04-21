import '@testing-library/jest-dom/vitest'
import { render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { GeneralInfoSummaryCard } from '../../components/GeneralInfoSummaryCard'

describe('GeneralInfoSummaryCard', () => {
  it('renders the folio summary and the empty timestamp fallback', () => {
    render(<GeneralInfoSummaryCard quote="1000011" version={0} updatedAt="" />)

    expect(screen.getByText('Datos generales')).toBeInTheDocument()
    expect(screen.getByText('1000011')).toBeInTheDocument()
    expect(screen.getByText((content, element) => element.textContent === 'Versión actual 0')).toBeInTheDocument()
    expect(screen.getByText((content, element) => element.textContent === 'Actualizado Pendiente de primer guardado')).toBeInTheDocument()
    expect(screen.getByText('Sección editable')).toBeInTheDocument()
  })

  it('formats the last update timestamp when it exists', () => {
    render(<GeneralInfoSummaryCard quote="1000011" version={3} updatedAt="2026-04-21T10:47:00Z" />)

    expect(screen.getByText(/actualizado .*2026/i)).toBeInTheDocument()
  })
})
import '@testing-library/jest-dom/vitest'
import { renderHook } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { useLocationsLayoutPreview } from '../../hooks/useLocationsLayoutPreview'

describe('useLocationsLayoutPreview', () => {
  it('sorts the preview by capture order while preserving the original indices', () => {
    const configuration = {
      modoCaptura: 'MULTIPLE',
      cantidadUbicaciones: 5,
      ubicaciones: [
        { indice: 2, ordenCaptura: 1 },
        { indice: 4, ordenCaptura: 2 },
        { indice: 3, ordenCaptura: 3 },
        { indice: 1, ordenCaptura: 4 },
        { indice: 5, ordenCaptura: 5 },
      ],
    }

    const { result } = renderHook(() => useLocationsLayoutPreview(configuration))

    expect(result.current.mode).toBe('MULTIPLE')
    expect(result.current.canAddSlot).toBe(false)
    expect(result.current.slots).toEqual([
      { indice: 2, ordenCaptura: 1 },
      { indice: 4, ordenCaptura: 2 },
      { indice: 3, ordenCaptura: 3 },
      { indice: 1, ordenCaptura: 4 },
      { indice: 5, ordenCaptura: 5 },
    ])
  })

  it('returns an empty preview when the layout is incomplete', () => {
    const { result } = renderHook(() => useLocationsLayoutPreview(null))

    expect(result.current.mode).toBeNull()
    expect(result.current.canAddSlot).toBe(false)
    expect(result.current.slots).toEqual([])
  })
})
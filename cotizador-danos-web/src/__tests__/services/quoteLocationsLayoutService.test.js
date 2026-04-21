import axios from 'axios'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  createEmptyLocationsLayout,
  getLocationsLayout,
  updateLocationsLayout,
} from '../../services/quoteLocationsLayoutService'

describe('quoteLocationsLayoutService', () => {
  const getMock = vi.fn()
  const putMock = vi.fn()
  let createSpy

  beforeEach(() => {
    vi.stubEnv('VITE_API_URL', 'http://localhost:8080')
    getMock.mockReset()
    putMock.mockReset()
    createSpy = vi.spyOn(axios, 'create').mockReturnValue({
      get: getMock,
      put: putMock,
    })
  })

  afterEach(() => {
    vi.unstubAllEnvs()
    vi.restoreAllMocks()
    createSpy = undefined
  })

  it('returns an empty layout shape for a new folio', () => {
    expect(createEmptyLocationsLayout('1000013')).toEqual({
      numeroFolio: '1000013',
      version: 0,
      fechaUltimaActualizacion: '',
      configuracionLayout: {
        modoCaptura: null,
        cantidadUbicaciones: null,
        ubicaciones: [],
      },
    })
  })

  it('reads and normalizes empty layout slots from the backend envelope', async () => {
    getMock.mockResolvedValue({
      data: {
        data: {
          numeroFolio: '1000013',
          version: 1,
          fechaUltimaActualizacion: '2026-04-21T12:12:00Z',
          configuracionLayout: {
            modoCaptura: null,
            cantidadUbicaciones: null,
            ubicaciones: null,
          },
        },
      },
    })

    const response = await getLocationsLayout('1000013')

    expect(createSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        baseURL: 'http://localhost:8080',
        timeout: 10000,
      }),
    )
    expect(getMock).toHaveBeenCalledWith('/v1/quotes/1000013/locations/layout')
    expect(response).toEqual({
      numeroFolio: '1000013',
      version: 1,
      fechaUltimaActualizacion: '2026-04-21T12:12:00Z',
      configuracionLayout: {
        modoCaptura: null,
        cantidadUbicaciones: null,
        ubicaciones: [],
      },
    })
  })

  it('sends the current version when updating the layout', async () => {
    const payload = {
      version: 4,
      configuracionLayout: {
        modoCaptura: 'MULTIPLE',
        cantidadUbicaciones: 4,
        ubicaciones: [
          { indice: 1, ordenCaptura: 1 },
          { indice: 2, ordenCaptura: 2 },
          { indice: 3, ordenCaptura: 3 },
          { indice: 4, ordenCaptura: 4 },
        ],
      },
    }

    putMock.mockResolvedValue({
      data: {
        data: {
          numeroFolio: '1000013',
          version: 5,
          fechaUltimaActualizacion: '2026-04-21T12:45:00Z',
          configuracionLayout: payload.configuracionLayout,
        },
      },
    })

    const response = await updateLocationsLayout('1000013', payload)

    expect(putMock).toHaveBeenCalledWith('/v1/quotes/1000013/locations/layout', payload)
    expect(response).toEqual({
      numeroFolio: '1000013',
      version: 5,
      fechaUltimaActualizacion: '2026-04-21T12:45:00Z',
      configuracionLayout: payload.configuracionLayout,
    })
  })
})
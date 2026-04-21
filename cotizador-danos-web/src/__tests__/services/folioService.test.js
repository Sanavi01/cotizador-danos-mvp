import axios from 'axios'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createFolio, getQuoteState } from '../../services/folioService'

describe('folioService', () => {
  const postMock = vi.fn()
  const getMock = vi.fn()
  let createSpy

  beforeEach(() => {
    vi.stubEnv('VITE_API_URL', 'http://localhost:8080')
    postMock.mockReset()
    getMock.mockReset()
    createSpy = vi.spyOn(axios, 'create').mockReturnValue({
      post: postMock,
      get: getMock,
    })
  })

  afterEach(() => {
    vi.unstubAllEnvs()
    vi.restoreAllMocks()
    createSpy = undefined
  })

  it('sends the folio creation request with the idempotency header', async () => {
    const createdFolio = {
      numeroFolio: '1000001',
      estadoCotizacion: 'BORRADOR',
      version: 0,
      fechaUltimaActualizacion: '2026-04-20T00:00:00Z',
    }

    postMock.mockResolvedValue({ data: { data: createdFolio } })

    const response = await createFolio({ origin: 'spa' }, 'key-123')

    expect(createSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        baseURL: 'http://localhost:8080',
        timeout: 10000,
      }),
    )
    expect(postMock).toHaveBeenCalledWith('/v1/folios', { origin: 'spa' }, {
      headers: {
        'Idempotency-Key': 'key-123',
      },
    })
    expect(response).toEqual(createdFolio)
  })

  it('reads the quote state contract from the backend envelope', async () => {
    const state = {
      numeroFolio: '1000001',
      estadoCotizacion: 'BORRADOR',
      tieneAlertas: false,
      seccionesCompletadas: [],
      ubicacionesCalculables: 0,
      ubicacionesIncompletas: 0,
      version: 0,
      fechaUltimaActualizacion: '2026-04-20T00:00:00Z',
    }

    getMock.mockResolvedValue({ data: { data: state } })

    const response = await getQuoteState('1000001')

    expect(getMock).toHaveBeenCalledWith('/v1/quotes/1000001/state')
    expect(response).toEqual(state)
  })

  it('surfaces problem details on non-retryable errors', async () => {
    const error = {
      isAxiosError: true,
      response: {
        status: 400,
        data: {
          detail: 'La llave de idempotencia es obligatoria.',
        },
      },
      message: 'Request failed with status code 400',
    }

    postMock.mockRejectedValue(error)

    await expect(createFolio({ origin: 'spa' }, 'key-123')).rejects.toThrow(
      'La llave de idempotencia es obligatoria.',
    )
  })
})
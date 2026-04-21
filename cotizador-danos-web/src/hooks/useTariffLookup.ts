import { useState } from 'react'
import { getProblemMessage } from '../services/httpClient'
import { getTariff as getTariffService, type TariffDetails } from '../services/referenceCoreService'

export function useTariffLookup() {
  const [tariff, setTariff] = useState<TariffDetails | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function lookupTariff(tariffKey: string): Promise<TariffDetails> {
    const normalizedTariffKey = tariffKey.trim()

    if (!normalizedTariffKey) {
      const message = 'La clave de tarifa es obligatoria.'
      setTariff(null)
      setError(message)
      throw new Error(message)
    }

    setLoading(true)
    setError(null)

    try {
      const result = await getTariffService(normalizedTariffKey)
      setTariff(result)
      return result
    } catch (tariffError) {
      const message = getProblemMessage(tariffError)
      setError(message)
      setTariff(null)
      throw new Error(message)
    } finally {
      setLoading(false)
    }
  }

  function reset() {
    setTariff(null)
    setError(null)
  }

  return {
    tariff,
    loading,
    error,
    lookupTariff,
    reset,
  }
}

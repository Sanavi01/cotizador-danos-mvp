import { useState } from 'react'
import { getProblemMessage } from '../services/httpClient'
import { validateZipCode as validateZipCodeService, type ZipCodeValidation } from '../services/referenceCoreService'

export function useZipCodeValidation() {
  const [validation, setValidation] = useState<ZipCodeValidation | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function validateZipCode(zipCode: string): Promise<ZipCodeValidation> {
    const normalizedZipCode = zipCode.trim()

    if (!normalizedZipCode) {
      const message = 'El codigo postal es obligatorio.'
      setValidation(null)
      setError(message)
      throw new Error(message)
    }

    setLoading(true)
    setError(null)

    try {
      const result = await validateZipCodeService({ zipCode: normalizedZipCode })
      setValidation(result)
      return result
    } catch (zipCodeError) {
      const message = getProblemMessage(zipCodeError)
      setError(message)
      setValidation(null)
      throw new Error(message)
    } finally {
      setLoading(false)
    }
  }

  function reset() {
    setValidation(null)
    setError(null)
  }

  return {
    validation,
    loading,
    error,
    validateZipCode,
    reset,
  }
}

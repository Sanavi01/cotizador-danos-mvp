import { type FormEvent, type ChangeEvent, useState } from 'react'
import type { ZipCodeValidation } from '../services/referenceCoreService'
import { ValidationAlertList } from './ValidationAlertList'
import styles from './ZipCodeValidationCard.module.css'

interface ZipCodeValidationCardProps {
  value: string
  validation: ZipCodeValidation | null
  onValidate: (zipCode: string) => void | Promise<void>
  loading?: boolean
  error?: string | null
  onValueChange?: (value: string) => void
}

export function ZipCodeValidationCard({
  value,
  validation,
  onValidate,
  loading = false,
  error = null,
  onValueChange,
}: ZipCodeValidationCardProps) {
  const [draftZipCode, setDraftZipCode] = useState(value)

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const nextValue = event.target.value
    setDraftZipCode(nextValue)
    onValueChange?.(nextValue)
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await onValidate(draftZipCode)
  }

  return (
    <article className={styles.card}>
      <div className={styles.header}>
        <div>
          <p className={styles.kicker}>Validación territorial</p>
          <h3>Código postal</h3>
        </div>
        <span className={validation?.valido ? styles.valid : styles.pending}>
          {validation ? (validation.valido ? 'Válido' : 'No reconocido') : 'Pendiente'}
        </span>
      </div>

      <form className={styles.form} onSubmit={handleSubmit}>
        <label className={styles.field}>
          <span>ZIP</span>
          <input
            value={draftZipCode}
            onChange={handleChange}
            placeholder="110111"
            inputMode="numeric"
            maxLength={10}
          />
        </label>

        <button className={styles.button} type="submit" disabled={loading}>
          {loading ? 'Validando...' : 'Validar ZIP'}
        </button>
      </form>

      {error ? <p className={styles.error}>{error}</p> : null}

      {validation ? (
        <div className={styles.result}>
          <div className={styles.summary}>
            <div>
              <p className={styles.label}>Municipio</p>
              <p className={styles.value}>{validation.municipio ?? 'No disponible'}</p>
            </div>
            <div>
              <p className={styles.label}>Estado</p>
              <p className={styles.value}>{validation.estado ?? 'No disponible'}</p>
            </div>
            <div>
              <p className={styles.label}>Zona TEV</p>
              <p className={styles.value}>{validation.zona_tev ?? 'No disponible'}</p>
            </div>
            <div>
              <p className={styles.label}>Zona FHM</p>
              <p className={styles.value}>{validation.zona_fhm ?? 'No disponible'}</p>
            </div>
          </div>

          <ValidationAlertList alerts={validation.alertas} />
        </div>
      ) : null}
    </article>
  )
}

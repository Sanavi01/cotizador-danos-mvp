import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { FolioCard } from '../components/FolioCard'
import { IdempotencyNotice } from '../components/IdempotencyNotice'
import { useFolioCreation } from '../hooks/useFolioCreation'
import type { CreateFolioPayload } from '../services/folioService'
import styles from './FolioPage.module.css'

function createIdempotencyKey() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }

  return `folio-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

export function FolioPage() {
  const navigate = useNavigate()
  const { createFolio, loading, error, folio, reset } = useFolioCreation()
  const [origin, setOrigin] = useState('spa')
  const [idempotencyKey, setIdempotencyKey] = useState(() => createIdempotencyKey())

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const payload: CreateFolioPayload = {
      origin: origin.trim() || 'spa',
    }

    await createFolio(payload, idempotencyKey)
  }

  function openFolio(number: string) {
    navigate(`/quotes/${encodeURIComponent(number)}/state`)
  }

  function handleNewRequest() {
    reset()
    setIdempotencyKey(createIdempotencyKey())
  }

  return (
    <main className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroCopy}>
          <p className={styles.kicker}>Cotizador de daños</p>
          <h1>Abre un folio nuevo y continúa la captura sin duplicados.</h1>
          <p className={styles.description}>
            La creación usa una llave de idempotencia por solicitud y la consulta de estado permite retomar la cotización
            en cualquier momento.
          </p>
          <IdempotencyNotice
            variant="info"
            message={`Llave activa: ${idempotencyKey}. Reintentos con la misma llave devolverán la misma respuesta.`}
          />
        </div>

        <form className={styles.panel} onSubmit={handleSubmit}>
          <label className={styles.field}>
            <span>Origen</span>
            <input
              value={origin}
              onChange={(event) => setOrigin(event.target.value)}
              placeholder="spa"
              maxLength={32}
            />
          </label>

          <div className={styles.actions}>
            <button className={styles.primaryButton} type="submit" disabled={loading}>
              {loading ? 'Creando...' : 'Crear folio'}
            </button>
            <button className={styles.secondaryButton} type="button" onClick={handleNewRequest}>
              Nueva llave
            </button>
          </div>

          {error ? <IdempotencyNotice variant="warning" message={error} /> : null}
        </form>
      </section>

      <section className={styles.stack}>
        <div className={styles.lookupCard}>
          <div>
            <h2>Abrir un folio existente</h2>
            <p>Ingresa el número de folio para consultar su estado actual y continuar la captura.</p>
          </div>

          <form
            className={styles.lookupForm}
            onSubmit={(event) => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              const folioValue = String(formData.get('folio') ?? '').trim()

              if (folioValue) {
                navigate(`/quotes/${encodeURIComponent(folioValue)}/state`)
              }
            }}
          >
            <input name="folio" placeholder="1000001" />
            <button className={styles.secondaryButton} type="submit">
              Abrir estado
            </button>
          </form>
        </div>

        {folio ? <FolioCard folio={folio} onOpen={openFolio} /> : null}
      </section>
    </main>
  )
}
import { useNavigate, useParams } from 'react-router-dom'
import { IdempotencyNotice } from '../components/IdempotencyNotice'
import { QuoteProgressCard } from '../components/QuoteProgressCard'
import { useQuoteState } from '../hooks/useQuoteState'
import styles from './QuoteStatePage.module.css'

export function QuoteStatePage() {
  const navigate = useNavigate()
  const { folio } = useParams<{ folio: string }>()
  const { state, loading, error, refresh } = useQuoteState(folio)

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div className={styles.headerCopy}>
          <p className={styles.kicker}>Estado de cotización</p>
          <h1>{folio ?? 'Folio no disponible'}</h1>
          <p>
            Consulta el avance persistido del folio sin recalcular primas ni alterar el agregado.
          </p>
        </div>

        <div className={styles.actions}>
          <button
            className={styles.secondaryButton}
            type="button"
            onClick={() => navigate(`/quotes/${encodeURIComponent(folio ?? '')}/general-info`)}
            disabled={!folio}
          >
            Datos generales
          </button>
          <button
            className={styles.secondaryButton}
            type="button"
            onClick={() => navigate(`/quotes/${encodeURIComponent(folio ?? '')}/locations/layout`)}
            disabled={!folio}
          >
            Layout de ubicaciones
          </button>
          <button
            className={styles.secondaryButton}
            type="button"
            onClick={() => navigate(`/quotes/${encodeURIComponent(folio ?? '')}/locations`)}
            disabled={!folio}
          >
            Ubicaciones
          </button>
          <button className={styles.secondaryButton} type="button" onClick={() => navigate('/cotizador')}>
            Volver
          </button>
          <button className={styles.primaryButton} type="button" onClick={() => void refresh()} disabled={loading}>
            {loading ? 'Actualizando...' : 'Actualizar'}
          </button>
        </div>
      </section>

      {error ? <IdempotencyNotice variant="warning" message={error} /> : null}

      <QuoteProgressCard state={state} />
    </main>
  )
}
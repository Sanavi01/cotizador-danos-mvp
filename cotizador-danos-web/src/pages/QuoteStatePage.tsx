import { useNavigate, useParams } from 'react-router-dom'
import { IdempotencyNotice } from '../components/IdempotencyNotice'
import { useQuoteState } from '../hooks/useQuoteState'
import styles from './QuoteStatePage.module.css'

export function QuoteStatePage() {
  const navigate = useNavigate()
  const { folio } = useParams<{ folio: string }>()
  const { state, loading, error, refresh } = useQuoteState(folio)

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.kicker}>Estado de cotización</p>
          <h1>{folio ?? 'Folio no disponible'}</h1>
          <p>
            Consulta el avance persistido del folio para continuar la captura sin perder la información ya registrada.
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
          <button className={styles.secondaryButton} type="button" onClick={() => navigate('/cotizador')}>
            Volver
          </button>
          <button className={styles.primaryButton} type="button" onClick={() => void refresh()} disabled={loading}>
            {loading ? 'Actualizando...' : 'Actualizar'}
          </button>
        </div>
      </section>

      {error ? <IdempotencyNotice variant="warning" message={error} /> : null}

      {state ? (
        <section className={styles.grid}>
          <article className={styles.card}>
            <span className={styles.cardLabel}>Estado actual</span>
            <strong className={styles.cardValue}>{state.estadoCotizacion}</strong>
            <p className={styles.cardMeta}>Versión {state.version}</p>
          </article>

          <article className={styles.card}>
            <span className={styles.cardLabel}>Alertas</span>
            <strong className={styles.cardValue}>{state.tieneAlertas ? 'Sí' : 'No'}</strong>
            <p className={styles.cardMeta}>Ubicaciones incompletas: {state.ubicacionesIncompletas}</p>
          </article>

          <article className={styles.card}>
            <span className={styles.cardLabel}>Ubicaciones</span>
            <strong className={styles.cardValue}>{state.ubicacionesCalculables}</strong>
            <p className={styles.cardMeta}>Calculables en este momento</p>
          </article>

          <article className={styles.cardWide}>
            <span className={styles.cardLabel}>Secciones completadas</span>
            {state.seccionesCompletadas.length > 0 ? (
              <ul className={styles.list}>
                {state.seccionesCompletadas.map((section) => (
                  <li key={section}>{section}</li>
                ))}
              </ul>
            ) : (
              <p className={styles.emptyState}>Aún no hay secciones completadas.</p>
            )}
            <p className={styles.cardMeta}>Actualizado {new Date(state.fechaUltimaActualizacion).toLocaleString('es-CO')}</p>
          </article>
        </section>
      ) : null}
    </main>
  )
}
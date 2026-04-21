import styles from './QuoteStateSummaryCard.module.css'

interface QuoteStateSummaryCardProps {
  quote: string
  state: string
  version: number
  updatedAt: string
  readyToCalculate: boolean
}

function formatUpdatedAt(updatedAt: string) {
  if (!updatedAt) {
    return 'Pendiente de actualización'
  }

  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(updatedAt))
}

export function QuoteStateSummaryCard({ quote, state, version, updatedAt, readyToCalculate }: QuoteStateSummaryCardProps) {
  return (
    <article className={styles.card}>
      <div>
        <p className={styles.kicker}>Resumen de trazabilidad</p>
        <h2 className={styles.quote}>{quote}</h2>
        <p className={styles.meta}>
          Estado <strong>{state}</strong> · Versión <strong>{version}</strong>
        </p>
        <p className={styles.meta}>Actualizado {formatUpdatedAt(updatedAt)}</p>
      </div>

      <div className={`${styles.badge} ${readyToCalculate ? styles.ready : styles.pending}`}>
        {readyToCalculate ? 'Listo para cálculo' : 'Aún en captura'}
      </div>
    </article>
  )
}
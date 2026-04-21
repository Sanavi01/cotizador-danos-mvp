import styles from './GeneralInfoSummaryCard.module.css'

interface GeneralInfoSummaryCardProps {
  quote: string
  version: number
  updatedAt: string
}

function formatUpdatedAt(updatedAt: string) {
  if (!updatedAt) {
    return 'Pendiente de primer guardado'
  }

  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(updatedAt))
}

export function GeneralInfoSummaryCard({ quote, version, updatedAt }: GeneralInfoSummaryCardProps) {
  return (
    <article className={styles.card}>
      <div>
        <p className={styles.kicker}>Datos generales</p>
        <h2 className={styles.quote}>{quote}</h2>
        <p className={styles.meta}>
          Versión actual <strong>{version}</strong>
        </p>
        <p className={styles.meta}>Actualizado {formatUpdatedAt(updatedAt)}</p>
      </div>

      <div className={styles.badge}>Sección editable</div>
    </article>
  )
}
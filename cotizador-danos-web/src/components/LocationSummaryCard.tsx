import type { LocationSummaryRecord } from '../services/quoteLocationsService'
import styles from './LocationSummaryCard.module.css'

interface LocationSummaryCardProps {
  summary: LocationSummaryRecord | null
  onSelectIndice?: (indice: number) => void
}

function formatCoverage(totalActual: number, totalEsperado: number): string {
  if (totalEsperado === 0) {
    return '0%'
  }

  return `${Math.min(100, Math.round((totalActual / totalEsperado) * 100))}%`
}

function getProgressWidth(totalActual: number, totalEsperado: number): string {
  if (totalEsperado === 0) {
    return '0%'
  }

  return `${Math.min(100, (totalActual / totalEsperado) * 100)}%`
}

export function LocationSummaryCard({ summary, onSelectIndice }: LocationSummaryCardProps) {
  if (!summary) {
    return (
      <article className={styles.card}>
        <p className={styles.kicker}>Resumen operativo</p>
        <h2>Sin datos de resumen todavía.</h2>
        <p className={styles.meta}>Consulta el folio para ver progreso, slots esperados y alertas por índice.</p>
      </article>
    )
  }

  const progressWidth = getProgressWidth(summary.totalActual, summary.totalEsperado)

  return (
    <article className={styles.card}>
      <div className={styles.header}>
        <div>
          <p className={styles.kicker}>Resumen operativo</p>
          <h2>{summary.numeroFolio}</h2>
          <p className={styles.meta}>
            {summary.totalActual} de {summary.totalEsperado} ubicaciones capturadas · {formatCoverage(summary.totalActual, summary.totalEsperado)}
          </p>
        </div>

        <div className={styles.badge}>Progreso</div>
      </div>

      <div className={styles.progressBar} aria-hidden="true">
        <span style={{ width: progressWidth }} />
      </div>

      <div className={styles.grid}>
        <article>
          <strong>{summary.calculables}</strong>
          <span>calculables</span>
        </article>
        <article>
          <strong>{summary.incompletas}</strong>
          <span>incompletas</span>
        </article>
        <article>
          <strong>{summary.invalidas}</strong>
          <span>inválidas</span>
        </article>
        <article>
          <strong>{summary.conAlertas}</strong>
          <span>con alertas</span>
        </article>
      </div>

      <div className={styles.indices}>
        {summary.resumenPorIndice.map((item) => (
          <button
            key={item.indice}
            className={`${styles.indexItem} ${styles[item.estadoValidacion.toLowerCase()]}`}
            type="button"
            onClick={() => onSelectIndice?.(item.indice)}
          >
            <span>#{item.indice}</span>
            <strong>{item.estadoValidacion}</strong>
            <small>{item.tieneAlertasBloqueantes ? 'Con alertas' : 'Sin alertas'}</small>
          </button>
        ))}
      </div>
    </article>
  )
}
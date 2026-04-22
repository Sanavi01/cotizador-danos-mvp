import type { CoverageOptionsConfiguration } from '../services/quoteCoverageOptionsService'
import styles from './CoverageOptionsSummaryCard.module.css'

interface CoverageOptionsSummaryCardProps {
  options: CoverageOptionsConfiguration
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

export function CoverageOptionsSummaryCard({ options, version, updatedAt }: CoverageOptionsSummaryCardProps) {
  const guaranteeCount = options.garantiasSeleccionadas.length

  return (
    <article className={styles.card}>
      <div>
        <p className={styles.kicker}>Opciones de cobertura</p>
        <h2 className={styles.title}>{guaranteeCount > 0 ? 'Configuración vigente' : 'Sin coberturas definidas'}</h2>
        <p className={styles.meta}>
          Versión actual <strong>{version}</strong>
        </p>
        <p className={styles.meta}>Actualizado {formatUpdatedAt(updatedAt)}</p>
        <p className={styles.meta}>
          Garantías seleccionadas <strong>{guaranteeCount}</strong>
        </p>
        <p className={styles.meta}>{options.observaciones ?? 'Sin observaciones registradas'}</p>
      </div>

      <div className={styles.badge}>{guaranteeCount > 0 ? 'Editable' : 'Inicializable'}</div>
    </article>
  )
}
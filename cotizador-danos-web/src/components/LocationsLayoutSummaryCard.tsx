import type { LocationsLayoutRecord } from '../services/quoteLocationsLayoutService'
import styles from './LocationsLayoutSummaryCard.module.css'

interface LocationsLayoutSummaryCardProps {
  layout: LocationsLayoutRecord | null
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

export function LocationsLayoutSummaryCard({ layout, version, updatedAt }: LocationsLayoutSummaryCardProps) {
  const mode = layout?.configuracionLayout.modoCaptura
  const quantity = layout?.configuracionLayout.cantidadUbicaciones
  const slots = layout?.configuracionLayout.ubicaciones.length ?? 0

  return (
    <article className={styles.card}>
      <div>
        <p className={styles.kicker}>Layout de ubicaciones</p>
        <h2 className={styles.quote}>{layout?.numeroFolio ?? 'Folio'}</h2>
        <p className={styles.meta}>
          Versión actual <strong>{version}</strong>
        </p>
        <p className={styles.meta}>Actualizado {formatUpdatedAt(updatedAt)}</p>
        <p className={styles.meta}>
          Modo <strong>{mode ?? 'Sin definir'}</strong> · Cantidad <strong>{quantity ?? 'Pendiente'}</strong> · Slots{' '}
          <strong>{slots}</strong>
        </p>
      </div>

      <div className={styles.badge}>{mode ?? 'Sin layout'}</div>
    </article>
  )
}
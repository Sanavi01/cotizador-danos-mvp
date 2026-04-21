import type { LocationsLayoutMode, LocationsLayoutSlot } from '../services/quoteLocationsLayoutService'
import styles from './LocationsLayoutPreview.module.css'

interface LocationsLayoutPreviewProps {
  slots: LocationsLayoutSlot[]
  mode: LocationsLayoutMode | null
}

export function LocationsLayoutPreview({ slots, mode }: LocationsLayoutPreviewProps) {
  return (
    <section className={styles.panel}>
      <div className={styles.header}>
        <div>
          <p className={styles.kicker}>Vista previa</p>
          <h2 className={styles.title}>Ubicaciones planificadas</h2>
        </div>

        <span className={styles.modeBadge}>{mode ?? 'Sin definir'}</span>
      </div>

      {slots.length > 0 ? (
        <div className={styles.list}>
          {slots.map((slot) => (
            <article key={slot.indice} className={styles.slot}>
              <span className={styles.slotIndex}>Ubicación {slot.indice}</span>
              <strong className={styles.slotOrder}>Orden {slot.ordenCaptura}</strong>
            </article>
          ))}
        </div>
      ) : (
        <p className={styles.emptyState}>Define el modo y la cantidad para proyectar la captura de ubicaciones.</p>
      )}

      <p className={styles.note}>
        El índice conserva la posición lógica de cada ubicación y el orden de captura controla cómo se presentará la
        sección.
      </p>
    </section>
  )
}
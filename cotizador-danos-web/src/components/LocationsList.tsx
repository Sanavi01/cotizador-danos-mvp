import type { LocationRecord } from '../services/quoteLocationsService'
import styles from './LocationsList.module.css'

interface LocationsListProps {
  locations: LocationRecord[]
  onEdit: (location: LocationRecord) => void
  onSelect: (location: LocationRecord) => void
}

function getValidationTone(state: LocationRecord['estadoValidacion']): string {
  switch (state) {
    case 'CALCULABLE':
      return styles.success
    case 'VALID':
      return styles.info
    case 'INVALID':
      return styles.danger
    case 'INCOMPLETE':
      return styles.warning
    default:
      return styles.neutral
  }
}

export function LocationsList({ locations, onEdit, onSelect }: LocationsListProps) {
  if (locations.length === 0) {
    return (
      <article className={styles.emptyState}>
        <p className={styles.kicker}>Ubicaciones</p>
        <h2>No hay ubicaciones persistidas todavía.</h2>
        <p>Usa el editor para completar el snapshot o abre una ubicación existente desde el resumen del folio.</p>
      </article>
    )
  }

  return (
    <section className={styles.card}>
      <div className={styles.header}>
        <div>
          <p className={styles.kicker}>Lista de ubicaciones</p>
          <h2>Selecciona un registro para revisar su estado o abrir el detalle.</h2>
        </div>
        <span className={styles.counter}>{locations.length} ubicaciones</span>
      </div>

      <div className={styles.list}>
        {locations.map((location) => (
          <article key={location.indice} className={styles.item}>
            <div className={styles.itemHeader}>
              <div>
                <p className={styles.index}>Ubicación {location.indice}</p>
                <h3>{location.nombreUbicacion ?? 'Sin nombre'}</h3>
              </div>

              <span className={`${styles.badge} ${getValidationTone(location.estadoValidacion)}`}>
                {location.estadoValidacion}
              </span>
            </div>

            <p className={styles.meta}>{location.direccion ?? 'Sin dirección capturada'}</p>
            <p className={styles.meta}>
              CP {location.codigoPostal ?? 'Pendiente'} · {location.municipio ?? 'Municipio pendiente'} ·{' '}
              {location.giro?.nombre ?? 'Giro sin definir'}
            </p>

            <div className={styles.footer}>
              <div className={styles.tags}>
                <span className={styles.tag}>{location.alertasBloqueantes.length} alertas</span>
                <span className={styles.tag}>{location.garantias.length} garantías</span>
              </div>

              <div className={styles.actions}>
                <button className={styles.secondaryButton} type="button" onClick={() => onSelect(location)}>
                  Seleccionar
                </button>
                <button className={styles.primaryButton} type="button" onClick={() => onEdit(location)}>
                  Abrir detalle
                </button>
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}
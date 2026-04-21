import type { ReferenceCatalogItem } from '../services/referenceCoreService'
import styles from './CatalogLookupPanel.module.css'

interface CatalogLookupPanelProps {
  title: string
  items: ReferenceCatalogItem[]
  loading: boolean
  error: string | null
  filterValue: string
  onFilterChange: (value: string) => void
}

function matchesFilter(item: ReferenceCatalogItem, filterValue: string) {
  const normalizedFilter = filterValue.trim().toLowerCase()

  if (!normalizedFilter) {
    return true
  }

  return item.nombre.toLowerCase().includes(normalizedFilter)
}

export function CatalogLookupPanel({
  title,
  items,
  loading,
  error,
  filterValue,
  onFilterChange,
}: CatalogLookupPanelProps) {
  const visibleItems = items.filter((item) => matchesFilter(item, filterValue))

  return (
    <article className={styles.panel}>
      <div className={styles.header}>
        <div>
          <p className={styles.kicker}>Catálogo</p>
          <h3>{title}</h3>
        </div>
        <span className={styles.count}>{visibleItems.length}</span>
      </div>

      <label className={styles.field}>
        <span>Filtrar por nombre</span>
        <input
          value={filterValue}
          onChange={(event) => onFilterChange(event.target.value)}
          placeholder="Busca por texto"
        />
      </label>

      {loading ? <p className={styles.state}>Cargando catálogo...</p> : null}

      {error ? <p className={styles.error}>{error}</p> : null}

      {!loading && !error ? (
        visibleItems.length > 0 ? (
          <ul className={styles.list}>
            {visibleItems.map((item) => (
              <li key={item.codigo} className={styles.item}>
                <div>
                  <p className={styles.code}>{item.codigo}</p>
                  <p className={styles.name}>{item.nombre}</p>
                </div>
                <div className={styles.meta}>
                  <span className={item.activo ? styles.active : styles.inactive}>
                    {item.activo ? 'Activo' : 'Inactivo'}
                  </span>
                  {item.claveIncendio ? <span className={styles.tag}>{item.claveIncendio}</span> : null}
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <p className={styles.state}>No hay resultados para este filtro.</p>
        )
      ) : null}
    </article>
  )
}

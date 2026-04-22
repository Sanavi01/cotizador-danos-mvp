import type { ReferenceCatalogItem } from '../services/referenceCoreService'
import type { CoverageSelection } from '../services/quoteCoverageOptionsService'
import styles from './GuaranteeSelectorPanel.module.css'

interface GuaranteeSelectorPanelProps {
  items: ReferenceCatalogItem[]
  selected: CoverageSelection[]
  onToggle: (guaranteeCode: string) => void
  disabled?: boolean
}

export function GuaranteeSelectorPanel({ items, selected, onToggle, disabled = false }: GuaranteeSelectorPanelProps) {
  const selectedCodes = new Set(selected.map((item) => item.garantiaCode))

  return (
    <section className={styles.panel}>
      <div className={styles.header}>
        <div>
          <p className={styles.kicker}>Catálogo aprobado</p>
          <h3 className={styles.title}>Selecciona las garantías que participan en el cálculo</h3>
        </div>
        <span className={styles.count}>{selectedCodes.size} seleccionadas</span>
      </div>

      {items.length > 0 ? (
        <div className={styles.list}>
          {items.map((item) => {
            const isSelected = selectedCodes.has(item.codigo)

            return (
              <label key={item.codigo} className={`${styles.item} ${isSelected ? styles.selected : ''}`}>
                <input
                  type="checkbox"
                  checked={isSelected}
                  onChange={() => onToggle(item.codigo)}
                  disabled={disabled}
                />
                <div className={styles.itemCopy}>
                  <div className={styles.itemHeader}>
                    <strong>{item.nombre}</strong>
                    <span className={styles.code}>{item.codigo}</span>
                  </div>
                  <div className={styles.metaRow}>
                    <span className={`${styles.badge} ${item.activo ? styles.active : styles.inactive}`}>
                      {item.activo ? 'Activa' : 'Inactiva'}
                    </span>
                    {item.claveIncendio ? <span className={styles.badge}>{item.claveIncendio}</span> : null}
                  </div>
                </div>
              </label>
            )
          })}
        </div>
      ) : (
        <p className={styles.emptyState}>No hay garantías disponibles en el catálogo core.</p>
      )}
    </section>
  )
}
import type { ReferenceCatalogItem } from '../services/referenceCoreService'
import styles from './GeneralInfoCatalogPanel.module.css'

interface GeneralInfoCatalogPanelProps {
  agents: ReferenceCatalogItem[]
  riskClassifications: ReferenceCatalogItem[]
  businessLines: ReferenceCatalogItem[]
}

function renderCatalogItems(items: ReferenceCatalogItem[]) {
  if (items.length === 0) {
    return <p className={styles.empty}>Sin referencias disponibles.</p>
  }

  return (
    <ul className={styles.list}>
      {items.map((item) => (
        <li key={item.codigo} className={styles.item}>
          <div>
            <p className={styles.code}>{item.codigo}</p>
            <p className={styles.name}>{item.nombre}</p>
          </div>
          <div className={styles.meta}>
            <span className={item.activo ? styles.active : styles.inactive}>{item.activo ? 'Activo' : 'Inactivo'}</span>
            {item.claveIncendio ? <span className={styles.tag}>{item.claveIncendio}</span> : null}
          </div>
        </li>
      ))}
    </ul>
  )
}

function CatalogBlock({ title, items }: { title: string; items: ReferenceCatalogItem[] }) {
  return (
    <details className={styles.block} open>
      <summary className={styles.blockHeader}>
        <h4>{title}</h4>
        <span className={styles.count}>{items.length}</span>
      </summary>
      <div className={styles.blockBody}>{renderCatalogItems(items)}</div>
    </details>
  )
}

export function GeneralInfoCatalogPanel({ agents, riskClassifications, businessLines }: GeneralInfoCatalogPanelProps) {
  return (
    <article className={styles.panel}>
      <div className={styles.header}>
        <div>
          <p className={styles.kicker}>Referencia core</p>
          <h3>Catálogos comerciales</h3>
        </div>
        <p className={styles.description}>Usa estas referencias para alimentar los campos de conducción comercial.</p>
      </div>

      <div className={styles.grid}>
        <CatalogBlock title="Agentes" items={agents} />
        <CatalogBlock title="Clasificaciones de riesgo" items={riskClassifications} />
        <CatalogBlock title="Giros / negocios" items={businessLines} />
      </div>
    </article>
  )
}
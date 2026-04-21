import type { QuoteProgressSummary } from '../services/folioService'
import styles from './ProgressSectionList.module.css'

interface ProgressSectionListProps {
  progress: QuoteProgressSummary
}

const sectionLabels: Array<{ key: keyof QuoteProgressSummary; label: string }> = [
  { key: 'datosGenerales', label: 'Datos generales' },
  { key: 'layoutUbicaciones', label: 'Layout de ubicaciones' },
  { key: 'ubicaciones', label: 'Ubicaciones' },
  { key: 'opcionesCobertura', label: 'Opciones de cobertura' },
]

export function ProgressSectionList({ progress }: ProgressSectionListProps) {
  return (
    <ul className={styles.list}>
      {sectionLabels.map((section) => {
        const status = progress[section.key]

        return (
          <li key={section.key} className={styles.item}>
            <span className={styles.label}>{section.label}</span>
            <strong className={`${styles.status} ${styles[status.toLowerCase()]}`}>{status}</strong>
          </li>
        )
      })}
    </ul>
  )
}
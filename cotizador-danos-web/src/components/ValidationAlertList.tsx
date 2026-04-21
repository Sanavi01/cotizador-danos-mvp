import type { ValidationAlert } from '../services/referenceCoreService'
import styles from './ValidationAlertList.module.css'

interface ValidationAlertListProps {
  alerts: ValidationAlert[]
}

export function ValidationAlertList({ alerts }: ValidationAlertListProps) {
  if (alerts.length === 0) {
    return null
  }

  return (
    <ul className={styles.list}>
      {alerts.map((alert) => (
        <li key={`${alert.codigo}-${alert.mensaje}`} className={`${styles.alert} ${styles[alert.severidad.toLowerCase()]}`}>
          <span className={styles.badge}>{alert.severidad}</span>
          <div>
            <p className={styles.code}>{alert.codigo}</p>
            <p className={styles.message}>{alert.mensaje}</p>
          </div>
        </li>
      ))}
    </ul>
  )
}

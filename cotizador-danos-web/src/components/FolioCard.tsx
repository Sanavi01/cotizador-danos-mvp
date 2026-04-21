import type { CreateFolioResponse } from '../services/folioService'
import styles from './FolioCard.module.css'

interface FolioCardProps {
  folio: CreateFolioResponse
  onOpen: (folio: string) => void
}

export function FolioCard({ folio, onOpen }: FolioCardProps) {
  return (
    <article className={styles.card}>
      <div>
        <p className={styles.label}>Folio creado</p>
        <h2 className={styles.number}>{folio.numeroFolio}</h2>
        <p className={styles.meta}>
          Estado <strong>{folio.estadoCotizacion}</strong> · Versión <strong>{folio.version}</strong>
        </p>
        <p className={styles.meta}>Actualizado {new Date(folio.fechaUltimaActualizacion).toLocaleString('es-CO')}</p>
      </div>
      <button className={styles.primaryButton} type="button" onClick={() => onOpen(folio.numeroFolio)}>
        Abrir estado
      </button>
    </article>
  )
}
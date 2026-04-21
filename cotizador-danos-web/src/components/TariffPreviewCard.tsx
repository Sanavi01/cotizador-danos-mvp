import type { TariffDetails } from '../services/referenceCoreService'
import styles from './TariffPreviewCard.module.css'

interface TariffPreviewCardProps {
  tariff: TariffDetails | null
  loading: boolean
  error: string | null
}

function formatDecimal(value: number) {
  return new Intl.NumberFormat('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4,
  }).format(value)
}

export function TariffPreviewCard({ tariff, loading, error }: TariffPreviewCardProps) {
  return (
    <article className={styles.card}>
      <div className={styles.header}>
        <div>
          <p className={styles.kicker}>Tarifa técnica</p>
          <h3>Vista previa</h3>
        </div>
        <span className={tariff ? styles.ready : styles.idle}>{tariff ? tariff.moneda : 'Sin datos'}</span>
      </div>

      {loading ? <p className={styles.state}>Buscando tarifa...</p> : null}
      {error ? <p className={styles.error}>{error}</p> : null}

      {tariff ? (
        <div className={styles.grid}>
          <div>
            <p className={styles.label}>Clave</p>
            <p className={styles.value}>{tariff.tariffKey}</p>
          </div>
          <div>
            <p className={styles.label}>Tasa</p>
            <p className={styles.value}>{formatDecimal(tariff.rate)}</p>
          </div>
          <div>
            <p className={styles.label}>Factor</p>
            <p className={styles.value}>{formatDecimal(tariff.factor)}</p>
          </div>
          <div>
            <p className={styles.label}>Vigencia</p>
            <p className={styles.value}>
              {new Date(tariff.vigenciaDesde).toLocaleDateString('es-CO')} -{' '}
              {new Date(tariff.vigenciaHasta).toLocaleDateString('es-CO')}
            </p>
          </div>
        </div>
      ) : (
        !loading && !error ? <p className={styles.state}>Ingresa una clave compuesta para consultar la tarifa.</p> : null
      )}
    </article>
  )
}

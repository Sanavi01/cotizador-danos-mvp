import { IdempotencyNotice } from './IdempotencyNotice'
import { ProgressSectionList } from './ProgressSectionList'
import { QuoteStateSummaryCard } from './QuoteStateSummaryCard'
import { ValidationAlertList } from './ValidationAlertList'
import { useQuoteProgress } from '../hooks/useQuoteProgress'
import type { QuoteStateResponse } from '../services/folioService'
import styles from './QuoteProgressCard.module.css'

interface QuoteProgressCardProps {
  state: QuoteStateResponse | null
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value)
}

export function QuoteProgressCard({ state }: QuoteProgressCardProps) {
  const { summary, alerts, financialSummary, readyToCalculate } = useQuoteProgress(state)

  if (!state) {
    return (
      <article className={styles.card}>
        <p className={styles.kicker}>Estado consolidado</p>
        <h2>Consultando el progreso del folio.</h2>
        <p className={styles.description}>
          El backend devolverá aquí el snapshot operativo con estado, progreso, alertas y resultado financiero vigente.
        </p>
      </article>
    )
  }

  return (
    <article className={styles.card}>
      <QuoteStateSummaryCard
        quote={state.numeroFolio}
        state={state.estadoCotizacion}
        version={state.version}
        updatedAt={state.fechaUltimaActualizacion}
        readyToCalculate={readyToCalculate}
      />

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <div>
            <p className={styles.kicker}>Progreso por sección</p>
            <h3>Captura operativa del folio</h3>
          </div>
          <span className={styles.sectionMeta}>{readyToCalculate ? 'Elegible para cálculo' : 'Aún faltan pasos'}</span>
        </div>
        <ProgressSectionList progress={state.progreso} />
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <div>
            <p className={styles.kicker}>Resumen operativo</p>
            <h3>Ubicaciones consolidadas</h3>
          </div>
          <span className={styles.sectionMeta}>{summary.totalActual} registradas</span>
        </div>

        <div className={styles.metrics}>
          <article>
            <strong>{summary.totalEsperado}</strong>
            <span>esperadas</span>
          </article>
          <article>
            <strong>{summary.totalActual}</strong>
            <span>capturadas</span>
          </article>
          <article>
            <strong>{summary.calculables}</strong>
            <span>calculables</span>
          </article>
          <article>
            <strong>{summary.incompletas}</strong>
            <span>incompletas</span>
          </article>
          <article>
            <strong>{summary.invalidas}</strong>
            <span>inválidas</span>
          </article>
          <article>
            <strong>{summary.conAlertas}</strong>
            <span>con alertas</span>
          </article>
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <div>
            <p className={styles.kicker}>Alertas vigentes</p>
            <h3>Validaciones activas del folio</h3>
          </div>
          <span className={styles.sectionMeta}>{state.tieneAlertas ? `${alerts.length} alertas` : 'Sin alertas'}</span>
        </div>

        <ValidationAlertList alerts={alerts} />
        {!state.tieneAlertas ? <p className={styles.emptyState}>No hay alertas activas para este snapshot.</p> : null}
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <div>
            <p className={styles.kicker}>Resultado financiero</p>
            <h3>Snapshot persistido del último cálculo</h3>
          </div>
          <span className={styles.sectionMeta}>{financialSummary ? financialSummary.estadoCalculo : 'Sin cálculo'}</span>
        </div>

        {financialSummary ? (
          <div className={styles.financialGrid}>
            <article>
              <strong>{formatMoney(financialSummary.primaNeta)}</strong>
              <span>prima neta</span>
            </article>
            <article>
              <strong>{formatMoney(financialSummary.primaComercial)}</strong>
              <span>prima comercial</span>
            </article>
            <article>
              <strong>{financialSummary.ubicacionesCalculadas}</strong>
              <span>ubicaciones calculadas</span>
            </article>
            <article>
              <strong>{financialSummary.ubicacionesNoCalculables}</strong>
              <span>no calculables</span>
            </article>
            <article>
              <strong>{financialSummary.calculationParameterVersion}</strong>
              <span>versión de parámetros</span>
            </article>
            <article>
              <strong>{new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(financialSummary.calculatedAt))}</strong>
              <span>calculado</span>
            </article>
          </div>
        ) : (
          <p className={styles.emptyState}>El folio todavía no tiene un resultado financiero persistido.</p>
        )}
      </section>

      <IdempotencyNotice
        variant={readyToCalculate ? 'success' : 'info'}
        message={readyToCalculate ? 'El folio ya cumple con las condiciones mínimas para pasar a cálculo.' : 'El folio sigue en captura y solo refleja el avance consolidado del agregado.'}
      />
    </article>
  )
}
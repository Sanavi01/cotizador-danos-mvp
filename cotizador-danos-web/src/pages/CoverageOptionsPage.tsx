import { type FormEvent, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { CoverageOptionsForm } from '../components/CoverageOptionsForm'
import { CoverageOptionsSummaryCard } from '../components/CoverageOptionsSummaryCard'
import { IdempotencyNotice } from '../components/IdempotencyNotice'
import { ValidationAlertList } from '../components/ValidationAlertList'
import { useCoverageCatalogs } from '../hooks/useCoverageCatalogs'
import { useCoverageOptions } from '../hooks/useCoverageOptions'
import type { ValidationAlert } from '../services/referenceCoreService'
import {
  createEmptyCoverageOptions,
  type CoverageOptionsPayload,
  type CoverageOptionsRecord,
} from '../services/quoteCoverageOptionsService'
import styles from './CoverageOptionsPage.module.css'

interface TechnicalSourceCopy {
  label: string
  description: string
}

const technicalSources: Record<string, TechnicalSourceCopy> = {
  CORE_TARIFF: { label: 'CORE_TARIFF', description: 'Ruta base del fixture principal.' },
  FIRE_TARIFF: { label: 'FIRE_TARIFF', description: 'Tarifa de incendio resuelta por giro y nivel.' },
  CAT_TARIFF: { label: 'CAT_TARIFF', description: 'Ruta catastrófica por zona TEV o equivalente.' },
  FHM_TARIFF: { label: 'FHM_TARIFF', description: 'Ruta FHM opcional cuando la ubicación lo permita.' },
  ELECTRONIC_FACTOR: { label: 'ELECTRONIC_FACTOR', description: 'Factor técnico para equipo electrónico.' },
  UNRESOLVED: { label: 'UNRESOLVED', description: 'La proyección no pudo resolver una clave técnica obligatoria.' },
}

function createDraftCoverageOptions(folio: string, options: CoverageOptionsRecord | null): CoverageOptionsRecord {
  return options ?? createEmptyCoverageOptions(folio)
}

function isCoverageOptionsEmpty(options: CoverageOptionsRecord): boolean {
  return options.opcionesCobertura.garantiasSeleccionadas.length === 0 && options.opcionesCobertura.observaciones === null
}

function validateCoverageDraft(
  coverageOptions: CoverageOptionsRecord,
  guarantees: Array<{ codigo: string; activo: boolean }>,
): ValidationAlert[] {
  const alerts: ValidationAlert[] = []
  const selectedCodes = new Set<string>()
  const guaranteesByCode = new Map(guarantees.map((item) => [item.codigo, item]))

  coverageOptions.opcionesCobertura.garantiasSeleccionadas.forEach((selection, index) => {
    const guaranteeCode = selection.garantiaCode.trim()

    if (!guaranteeCode) {
      alerts.push({
        codigo: `COV-${index + 1}`,
        mensaje: 'Cada garantía seleccionada debe tener un código válido.',
        severidad: 'Error',
      })
      return
    }

    if (selectedCodes.has(guaranteeCode)) {
      alerts.push({
        codigo: 'COV-DUPLICATE',
        mensaje: `La garantía ${guaranteeCode} está duplicada en la configuración.`,
        severidad: 'Error',
      })
      return
    }

    selectedCodes.add(guaranteeCode)

    const guarantee = guaranteesByCode.get(guaranteeCode)
    if (!guarantee) {
      alerts.push({
        codigo: 'COV-UNKNOWN',
        mensaje: `La garantía ${guaranteeCode} no existe en el catálogo aprobado.`,
        severidad: 'Error',
      })
      return
    }

    if (!guarantee.activo) {
      alerts.push({
        codigo: 'COV-INACTIVE',
        mensaje: `La garantía ${guaranteeCode} está inactiva y no puede guardarse.`,
        severidad: 'Error',
      })
    }
  })

  return alerts
}

function buildCoveragePayload(value: CoverageOptionsRecord): CoverageOptionsPayload {
  return {
    version: value.version,
    opcionesCobertura: {
      garantiasSeleccionadas: value.opcionesCobertura.garantiasSeleccionadas.map((selection) => ({
        garantiaCode: selection.garantiaCode.trim(),
        terminos: selection.terminos.map((termino) => termino.trim()).filter((termino) => termino.length > 0),
      })),
      observaciones: value.opcionesCobertura.observaciones?.trim() ? value.opcionesCobertura.observaciones.trim() : null,
    },
  }
}

function formatProjectionReasons(reasons: string[]) {
  if (reasons.length === 0) {
    return null
  }

  return (
    <ul className={styles.reasonList}>
      {reasons.map((reason) => (
        <li key={reason}>{reason}</li>
      ))}
    </ul>
  )
}

export function CoverageOptionsPage() {
  const navigate = useNavigate()
  const { folio } = useParams<{ folio: string }>()
  const { options, loading, saving, error, loadOptions, saveOptions } = useCoverageOptions(folio)
  const {
    guarantees,
    loading: catalogsLoading,
    error: catalogsError,
    reload: reloadGuarantees,
  } = useCoverageCatalogs()
  const [draft, setDraft] = useState<CoverageOptionsRecord>(createDraftCoverageOptions(folio ?? '', options))
  const [attemptedSave, setAttemptedSave] = useState(false)
  const [saveFeedback, setSaveFeedback] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    queueMicrotask(() => {
      if (cancelled) {
        return
      }

      setDraft(createDraftCoverageOptions(folio ?? '', options))
      setAttemptedSave(false)
      setSaveFeedback(null)
    })

    return () => {
      cancelled = true
    }
  }, [folio, options])

  if (!folio) {
    return (
      <main className={styles.page}>
        <IdempotencyNotice variant="warning" message="El número de folio es obligatorio para consultar coberturas." />
      </main>
    )
  }

  const currentDraft = draft ?? createDraftCoverageOptions(folio, options)
  const formLocked = loading || saving || catalogsLoading || Boolean(catalogsError)
  const validationAlerts = validateCoverageDraft(currentDraft, guarantees)

  async function handleRefresh() {
    setAttemptedSave(false)
    setSaveFeedback(null)

    const refreshedOptions = await loadOptions(folio)
    if (refreshedOptions) {
      setDraft(refreshedOptions)
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setAttemptedSave(true)
    setSaveFeedback(null)

    if (validationAlerts.some((alert) => alert.severidad === 'Error')) {
      return
    }

    const savedOptions = await saveOptions(folio, buildCoveragePayload(currentDraft))

    if (savedOptions) {
      setDraft(savedOptions)
      setAttemptedSave(false)
      setSaveFeedback('Las opciones de cobertura se guardaron correctamente.')
    }
  }

  function handleChange(nextDraft: CoverageOptionsRecord) {
    setDraft(nextDraft)
    setAttemptedSave(false)
    setSaveFeedback(null)
  }

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.kicker}>Opciones de cobertura</p>
          <h1>{folio}</h1>
          <p>
            Selecciona garantías globales, ajusta sus términos y consulta la proyección derivada por ubicación sin
            editarla directamente.
          </p>
        </div>

        <div className={styles.actions}>
          <button className={styles.secondaryButton} type="button" onClick={() => navigate(`/quotes/${encodeURIComponent(folio)}/state`)}>
            Estado del folio
          </button>
          <button className={styles.secondaryButton} type="button" onClick={() => navigate(`/quotes/${encodeURIComponent(folio)}/general-info`)}>
            Datos generales
          </button>
          <button className={styles.secondaryButton} type="button" onClick={() => navigate(`/quotes/${encodeURIComponent(folio)}/locations/layout`)}>
            Layout de ubicaciones
          </button>
          <button className={styles.secondaryButton} type="button" onClick={() => navigate(`/quotes/${encodeURIComponent(folio)}/locations`)}>
            Ubicaciones
          </button>
          <button className={styles.secondaryButton} type="button" onClick={() => void reloadGuarantees()} disabled={catalogsLoading}>
            {catalogsLoading ? 'Recargando catálogo...' : 'Recargar catálogo'}
          </button>
          <button className={styles.primaryButton} type="button" onClick={() => void handleRefresh()} disabled={loading}>
            {loading ? 'Consultando...' : 'Actualizar'}
          </button>
        </div>
      </section>

      {error ? <IdempotencyNotice variant="warning" message={error} /> : null}
      {catalogsError ? <IdempotencyNotice variant="warning" message={catalogsError} /> : null}
      {saveFeedback ? <IdempotencyNotice variant="success" message={saveFeedback} /> : null}
      {loading ? <IdempotencyNotice variant="info" message="Consultando opciones de cobertura del folio..." /> : null}
      {catalogsLoading ? <IdempotencyNotice variant="info" message="Consultando catálogo de garantías aprobado..." /> : null}
      {isCoverageOptionsEmpty(currentDraft) && !loading && !error ? (
        <IdempotencyNotice
          variant="info"
          message="La cotización existe, pero todavía no tiene opciones de cobertura capturadas. Selecciona garantías para iniciarlas."
        />
      ) : null}
      {attemptedSave && validationAlerts.length > 0 ? <ValidationAlertList alerts={validationAlerts} /> : null}

      <section className={styles.summaryGrid}>
        <CoverageOptionsSummaryCard
          options={currentDraft.opcionesCobertura}
          version={currentDraft.version}
          updatedAt={currentDraft.fechaUltimaActualizacion}
        />

        <article className={styles.referenceCard}>
          <p className={styles.kicker}>Proyección técnica</p>
          <h2>Vista derivada de solo lectura</h2>
          <p>
            La proyección puede marcar rutas como resueltas o no resueltas sin inventar defaults técnicos. Se recalca
            el origen preliminar de la llave con la que el backend intentaría tarificar.
          </p>
          <div className={styles.referenceList}>
            {Object.values(technicalSources).map((source) => (
              <div key={source.label} className={styles.referenceItem}>
                <strong>{source.label}</strong>
                <span>{source.description}</span>
              </div>
            ))}
          </div>
        </article>
      </section>

      <section className={styles.contentGrid}>
        <CoverageOptionsForm
          value={currentDraft}
          guarantees={guarantees}
          onChange={handleChange}
          onSubmit={handleSubmit}
          loading={formLocked}
        />

        <aside className={styles.sidePanel}>
          <div>
            <p className={styles.kicker}>Catálogo cargado</p>
            <h2>Garantías disponibles</h2>
            <p>
              {guarantees.length} garantías en catálogo. La validación de guardado rechaza códigos inexistentes o
              inactivos.
            </p>
          </div>

          <div className={styles.sideStats}>
            <div>
              <strong>{guarantees.filter((guarantee) => guarantee.activo).length}</strong>
              <span>Activas</span>
            </div>
            <div>
              <strong>{guarantees.filter((guarantee) => !guarantee.activo).length}</strong>
              <span>Inactivas</span>
            </div>
          </div>
        </aside>
      </section>

      <section className={styles.projectionSection}>
        <div className={styles.sectionHeader}>
          <div>
            <p className={styles.kicker}>projectionPerLocation</p>
            <h2>Proyección por ubicación</h2>
          </div>
          <p className={styles.sectionHelper}>
            Esta vista es orientativa y no persiste montos. Solo expone qué garantías parecen tarifables con la
            información ya guardada del folio.
          </p>
        </div>

        {currentDraft.projectionPerLocation.length > 0 ? (
          <div className={styles.projectionGrid}>
            {currentDraft.projectionPerLocation.map((projection) => (
              <article key={projection.indice} className={styles.projectionCard}>
                <div className={styles.projectionHeader}>
                  <div>
                    <p className={styles.projectionIndex}>Ubicación {projection.indice}</p>
                    <h3>{projection.calculablePreview ? 'Calculable en preview' : 'No calculable en preview'}</h3>
                  </div>
                  <span className={`${styles.projectionBadge} ${projection.calculablePreview ? styles.success : styles.warning}`}>
                    {projection.calculablePreview ? 'Calculable' : 'Pendiente'}
                  </span>
                </div>

                {projection.garantiasDerivadas.length > 0 ? (
                  <div className={styles.derivativeList}>
                    {projection.garantiasDerivadas.map((derivative) => {
                      const technicalSource = technicalSources[derivative.fuenteTecnicaPreview] ?? technicalSources.UNRESOLVED

                      return (
                        <article key={`${projection.indice}-${derivative.garantiaCode}`} className={styles.derivativeCard}>
                          <div className={styles.derivativeHeader}>
                            <div>
                              <p className={styles.derivativeCode}>{derivative.garantiaCode}</p>
                              <strong>{technicalSource.label}</strong>
                            </div>
                            <span className={`${styles.projectionBadge} ${derivative.tariffablePreview ? styles.success : styles.warning}`}>
                              {derivative.tariffablePreview ? 'Tarifable' : 'No tarifable'}
                            </span>
                          </div>

                          <p className={styles.derivativeSource}>{technicalSource.description}</p>
                          <p className={styles.derivativeLookup}>
                            {derivative.lookupKeyPreview ?? 'Sin lookupKeyPreview resuelta'}
                          </p>

                          {formatProjectionReasons(derivative.motivosNoTarifable)}
                        </article>
                      )
                    })}
                  </div>
                ) : (
                  <IdempotencyNotice variant="info" message="Todavía no hay garantías derivadas para esta ubicación." />
                )}
              </article>
            ))}
          </div>
        ) : (
          <IdempotencyNotice
            variant="info"
            message="Aún no hay ubicaciones proyectables. Completa el layout y las ubicaciones para ver la vista derivada."
          />
        )}
      </section>
    </main>
  )
}
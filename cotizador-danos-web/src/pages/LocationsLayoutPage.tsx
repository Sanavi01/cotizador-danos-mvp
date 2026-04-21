import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { IdempotencyNotice } from '../components/IdempotencyNotice'
import { LocationsLayoutForm } from '../components/LocationsLayoutForm'
import { LocationsLayoutPreview } from '../components/LocationsLayoutPreview'
import { LocationsLayoutSummaryCard } from '../components/LocationsLayoutSummaryCard'
import { ValidationAlertList } from '../components/ValidationAlertList'
import { useLocationsLayout } from '../hooks/useLocationsLayout'
import { useLocationsLayoutPreview } from '../hooks/useLocationsLayoutPreview'
import { type ValidationAlert } from '../services/referenceCoreService'
import {
  createEmptyLocationsLayout,
  type LocationsLayoutPayload,
  type LocationsLayoutRecord,
} from '../services/quoteLocationsLayoutService'
import styles from './LocationsLayoutPage.module.css'

function createDraftLayout(folio: string, layout: LocationsLayoutRecord | null): LocationsLayoutRecord {
  return layout ?? createEmptyLocationsLayout(folio)
}

function validateLayoutDraft(layout: LocationsLayoutRecord): ValidationAlert[] {
  const alerts: ValidationAlert[] = []
  const { modoCaptura, cantidadUbicaciones, ubicaciones } = layout.configuracionLayout

  if (!modoCaptura) {
    alerts.push({
      codigo: 'LAYOUT-001',
      mensaje: 'El modo de captura es obligatorio.',
      severidad: 'Error',
    })
  }

  if (cantidadUbicaciones === null) {
    alerts.push({
      codigo: 'LAYOUT-002',
      mensaje: 'La cantidad de ubicaciones es obligatoria.',
      severidad: 'Error',
    })
  } else if (cantidadUbicaciones < 1) {
    alerts.push({
      codigo: 'LAYOUT-003',
      mensaje: 'La cantidad de ubicaciones debe ser mayor o igual a 1.',
      severidad: 'Error',
    })
  }

  if (modoCaptura === 'UNICA' && cantidadUbicaciones !== 1) {
    alerts.push({
      codigo: 'LAYOUT-004',
      mensaje: 'El modo UNICA requiere exactamente una ubicación.',
      severidad: 'Error',
    })
  }

  if (modoCaptura === 'MULTIPLE' && (cantidadUbicaciones ?? 0) < 2) {
    alerts.push({
      codigo: 'LAYOUT-005',
      mensaje: 'El modo MULTIPLE requiere al menos dos ubicaciones.',
      severidad: 'Error',
    })
  }

  if (cantidadUbicaciones !== null && ubicaciones.length !== cantidadUbicaciones) {
    alerts.push({
      codigo: 'LAYOUT-006',
      mensaje: 'La cantidad de slots visibles no coincide con la cantidad configurada.',
      severidad: 'Warning',
    })
  }

  const duplicateIndices = ubicaciones
    .map((slot) => slot.indice)
    .filter((indice, index, list) => list.indexOf(indice) !== index)

  if (duplicateIndices.length > 0) {
    alerts.push({
      codigo: 'LAYOUT-007',
      mensaje: 'Los índices del layout deben ser únicos.',
      severidad: 'Error',
    })
  }

  const duplicateOrders = ubicaciones
    .map((slot) => slot.ordenCaptura)
    .filter((orden, index, list) => list.indexOf(orden) !== index)

  if (duplicateOrders.length > 0) {
    alerts.push({
      codigo: 'LAYOUT-008',
      mensaje: 'El orden de captura no puede repetirse.',
      severidad: 'Error',
    })
  }

  if (ubicaciones.some((slot) => slot.indice < 1)) {
    alerts.push({
      codigo: 'LAYOUT-009',
      mensaje: 'Los índices deben iniciar en 1.',
      severidad: 'Error',
    })
  }

  if (ubicaciones.some((slot) => slot.ordenCaptura < 1)) {
    alerts.push({
      codigo: 'LAYOUT-010',
      mensaje: 'El orden de captura debe iniciar en 1.',
      severidad: 'Error',
    })
  }

  if (cantidadUbicaciones !== null && ubicaciones.some((slot) => slot.ordenCaptura > cantidadUbicaciones)) {
    alerts.push({
      codigo: 'LAYOUT-011',
      mensaje: 'El orden de captura no puede ser mayor que la cantidad de ubicaciones configurada.',
      severidad: 'Error',
    })
  }

  return alerts
}

function buildLayoutPayload(layout: LocationsLayoutRecord): LocationsLayoutPayload {
  return {
    version: layout.version,
    configuracionLayout: {
      modoCaptura: layout.configuracionLayout.modoCaptura as NonNullable<
        LocationsLayoutRecord['configuracionLayout']['modoCaptura']
      >,
      cantidadUbicaciones: layout.configuracionLayout.cantidadUbicaciones as number,
      ubicaciones: layout.configuracionLayout.ubicaciones,
    },
  }
}

export function LocationsLayoutPage() {
  const navigate = useNavigate()
  const { folio } = useParams<{ folio: string }>()
  const { layout, loading, saving, error, loadLayout, saveLayout } = useLocationsLayout(folio)
  const [draft, setDraft] = useState<LocationsLayoutRecord>(createDraftLayout(folio ?? '', layout))
  const [attemptedSave, setAttemptedSave] = useState(false)
  const [saveFeedback, setSaveFeedback] = useState<string | null>(null)
  const validationAlerts = validateLayoutDraft(draft)
  const preview = useLocationsLayoutPreview(draft.configuracionLayout)
  const formLocked = loading || saving

  useEffect(() => {
    let cancelled = false

    queueMicrotask(() => {
      if (cancelled) {
        return
      }

      setDraft(createDraftLayout(folio ?? '', layout))
      setSaveFeedback(null)
    })

    return () => {
      cancelled = true
    }
  }, [folio, layout])

  if (!folio) {
    return (
      <main className={styles.page}>
        <IdempotencyNotice variant="warning" message="El número de folio es obligatorio para consultar el layout." />
      </main>
    )
  }

  async function handleRefresh() {
    setAttemptedSave(false)
    setSaveFeedback(null)

    const refreshedLayout = await loadLayout(folio)

    if (refreshedLayout) {
      setDraft(refreshedLayout)
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setAttemptedSave(true)
    setSaveFeedback(null)

    if (validationAlerts.some((alert) => alert.severidad === 'Error')) {
      return
    }

    const savedLayout = await saveLayout(folio, buildLayoutPayload(draft))

    if (savedLayout) {
      setDraft(savedLayout)
      setSaveFeedback('La configuración del layout se guardó correctamente.')
      setAttemptedSave(false)
    }
  }

  function handleChange(nextDraft: LocationsLayoutRecord) {
    setDraft(nextDraft)
    setSaveFeedback(null)
  }

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.kicker}>Layout de ubicaciones</p>
          <h1>{folio}</h1>
          <p>
            Define la cantidad y el orden de captura sin tocar las demás secciones del agregado. El folio queda listo
            para continuar con ubicaciones de una o varias posiciones.
          </p>
        </div>

        <div className={styles.actions}>
          <button
            className={styles.secondaryButton}
            type="button"
            onClick={() => navigate(`/quotes/${encodeURIComponent(folio)}/state`)}
          >
            Estado del folio
          </button>
          <button
            className={styles.secondaryButton}
            type="button"
            onClick={() => navigate(`/quotes/${encodeURIComponent(folio)}/general-info`)}
          >
            Datos generales
          </button>
          <button
            className={styles.secondaryButton}
            type="button"
            onClick={() => navigate(`/quotes/${encodeURIComponent(folio)}/locations`)}
          >
            Ubicaciones
          </button>
          <button className={styles.secondaryButton} type="button" onClick={() => void handleRefresh()} disabled={loading}>
            {loading ? 'Consultando...' : 'Actualizar layout'}
          </button>
        </div>
      </section>

      {error ? <IdempotencyNotice variant="warning" message={error} /> : null}
      {saveFeedback ? <IdempotencyNotice variant="success" message={saveFeedback} /> : null}
      {!loading && !error && draft.configuracionLayout.modoCaptura === null ? (
        <IdempotencyNotice
          variant="info"
          message="La cotización existe, pero todavía no tiene un layout definido. Completa el modo y la cantidad para iniciarlo."
        />
      ) : null}
      {attemptedSave && validationAlerts.length > 0 ? <ValidationAlertList alerts={validationAlerts} /> : null}

      <section className={styles.summaryGrid}>
        <LocationsLayoutSummaryCard layout={draft} version={draft.version} updatedAt={draft.fechaUltimaActualizacion} />
      </section>

      <section className={styles.contentGrid}>
        <LocationsLayoutForm value={draft} onChange={handleChange} onSubmit={handleSubmit} loading={formLocked} />

        <div className={styles.sideStack}>
          <IdempotencyNotice
            variant={preview.canAddSlot ? 'warning' : 'info'}
            message={
              preview.mode && draft.configuracionLayout.cantidadUbicaciones
                ? preview.canAddSlot
                  ? 'La vista previa todavía admite slots adicionales respecto a la cantidad configurada.'
                  : 'La vista previa está sincronizada con la cantidad y el orden de captura definidos.'
                : 'Selecciona un modo de captura y una cantidad para proyectar las ubicaciones.'
            }
          />

          <LocationsLayoutPreview slots={preview.slots} mode={preview.mode} />
        </div>
      </section>
    </main>
  )
}
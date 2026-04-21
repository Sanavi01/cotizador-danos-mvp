import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { GeneralInfoCatalogPanel } from '../components/GeneralInfoCatalogPanel'
import { GeneralInfoForm } from '../components/GeneralInfoForm'
import type { GeneralInfoFieldName } from '../components/GeneralInfoForm'
import { GeneralInfoSummaryCard } from '../components/GeneralInfoSummaryCard'
import { IdempotencyNotice } from '../components/IdempotencyNotice'
import { useGeneralInfo } from '../hooks/useGeneralInfo'
import { useGeneralInfoCatalogs } from '../hooks/useGeneralInfoCatalogs'
import type { GeneralInfoPayload, GeneralInfoRecord } from '../services/quoteGeneralInfoService'
import styles from './GeneralInfoPage.module.css'

function createEmptyGeneralInfo(folio: string): GeneralInfoRecord {
  return {
    numeroFolio: folio,
    version: 0,
    fechaUltimaActualizacion: '',
    datosAsegurado: {
      tipoDocumento: '',
      numeroDocumento: '',
      nombreORazonSocial: '',
      correoElectronico: '',
      telefono: '',
    },
    datosConduccion: {
      codigoAgente: '',
      clasificacionRiesgo: '',
      tipoNegocio: '',
    },
  }
}

function isGeneralInfoEmpty(generalInfo: GeneralInfoRecord | null): boolean {
  if (!generalInfo) {
    return false
  }

  return [
    generalInfo.datosAsegurado.tipoDocumento,
    generalInfo.datosAsegurado.numeroDocumento,
    generalInfo.datosAsegurado.nombreORazonSocial,
    generalInfo.datosAsegurado.correoElectronico,
    generalInfo.datosAsegurado.telefono,
    generalInfo.datosConduccion.codigoAgente,
    generalInfo.datosConduccion.clasificacionRiesgo,
    generalInfo.datosConduccion.tipoNegocio,
  ].every((value) => value.trim().length === 0)
}

interface FieldFeedback {
  message: string
  severity: 'error' | 'warning'
}

type FieldErrorState = Partial<Record<GeneralInfoFieldName, FieldFeedback>>

function validateDraft(generalInfo: GeneralInfoRecord): FieldErrorState {
  const alerts: FieldErrorState = {}

  if (!generalInfo.datosAsegurado.tipoDocumento.trim()) {
    alerts.tipoDocumento = { message: 'El tipo de documento es obligatorio.', severity: 'error' }
  }

  if (!generalInfo.datosAsegurado.numeroDocumento.trim()) {
    alerts.numeroDocumento = { message: 'El número de documento es obligatorio.', severity: 'error' }
  }

  if (!generalInfo.datosAsegurado.nombreORazonSocial.trim()) {
    alerts.nombreORazonSocial = { message: 'El nombre o razón social es obligatorio.', severity: 'error' }
  }

  if (!generalInfo.datosConduccion.codigoAgente.trim()) {
    alerts.codigoAgente = { message: 'El código de agente es obligatorio.', severity: 'error' }
  }

  if (!generalInfo.datosConduccion.clasificacionRiesgo.trim()) {
    alerts.clasificacionRiesgo = { message: 'La clasificación de riesgo es obligatoria.', severity: 'error' }
  }

  if (!generalInfo.datosConduccion.tipoNegocio.trim()) {
    alerts.tipoNegocio = { message: 'El tipo de negocio es obligatorio.', severity: 'error' }
  }

  const correoElectronico = generalInfo.datosAsegurado.correoElectronico.trim()
  if (correoElectronico && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correoElectronico)) {
    alerts.correoElectronico = { message: 'El correo electrónico no tiene un formato válido.', severity: 'warning' }
  }

  return alerts
}

export function GeneralInfoPage() {
  const navigate = useNavigate()
  const { folio } = useParams<{ folio: string }>()
  const {
    generalInfo,
    loading,
    saving,
    error,
    loadGeneralInfo,
    saveGeneralInfo,
  } = useGeneralInfo(folio)
  const {
    agents,
    riskClassifications,
    businessLines,
    loading: catalogsLoading,
    error: catalogsError,
    reload: reloadCatalogs,
  } = useGeneralInfoCatalogs()
  const [draft, setDraft] = useState<GeneralInfoRecord | null>(folio ? createEmptyGeneralInfo(folio) : null)
  const [fieldErrors, setFieldErrors] = useState<FieldErrorState>({})

  useEffect(() => {
    if (folio) {
      setDraft(createEmptyGeneralInfo(folio))
      setFieldErrors({})
    }
  }, [folio])

  useEffect(() => {
    if (generalInfo) {
      setDraft(generalInfo)
      setFieldErrors({})
    }
  }, [generalInfo])

  if (!folio) {
    return (
      <main className={styles.page}>
        <IdempotencyNotice variant="warning" message="El número de folio es obligatorio para consultar los datos generales." />
      </main>
    )
  }

  const currentDraft = draft ?? createEmptyGeneralInfo(folio)
  const formLocked = loading || saving || (!generalInfo && Boolean(error))

  async function handleRefresh() {
    const refreshedGeneralInfo = await loadGeneralInfo(folio)

    if (refreshedGeneralInfo) {
      setDraft(refreshedGeneralInfo)
      setFieldErrors({})
    }
  }

  async function handleSave(payload: GeneralInfoPayload) {
    if (!draft) {
      return
    }

    const draftErrors = validateDraft(draft)
    if (Object.keys(draftErrors).length > 0) {
      setFieldErrors(draftErrors)
      return
    }

    const savedGeneralInfo = await saveGeneralInfo(folio, payload)

    if (savedGeneralInfo) {
      setDraft(savedGeneralInfo)
      setFieldErrors({})
    }
  }

  function handleChange(nextDraft: GeneralInfoRecord) {
    setDraft(nextDraft)
    setFieldErrors({})
  }

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.kicker}>Datos generales de cotización</p>
          <h1>Retoma el folio, valida referencias y guarda solo esta sección.</h1>
          <p>
            La captura conserva la versión del agregado y valida los catálogos comerciales antes de persistir el
            formulario.
          </p>
        </div>

        <div className={styles.actions}>
          <button className={styles.secondaryButton} type="button" onClick={() => navigate(`/quotes/${encodeURIComponent(folio)}/state`)}>
            Estado del folio
          </button>
          <button className={styles.secondaryButton} type="button" onClick={() => void reloadCatalogs()} disabled={catalogsLoading}>
            {catalogsLoading ? 'Recargando catálogos...' : 'Recargar catálogos'}
          </button>
          <button className={styles.primaryButton} type="button" onClick={() => void handleRefresh()} disabled={loading}>
            {loading ? 'Consultando...' : 'Actualizar datos'}
          </button>
        </div>
      </section>

      {error ? <IdempotencyNotice variant="warning" message={error} /> : null}
      {catalogsError ? <IdempotencyNotice variant="warning" message={catalogsError} /> : null}
      {loading ? <IdempotencyNotice variant="info" message="Consultando la sección general del folio..." /> : null}
      {isGeneralInfoEmpty(generalInfo) && !loading && !error ? (
        <IdempotencyNotice
          variant="info"
          message="Esta cotización existe, pero aún no tiene datos generales capturados. Completa el formulario para iniciar la edición."
        />
      ) : null}

      <section className={styles.summaryGrid}>
        <GeneralInfoSummaryCard
          quote={currentDraft.numeroFolio}
          version={currentDraft.version}
          updatedAt={currentDraft.fechaUltimaActualizacion}
        />
      </section>

      <section className={styles.contentGrid}>
        <GeneralInfoForm
          value={currentDraft}
          catalogs={{ agents, riskClassifications, businessLines }}
          fieldErrors={fieldErrors}
          onChange={handleChange}
          onSubmit={handleSave}
          loading={formLocked}
        />

        <GeneralInfoCatalogPanel
          agents={agents}
          riskClassifications={riskClassifications}
          businessLines={businessLines}
        />
      </section>
    </main>
  )
}
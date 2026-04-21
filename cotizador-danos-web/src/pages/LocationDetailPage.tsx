import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { IdempotencyNotice } from '../components/IdempotencyNotice'
import { LocationForm } from '../components/LocationForm'
import { ValidationAlertList } from '../components/ValidationAlertList'
import { useLocationCatalogs } from '../hooks/useLocationCatalogs'
import { useLocationEditor } from '../hooks/useLocationEditor'
import { useZipCodeValidation } from '../hooks/useZipCodeValidation'
import type { LocationChanges, LocationRecord } from '../services/quoteLocationsService'
import styles from './LocationDetailPage.module.css'

function cloneLocation(location: LocationRecord): LocationRecord {
  return {
    ...location,
    giro: location.giro ? { ...location.giro } : null,
    garantias: location.garantias.map((garantia) => ({ ...garantia })),
    zonaCatastrofica: location.zonaCatastrofica ? { ...location.zonaCatastrofica } : null,
    alertasBloqueantes: location.alertasBloqueantes.map((alerta) => ({ ...alerta })),
  }
}

function buildLocationChanges(location: LocationRecord): LocationChanges {
  return {
    nombreUbicacion: location.nombreUbicacion,
    direccion: location.direccion,
    codigoPostal: location.codigoPostal,
    estado: location.estado,
    municipio: location.municipio,
    colonia: location.colonia,
    ciudad: location.ciudad,
    tipoConstructivo: location.tipoConstructivo,
    nivel: location.nivel,
    anioConstruccion: location.anioConstruccion,
    giro: location.giro,
    zonaCatastrofica: location.zonaCatastrofica,
  }
}

function applyZipCodeReference(location: LocationRecord, validation: {
  zipCode: string
  municipio: string | null
  estado: string | null
  coloniaBarrio: string | null
  zona_tev: string | null
  zona_fhm: string | null
}): LocationRecord {
  return {
    ...location,
    codigoPostal: validation.zipCode,
    estado: validation.estado,
    municipio: validation.municipio,
    ciudad: validation.municipio,
    colonia: validation.coloniaBarrio,
    zonaCatastrofica:
      validation.zona_tev || validation.zona_fhm
        ? {
            zonaTev: validation.zona_tev,
            zonaFhm: validation.zona_fhm,
          }
        : null,
  }
}

export function LocationDetailPage() {
  const navigate = useNavigate()
  const { folio, indice } = useParams<{ folio: string; indice: string }>()
  const indiceNumero = Number.parseInt(indice ?? '', 10)
  const { location, version, loading, saving, error, loadLocation, saveLocation } = useLocationEditor(
    folio,
    Number.isFinite(indiceNumero) ? indiceNumero : undefined,
  )
  const { businessLines, loading: catalogsLoading, error: catalogsError } = useLocationCatalogs()
  const {
    validation: zipCodeValidation,
    loading: zipCodeLoading,
    error: zipCodeError,
    validateZipCode,
    reset: resetZipCodeValidation,
  } = useZipCodeValidation()
  const [draftLocation, setDraftLocation] = useState<LocationRecord | null>(null)
  const [feedback, setFeedback] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    queueMicrotask(() => {
      if (cancelled || !location) {
        return
      }

      setDraftLocation(cloneLocation(location))
    })

    return () => {
      cancelled = true
    }
  }, [location])

  if (!folio || !Number.isFinite(indiceNumero)) {
    return (
      <main className={styles.page}>
        <IdempotencyNotice variant="warning" message="El folio y el índice de la ubicación son obligatorios." />
      </main>
    )
  }

  async function handleRefresh() {
    setFeedback(null)
    const refreshedLocation = await loadLocation(folio, indiceNumero)

    if (refreshedLocation) {
      setDraftLocation(cloneLocation(refreshedLocation))
    }
  }

  function handleChange(nextLocation: LocationRecord) {
    setDraftLocation(nextLocation)
    setFeedback(null)

    if (nextLocation.codigoPostal !== draftLocation?.codigoPostal) {
      resetZipCodeValidation()
    }
  }

  async function handleZipCodeSelect(zipCode: string) {
    if (!draftLocation) {
      return
    }

    if (!zipCode.trim()) {
      setDraftLocation({
        ...draftLocation,
        codigoPostal: null,
        estado: null,
        municipio: null,
        ciudad: null,
        colonia: null,
        zonaCatastrofica: null,
      })
      resetZipCodeValidation()
      return
    }

    const validation = await validateZipCode(zipCode)
    setDraftLocation((currentDraft) => (currentDraft ? applyZipCodeReference(currentDraft, validation) : currentDraft))
    setFeedback(null)
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!draftLocation) {
      return
    }

    const savedLocation = await saveLocation(folio, indiceNumero, {
      version,
      changes: buildLocationChanges(draftLocation),
    })

    if (!savedLocation) {
      return
    }

    setDraftLocation(cloneLocation(savedLocation))
    setFeedback('La ubicación se guardó correctamente.')
  }

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.kicker}>Detalle de ubicación</p>
          <h1>
            Folio {folio} · Ubicación {indiceNumero}
          </h1>
          <p>
            Edita una ubicación puntual sin sobrescribir las demás. El backend recalcula el estado de validación y las
            alertas bloqueantes al guardar.
          </p>
        </div>

        <div className={styles.actions}>
          <button className={styles.secondaryButton} type="button" onClick={() => navigate(`/quotes/${encodeURIComponent(folio)}/locations`)}>
            Volver a ubicaciones
          </button>
          <button className={styles.secondaryButton} type="button" onClick={() => navigate(`/quotes/${encodeURIComponent(folio)}/state`)}>
            Estado del folio
          </button>
          <button className={styles.secondaryButton} type="button" onClick={() => navigate(`/quotes/${encodeURIComponent(folio)}/general-info`)}>
            Datos generales
          </button>
          <button className={styles.primaryButton} type="button" onClick={() => void handleRefresh()} disabled={loading}>
            {loading ? 'Consultando...' : 'Actualizar ubicación'}
          </button>
        </div>
      </section>

      {error ? <IdempotencyNotice variant="warning" message={error} /> : null}
      {feedback ? <IdempotencyNotice variant="success" message={feedback} /> : null}
      {loading ? <IdempotencyNotice variant="info" message="Consultando la ubicación seleccionada..." /> : null}
      {!loading && !error && !draftLocation ? (
        <IdempotencyNotice variant="info" message="No se encontró la ubicación solicitada dentro del folio." />
      ) : null}

      {draftLocation ? (
        <section className={styles.contentGrid}>
          <LocationForm
            value={draftLocation}
            onChange={handleChange}
            onSubmit={handleSubmit}
            loading={saving}
            submitLabel="Guardar ubicación puntual"
            businessLines={businessLines}
            catalogsLoading={catalogsLoading}
            catalogsError={catalogsError}
            zipCodeValidation={zipCodeValidation}
            zipCodeLoading={zipCodeLoading}
            zipCodeError={zipCodeError}
            onZipCodeSelect={handleZipCodeSelect}
          />

          <aside className={styles.sideStack}>
            <article className={styles.summaryCard}>
              <p className={styles.kicker}>Metadata</p>
              <h2>{draftLocation.nombreUbicacion ?? `Ubicación ${draftLocation.indice}`}</h2>
              <p className={styles.meta}>Versión {version}</p>
              <p className={styles.meta}>Estado {draftLocation.estadoValidacion}</p>
              <p className={styles.meta}>Actualizado {draftLocation.updatedAt ?? 'Pendiente'}</p>
            </article>

            {draftLocation.alertasBloqueantes.length > 0 ? (
              <ValidationAlertList alerts={draftLocation.alertasBloqueantes} />
            ) : (
              <IdempotencyNotice variant="info" message="La ubicación no presenta alertas bloqueantes visibles en el momento." />
            )}
          </aside>
        </section>
      ) : null}
    </main>
  )
}
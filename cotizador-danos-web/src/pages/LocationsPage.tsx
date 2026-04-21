import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { IdempotencyNotice } from '../components/IdempotencyNotice'
import { LocationForm } from '../components/LocationForm'
import { LocationSummaryCard } from '../components/LocationSummaryCard'
import { LocationsList } from '../components/LocationsList'
import { ValidationAlertList } from '../components/ValidationAlertList'
import { useLocationCatalogs } from '../hooks/useLocationCatalogs'
import { useLocationSummary } from '../hooks/useLocationSummary'
import { useLocations } from '../hooks/useLocations'
import { useZipCodeValidation } from '../hooks/useZipCodeValidation'
import { type LocationRecord, toLocationDraft } from '../services/quoteLocationsService'
import styles from './LocationsPage.module.css'

function cloneLocation(location: LocationRecord): LocationRecord {
  return {
    ...location,
    giro: location.giro ? { ...location.giro } : null,
    garantias: location.garantias.map((garantia) => ({ ...garantia })),
    zonaCatastrofica: location.zonaCatastrofica ? { ...location.zonaCatastrofica } : null,
    alertasBloqueantes: location.alertasBloqueantes.map((alerta) => ({ ...alerta })),
  }
}

function mergeDraftLocation(locations: LocationRecord[], draftLocation: LocationRecord): LocationRecord[] {
  const existingIndex = locations.findIndex((location) => location.indice === draftLocation.indice)

  if (existingIndex < 0) {
    return [...locations, draftLocation].sort((left, right) => left.indice - right.indice)
  }

  return locations.map((location) => (location.indice === draftLocation.indice ? draftLocation : location))
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

export function LocationsPage() {
  const navigate = useNavigate()
  const { folio } = useParams<{ folio: string }>()
  const {
    locations,
    version,
    loading,
    saving,
    error,
    loadLocations,
    saveLocations,
    createEmptyLocation,
  } = useLocations(folio)
  const { businessLines, loading: catalogsLoading, error: catalogsError } = useLocationCatalogs()
  const {
    validation: zipCodeValidation,
    loading: zipCodeLoading,
    error: zipCodeError,
    validateZipCode,
    reset: resetZipCodeValidation,
  } = useZipCodeValidation()
  const { summary, loading: summaryLoading, error: summaryError, refresh: refreshSummary } = useLocationSummary(folio)
  const [selectedIndice, setSelectedIndice] = useState<number | null>(null)
  const [draftLocation, setDraftLocation] = useState<LocationRecord | null>(null)
  const [feedback, setFeedback] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    queueMicrotask(() => {
      if (cancelled) {
        return
      }

      if (locations.length === 0) {
        if (draftLocation) {
          return
        }

        setSelectedIndice(null)
        setDraftLocation(null)
        return
      }

      const matchedLocation = selectedIndice === null ? null : locations.find((location) => location.indice === selectedIndice)

      if (draftLocation && selectedIndice !== null && draftLocation.indice === selectedIndice && !matchedLocation) {
        return
      }

      const nextLocation = matchedLocation ?? locations[0]

      if (selectedIndice === null || !matchedLocation) {
        setSelectedIndice(nextLocation.indice)
      }

      setDraftLocation(cloneLocation(nextLocation))
    })

    return () => {
      cancelled = true
    }
  }, [locations, selectedIndice])

  if (!folio) {
    return (
      <main className={styles.page}>
        <IdempotencyNotice variant="warning" message="El número de folio es obligatorio para consultar ubicaciones." />
      </main>
    )
  }

  async function handleRefresh() {
    setFeedback(null)
    await loadLocations(folio)
    await refreshSummary(folio)
  }

  function handleSelect(location: LocationRecord) {
    setSelectedIndice(location.indice)
    setDraftLocation(cloneLocation(location))
    setFeedback(null)
  }

  function handleDraftChange(nextLocation: LocationRecord) {
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
      const clearedLocation = {
        ...draftLocation,
        codigoPostal: null,
        estado: null,
        municipio: null,
        ciudad: null,
        colonia: null,
        zonaCatastrofica: null,
      }

      setDraftLocation(clearedLocation)
      resetZipCodeValidation()
      return
    }

    const validation = await validateZipCode(zipCode)
    setDraftLocation((currentDraft) => (currentDraft ? applyZipCodeReference(currentDraft, validation) : currentDraft))
    setFeedback(null)
  }

  function handleCreateLocation(indice: number) {
    const nextLocation = createEmptyLocation(indice)
    setSelectedIndice(indice)
    setDraftLocation(nextLocation)
    setFeedback(null)
  }

  function handleSummarySelect(indice: number) {
    const existingLocation = locations.find((location) => location.indice === indice)

    if (existingLocation) {
      handleSelect(existingLocation)
      return
    }

    handleCreateLocation(indice)
  }

  async function handleSnapshotSave() {
    if (!draftLocation) {
      return
    }

    const nextLocations = mergeDraftLocation(locations, draftLocation)
    const savedLocations = await saveLocations(folio, {
      version,
      ubicaciones: nextLocations.map(toLocationDraft),
    })

    if (savedLocations) {
      const savedLocation = savedLocations.find((location) => location.indice === draftLocation.indice)
      setDraftLocation(savedLocation ? cloneLocation(savedLocation) : cloneLocation(draftLocation))
      setSelectedIndice(draftLocation.indice)
      setFeedback('El snapshot completo de ubicaciones se guardó correctamente.')
      await refreshSummary(folio)
    }
  }

  const selectedLocation = draftLocation ?? locations.find((location) => location.indice === selectedIndice) ?? null
  const nextEmptyIndice = summary?.resumenPorIndice.find((item) => item.estadoValidacion === 'EMPTY')?.indice ?? 1
  const canCreateLocation = !loading && !saving && nextEmptyIndice >= 1

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.kicker}>Ubicaciones de cotización</p>
          <h1>{folio}</h1>
          <p>
            Revisa el estado operativo, selecciona una ubicación y persiste el snapshot completo sin afectar las demás
            secciones del agregado.
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
          <button className={styles.primaryButton} type="button" onClick={() => void handleRefresh()} disabled={loading || summaryLoading}>
            {loading || summaryLoading ? 'Actualizando...' : 'Actualizar ubicaciones'}
          </button>
        </div>
      </section>

      {error ? <IdempotencyNotice variant="warning" message={error} /> : null}
      {summaryError ? <IdempotencyNotice variant="warning" message={summaryError} /> : null}
      {feedback ? <IdempotencyNotice variant="success" message={feedback} /> : null}
      {!loading && !error && locations.length === 0 ? (
        <section className={styles.mainStack}>
          <IdempotencyNotice variant="info" message="Este folio todavía no tiene ubicaciones persistidas. Inicia la captura desde el primer slot esperado del layout." />
          <div className={styles.actions}>
            <button
              className={styles.primaryButton}
              type="button"
              onClick={() => handleCreateLocation(nextEmptyIndice)}
              disabled={!canCreateLocation}
            >
              {`Capturar ubicación ${nextEmptyIndice}`}
            </button>
          </div>
        </section>
      ) : null}

      <section className={styles.summaryGrid}>
        <LocationSummaryCard summary={summary} onSelectIndice={handleSummarySelect} />
      </section>

      <section className={styles.contentGrid}>
        <div className={styles.mainStack}>
          <LocationsList
            locations={locations}
            onSelect={handleSelect}
            onEdit={(location) => navigate(`/quotes/${encodeURIComponent(folio)}/locations/${location.indice}`)}
          />
        </div>

        <aside className={styles.sideStack}>
          {selectedLocation ? (
            <>
              <LocationForm
                value={selectedLocation}
                onChange={handleDraftChange}
                onSubmit={(event) => {
                  event.preventDefault()
                  void handleSnapshotSave()
                }}
                loading={saving}
                submitLabel="Guardar snapshot completo"
                businessLines={businessLines}
                catalogsLoading={catalogsLoading}
                catalogsError={catalogsError}
                zipCodeValidation={zipCodeValidation}
                zipCodeLoading={zipCodeLoading}
                zipCodeError={zipCodeError}
                onZipCodeSelect={handleZipCodeSelect}
              />

              {selectedLocation.alertasBloqueantes.length > 0 ? (
                <ValidationAlertList alerts={selectedLocation.alertasBloqueantes} />
              ) : null}
            </>
          ) : (
            <section className={styles.mainStack}>
              <IdempotencyNotice
                variant="info"
                message="Selecciona una ubicación para editarla en el panel lateral o inicia la captura de un slot pendiente del layout."
              />
              <div className={styles.actions}>
                <button
                  className={styles.primaryButton}
                  type="button"
                  onClick={() => handleCreateLocation(nextEmptyIndice)}
                  disabled={!canCreateLocation}
                >
                  {`Capturar ubicación ${nextEmptyIndice}`}
                </button>
              </div>
            </section>
          )}
        </aside>
      </section>
    </main>
  )
}
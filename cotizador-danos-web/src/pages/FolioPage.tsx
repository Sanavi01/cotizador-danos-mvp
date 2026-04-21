import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { CatalogLookupPanel } from '../components/CatalogLookupPanel'
import { FolioCard } from '../components/FolioCard'
import { IdempotencyNotice } from '../components/IdempotencyNotice'
import { TariffPreviewCard } from '../components/TariffPreviewCard'
import { ZipCodeValidationCard } from '../components/ZipCodeValidationCard'
import { useCoreCatalogs } from '../hooks/useCoreCatalogs'
import { useFolioCreation } from '../hooks/useFolioCreation'
import { useTariffLookup } from '../hooks/useTariffLookup'
import { useZipCodeValidation } from '../hooks/useZipCodeValidation'
import type { CreateFolioPayload } from '../services/folioService'
import type { ReferenceCatalogItem } from '../services/referenceCoreService'
import styles from './FolioPage.module.css'

function createIdempotencyKey() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }

  return `folio-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

export function FolioPage() {
  const navigate = useNavigate()
  const { createFolio, loading, error, folio, reset } = useFolioCreation()
  const {
    subscribers,
    agents,
    businessLines,
    riskClassifications,
    guarantees,
    loading: catalogsLoading,
    error: catalogsError,
    reload: reloadCatalogs,
  } = useCoreCatalogs()
  const { validation: zipCodeValidation, loading: zipCodeLoading, error: zipCodeError, validateZipCode } =
    useZipCodeValidation()
  const { tariff, loading: tariffLoading, error: tariffError, lookupTariff } = useTariffLookup()
  const [origin, setOrigin] = useState('spa')
  const [idempotencyKey, setIdempotencyKey] = useState(() => createIdempotencyKey())
  const [subscriberFilter, setSubscriberFilter] = useState('')
  const [agentFilter, setAgentFilter] = useState('')
  const [businessLineFilter, setBusinessLineFilter] = useState('')
  const [riskClassificationFilter, setRiskClassificationFilter] = useState('')
  const [guaranteeFilter, setGuaranteeFilter] = useState('')
  const [zipCode, setZipCode] = useState('110111')
  const [tariffKey, setTariffKey] = useState('GIRO-001|ZTEV-2|GAR-INC-ED')

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const payload: CreateFolioPayload = {
      origin: origin.trim() || 'spa',
    }

    await createFolio(payload, idempotencyKey)
  }

  function openFolio(number: string) {
    navigate(`/quotes/${encodeURIComponent(number)}/state`)
  }

  function handleNewRequest() {
    reset()
    setIdempotencyKey(createIdempotencyKey())
  }

  async function handleZipCodeValidation(value: string) {
    await validateZipCode(value)
  }

  async function handleTariffLookup(value: string) {
    await lookupTariff(value)
  }

  const catalogPanels: Array<{
    key: string
    title: string
    items: ReferenceCatalogItem[]
    filterValue: string
    onFilterChange: (value: string) => void
  }> = [
    {
      key: 'subscribers',
      title: 'Suscriptores',
      items: subscribers,
      filterValue: subscriberFilter,
      onFilterChange: setSubscriberFilter,
    },
    {
      key: 'agents',
      title: 'Agentes',
      items: agents,
      filterValue: agentFilter,
      onFilterChange: setAgentFilter,
    },
    {
      key: 'businessLines',
      title: 'Giros',
      items: businessLines,
      filterValue: businessLineFilter,
      onFilterChange: setBusinessLineFilter,
    },
    {
      key: 'riskClassifications',
      title: 'Clasificaciones de riesgo',
      items: riskClassifications,
      filterValue: riskClassificationFilter,
      onFilterChange: setRiskClassificationFilter,
    },
    {
      key: 'guarantees',
      title: 'Garantías',
      items: guarantees,
      filterValue: guaranteeFilter,
      onFilterChange: setGuaranteeFilter,
    },
  ]

  return (
    <main className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroCopy}>
          <p className={styles.kicker}>Cotizador de daños</p>
          <h1>Abre un folio nuevo y continúa la captura sin duplicados.</h1>
          <p className={styles.description}>
            La creación usa una llave de idempotencia por solicitud y la consulta de estado permite retomar la cotización
            en cualquier momento.
          </p>
          <IdempotencyNotice
            variant="info"
            message={`Llave activa: ${idempotencyKey}. Reintentos con la misma llave devolverán la misma respuesta.`}
          />
        </div>

        <form className={styles.panel} onSubmit={handleSubmit}>
          <label className={styles.field}>
            <span>Origen</span>
            <input
              value={origin}
              onChange={(event) => setOrigin(event.target.value)}
              placeholder="spa"
              maxLength={32}
            />
          </label>

          <div className={styles.actions}>
            <button className={styles.primaryButton} type="submit" disabled={loading}>
              {loading ? 'Creando...' : 'Crear folio'}
            </button>
            <button className={styles.secondaryButton} type="button" onClick={handleNewRequest}>
              Nueva llave
            </button>
          </div>

          {error ? <IdempotencyNotice variant="warning" message={error} /> : null}
        </form>
      </section>

      <section className={styles.stack}>
        <div className={styles.lookupCard}>
          <div>
            <h2>Abrir un folio existente</h2>
            <p>Ingresa el número de folio para consultar su estado actual y continuar la captura.</p>
          </div>

          <form
            className={styles.lookupForm}
            onSubmit={(event) => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              const folioValue = String(formData.get('folio') ?? '').trim()

              if (folioValue) {
                navigate(`/quotes/${encodeURIComponent(folioValue)}/state`)
              }
            }}
          >
            <input name="folio" placeholder="1000001" />
            <button className={styles.secondaryButton} type="submit">
              Abrir estado
            </button>
          </form>
        </div>

        {folio ? <FolioCard folio={folio} onOpen={openFolio} /> : null}
      </section>

      <section className={styles.catalogSection}>
        <div className={styles.sectionHeader}>
          <div>
            <p className={styles.kicker}>Referencia core</p>
            <h2>Catálogos reutilizables para captura y cálculo</h2>
          </div>
          <div className={styles.sectionActions}>
            <button className={styles.secondaryButton} type="button" onClick={() => void reloadCatalogs()}>
              {catalogsLoading ? 'Refrescando...' : 'Refrescar catálogos'}
            </button>
          </div>
        </div>

        {catalogsError ? <IdempotencyNotice variant="warning" message={catalogsError} /> : null}

        <div className={styles.catalogGrid}>
          {catalogPanels.map((panel) => (
            <CatalogLookupPanel
              key={panel.key}
              title={panel.title}
              items={panel.items}
              loading={catalogsLoading}
              error={null}
              filterValue={panel.filterValue}
              onFilterChange={panel.onFilterChange}
            />
          ))}
        </div>
      </section>

      <section className={styles.toolboxSection}>
        <div className={styles.toolboxHeader}>
          <div>
            <p className={styles.kicker}>Validación no bloqueante</p>
            <h2>Enriquece una ubicación sin frenar el folio</h2>
          </div>
          <p className={styles.toolboxCopy}>
            La validación postal y la consulta tarifaria operan sobre la referencia core, con alertas visibles y sin
            bloquear el resto de la cotización.
          </p>
        </div>

        <div className={styles.toolboxGrid}>
          <ZipCodeValidationCard
            value={zipCode}
            validation={zipCodeValidation}
            loading={zipCodeLoading}
            error={zipCodeError}
            onValidate={handleZipCodeValidation}
            onValueChange={setZipCode}
          />

          <article className={styles.manualCard}>
            <div className={styles.manualHeader}>
              <div>
                <p className={styles.kicker}>Tarifa técnica</p>
                <h3>Consulta directa por clave compuesta</h3>
              </div>
              <span className={styles.inlineHint}>giro|zona|garantía</span>
            </div>

            <label className={styles.field}>
              <span>Tariff key</span>
              <input
                value={tariffKey}
                onChange={(event) => setTariffKey(event.target.value)}
                placeholder="GIRO-001|ZTEV-2|GAR-INC-ED"
              />
            </label>

            <div className={styles.actionsRow}>
              <button
                className={styles.primaryButton}
                type="button"
                onClick={() => void handleTariffLookup(tariffKey)}
                disabled={tariffLoading}
              >
                {tariffLoading ? 'Consultando...' : 'Consultar tarifa'}
              </button>
              <button
                className={styles.secondaryButton}
                type="button"
                onClick={() => {
                  setTariffKey('GIRO-001|ZTEV-2|GAR-INC-ED')
                  void handleTariffLookup('GIRO-001|ZTEV-2|GAR-INC-ED')
                }}
              >
                Usar ejemplo
              </button>
            </div>
          </article>

          <TariffPreviewCard tariff={tariff} loading={tariffLoading} error={tariffError} />
        </div>
      </section>
    </main>
  )
}
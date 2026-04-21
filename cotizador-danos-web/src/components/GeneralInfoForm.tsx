import { type FormEvent, useEffect, useRef, useState } from 'react'
import type { ReferenceCatalogItem } from '../services/referenceCoreService'
import type { GeneralInfoPayload, GeneralInfoRecord } from '../services/quoteGeneralInfoService'
import styles from './GeneralInfoForm.module.css'

interface GeneralInfoCatalogs {
  agents: ReferenceCatalogItem[]
  riskClassifications: ReferenceCatalogItem[]
  businessLines: ReferenceCatalogItem[]
}

interface GeneralInfoFormProps {
  value: GeneralInfoRecord
  catalogs: GeneralInfoCatalogs
  fieldErrors: Partial<Record<GeneralInfoFieldName, FieldFeedback>>
  onChange: (value: GeneralInfoRecord) => void
  onSubmit: (payload: GeneralInfoPayload) => void
  loading: boolean
}

export type GeneralInfoFieldName =
  | 'tipoDocumento'
  | 'numeroDocumento'
  | 'nombreORazonSocial'
  | 'correoElectronico'
  | 'telefono'
  | 'codigoAgente'
  | 'clasificacionRiesgo'
  | 'tipoNegocio'

interface FieldFeedback {
  message: string
  severity: 'error' | 'warning'
}

interface CatalogSelectorProps {
  label: string
  value: string
  placeholder: string
  items: ReferenceCatalogItem[]
  loading: boolean
  error?: FieldFeedback | null
  onChange: (value: string) => void
}

function CatalogSelector({ label, value, placeholder, items, loading, error, onChange }: CatalogSelectorProps) {
  const [open, setOpen] = useState(false)
  const containerRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    function handlePointerDown(event: PointerEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setOpen(false)
      }
    }

    document.addEventListener('pointerdown', handlePointerDown)

    return () => {
      document.removeEventListener('pointerdown', handlePointerDown)
    }
  }, [])

  function selectItem(item: ReferenceCatalogItem) {
    onChange(item.codigo)
    setOpen(false)
  }

  return (
    <label className={styles.field}>
      <span>{label}</span>
      <div ref={containerRef} className={styles.selectorWrap}>
        <div className={styles.selectorRow}>
          <input
            value={value}
            onChange={(event) => onChange(event.target.value)}
            onFocus={() => setOpen(true)}
            placeholder={placeholder}
            maxLength={40}
            disabled={loading}
            aria-invalid={Boolean(error)}
            aria-expanded={open}
            role="combobox"
            autoComplete="off"
          />
          <button
            type="button"
            className={styles.selectorButton}
            onClick={() => setOpen((currentValue) => !currentValue)}
            disabled={loading}
            aria-label={open ? `Cerrar opciones de ${label}` : `Abrir opciones de ${label}`}
          >
            ▾
          </button>
        </div>

        {open ? (
          <div className={styles.selectorMenu} role="listbox" aria-label={label}>
            {items.length > 0 ? (
              items.map((item) => (
                <button
                  key={item.codigo}
                  type="button"
                  className={styles.selectorOption}
                  onClick={() => selectItem(item)}
                >
                  <span className={styles.selectorCode}>{item.codigo}</span>
                  <span className={styles.selectorName}>{item.nombre}</span>
                  {item.claveIncendio ? <span className={styles.selectorTag}>{item.claveIncendio}</span> : null}
                </button>
              ))
            ) : (
              <p className={styles.selectorEmpty}>No hay resultados disponibles.</p>
            )}
          </div>
        ) : null}

        {error ? <p className={`${styles.feedback} ${error.severity === 'warning' ? styles.warning : styles.error}`}>{error.message}</p> : null}
      </div>
    </label>
  )
}

function updateAsegurado(value: GeneralInfoRecord, field: keyof GeneralInfoRecord['datosAsegurado'], nextValue: string): GeneralInfoRecord {
  return {
    ...value,
    datosAsegurado: {
      ...value.datosAsegurado,
      [field]: nextValue,
    },
  }
}

function updateConduccion(value: GeneralInfoRecord, field: keyof GeneralInfoRecord['datosConduccion'], nextValue: string): GeneralInfoRecord {
  return {
    ...value,
    datosConduccion: {
      ...value.datosConduccion,
      [field]: nextValue,
    },
  }
}

function toPayload(value: GeneralInfoRecord): GeneralInfoPayload {
  return {
    version: value.version,
    datosAsegurado: {
      tipoDocumento: value.datosAsegurado.tipoDocumento.trim(),
      numeroDocumento: value.datosAsegurado.numeroDocumento.trim(),
      nombreORazonSocial: value.datosAsegurado.nombreORazonSocial.trim(),
      correoElectronico: value.datosAsegurado.correoElectronico.trim(),
      telefono: value.datosAsegurado.telefono.trim(),
    },
    datosConduccion: {
      codigoAgente: value.datosConduccion.codigoAgente.trim(),
      clasificacionRiesgo: value.datosConduccion.clasificacionRiesgo.trim(),
      tipoNegocio: value.datosConduccion.tipoNegocio.trim(),
    },
  }
}

export function GeneralInfoForm({ value, catalogs, fieldErrors, onChange, onSubmit, loading }: GeneralInfoFormProps) {
  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    onSubmit(toPayload(value))
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <div>
            <p className={styles.kicker}>Asegurado</p>
            <h3>Datos de identificación y contacto</h3>
          </div>
          <p className={styles.helper}>La captura documenta al asegurado sin alterar las demás secciones del folio.</p>
        </div>

        <div className={styles.grid}>
          <label className={styles.field}>
            <span>Tipo de documento</span>
            <input
              aria-invalid={Boolean(fieldErrors.tipoDocumento)}
              value={value.datosAsegurado.tipoDocumento}
              onChange={(event) => onChange(updateAsegurado(value, 'tipoDocumento', event.target.value))}
              placeholder="NIT"
              maxLength={20}
              disabled={loading}
              required
            />
            {fieldErrors.tipoDocumento ? <p className={`${styles.feedback} ${styles.error}`}>{fieldErrors.tipoDocumento.message}</p> : null}
          </label>

          <label className={styles.field}>
            <span>Número de documento</span>
            <input
              aria-invalid={Boolean(fieldErrors.numeroDocumento)}
              value={value.datosAsegurado.numeroDocumento}
              onChange={(event) => onChange(updateAsegurado(value, 'numeroDocumento', event.target.value))}
              placeholder="900123456"
              maxLength={40}
              disabled={loading}
              required
            />
            {fieldErrors.numeroDocumento ? <p className={`${styles.feedback} ${styles.error}`}>{fieldErrors.numeroDocumento.message}</p> : null}
          </label>

          <label className={styles.fieldWide}>
            <span>Nombre o razón social</span>
            <input
              aria-invalid={Boolean(fieldErrors.nombreORazonSocial)}
              value={value.datosAsegurado.nombreORazonSocial}
              onChange={(event) => onChange(updateAsegurado(value, 'nombreORazonSocial', event.target.value))}
              placeholder="ACME SAS"
              maxLength={120}
              disabled={loading}
              required
            />
            {fieldErrors.nombreORazonSocial ? <p className={`${styles.feedback} ${styles.error}`}>{fieldErrors.nombreORazonSocial.message}</p> : null}
          </label>

          <label className={styles.field}>
            <span>Correo electrónico</span>
            <input
              type="email"
              aria-invalid={Boolean(fieldErrors.correoElectronico)}
              value={value.datosAsegurado.correoElectronico}
              onChange={(event) => onChange(updateAsegurado(value, 'correoElectronico', event.target.value))}
              placeholder="contacto@acme.com"
              maxLength={120}
              disabled={loading}
            />
            {fieldErrors.correoElectronico ? <p className={`${styles.feedback} ${fieldErrors.correoElectronico.severity === 'warning' ? styles.warning : styles.error}`}>{fieldErrors.correoElectronico.message}</p> : null}
          </label>

          <label className={styles.field}>
            <span>Teléfono</span>
            <input
              aria-invalid={Boolean(fieldErrors.telefono)}
              value={value.datosAsegurado.telefono}
              onChange={(event) => onChange(updateAsegurado(value, 'telefono', event.target.value))}
              placeholder="6015550101"
              maxLength={30}
              disabled={loading}
            />
            {fieldErrors.telefono ? <p className={`${styles.feedback} ${styles.error}`}>{fieldErrors.telefono.message}</p> : null}
          </label>
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <div>
            <p className={styles.kicker}>Conducción comercial</p>
            <h3>Catálogos y referencias core</h3>
          </div>
          <p className={styles.helper}>Los tres campos comerciales se validan contra la referencia core al guardar.</p>
        </div>

        <div className={styles.grid}>
          <CatalogSelector
            label="Código de agente"
            value={value.datosConduccion.codigoAgente}
            placeholder="AG-102"
            items={catalogs.agents}
            loading={loading}
            error={fieldErrors.codigoAgente ?? null}
            onChange={(nextValue) => onChange(updateConduccion(value, 'codigoAgente', nextValue))}
          />

          <CatalogSelector
            label="Clasificación de riesgo"
            value={value.datosConduccion.clasificacionRiesgo}
            placeholder="RISK-A"
            items={catalogs.riskClassifications}
            loading={loading}
            error={fieldErrors.clasificacionRiesgo ?? null}
            onChange={(nextValue) => onChange(updateConduccion(value, 'clasificacionRiesgo', nextValue))}
          />

          <CatalogSelector
            label="Tipo de negocio"
            value={value.datosConduccion.tipoNegocio}
            placeholder="GIRO-001"
            items={catalogs.businessLines}
            loading={loading}
            error={fieldErrors.tipoNegocio ?? null}
            onChange={(nextValue) => onChange(updateConduccion(value, 'tipoNegocio', nextValue))}
          />
        </div>
      </section>

      <div className={styles.footer}>
        <p className={styles.note}>Los campos obligatorios se validan antes de guardar para proteger la versión del folio.</p>
        <button className={styles.primaryButton} type="submit" disabled={loading}>
          {loading ? 'Procesando...' : 'Guardar datos generales'}
        </button>
      </div>
    </form>
  )
}

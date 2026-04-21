import type { FormEvent } from 'react'
import type { ReferenceCatalogItem, ZipCodeValidation } from '../services/referenceCoreService'
import type { LocationRecord, LocationValidationState } from '../services/quoteLocationsService'
import { ValidationAlertList } from './ValidationAlertList'
import {
  buildZipCodeRecommendationLabel,
  featuredZipCodeRecommendationOptions,
  findZipCodeRecommendation,
  zipCodeRecommendationOptions,
} from './locationReferenceCatalog'
import {
  constructionCatalogOptions,
  levelPresetOptions,
  normalizeConstructionCode,
  resolveConstructionCatalogOption,
} from './locationTechnicalCatalog'
import styles from './LocationForm.module.css'

interface LocationFormProps {
  value: LocationRecord
  onChange: (nextValue: LocationRecord) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  loading: boolean
  submitLabel?: string
  businessLines?: ReferenceCatalogItem[]
  catalogsLoading?: boolean
  catalogsError?: string | null
  zipCodeValidation?: ZipCodeValidation | null
  zipCodeLoading?: boolean
  zipCodeError?: string | null
  onZipCodeSelect?: (zipCode: string) => void | Promise<void>
}

function hasText(value: string | null | undefined): boolean {
  return Boolean(value?.trim())
}

function hasCompleteGiro(giro: LocationRecord['giro']): boolean {
  return Boolean(giro && hasText(giro.codigo) && hasText(giro.nombre) && hasText(giro.claveIncendio))
}

function parseOptionalNumber(rawValue: string): number | null {
  if (!rawValue.trim()) {
    return null
  }

  const parsedValue = Number(rawValue)

  return Number.isFinite(parsedValue) ? Math.trunc(parsedValue) : null
}

function updateField(value: LocationRecord, field: keyof LocationRecord, nextValue: string | number | null): LocationRecord {
  return {
    ...value,
    [field]: nextValue,
  }
}

function applyBusinessLine(value: LocationRecord, businessLine: ReferenceCatalogItem): LocationRecord {
  return {
    ...value,
    giro: {
      codigo: businessLine.codigo,
      nombre: businessLine.nombre,
      claveIncendio: businessLine.claveIncendio ?? null,
    },
  }
}

function updateConstructionField(value: LocationRecord, nextValue: string): LocationRecord {
  return {
    ...value,
    tipoConstructivo: normalizeConstructionCode(nextValue),
  }
}

function buildConstructionSummary(value: LocationRecord['tipoConstructivo']): string {
  const selectedConstruction = resolveConstructionCatalogOption(value)

  if (selectedConstruction) {
    return `${selectedConstruction.code} · ${selectedConstruction.label}`
  }

  return value ?? 'Sin seleccionar'
}

function getValidationTone(state: LocationValidationState): string {
  switch (state) {
    case 'CALCULABLE':
      return styles.success
    case 'VALID':
      return styles.info
    case 'INVALID':
      return styles.danger
    case 'INCOMPLETE':
      return styles.warning
    default:
      return styles.neutral
  }
}

export function LocationForm({
  value,
  onChange,
  onSubmit,
  loading,
  submitLabel = 'Guardar ubicación',
  businessLines = [],
  catalogsLoading = false,
  catalogsError = null,
  zipCodeValidation = null,
  zipCodeLoading = false,
  zipCodeError = null,
  onZipCodeSelect,
}: LocationFormProps) {
  const draftReady = hasText(value.nombreUbicacion) && hasText(value.codigoPostal)
  const technicalReady = hasText(value.tipoConstructivo) && value.nivel !== null && value.anioConstruccion !== null
  const giroReady = hasCompleteGiro(value.giro)
  const postalLooksValid = /^(?!000000)\d{6}$/.test(value.codigoPostal?.trim() ?? '')
  const calculableReady = technicalReady && giroReady && postalLooksValid
  const selectedBusinessLine = businessLines.find((item) => item.codigo === value.giro?.codigo) ?? null
  const selectedConstruction = resolveConstructionCatalogOption(value.tipoConstructivo)
  const selectedZipRecommendation = findZipCodeRecommendation(value.codigoPostal)
  const availableYears = Array.from({ length: 55 }, (_, index) => new Date().getFullYear() - index)

  return (
    <form className={styles.card} onSubmit={onSubmit}>
      <div className={styles.header}>
        <div>
          <p className={styles.kicker}>Editor de ubicación</p>
          <h2>Ubicación {value.indice}</h2>
          <p className={styles.helper}>La captura puede guardarse parcialmente y el backend resolverá el estado operativo.</p>
        </div>

        <div className={`${styles.badge} ${getValidationTone(value.estadoValidacion)}`}>
          {value.estadoValidacion}
        </div>
      </div>

      <section className={styles.guide}>
        <div className={styles.guideHeader}>
          <div>
            <p className={styles.kicker}>Qué se espera</p>
            <h3>Guía de completitud visible</h3>
          </div>
          <p className={styles.helper}>La ubicación puede guardarse incompleta, pero esta guía te muestra qué falta para avanzar.</p>
        </div>

        <div className={styles.guideGrid}>
          <article className={styles.guideItem}>
            <div className={styles.guideItemHeader}>
              <strong>Guardar draft</strong>
              <span className={`${styles.guideState} ${draftReady ? styles.guideReady : styles.guidePending}`}>
                {draftReady ? 'Listo' : 'Pendiente'}
              </span>
            </div>
            <p>Nombre de ubicación + código postal.</p>
          </article>

          <article className={styles.guideItem}>
            <div className={styles.guideItemHeader}>
              <strong>Salir de incompleta</strong>
              <span className={`${styles.guideState} ${technicalReady ? styles.guideReady : styles.guidePending}`}>
                {technicalReady ? 'Listo' : 'Pendiente'}
              </span>
            </div>
            <p>Tipo constructivo + nivel + año de construcción.</p>
          </article>

          <article className={styles.guideItem}>
            <div className={styles.guideItemHeader}>
              <strong>Quedar calculable</strong>
              <span className={`${styles.guideState} ${calculableReady ? styles.guideReady : styles.guidePending}`}>
                {calculableReady ? 'Listo' : 'Pendiente'}
              </span>
            </div>
            <p>Giro completo + código postal válido de 6 dígitos.</p>
          </article>
        </div>
      </section>

      <div className={styles.grid}>
        <label className={styles.field}>
          <span>Nombre de ubicación</span>
          <small className={styles.fieldHint}>Esperado para poder guardar el draft.</small>
          <input
            value={value.nombreUbicacion ?? ''}
            onChange={(event) => onChange(updateField(value, 'nombreUbicacion', event.currentTarget.value || null))}
            disabled={loading}
            placeholder="Planta principal"
          />
        </label>

        <label className={styles.field}>
          <span>Código postal</span>
          <small className={styles.fieldHint}>Selecciona un ZIP del fixture para que el sistema recomiende y complete la referencia territorial.</small>
          <select
            value={value.codigoPostal ?? ''}
            onChange={(event) => void onZipCodeSelect?.(event.currentTarget.value)}
            disabled={loading || zipCodeLoading}
          >
            <option value="">Selecciona un código postal</option>
            {zipCodeRecommendationOptions.map((option) => (
              <option key={option.zipCode} value={option.zipCode}>
                {buildZipCodeRecommendationLabel(option)}
              </option>
            ))}
          </select>
        </label>

        <div className={`${styles.field} ${styles.zipReference}`}>
          <span>ZIPs recomendados</span>
          <small className={styles.fieldHint}>Accesos directos basados en el fixture para que el usuario no tenga que escribir ni buscar toda la lista.</small>
          <div className={styles.quickChoices}>
            {featuredZipCodeRecommendationOptions.map((option) => {
              const isSelected = option.zipCode === value.codigoPostal

              return (
                <button
                  key={option.zipCode}
                  className={`${styles.catalogButton} ${isSelected ? styles.catalogButtonSelected : ''}`}
                  type="button"
                  onClick={() => void onZipCodeSelect?.(option.zipCode)}
                  disabled={loading || zipCodeLoading}
                >
                  <span className={styles.catalogCode}>{option.zipCode}</span>
                  <strong className={styles.catalogName}>{option.coloniaBarrio}</strong>
                  <small className={styles.catalogMeta}>{`${option.municipio} · ${option.estado}`}</small>
                </button>
              )
            })}
          </div>

          {zipCodeError ? <p className={styles.catalogFallback}>{zipCodeError}</p> : null}

          {zipCodeValidation ? (
            <div className={styles.zipCard}>
              <div className={styles.zipGrid}>
                <div>
                  <p className={styles.zipLabel}>Municipio</p>
                  <p className={styles.zipValue}>{zipCodeValidation.municipio ?? 'No disponible'}</p>
                </div>
                <div>
                  <p className={styles.zipLabel}>Estado</p>
                  <p className={styles.zipValue}>{zipCodeValidation.estado ?? 'No disponible'}</p>
                </div>
                <div>
                  <p className={styles.zipLabel}>Colonia / barrio</p>
                  <p className={styles.zipValue}>{zipCodeValidation.coloniaBarrio ?? 'No disponible'}</p>
                </div>
                <div>
                  <p className={styles.zipLabel}>Zona TEV</p>
                  <p className={styles.zipValue}>{zipCodeValidation.zona_tev ?? 'No disponible'}</p>
                </div>
                <div>
                  <p className={styles.zipLabel}>Zona FHM</p>
                  <p className={styles.zipValue}>{zipCodeValidation.zona_fhm ?? 'No disponible'}</p>
                </div>
                <div>
                  <p className={styles.zipLabel}>Resultado</p>
                  <p className={styles.zipValue}>{zipCodeValidation.valido ? 'Código reconocido' : 'Código no reconocido'}</p>
                </div>
              </div>

              <ValidationAlertList alerts={zipCodeValidation.alertas} />
            </div>
          ) : null}
        </div>

        <label className={`${styles.field} ${styles.fullWidth}`}>
          <span>Dirección</span>
          <textarea
            value={value.direccion ?? ''}
            onChange={(event) => onChange(updateField(value, 'direccion', event.currentTarget.value || null))}
            disabled={loading}
            rows={3}
            placeholder="Calle 100 # 10-10"
          />
        </label>

        <div className={`${styles.field} ${styles.fullWidth}`}>
          <span>Territorio derivado del ZIP</span>
          <small className={styles.fieldHint}>Estos datos quedan listos para guardarse desde la selección del código postal y se muestran como referencia, no como captura manual.</small>

          <div className={styles.referenceGrid}>
            <article className={styles.referenceItem}>
              <strong>Estado</strong>
              <p>{value.estado ?? selectedZipRecommendation?.estado ?? 'Pendiente'}</p>
            </article>
            <article className={styles.referenceItem}>
              <strong>Municipio</strong>
              <p>{value.municipio ?? selectedZipRecommendation?.municipio ?? 'Pendiente'}</p>
            </article>
            <article className={styles.referenceItem}>
              <strong>Ciudad</strong>
              <p>{value.ciudad ?? selectedZipRecommendation?.municipio ?? 'Pendiente'}</p>
            </article>
            <article className={styles.referenceItem}>
              <strong>Colonia / barrio</strong>
              <p>{value.colonia ?? selectedZipRecommendation?.coloniaBarrio ?? 'Pendiente'}</p>
            </article>
          </div>
        </div>

        <div className={`${styles.field} ${styles.fullWidth}`}>
          <span>Tipo constructivo sugerido por fixture</span>
          <small className={styles.fieldHint}>Las tarifas actuales del fixture usan codigos canonicos. Seleccionar uno aqui ayuda a que el backend reciba un valor mas alineado con el calculo.</small>

          <div className={styles.catalogPanel}>
            <div className={styles.catalogHeader}>
              <div>
                <strong>Catalogo tecnico de construccion</strong>
                <p className={styles.meta}>Se basa en los codigos presentes en `fireTariffs`: `MAMP`, `MIX` y `MET`.</p>
              </div>
              <span className={styles.catalogTag}>{selectedConstruction?.code ?? value.tipoConstructivo ?? 'Sin seleccionar'}</span>
            </div>

            <div className={styles.catalogList}>
              {constructionCatalogOptions.map((option) => {
                const isSelected = option.code === selectedConstruction?.code

                return (
                  <button
                    key={option.code}
                    className={`${styles.catalogButton} ${isSelected ? styles.catalogButtonSelected : ''}`}
                    type="button"
                    onClick={() => onChange(updateConstructionField(value, option.code))}
                    disabled={loading}
                  >
                    <span className={styles.catalogCode}>{option.code}</span>
                    <strong className={styles.catalogName}>{option.label}</strong>
                    <small className={styles.catalogMeta}>{option.description}</small>
                  </button>
                )
              })}
            </div>
          </div>
        </div>

        <div className={styles.field}>
          <span>Tipo constructivo seleccionado</span>
          <div className={styles.readonlyValue}>{buildConstructionSummary(value.tipoConstructivo)}</div>
        </div>

        <div className={styles.field}>
          <span>Nivel</span>
          <small className={styles.fieldHint}>Selecciona el piso real que se debe guardar. Los atajos BAS, MED y ALT del fixture siguen disponibles abajo como ayuda visual.</small>
          <select
            value={value.nivel ?? ''}
            onChange={(event) => onChange(updateField(value, 'nivel', parseOptionalNumber(event.currentTarget.value)))}
            disabled={loading}
          >
            <option value="">Selecciona un piso</option>
            {Array.from({ length: 12 }, (_, index) => index + 1).map((floor) => (
              <option key={floor} value={floor}>
                {`Piso ${floor}`}
              </option>
            ))}
          </select>

          <div className={styles.catalogPanel}>
            <div className={styles.catalogHeader}>
              <div>
                <strong>Atajos de nivel tecnico</strong>
                <p className={styles.meta}>Referencias tomadas de `fireTariffs` y `electronicEquipmentFactors`. Puedes usarlas como punto de partida y luego ajustar el piso real.</p>
              </div>
              <span className={styles.catalogTag}>{value.nivel ?? 'Sin piso'}</span>
            </div>

            <div className={styles.catalogList}>
              {levelPresetOptions.map((option) => {
                const isSelected = value.nivel === option.suggestedLevel

                return (
                  <button
                    key={option.band}
                    className={`${styles.catalogButton} ${isSelected ? styles.catalogButtonSelected : ''}`}
                    type="button"
                    onClick={() => onChange(updateField(value, 'nivel', option.suggestedLevel))}
                    disabled={loading}
                  >
                    <span className={styles.catalogCode}>{option.band}</span>
                    <strong className={styles.catalogName}>{`Piso sugerido ${option.suggestedLevel}`}</strong>
                    <small className={styles.catalogMeta}>{option.description}</small>
                  </button>
                )
              })}
            </div>
          </div>
        </div>

        <label className={styles.field}>
          <span>Año de construcción</span>
          <small className={styles.fieldHint}>Selecciona el año para mantener consistencia y evitar errores de digitación.</small>
          <select
            value={value.anioConstruccion ?? ''}
            onChange={(event) => onChange(updateField(value, 'anioConstruccion', parseOptionalNumber(event.currentTarget.value)))}
            disabled={loading}
          >
            <option value="">Selecciona un año</option>
            {availableYears.map((year) => (
              <option key={year} value={year}>
                {year}
              </option>
            ))}
          </select>
        </label>
      </div>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <div>
            <p className={styles.kicker}>Giro técnico</p>
            <h3>Datos de clasificación</h3>
            <p className={styles.meta}>Puedes escribirlo manualmente o cargarlo desde el catálogo del mock core, alimentado por las fixtures de referencia.</p>
          </div>
          <span className={styles.inlineHint}>{value.giro?.codigo ?? 'Sin giro'}</span>
        </div>

        <div className={styles.catalogPanel}>
          <div className={styles.catalogHeader}>
            <div>
              <strong>Giros disponibles</strong>
              <p className={styles.meta}>Al seleccionar uno, se completan código, nombre y clave de incendio.</p>
            </div>
            {selectedBusinessLine ? <span className={styles.catalogTag}>{selectedBusinessLine.claveIncendio ?? 'Sin clave'}</span> : null}
          </div>

          {catalogsError ? <p className={styles.catalogFallback}>{catalogsError}</p> : null}
          {catalogsLoading ? <p className={styles.catalogFallback}>Cargando giros desde referencia core...</p> : null}
          {!catalogsLoading && !catalogsError && businessLines.length > 0 ? (
            <div className={styles.catalogList}>
              {businessLines.map((businessLine) => {
                const isSelected = businessLine.codigo === value.giro?.codigo

                return (
                  <button
                    key={businessLine.codigo}
                    className={`${styles.catalogButton} ${isSelected ? styles.catalogButtonSelected : ''}`}
                    type="button"
                    onClick={() => onChange(applyBusinessLine(value, businessLine))}
                    disabled={loading}
                  >
                    <span className={styles.catalogCode}>{businessLine.codigo}</span>
                    <strong className={styles.catalogName}>{businessLine.nombre}</strong>
                    <small className={styles.catalogMeta}>{businessLine.claveIncendio ?? 'Sin clave de incendio'}</small>
                  </button>
                )
              })}
            </div>
          ) : null}
        </div>

        <div className={styles.referenceGrid}>
          <article className={styles.referenceItem}>
            <strong>Código</strong>
            <p>{value.giro?.codigo ?? 'Pendiente'}</p>
          </article>
          <article className={styles.referenceItem}>
            <strong>Nombre</strong>
            <p>{value.giro?.nombre ?? 'Pendiente'}</p>
          </article>
          <article className={styles.referenceItem}>
            <strong>Clave de incendio</strong>
            <p>{value.giro?.claveIncendio ?? 'Pendiente'}</p>
          </article>
        </div>
      </section>

      <section className={styles.preview}>
        <div>
          <p className={styles.kicker}>Proyección técnica</p>
          <p className={styles.meta}>
            Zona TEV: <strong>{value.zonaCatastrofica?.zonaTev ?? 'Pendiente'}</strong>
          </p>
          <p className={styles.meta}>
            Zona FHM: <strong>{value.zonaCatastrofica?.zonaFhm ?? 'Pendiente'}</strong>
          </p>
          <p className={styles.meta}>Estado visible de avance: draft {draftReady ? 'listo' : 'pendiente'} · estructura {technicalReady ? 'lista' : 'pendiente'} · cálculo {calculableReady ? 'listo' : 'pendiente'}.</p>
        </div>

        <p className={styles.note}>
          Los alertas, la proyección de garantías y el estado de calculabilidad se conservan como lectura del backend.
        </p>
      </section>

      <div className={styles.actions}>
        <button className={styles.primaryButton} type="submit" disabled={loading}>
          {loading ? 'Guardando...' : submitLabel}
        </button>
      </div>
    </form>
  )
}
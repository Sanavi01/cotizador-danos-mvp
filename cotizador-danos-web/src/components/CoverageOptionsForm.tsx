import { type FormEvent } from 'react'
import { GuaranteeSelectorPanel } from './GuaranteeSelectorPanel'
import type { ReferenceCatalogItem } from '../services/referenceCoreService'
import type { CoverageOptionsRecord, CoverageSelection } from '../services/quoteCoverageOptionsService'
import styles from './CoverageOptionsForm.module.css'

interface CoverageOptionsFormProps {
  value: CoverageOptionsRecord
  guarantees: ReferenceCatalogItem[]
  onChange: (value: CoverageOptionsRecord) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  loading: boolean
}

function updateCoverageSelection(
  value: CoverageOptionsRecord,
  guaranteeCode: string,
  mutator: (selection: CoverageSelection) => CoverageSelection,
): CoverageOptionsRecord {
  return {
    ...value,
    opcionesCobertura: {
      ...value.opcionesCobertura,
      garantiasSeleccionadas: value.opcionesCobertura.garantiasSeleccionadas.map((selection) =>
        selection.garantiaCode === guaranteeCode ? mutator(selection) : selection,
      ),
    },
  }
}

function updateSelectedGuarantees(value: CoverageOptionsRecord, guaranteeCode: string): CoverageOptionsRecord {
  const selectedIndex = value.opcionesCobertura.garantiasSeleccionadas.findIndex(
    (selection) => selection.garantiaCode === guaranteeCode,
  )

  if (selectedIndex >= 0) {
    return {
      ...value,
      opcionesCobertura: {
        ...value.opcionesCobertura,
        garantiasSeleccionadas: value.opcionesCobertura.garantiasSeleccionadas.filter(
          (selection) => selection.garantiaCode !== guaranteeCode,
        ),
      },
    }
  }

  return {
    ...value,
    opcionesCobertura: {
      ...value.opcionesCobertura,
      garantiasSeleccionadas: [
        ...value.opcionesCobertura.garantiasSeleccionadas,
        { garantiaCode: guaranteeCode, terminos: [] },
      ],
    },
  }
}

function toTermsText(terminos: string[]): string {
  return terminos.join(', ')
}

function parseTermsText(rawValue: string): string[] {
  return rawValue
    .split(',')
    .map((termino) => termino.trim())
    .filter((termino) => termino.length > 0)
}

export function CoverageOptionsForm({ value, guarantees, onChange, onSubmit, loading }: CoverageOptionsFormProps) {
  const guaranteeMap = new Map(guarantees.map((guarantee) => [guarantee.codigo, guarantee]))
  const selected = value.opcionesCobertura.garantiasSeleccionadas

  function handleToggleGuarantee(guaranteeCode: string) {
    onChange(updateSelectedGuarantees(value, guaranteeCode))
  }

  function handleRemoveGuarantee(guaranteeCode: string) {
    onChange({
      ...value,
      opcionesCobertura: {
        ...value.opcionesCobertura,
        garantiasSeleccionadas: value.opcionesCobertura.garantiasSeleccionadas.filter(
          (selection) => selection.garantiaCode !== guaranteeCode,
        ),
      },
    })
  }

  function handleTermsChange(guaranteeCode: string, rawValue: string) {
    onChange(
      updateCoverageSelection(value, guaranteeCode, (selection) => ({
        ...selection,
        terminos: parseTermsText(rawValue),
      })),
    )
  }

  function handleObservationsChange(rawValue: string) {
    onChange({
      ...value,
      opcionesCobertura: {
        ...value.opcionesCobertura,
        observaciones: rawValue.trim().length > 0 ? rawValue : null,
      },
    })
  }

  return (
    <form className={styles.form} onSubmit={onSubmit} noValidate>
      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <div>
            <p className={styles.kicker}>Edición global</p>
            <h3>Garantías y términos de participación</h3>
          </div>
          <p className={styles.helper}>
            Esta edición no modifica datos generales, layout ni ubicaciones. La proyección por ubicación es de solo
            lectura.
          </p>
        </div>

        <GuaranteeSelectorPanel
          items={guarantees}
          selected={selected}
          onToggle={handleToggleGuarantee}
          disabled={loading}
        />

        <div className={styles.selectionGrid}>
          {selected.length > 0 ? (
            selected.map((selection) => {
              const guarantee = guaranteeMap.get(selection.garantiaCode)

              return (
                <article key={selection.garantiaCode} className={styles.selectionCard}>
                  <div className={styles.selectionHeader}>
                    <div>
                      <p className={styles.selectionCode}>{selection.garantiaCode}</p>
                      <h4>{guarantee?.nombre ?? 'Garantía sin referencia core'}</h4>
                    </div>
                    <button
                      className={styles.removeButton}
                      type="button"
                      onClick={() => handleRemoveGuarantee(selection.garantiaCode)}
                      disabled={loading}
                    >
                      Quitar
                    </button>
                  </div>

                  <label className={styles.field}>
                    <span>Términos asociados</span>
                    <input
                      value={toTermsText(selection.terminos)}
                      onChange={(event) => handleTermsChange(selection.garantiaCode, event.target.value)}
                      placeholder="incendio, robo, daños"
                      maxLength={220}
                      disabled={loading}
                    />
                  </label>
                </article>
              )
            })
          ) : (
            <p className={styles.emptyState}>
              Selecciona una garantía del catálogo para empezar a definir los términos que participarán en el siguiente
              cálculo.
            </p>
          )}
        </div>

        <label className={styles.fieldWide}>
          <span>Observaciones</span>
          <textarea
            value={value.opcionesCobertura.observaciones ?? ''}
            onChange={(event) => handleObservationsChange(event.target.value)}
            placeholder="Cobertura base para el análisis inicial"
            maxLength={300}
            rows={4}
            disabled={loading}
          />
        </label>

        <div className={styles.footer}>
          <p className={styles.note}>
            Los cambios guardan solo opcionesCobertura, incrementan la versión y actualizan la fecha del agregado.
          </p>

          <button className={styles.primaryButton} type="submit" disabled={loading}>
            {loading ? 'Guardando...' : 'Guardar opciones'}
          </button>
        </div>
      </section>
    </form>
  )
}
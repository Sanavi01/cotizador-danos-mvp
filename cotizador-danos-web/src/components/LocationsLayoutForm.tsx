import type { FormEvent } from 'react'
import type {
  LocationsLayoutMode,
  LocationsLayoutRecord,
  LocationsLayoutSlot,
} from '../services/quoteLocationsLayoutService'
import styles from './LocationsLayoutForm.module.css'

interface LocationsLayoutFormProps {
  value: LocationsLayoutRecord
  onChange: (nextValue: LocationsLayoutRecord) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  loading: boolean
}

function buildSlots(quantity: number, existingSlots: LocationsLayoutSlot[]): LocationsLayoutSlot[] {
  return Array.from({ length: quantity }, (_, index) => {
    const existingSlot = existingSlots[index]

    return {
      indice: index + 1,
      ordenCaptura: existingSlot?.ordenCaptura ?? index + 1,
    }
  })
}

function sortSlotsByCaptureOrder(slots: LocationsLayoutSlot[]): LocationsLayoutSlot[] {
  return [...slots].sort((leftSlot, rightSlot) => {
    if (leftSlot.ordenCaptura !== rightSlot.ordenCaptura) {
      return leftSlot.ordenCaptura - rightSlot.ordenCaptura
    }

    return leftSlot.indice - rightSlot.indice
  })
}

function normalizeQuantity(rawValue: string): number | null {
  if (!rawValue.trim()) {
    return null
  }

  const parsedValue = Number(rawValue)

  if (!Number.isFinite(parsedValue)) {
    return null
  }

  return Math.max(1, Math.trunc(parsedValue))
}

function updateMode(value: LocationsLayoutRecord, nextMode: LocationsLayoutMode | null): LocationsLayoutRecord {
  if (!nextMode) {
    return {
      ...value,
      configuracionLayout: {
        modoCaptura: null,
        cantidadUbicaciones: null,
        ubicaciones: [],
      },
    }
  }

  const nextQuantity = nextMode === 'UNICA' ? 1 : Math.max(value.configuracionLayout.cantidadUbicaciones ?? 2, 2)

  return {
    ...value,
    configuracionLayout: {
      modoCaptura: nextMode,
      cantidadUbicaciones: nextQuantity,
      ubicaciones: buildSlots(nextQuantity, value.configuracionLayout.ubicaciones),
    },
  }
}

function updateQuantity(value: LocationsLayoutRecord, rawQuantity: string): LocationsLayoutRecord {
  const nextQuantity = normalizeQuantity(rawQuantity)

  if (!nextQuantity) {
    return {
      ...value,
      configuracionLayout: {
        ...value.configuracionLayout,
        cantidadUbicaciones: null,
        ubicaciones: [],
      },
    }
  }

  const nextMode: LocationsLayoutMode = nextQuantity === 1 ? 'UNICA' : 'MULTIPLE'

  return {
    ...value,
    configuracionLayout: {
      modoCaptura: nextMode,
      cantidadUbicaciones: nextQuantity,
      ubicaciones: buildSlots(nextQuantity, value.configuracionLayout.ubicaciones),
    },
  }
}

function updateSlotOrder(
  value: LocationsLayoutRecord,
  slotIndex: number,
  rawOrder: string,
): LocationsLayoutRecord {
  const nextOrder = normalizeQuantity(rawOrder) ?? 1

  return {
    ...value,
    configuracionLayout: {
      ...value.configuracionLayout,
      ubicaciones: value.configuracionLayout.ubicaciones.map((slot) =>
        slot.indice === slotIndex ? { ...slot, ordenCaptura: nextOrder } : slot,
      ),
    },
  }
}

export function LocationsLayoutForm({ value, onChange, onSubmit, loading }: LocationsLayoutFormProps) {
  const modeValue = value.configuracionLayout.modoCaptura ?? ''
  const quantityValue = value.configuracionLayout.cantidadUbicaciones ?? ''
  const orderedSlots = sortSlotsByCaptureOrder(value.configuracionLayout.ubicaciones)

  return (
    <form className={styles.card} onSubmit={onSubmit}>
      <div className={styles.header}>
        <div>
          <p className={styles.kicker}>Configuración</p>
          <h2 className={styles.title}>Define la estructura de captura</h2>
          <p className={styles.helper}>
            El modo y la cantidad determinan los slots previstos. El orden de captura se mantiene aislado de las otras
            secciones del folio.
          </p>
        </div>

        <button className={styles.primaryButton} type="submit" disabled={loading}>
          {loading ? 'Guardando...' : 'Guardar layout'}
        </button>
      </div>

      <div className={styles.fieldsGrid}>
        <label className={styles.field}>
          <span>Modo de captura</span>
          <select
            value={modeValue}
            onChange={(event) => onChange(updateMode(value, event.currentTarget.value as LocationsLayoutMode | null))}
            disabled={loading}
          >
            <option value="">Selecciona un modo</option>
            <option value="UNICA">UNICA</option>
            <option value="MULTIPLE">MULTIPLE</option>
          </select>
        </label>

        <label className={styles.field}>
          <span>Cantidad de ubicaciones</span>
          <input
            type="number"
            min={1}
            value={quantityValue}
            onChange={(event) => onChange(updateQuantity(value, event.currentTarget.value))}
            disabled={loading}
          />
        </label>
      </div>

      <section className={styles.slotsSection}>
        <div className={styles.sectionHeading}>
          <div>
            <p className={styles.kicker}>Slots</p>
            <h3 className={styles.sectionTitle}>Orden de captura previsto</h3>
          </div>
          <span className={styles.counter}>{value.configuracionLayout.ubicaciones.length} posiciones</span>
        </div>

        {orderedSlots.length > 0 ? (
          <div className={styles.slotList}>
            {orderedSlots.map((slot) => (
              <article key={slot.indice} className={styles.slotItem}>
                <div>
                  <p className={styles.slotLabel}>Ubicación {slot.indice}</p>
                  <p className={styles.slotMeta}>Índice fijo dentro del layout</p>
                </div>

                <label className={styles.slotField}>
                  <span>Orden de captura</span>
                  <input
                    type="number"
                    min={1}
                    max={value.configuracionLayout.cantidadUbicaciones ?? undefined}
                    value={slot.ordenCaptura}
                    onChange={(event) => onChange(updateSlotOrder(value, slot.indice, event.currentTarget.value))}
                    disabled={loading}
                  />
                </label>
              </article>
            ))}
          </div>
        ) : (
          <p className={styles.emptyState}>Selecciona un modo y una cantidad para generar la estructura inicial.</p>
        )}
      </section>

      <p className={styles.note}>
        Guardar este layout incrementa la versión del folio y no modifica los datos generales, coberturas ni resultados
        financieros.
      </p>
    </form>
  )
}
import { useMemo } from 'react'
import type {
  LocationsLayoutConfiguration,
  LocationsLayoutMode,
  LocationsLayoutSlot,
} from '../services/quoteLocationsLayoutService'

interface LocationsLayoutPreviewState {
  slots: LocationsLayoutSlot[]
  mode: LocationsLayoutMode | null
  canAddSlot: boolean
}

function sortSlotsByCaptureOrder(slots: LocationsLayoutSlot[]): LocationsLayoutSlot[] {
  return [...slots].sort((leftSlot, rightSlot) => {
    if (leftSlot.ordenCaptura !== rightSlot.ordenCaptura) {
      return leftSlot.ordenCaptura - rightSlot.ordenCaptura
    }

    return leftSlot.indice - rightSlot.indice
  })
}

function buildPreviewSlots(configuration: LocationsLayoutConfiguration): LocationsLayoutSlot[] {
  if (!configuration.modoCaptura || !configuration.cantidadUbicaciones || configuration.cantidadUbicaciones < 1) {
    return []
  }

  if (configuration.ubicaciones.length > 0) {
    return sortSlotsByCaptureOrder(configuration.ubicaciones.map((slot) => ({
      indice: slot.indice,
      ordenCaptura: slot.ordenCaptura,
    })))
  }

  return Array.from({ length: configuration.cantidadUbicaciones }, (_, index) => ({
    indice: index + 1,
    ordenCaptura: index + 1,
  }))
}

export function useLocationsLayoutPreview(
  configuration: LocationsLayoutConfiguration | null | undefined,
): LocationsLayoutPreviewState {
  return useMemo(() => {
    if (!configuration) {
      return {
        slots: [],
        mode: null,
        canAddSlot: false,
      }
    }

    const slots = buildPreviewSlots(configuration)
    const quantity = configuration.cantidadUbicaciones ?? 0

    return {
      slots,
      mode: configuration.modoCaptura,
      canAddSlot: Boolean(configuration.modoCaptura && quantity > 0 && slots.length < quantity),
    }
  }, [configuration])
}
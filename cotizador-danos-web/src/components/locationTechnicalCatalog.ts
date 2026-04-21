export interface ConstructionCatalogOption {
  code: string
  label: string
  description: string
  aliases: string[]
}

export interface LevelPresetOption {
  band: 'BAS' | 'MED' | 'ALT'
  label: string
  suggestedLevel: number
  description: string
}

export const constructionCatalogOptions: ConstructionCatalogOption[] = [
  {
    code: 'MAMP',
    label: 'Mamposteria / concreto tradicional',
    description: 'Codigo observado en las tarifas de incendio del fixture. Conviene cuando el riesgo es de mamposteria, concreto o construccion tradicional.',
    aliases: ['MAMP', 'MAMPOSTERIA', 'CONCRETO'],
  },
  {
    code: 'MIX',
    label: 'Construccion mixta',
    description: 'Codigo observado en las tarifas del fixture para riesgos con combinacion de materiales estructurales.',
    aliases: ['MIX', 'MIXTA'],
  },
  {
    code: 'MET',
    label: 'Estructura metalica',
    description: 'Codigo observado en las tarifas del fixture para riesgos predominantemente metalicos.',
    aliases: ['MET', 'METALICA', 'METALICA LIVIANA'],
  },
]

export const levelPresetOptions: LevelPresetOption[] = [
  {
    band: 'BAS',
    label: 'BAS',
    suggestedLevel: 1,
    description: 'Referencia tecnica presente en el fixture. Usa este atajo si el riesgo esta en planta baja o un piso bajo.',
  },
  {
    band: 'MED',
    label: 'MED',
    suggestedLevel: 3,
    description: 'Referencia tecnica presente en el fixture. Usa este atajo como valor medio y ajusta el piso real si aplica.',
  },
  {
    band: 'ALT',
    label: 'ALT',
    suggestedLevel: 6,
    description: 'Referencia tecnica presente en el fixture. Usa este atajo para riesgos en pisos altos y corrige luego el piso exacto si es distinto.',
  },
]

export function resolveConstructionCatalogOption(value: string | null | undefined): ConstructionCatalogOption | null {
  const normalizedValue = value?.trim().toUpperCase()

  if (!normalizedValue) {
    return null
  }

  return (
    constructionCatalogOptions.find((option) => option.code === normalizedValue || option.aliases.includes(normalizedValue)) ?? null
  )
}

export function normalizeConstructionCode(value: string): string | null {
  const normalizedValue = value.trim().toUpperCase()

  return normalizedValue ? normalizedValue : null
}
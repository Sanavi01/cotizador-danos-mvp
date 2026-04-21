export interface ZipCodeRecommendationOption {
  zipCode: string
  municipio: string
  estado: string
  coloniaBarrio: string
  zonaTev: string
  zonaFhm: string
  featured?: boolean
}

export const zipCodeRecommendationOptions: ZipCodeRecommendationOption[] = [
  { zipCode: '110111', municipio: 'Bogota D.C.', estado: 'Cundinamarca', coloniaBarrio: 'Chapinero', zonaTev: 'ZTEV-1', zonaFhm: 'ZFHM-1', featured: true },
  { zipCode: '110115', municipio: 'Bogota D.C.', estado: 'Cundinamarca', coloniaBarrio: 'Chico Norte', zonaTev: 'ZTEV-2', zonaFhm: 'ZFHM-2' },
  { zipCode: '110120', municipio: 'Bogota D.C.', estado: 'Cundinamarca', coloniaBarrio: 'Suba', zonaTev: 'ZTEV-3', zonaFhm: 'ZFHM-3' },
  { zipCode: '050001', municipio: 'Medellin', estado: 'Antioquia', coloniaBarrio: 'La Candelaria', zonaTev: 'ZTEV-2', zonaFhm: 'ZFHM-1' },
  { zipCode: '050002', municipio: 'Medellin', estado: 'Antioquia', coloniaBarrio: 'El Poblado', zonaTev: 'ZTEV-2', zonaFhm: 'ZFHM-1', featured: true },
  { zipCode: '050007', municipio: 'Medellin', estado: 'Antioquia', coloniaBarrio: 'Laureles', zonaTev: 'ZTEV-2', zonaFhm: 'ZFHM-1' },
  { zipCode: '760001', municipio: 'Cali', estado: 'Valle del Cauca', coloniaBarrio: 'Centenario', zonaTev: 'ZTEV-2', zonaFhm: 'ZFHM-1' },
  { zipCode: '760004', municipio: 'Cali', estado: 'Valle del Cauca', coloniaBarrio: 'Ciudad Jardin', zonaTev: 'ZTEV-3', zonaFhm: 'ZFHM-2' },
  { zipCode: '760005', municipio: 'Cali', estado: 'Valle del Cauca', coloniaBarrio: 'Aguacatal', zonaTev: 'ZTEV-4', zonaFhm: 'ZFHM-2', featured: true },
  { zipCode: '760006', municipio: 'Cali', estado: 'Valle del Cauca', coloniaBarrio: 'Pance', zonaTev: 'ZTEV-4', zonaFhm: 'ZFHM-3' },
  { zipCode: '080001', municipio: 'Barranquilla', estado: 'Atlantico', coloniaBarrio: 'El Prado', zonaTev: 'ZTEV-2', zonaFhm: 'ZFHM-1', featured: true },
  { zipCode: '080004', municipio: 'Barranquilla', estado: 'Atlantico', coloniaBarrio: 'Riomar', zonaTev: 'ZTEV-3', zonaFhm: 'ZFHM-2' },
  { zipCode: '080010', municipio: 'Barranquilla', estado: 'Atlantico', coloniaBarrio: 'Puerto Colombia', zonaTev: 'ZTEV-4', zonaFhm: 'ZFHM-3' },
  { zipCode: '680001', municipio: 'Bucaramanga', estado: 'Santander', coloniaBarrio: 'Cabecera', zonaTev: 'ZTEV-2', zonaFhm: 'ZFHM-1', featured: true },
  { zipCode: '680004', municipio: 'Bucaramanga', estado: 'Santander', coloniaBarrio: 'Floridablanca', zonaTev: 'ZTEV-3', zonaFhm: 'ZFHM-2' },
  { zipCode: '680006', municipio: 'Bucaramanga', estado: 'Santander', coloniaBarrio: 'Piedecuesta', zonaTev: 'ZTEV-4', zonaFhm: 'ZFHM-3' },
  { zipCode: '130001', municipio: 'Cartagena', estado: 'Bolivar', coloniaBarrio: 'Bocagrande', zonaTev: 'ZTEV-2', zonaFhm: 'ZFHM-1', featured: true },
  { zipCode: '130003', municipio: 'Cartagena', estado: 'Bolivar', coloniaBarrio: 'Getsemani', zonaTev: 'ZTEV-3', zonaFhm: 'ZFHM-2' },
  { zipCode: '130010', municipio: 'Cartagena', estado: 'Bolivar', coloniaBarrio: 'Turbaco', zonaTev: 'ZTEV-4', zonaFhm: 'ZFHM-3' },
]

export const featuredZipCodeRecommendationOptions = zipCodeRecommendationOptions.filter((option) => option.featured)

export function findZipCodeRecommendation(zipCode: string | null | undefined): ZipCodeRecommendationOption | null {
  const normalizedZipCode = zipCode?.trim()

  if (!normalizedZipCode) {
    return null
  }

  return zipCodeRecommendationOptions.find((option) => option.zipCode === normalizedZipCode) ?? null
}

export function buildZipCodeRecommendationLabel(option: ZipCodeRecommendationOption): string {
  return `${option.zipCode} · ${option.municipio} · ${option.coloniaBarrio}`
}
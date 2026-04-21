import { createApiClient, executeWithRetry, type ApiEnvelope } from './httpClient'

interface ApiGeneralInfoAsegurado {
  tipoDocumento: string | null
  numeroDocumento: string | null
  nombreORazonSocial: string | null
  correoElectronico: string | null
  telefono: string | null
}

interface ApiGeneralInfoConduccion {
  codigoAgente: string | null
  clasificacionRiesgo: string | null
  tipoNegocio: string | null
}

interface ApiGeneralInfoResponse {
  numeroFolio: string
  version: number
  fechaUltimaActualizacion: string
  datosAsegurado: ApiGeneralInfoAsegurado
  datosConduccion: ApiGeneralInfoConduccion
}

export interface GeneralInfoAsegurado {
  tipoDocumento: string
  numeroDocumento: string
  nombreORazonSocial: string
  correoElectronico: string
  telefono: string
}

export interface GeneralInfoConduccion {
  codigoAgente: string
  clasificacionRiesgo: string
  tipoNegocio: string
}

export interface GeneralInfoRecord {
  numeroFolio: string
  version: number
  fechaUltimaActualizacion: string
  datosAsegurado: GeneralInfoAsegurado
  datosConduccion: GeneralInfoConduccion
}

export interface GeneralInfoPayload {
  version: number
  datosAsegurado: GeneralInfoAsegurado
  datosConduccion: GeneralInfoConduccion
}

function normalizeText(value: string | null | undefined): string {
  return value ?? ''
}

function normalizeGeneralInfo(response: ApiGeneralInfoResponse): GeneralInfoRecord {
  return {
    numeroFolio: response.numeroFolio,
    version: response.version,
    fechaUltimaActualizacion: response.fechaUltimaActualizacion,
    datosAsegurado: {
      tipoDocumento: normalizeText(response.datosAsegurado.tipoDocumento),
      numeroDocumento: normalizeText(response.datosAsegurado.numeroDocumento),
      nombreORazonSocial: normalizeText(response.datosAsegurado.nombreORazonSocial),
      correoElectronico: normalizeText(response.datosAsegurado.correoElectronico),
      telefono: normalizeText(response.datosAsegurado.telefono),
    },
    datosConduccion: {
      codigoAgente: normalizeText(response.datosConduccion.codigoAgente),
      clasificacionRiesgo: normalizeText(response.datosConduccion.clasificacionRiesgo),
      tipoNegocio: normalizeText(response.datosConduccion.tipoNegocio),
    },
  }
}

export async function getGeneralInfo(folio: string): Promise<GeneralInfoRecord> {
  const client = createApiClient('VITE_API_URL', 'Define VITE_API_URL para conectar con el backend principal.')

  const response = await executeWithRetry(() =>
    client.get<ApiEnvelope<ApiGeneralInfoResponse>>(`/v1/quotes/${encodeURIComponent(folio)}/general-info`),
  )

  return normalizeGeneralInfo(response.data.data)
}

export async function updateGeneralInfo(folio: string, payload: GeneralInfoPayload): Promise<GeneralInfoRecord> {
  const client = createApiClient('VITE_API_URL', 'Define VITE_API_URL para conectar con el backend principal.')

  const response = await executeWithRetry(() =>
    client.put<ApiEnvelope<ApiGeneralInfoResponse>>(`/v1/quotes/${encodeURIComponent(folio)}/general-info`, payload),
  )

  return normalizeGeneralInfo(response.data.data)
}
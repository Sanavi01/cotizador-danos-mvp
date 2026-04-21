package com.sofka.plataforma_danos_back.folios.domain;

import java.time.Instant;

public record DatosGeneralesCotizacion(
        Long cotizacionId,
        DatosAsegurado datosAsegurado,
        DatosConduccion datosConduccion,
        Instant createdAt,
        Instant updatedAt
) {
    public static DatosGeneralesCotizacion nueva(
            Long cotizacionId,
            DatosAsegurado datosAsegurado,
            DatosConduccion datosConduccion,
            Instant now
    ) {
        return new DatosGeneralesCotizacion(cotizacionId, datosAsegurado, datosConduccion, now, now);
    }

    public record DatosAsegurado(
            String tipoDocumento,
            String numeroDocumento,
            String nombreORazonSocial,
            String correoElectronico,
            String telefono
    ) {
    }

    public record DatosConduccion(
            String codigoAgente,
            String clasificacionRiesgo,
            String tipoNegocio
    ) {
    }
}
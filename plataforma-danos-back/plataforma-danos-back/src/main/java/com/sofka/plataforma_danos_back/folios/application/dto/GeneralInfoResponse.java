package com.sofka.plataforma_danos_back.folios.application.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.DatosGeneralesCotizacion;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record GeneralInfoResponse(
        @Schema(description = "Numero de folio consultado", example = "1000001")
        String numeroFolio,
        @Schema(description = "Version optimista del agregado raiz", example = "3")
        Long version,
        @Schema(description = "Marca de tiempo de la ultima actualizacion logica", example = "2026-04-21T00:00:00Z")
        Instant fechaUltimaActualizacion,
        @Schema(description = "Datos del asegurado")
        DatosAsegurado datosAsegurado,
        @Schema(description = "Datos comerciales de conduccion")
        DatosConduccion datosConduccion
) {
    public static GeneralInfoResponse from(Cotizacion cotizacion, DatosGeneralesCotizacion datosGeneralesCotizacion) {
        return new GeneralInfoResponse(
                cotizacion.numeroFolio(),
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion(),
                new DatosAsegurado(
                        datosGeneralesCotizacion.datosAsegurado().tipoDocumento(),
                        datosGeneralesCotizacion.datosAsegurado().numeroDocumento(),
                        datosGeneralesCotizacion.datosAsegurado().nombreORazonSocial(),
                        datosGeneralesCotizacion.datosAsegurado().correoElectronico(),
                        datosGeneralesCotizacion.datosAsegurado().telefono()
                ),
                new DatosConduccion(
                        datosGeneralesCotizacion.datosConduccion().codigoAgente(),
                        datosGeneralesCotizacion.datosConduccion().clasificacionRiesgo(),
                        datosGeneralesCotizacion.datosConduccion().tipoNegocio()
                )
        );
    }

    public static GeneralInfoResponse empty(Cotizacion cotizacion) {
        return new GeneralInfoResponse(
                cotizacion.numeroFolio(),
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion(),
                new DatosAsegurado(null, null, null, null, null),
                new DatosConduccion(null, null, null)
        );
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record DatosAsegurado(
            String tipoDocumento,
            String numeroDocumento,
            String nombreORazonSocial,
            String correoElectronico,
            String telefono
    ) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record DatosConduccion(
            String codigoAgente,
            String clasificacionRiesgo,
            String tipoNegocio
    ) {
    }
}
package com.sofka.plataforma_danos_back.folios.application.dto;

import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record QuoteStateResponse(
    @Schema(description = "Numero de folio consultado", example = "1000001")
        String numeroFolio,
    @Schema(description = "Estado actual de la cotizacion", example = "BORRADOR")
        EstadoCotizacion estadoCotizacion,
    @Schema(description = "Indica si el folio tiene alertas bloqueantes", example = "false")
        boolean tieneAlertas,
    @Schema(description = "Secciones ya completadas en la captura")
        List<String> seccionesCompletadas,
    @Schema(description = "Cantidad de ubicaciones calculables", example = "0")
        int ubicacionesCalculables,
    @Schema(description = "Cantidad de ubicaciones incompletas", example = "0")
        int ubicacionesIncompletas,
    @Schema(description = "Version optimista de la entidad persistida", example = "0")
        Long version,
    @Schema(description = "Marca de tiempo de la ultima actualizacion logica", example = "2026-04-20T00:00:00Z")
        Instant fechaUltimaActualizacion
) {
    public static QuoteStateResponse from(Cotizacion cotizacion) {
        return from(cotizacion, false);
    }

    public static QuoteStateResponse from(Cotizacion cotizacion, boolean generalInfoCompleted) {
        return new QuoteStateResponse(
                cotizacion.numeroFolio(),
                cotizacion.estadoCotizacion(),
                false,
                generalInfoCompleted ? List.of("datos-generales") : List.of(),
                0,
                0,
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion()
        );
    }
}

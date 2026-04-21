package com.sofka.plataforma_danos_back.folios.application.dto;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record LocationsResponse(
        @Schema(description = "Numero de folio consultado", example = "1000001")
        String numeroFolio,
        @Schema(description = "Version optimista del agregado raiz", example = "3")
        Long version,
        @Schema(description = "Marca de tiempo de la ultima actualizacion logica", example = "2026-04-21T00:00:00Z")
        Instant fechaUltimaActualizacion,
        @Schema(description = "Ubicaciones persistidas del folio")
        List<LocationResponse.LocationView> ubicaciones
) {
    public static LocationsResponse from(Cotizacion cotizacion, List<UbicacionCotizacion> ubicaciones) {
        return new LocationsResponse(
                cotizacion.numeroFolio(),
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion(),
                ubicaciones.stream()
                        .sorted(Comparator.comparingInt(ubicacion -> ubicacion.detalle().indice()))
                        .map(LocationResponse.LocationView::from)
                        .toList()
        );
    }
}
package com.sofka.plataforma_danos_back.folios.application.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record LocationsLayoutResponse(
        @Schema(description = "Numero de folio consultado", example = "1000001")
        String numeroFolio,
        @Schema(description = "Version optimista del agregado raiz", example = "3")
        Long version,
        @Schema(description = "Marca de tiempo de la ultima actualizacion logica", example = "2026-04-21T00:00:00Z")
        Instant fechaUltimaActualizacion,
        @Schema(description = "Configuracion persistida del layout")
        ConfiguracionLayoutView configuracionLayout
) {
    public static LocationsLayoutResponse from(Cotizacion cotizacion, ConfiguracionLayout configuracionLayout) {
        return new LocationsLayoutResponse(
                cotizacion.numeroFolio(),
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion(),
                new ConfiguracionLayoutView(
                        configuracionLayout.modoCaptura(),
                        configuracionLayout.cantidadUbicaciones(),
                        configuracionLayout.ubicaciones().stream()
                                .map(LayoutUbicacionSlotView::from)
                                .toList()
                )
        );
    }

    public static LocationsLayoutResponse empty(Cotizacion cotizacion) {
        return new LocationsLayoutResponse(
                cotizacion.numeroFolio(),
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion(),
                new ConfiguracionLayoutView(null, null, List.of())
        );
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ConfiguracionLayoutView(
            ModoCaptura modoCaptura,
            Integer cantidadUbicaciones,
            List<LayoutUbicacionSlotView> ubicaciones
    ) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record LayoutUbicacionSlotView(
            Integer indice,
            Integer ordenCaptura
    ) {
        public static LayoutUbicacionSlotView from(LayoutUbicacionSlot slot) {
            return new LayoutUbicacionSlotView(slot.indice(), slot.ordenCaptura());
        }
    }
}
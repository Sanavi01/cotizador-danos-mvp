package com.sofka.plataforma_danos_back.folios.application.dto;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record LocationsSummaryResponse(
        @Schema(description = "Numero de folio consultado", example = "1000001")
        String numeroFolio,
        @Schema(description = "Cantidad de slots esperados segun el layout", example = "3")
        int totalEsperado,
        @Schema(description = "Cantidad de ubicaciones persistidas", example = "2")
        int totalActual,
        @Schema(description = "Cantidad de ubicaciones calculables", example = "1")
        int calculables,
        @Schema(description = "Cantidad de ubicaciones incompletas", example = "0")
        int incompletas,
        @Schema(description = "Cantidad de ubicaciones invalidas", example = "1")
        int invalidas,
        @Schema(description = "Cantidad de ubicaciones con alertas bloqueantes", example = "1")
        int conAlertas,
        @Schema(description = "Resumen operativo por indice")
        List<ResumenUbicacionView> resumenPorIndice
) {
    public static LocationsSummaryResponse from(String numeroFolio, List<LayoutUbicacionSlot> slots, List<UbicacionCotizacion> ubicaciones) {
        Map<Integer, UbicacionCotizacion> ubicacionesPorIndice = ubicaciones.stream()
                .collect(Collectors.toMap(ubicacion -> ubicacion.detalle().indice(), Function.identity(), (left, right) -> left));
        List<ResumenUbicacionView> resumen = slots.stream()
                .sorted(Comparator.comparingInt(LayoutUbicacionSlot::ordenCaptura))
                .map(slot -> ResumenUbicacionView.from(slot, ubicacionesPorIndice.get(slot.indice())))
                .toList();

        int calculables = (int) ubicaciones.stream().filter(ubicacion -> ubicacion.estadoValidacion() == EstadoValidacion.CALCULABLE).count();
        int incompletas = (int) ubicaciones.stream().filter(ubicacion -> ubicacion.estadoValidacion() == EstadoValidacion.INCOMPLETE).count();
        int invalidas = (int) ubicaciones.stream().filter(ubicacion -> ubicacion.estadoValidacion() == EstadoValidacion.INVALID).count();
        int conAlertas = (int) ubicaciones.stream().filter(ubicacion -> !ubicacion.alertasBloqueantes().isEmpty()).count();

        return new LocationsSummaryResponse(
                numeroFolio,
                slots.size(),
                ubicaciones.size(),
                calculables,
                incompletas,
                invalidas,
                conAlertas,
                resumen
        );
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ResumenUbicacionView(
            Integer indice,
            boolean slotEsperado,
            EstadoValidacion estadoValidacion,
            boolean tieneAlertasBloqueantes
    ) {
        public static ResumenUbicacionView from(LayoutUbicacionSlot slot, UbicacionCotizacion ubicacion) {
            if (ubicacion == null) {
                return new ResumenUbicacionView(slot.indice(), true, EstadoValidacion.EMPTY, false);
            }
            return new ResumenUbicacionView(
                    slot.indice(),
                    true,
                    ubicacion.estadoValidacion(),
                    !ubicacion.alertasBloqueantes().isEmpty()
            );
        }
    }
}
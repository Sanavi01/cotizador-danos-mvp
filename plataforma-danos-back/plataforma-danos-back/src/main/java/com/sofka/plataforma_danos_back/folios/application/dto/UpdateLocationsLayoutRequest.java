package com.sofka.plataforma_danos_back.folios.application.dto;

import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateLocationsLayoutRequest(
        @Schema(description = "Version actual del agregado para control optimista", example = "1")
        @NotNull(message = "La version es obligatoria")
        Long version,
        @Schema(description = "Configuracion del layout de ubicaciones")
        @Valid
        @NotNull(message = "La configuracion del layout es obligatoria")
        ConfiguracionLayoutRequest configuracionLayout
) {
    public record ConfiguracionLayoutRequest(
            @Schema(description = "Modo de captura del layout", example = "MULTIPLE")
            @NotNull(message = "El modo de captura es obligatorio")
            ModoCaptura modoCaptura,
            @Schema(description = "Cantidad total de ubicaciones previstas", example = "3")
            @NotNull(message = "La cantidad de ubicaciones es obligatoria")
            @Min(value = 1, message = "La cantidad de ubicaciones debe ser al menos 1")
            Integer cantidadUbicaciones,
            @Schema(description = "Slots del layout en orden de captura")
            @NotEmpty(message = "Debe definirse al menos una ubicacion")
            @Valid
            @Size(max = 100, message = "El layout no puede superar 100 ubicaciones")
            List<LayoutUbicacionSlotRequest> ubicaciones
    ) {
    }

    public record LayoutUbicacionSlotRequest(
            @Schema(description = "Indice funcional de la ubicacion", example = "1")
            @NotNull(message = "El indice de la ubicacion es obligatorio")
            @Min(value = 1, message = "El indice de la ubicacion debe ser al menos 1")
            Integer indice,
            @Schema(description = "Orden de captura de la ubicacion", example = "1")
            @NotNull(message = "El orden de captura es obligatorio")
            @Min(value = 1, message = "El orden de captura debe ser al menos 1")
            Integer ordenCaptura
    ) {
    }
}
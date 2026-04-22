package com.sofka.plataforma_danos_back.folios.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CalculateQuoteRequest(
        @Schema(description = "Version optimista vigente de la cotizacion", example = "4")
        @NotNull(message = "La version de la cotizacion es obligatoria")
        Long version
) {
}
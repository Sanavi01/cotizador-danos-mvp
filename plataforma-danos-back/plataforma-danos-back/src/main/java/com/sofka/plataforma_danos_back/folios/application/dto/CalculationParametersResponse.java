package com.sofka.plataforma_danos_back.folios.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.sofka.plataforma_danos_back.folios.domain.ActiveCalculationParameters;

import io.swagger.v3.oas.annotations.media.Schema;

public record CalculationParametersResponse(
        @Schema(description = "Codigo canonico de la configuracion activa", example = "CALC-2026-CORE")
        String codigo,
        @Schema(description = "Indica si la configuracion esta activa", example = "true")
        boolean activo,
        @Schema(description = "Descripcion funcional de la configuracion activa", example = "Parametros activos de calculo")
        String descripcion,
        @Schema(description = "Version de la configuracion activa", example = "1.0.0")
        String version,
        @Schema(description = "Fecha de corte utilizada para vigencias", example = "2026-04-20T00:00:00Z")
        Instant fechaCorte,
        @Schema(description = "Recargo de administracion usado por el MVP", example = "0.12")
        BigDecimal recargoAdministracion,
        @Schema(description = "Margen comercial usado por el MVP", example = "0.05")
        BigDecimal margenComercial,
        @Schema(description = "Moneda configurada para el calculo", example = "COP")
        String moneda,
        @Schema(description = "Modo de redondeo usado por el calculo", example = "HALF_UP")
        String roundingMode
) {
    public static CalculationParametersResponse from(ActiveCalculationParameters parameters) {
        return new CalculationParametersResponse(
                parameters.codigo(),
                parameters.activo(),
                parameters.descripcion(),
                parameters.version(),
                parameters.fechaCorte(),
                parameters.recargoAdministracion(),
                parameters.margenComercial(),
                parameters.moneda(),
                parameters.roundingMode()
        );
    }
}
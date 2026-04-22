package com.sofka.plataforma_danos_back.folios.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record ActiveCalculationParameters(
        String codigo,
        boolean activo,
        String descripcion,
        String version,
        Instant fechaCorte,
        BigDecimal recargoAdministracion,
        BigDecimal margenComercial,
        String moneda,
        String roundingMode
) {
}
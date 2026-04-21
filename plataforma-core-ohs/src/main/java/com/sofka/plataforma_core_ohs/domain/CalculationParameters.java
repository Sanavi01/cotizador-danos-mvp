package com.sofka.plataforma_core_ohs.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CalculationParameters(
		String codigo,
		boolean activo,
		String descripcion,
		String version,
		OffsetDateTime fechaCorte,
		BigDecimal recargoAdministracion,
		BigDecimal margenComercial,
		String moneda,
		String roundingMode) {
}
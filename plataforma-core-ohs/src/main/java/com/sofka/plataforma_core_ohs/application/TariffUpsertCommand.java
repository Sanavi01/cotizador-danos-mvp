package com.sofka.plataforma_core_ohs.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TariffUpsertCommand(
		String giroCode,
		String zonaCode,
		String garantiaCode,
		BigDecimal rate,
		BigDecimal factor,
		String moneda,
		OffsetDateTime vigenciaDesde,
		OffsetDateTime vigenciaHasta) {
}
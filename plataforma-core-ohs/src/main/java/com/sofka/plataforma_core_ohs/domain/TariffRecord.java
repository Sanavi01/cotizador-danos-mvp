package com.sofka.plataforma_core_ohs.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TariffRecord(
		String tariffKey,
		String giroCode,
		String zonaCode,
		String garantiaCode,
		BigDecimal rate,
		BigDecimal factor,
		String moneda,
		OffsetDateTime vigenciaDesde,
		OffsetDateTime vigenciaHasta) {
}
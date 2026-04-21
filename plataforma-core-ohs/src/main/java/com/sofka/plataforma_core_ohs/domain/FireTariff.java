package com.sofka.plataforma_core_ohs.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record FireTariff(
		String tariffKey,
		String giroCode,
		String tipoConstructivo,
		String nivel,
		BigDecimal rate,
		BigDecimal factor,
		String moneda,
		OffsetDateTime vigenciaDesde,
		OffsetDateTime vigenciaHasta) {
}
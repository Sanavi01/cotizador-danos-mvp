package com.sofka.plataforma_core_ohs.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CatTariff(
		String tariffKey,
		String zonaTev,
		String coberturaCode,
		BigDecimal rate,
		BigDecimal factor,
		String moneda,
		OffsetDateTime vigenciaDesde,
		OffsetDateTime vigenciaHasta) {
}
package com.sofka.plataforma_core_ohs.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record FhmTariff(
		String tariffKey,
		String zonaFhm,
		String grupo,
		BigDecimal rate,
		BigDecimal factor,
		String moneda,
		OffsetDateTime vigenciaDesde,
		OffsetDateTime vigenciaHasta) {
}
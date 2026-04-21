package com.sofka.plataforma_core_ohs.domain;

import java.math.BigDecimal;

public record SeedQuote(
		String numeroFolio,
		String estadoCotizacion,
		String subscriberCode,
		String agentCode,
		String zipCode,
		boolean calculada,
		BigDecimal primaNeta,
		BigDecimal primaComercial,
		String observacion) {
}
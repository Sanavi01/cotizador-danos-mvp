package com.sofka.plataforma_core_ohs.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ElectronicEquipmentFactor(
		String factorKey,
		String clase,
		String nivel,
		BigDecimal factor,
		OffsetDateTime vigenciaDesde,
		OffsetDateTime vigenciaHasta) {
}
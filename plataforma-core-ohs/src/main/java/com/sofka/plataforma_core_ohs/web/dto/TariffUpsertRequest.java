package com.sofka.plataforma_core_ohs.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TariffUpsertRequest(
		@NotBlank(message = "El giro es obligatorio") String giroCode,
		@NotBlank(message = "La zona tecnica es obligatoria") String zonaCode,
		@NotBlank(message = "La garantia es obligatoria") String garantiaCode,
		@NotNull(message = "La tasa es obligatoria")
		@Positive(message = "La tasa debe ser positiva")
		@Digits(integer = 10, fraction = 6, message = "La tasa tiene un formato invalido")
		BigDecimal rate,
		@NotNull(message = "El factor es obligatorio")
		@Positive(message = "El factor debe ser positivo")
		@Digits(integer = 10, fraction = 6, message = "El factor tiene un formato invalido")
		BigDecimal factor,
		@NotBlank(message = "La moneda es obligatoria")
		@Pattern(regexp = "COP", message = "La moneda debe ser COP")
		String moneda,
		@NotNull(message = "La vigencia inicial es obligatoria") OffsetDateTime vigenciaDesde,
		@NotNull(message = "La vigencia final es obligatoria") OffsetDateTime vigenciaHasta) {
}
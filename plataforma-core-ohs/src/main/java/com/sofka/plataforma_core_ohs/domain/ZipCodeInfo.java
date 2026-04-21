package com.sofka.plataforma_core_ohs.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ZipCodeInfo(
		String zipCode,
		boolean valido,
		String municipio,
		String estado,
		String coloniaBarrio,
		@JsonProperty("zona_tev") String zonaTev,
		@JsonProperty("zona_fhm") String zonaFhm,
		List<ValidationAlert> alertas) {
}
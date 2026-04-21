package com.sofka.plataforma_core_ohs.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ZipCodeValidationRequest(
		@NotBlank(message = "El codigo postal es obligatorio")
		@Pattern(regexp = "\\d{6}", message = "El codigo postal debe tener 6 digitos")
		String zipCode) {
}
package com.sofka.plataforma_core_ohs.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.plataforma_core_ohs.PlataformaCoreOhsApplication;
import com.sofka.plataforma_core_ohs.web.dto.ZipCodeValidationRequest;

@SpringBootTest(classes = PlataformaCoreOhsApplication.class)
class ReferenceCoreContractTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private ObjectMapper objectMapper;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void getSubscribers_returns200WithDataEnvelope() throws Exception {
		mockMvc.perform(get("/v1/subscribers"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
				.andExpect(jsonPath("$.data[0].codigo").value("SUS-001"))
				.andExpect(jsonPath("$.data[0].activo").value(true));
	}

	@Test
	void getActiveCalculationParameters_returns200WithCommercialFactors() throws Exception {
		mockMvc.perform(get("/v1/calculation-parameters/active"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
				.andExpect(jsonPath("$.data.codigo").value("CALC-2026-CORE"))
				.andExpect(jsonPath("$.data.version").value("1.0.0"))
				.andExpect(jsonPath("$.data.recargoAdministracion").value(0.12))
				.andExpect(jsonPath("$.data.margenComercial").value(0.05))
				.andExpect(jsonPath("$.data.moneda").value("COP"))
				.andExpect(jsonPath("$.data.roundingMode").value("HALF_UP"));
	}

	@Test
	void getZipCode_returns200WithTerritorialEnrichmentAndWarningAlert() throws Exception {
		mockMvc.perform(get("/v1/zip-codes/{zipCode}", "999999"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
				.andExpect(jsonPath("$.data.zipCode").value("999999"))
				.andExpect(jsonPath("$.data.valido").value(false))
				.andExpect(jsonPath("$.data.alertas[0].codigo").value("ZIP_NO_RECONOCIDO"))
				.andExpect(jsonPath("$.data.alertas[0].severidad").value("Warning"));
	}

	@Test
	void validateZipCode_returns400ProblemDetailsWhenPayloadIsInvalid() throws Exception {
		ZipCodeValidationRequest request = new ZipCodeValidationRequest("12");

		mockMvc.perform(post("/v1/zip-codes/validate")
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Validacion fallida"))
				.andExpect(jsonPath("$.detail").value("El codigo postal debe tener 6 digitos"))
				.andExpect(jsonPath("$.code").value("CORE_VALIDATION_ERROR"));
	}

	@Test
	void getTariff_returns404ProblemDetailsWhenTariffDoesNotExist() throws Exception {
		mockMvc.perform(get("/v1/tariffs/{tariffKey}", "GIRO-999|ZTEV-9|GAR-XXX"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Recurso no encontrado"))
				.andExpect(jsonPath("$.detail").value("No existe tarifa vigente para la combinacion solicitada."))
				.andExpect(jsonPath("$.code").value("CORE_NOT_FOUND"));
	}
}
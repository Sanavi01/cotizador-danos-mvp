package com.sofka.plataforma_core_ohs.infrastructure;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sofka.plataforma_core_ohs.application.ReferenceCoreNotFoundException;
import com.sofka.plataforma_core_ohs.domain.AlertSeverity;
import com.sofka.plataforma_core_ohs.domain.CatalogItem;
import com.sofka.plataforma_core_ohs.domain.CalculationParameters;
import com.sofka.plataforma_core_ohs.domain.FolioSequence;
import com.sofka.plataforma_core_ohs.domain.TariffRecord;
import com.sofka.plataforma_core_ohs.domain.ValidationAlert;
import com.sofka.plataforma_core_ohs.domain.ZipCodeInfo;

class InMemoryReferenceCoreServiceTest {

	private ReferenceCoreFixtureStore fixtureStore;
	private InMemoryReferenceCoreService service;

	@BeforeEach
	void setUp() {
		fixtureStore = new ReferenceCoreFixtureStore(new ObjectMapper().registerModule(new JavaTimeModule()));
		fixtureStore.loadFixtures();
		service = new InMemoryReferenceCoreService(fixtureStore, Clock.fixed(Instant.parse("2026-04-20T00:00:00Z"), ZoneOffset.UTC));
	}

	@Test
	void listCatalogs_returnsFixtureCatalogsWithExpectedSizes() {
		assertAll(
				() -> assertEquals(5, service.listSubscribers().size()),
				() -> assertEquals(12, service.listAgents().size()),
				() -> assertEquals(20, service.listBusinessLines().size()),
				() -> assertEquals(4, service.listRiskClassifications().size()),
				() -> assertEquals(14, service.listGuarantees().size())
		);

		CatalogItem firstSubscriber = service.listSubscribers().get(0);
		CatalogItem firstAgent = service.listAgents().get(0);
		CatalogItem firstBusinessLine = service.listBusinessLines().get(0);

		assertAll(
				() -> assertEquals("SUS-001", firstSubscriber.codigo()),
				() -> assertEquals("AG-101", firstAgent.codigo()),
				() -> assertEquals("GIRO-001", firstBusinessLine.codigo()),
				() -> assertEquals("INC-OFI", firstBusinessLine.claveIncendio())
		);
	}

	@Test
	void getZipCode_returnsEnrichedTerritorialDataWhenZipExists() {
		ZipCodeInfo zipCodeInfo = service.getZipCode("110111");

		assertAll(
				() -> assertEquals("110111", zipCodeInfo.zipCode()),
				() -> assertTrue(zipCodeInfo.valido()),
				() -> assertEquals("Bogota D.C.", zipCodeInfo.municipio()),
				() -> assertEquals("Cundinamarca", zipCodeInfo.estado()),
				() -> assertEquals("Chapinero", zipCodeInfo.coloniaBarrio()),
				() -> assertEquals("ZTEV-1", zipCodeInfo.zonaTev()),
				() -> assertEquals("ZFHM-1", zipCodeInfo.zonaFhm()),
				() -> assertTrue(zipCodeInfo.alertas().isEmpty())
		);
	}

	@Test
	void getZipCode_returnsWarningAlertWhenZipDoesNotExist() {
		ZipCodeInfo zipCodeInfo = service.getZipCode("999999");

		assertAll(
				() -> assertEquals("999999", zipCodeInfo.zipCode()),
				() -> assertFalse(zipCodeInfo.valido()),
				() -> assertTrue(zipCodeInfo.municipio() == null),
				() -> assertEquals(1, zipCodeInfo.alertas().size())
		);

		ValidationAlert alert = zipCodeInfo.alertas().get(0);
		assertAll(
				() -> assertEquals("ZIP_NO_RECONOCIDO", alert.codigo()),
				() -> assertEquals("El codigo postal no esta registrado en la referencia core.", alert.mensaje()),
				() -> assertEquals(AlertSeverity.WARNING, alert.severidad())
		);
	}

	@Test
	void validateZipCode_returnsErrorAlertWhenZipDoesNotExist() {
		ZipCodeInfo zipCodeInfo = service.validateZipCode("999999");

		assertFalse(zipCodeInfo.valido());
		assertEquals(AlertSeverity.ERROR, zipCodeInfo.alertas().get(0).severidad());
	}

	@Test
	void getTariff_returnsActiveTariffWhenTariffKeyExists() {
		TariffRecord tariffRecord = service.getTariff("GIRO-001|ZTEV-1|GAR-INC-ED");

		assertAll(
				() -> assertEquals("GIRO-001|ZTEV-1|GAR-INC-ED", tariffRecord.tariffKey()),
				() -> assertEquals("GIRO-001", tariffRecord.giroCode()),
				() -> assertEquals("ZTEV-1", tariffRecord.zonaCode()),
				() -> assertEquals("GAR-INC-ED", tariffRecord.garantiaCode()),
				() -> assertEquals(new BigDecimal("0.015"), tariffRecord.rate()),
				() -> assertEquals(new BigDecimal("1.2"), tariffRecord.factor()),
				() -> assertEquals("COP", tariffRecord.moneda()),
				() -> assertNotNull(tariffRecord.vigenciaDesde()),
				() -> assertNotNull(tariffRecord.vigenciaHasta())
		);
	}

	@Test
	void getActiveCalculationParameters_returnsCommercialFactorsFromFixtures() {
		CalculationParameters parameters = service.getActiveCalculationParameters();

		assertAll(
				() -> assertEquals("CALC-2026-CORE", parameters.codigo()),
				() -> assertTrue(parameters.activo()),
				() -> assertEquals("1.0.0", parameters.version()),
				() -> assertEquals(new BigDecimal("0.12"), parameters.recargoAdministracion()),
				() -> assertEquals(new BigDecimal("0.05"), parameters.margenComercial()),
				() -> assertEquals("COP", parameters.moneda()),
				() -> assertEquals("HALF_UP", parameters.roundingMode())
		);
	}

	@Test
	void getTariff_throwsNotFoundWhenTariffKeyDoesNotExist() {
		ReferenceCoreNotFoundException exception = assertThrows(
				ReferenceCoreNotFoundException.class,
				() -> service.getTariff("GIRO-999|ZTEV-9|GAR-XXX")
		);

		assertEquals("No existe tarifa vigente para la combinacion solicitada.", exception.getMessage());
	}

	@Test
	void getFolioSequence_returnsMockSequenceFromFixtures() {
		FolioSequence folioSequence = service.getFolioSequence();

		assertAll(
				() -> assertEquals("1000004", folioSequence.nextNumeroFolio()),
				() -> assertEquals("mock", folioSequence.fuente()),
				() -> assertTrue(folioSequence.vigente())
		);
	}
}
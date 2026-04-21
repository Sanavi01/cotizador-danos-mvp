package com.sofka.plataforma_core_ohs.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.plataforma_core_ohs.domain.CalculationParameters;
import com.sofka.plataforma_core_ohs.domain.CatTariff;
import com.sofka.plataforma_core_ohs.domain.CatalogItem;
import com.sofka.plataforma_core_ohs.domain.ElectronicEquipmentFactor;
import com.sofka.plataforma_core_ohs.domain.FhmTariff;
import com.sofka.plataforma_core_ohs.domain.FireTariff;
import com.sofka.plataforma_core_ohs.domain.FolioSequence;
import com.sofka.plataforma_core_ohs.domain.SeedQuote;
import com.sofka.plataforma_core_ohs.domain.TariffRecord;
import com.sofka.plataforma_core_ohs.domain.ZipCodeInfo;

import jakarta.annotation.PostConstruct;

@Component
public class ReferenceCoreFixtureStore {

	private final ObjectMapper objectMapper;

	private List<CatalogItem> subscribers = List.of();
	private List<CatalogItem> agents = List.of();
	private List<CatalogItem> businessLines = List.of();
	private List<CatalogItem> riskClassifications = List.of();
	private List<CatalogItem> guarantees = List.of();
	private CalculationParameters calculationParameters = new CalculationParameters("CALC-2026-CORE", true, "Parametros activos de calculo", "1.0.0", java.time.OffsetDateTime.parse("2026-04-20T00:00:00Z"));
	private Map<String, ZipCodeInfo> zipCodesByCode = Map.of();
	private List<FireTariff> fireTariffs = List.of();
	private List<CatTariff> catTariffs = List.of();
	private List<FhmTariff> fhmTariffs = List.of();
	private List<ElectronicEquipmentFactor> electronicEquipmentFactors = List.of();
	private List<SeedQuote> seedQuotes = List.of();
	private final Map<String, TariffRecord> tariffsByKey = new ConcurrentHashMap<>();
	private FolioSequence folioSequence = new FolioSequence("1000001", "mock", true);

	public ReferenceCoreFixtureStore(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	void loadFixtures() {
		FixtureBundle bundle = readBundle();
		subscribers = List.copyOf(bundle.subscribers());
		agents = List.copyOf(bundle.agents());
		businessLines = List.copyOf(bundle.businessLines());
		riskClassifications = List.copyOf(bundle.riskClassifications());
		guarantees = List.copyOf(bundle.guarantees());
		calculationParameters = bundle.calculationParameters();
		zipCodesByCode = bundle.zipCodes().stream()
				.collect(Collectors.toMap(ZipCodeInfo::zipCode, zipCode -> zipCode, (left, right) -> left, LinkedHashMap::new));
		fireTariffs = List.copyOf(bundle.fireTariffs());
		catTariffs = List.copyOf(bundle.catTariffs());
		fhmTariffs = List.copyOf(bundle.fhmTariffs());
		electronicEquipmentFactors = List.copyOf(bundle.electronicEquipmentFactors());
		seedQuotes = List.copyOf(bundle.seedQuotes());
		tariffsByKey.clear();
		bundle.tariffs().forEach(tariff -> tariffsByKey.put(tariff.tariffKey(), tariff));
		folioSequence = bundle.folioSequence();
	}

	public List<CatalogItem> subscribers() {
		return subscribers;
	}

	public List<CatalogItem> agents() {
		return agents;
	}

	public List<CatalogItem> businessLines() {
		return businessLines;
	}

	public List<CatalogItem> riskClassifications() {
		return riskClassifications;
	}

	public List<CatalogItem> guarantees() {
		return guarantees;
	}

	public CalculationParameters calculationParameters() {
		return calculationParameters;
	}

	public List<FireTariff> fireTariffs() {
		return fireTariffs;
	}

	public List<CatTariff> catTariffs() {
		return catTariffs;
	}

	public List<FhmTariff> fhmTariffs() {
		return fhmTariffs;
	}

	public List<ElectronicEquipmentFactor> electronicEquipmentFactors() {
		return electronicEquipmentFactors;
	}

	public List<SeedQuote> seedQuotes() {
		return seedQuotes;
	}

	public Optional<ZipCodeInfo> findZipCode(String zipCode) {
		return Optional.ofNullable(zipCodesByCode.get(zipCode));
	}

	public Optional<TariffRecord> findTariff(String tariffKey) {
		return Optional.ofNullable(tariffsByKey.get(tariffKey));
	}

	public TariffRecord saveTariff(TariffRecord tariffRecord) {
		tariffsByKey.put(tariffRecord.tariffKey(), tariffRecord);
		return tariffRecord;
	}

	public FolioSequence folioSequence() {
		return folioSequence;
	}

	private FixtureBundle readBundle() {
		ClassPathResource resource = new ClassPathResource("fixtures/reference-core-fixtures.json");
		try (InputStream inputStream = resource.getInputStream()) {
			return objectMapper.readValue(inputStream, FixtureBundle.class);
		} catch (IOException exception) {
			throw new IllegalStateException("No fue posible cargar las fixtures del core de referencia", exception);
		}
	}

	public record FixtureBundle(
			List<CatalogItem> subscribers,
			List<CatalogItem> agents,
			List<CatalogItem> businessLines,
			List<CatalogItem> riskClassifications,
			List<CatalogItem> guarantees,
			CalculationParameters calculationParameters,
			List<ZipCodeInfo> zipCodes,
			List<FireTariff> fireTariffs,
			List<CatTariff> catTariffs,
			List<FhmTariff> fhmTariffs,
			List<ElectronicEquipmentFactor> electronicEquipmentFactors,
			List<SeedQuote> seedQuotes,
			List<TariffRecord> tariffs,
			FolioSequence folioSequence) {
	}
}
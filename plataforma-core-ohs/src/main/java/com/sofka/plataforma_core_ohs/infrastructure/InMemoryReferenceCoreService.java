package com.sofka.plataforma_core_ohs.infrastructure;

import com.sofka.plataforma_core_ohs.application.ReferenceCoreBadRequestException;
import com.sofka.plataforma_core_ohs.application.ReferenceCoreNotFoundException;
import com.sofka.plataforma_core_ohs.application.ReferenceCoreService;
import com.sofka.plataforma_core_ohs.application.TariffUpsertCommand;
import com.sofka.plataforma_core_ohs.domain.AlertSeverity;
import com.sofka.plataforma_core_ohs.domain.CatalogItem;
import com.sofka.plataforma_core_ohs.domain.FolioSequence;
import com.sofka.plataforma_core_ohs.domain.TariffRecord;
import com.sofka.plataforma_core_ohs.domain.ValidationAlert;
import com.sofka.plataforma_core_ohs.domain.ZipCodeInfo;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class InMemoryReferenceCoreService implements ReferenceCoreService {

	private final ReferenceCoreFixtureStore fixtureStore;
	private final Clock clock;

	public InMemoryReferenceCoreService(ReferenceCoreFixtureStore fixtureStore, Clock clock) {
		this.fixtureStore = fixtureStore;
		this.clock = clock;
	}

	@Override
	public List<CatalogItem> listSubscribers() {
		return fixtureStore.subscribers();
	}

	@Override
	public List<CatalogItem> listAgents() {
		return fixtureStore.agents();
	}

	@Override
	public List<CatalogItem> listBusinessLines() {
		return fixtureStore.businessLines();
	}

	@Override
	public List<CatalogItem> listRiskClassifications() {
		return fixtureStore.riskClassifications();
	}

	@Override
	public List<CatalogItem> listGuarantees() {
		return fixtureStore.guarantees();
	}

	@Override
	public ZipCodeInfo getZipCode(String zipCode) {
		return resolveZipCode(zipCode, AlertSeverity.WARNING);
	}

	@Override
	public ZipCodeInfo validateZipCode(String zipCode) {
		return resolveZipCode(zipCode, AlertSeverity.ERROR);
	}

	@Override
	public FolioSequence getFolioSequence() {
		return fixtureStore.folioSequence();
	}

	@Override
	public TariffRecord getTariff(String tariffKey) {
		TariffRecord tariffRecord = fixtureStore.findTariff(tariffKey)
				.orElseThrow(() -> new ReferenceCoreNotFoundException("No existe tarifa vigente para la combinacion solicitada."));
		if (!isCurrentlyValid(tariffRecord)) {
			throw new ReferenceCoreNotFoundException("No existe tarifa vigente para la combinacion solicitada.");
		}
		return tariffRecord;
	}

	@Override
	public TariffRecord upsertTariff(String tariffKey, TariffUpsertCommand command) {
		String expectedKey = buildTariffKey(command.giroCode(), command.zonaCode(), command.garantiaCode());
		if (!expectedKey.equals(tariffKey)) {
			throw new ReferenceCoreBadRequestException("La clave de tarifa no coincide con el contenido de la solicitud.");
		}
		if (!"COP".equals(command.moneda())) {
			throw new ReferenceCoreBadRequestException("La moneda debe ser COP.");
		}
		if (command.vigenciaDesde().isAfter(command.vigenciaHasta())) {
			throw new ReferenceCoreBadRequestException("La vigencia inicial no puede ser posterior a la vigencia final.");
		}
		TariffRecord tariffRecord = new TariffRecord(
				tariffKey,
				command.giroCode(),
				command.zonaCode(),
				command.garantiaCode(),
				command.rate(),
				command.factor(),
				command.moneda(),
				command.vigenciaDesde(),
				command.vigenciaHasta());
		return fixtureStore.saveTariff(tariffRecord);
	}

	private ZipCodeInfo resolveZipCode(String zipCode, AlertSeverity severity) {
		if (zipCode == null || zipCode.isBlank()) {
			throw new ReferenceCoreBadRequestException("El codigo postal es obligatorio.");
		}
		return fixtureStore.findZipCode(zipCode)
				.orElseGet(() -> new ZipCodeInfo(
						zipCode,
						false,
						null,
						null,
						null,
						null,
						null,
						List.of(new ValidationAlert(
								"ZIP_NO_RECONOCIDO",
								"El codigo postal no esta registrado en la referencia core.",
								severity))));
	}

	private boolean isCurrentlyValid(TariffRecord tariffRecord) {
		OffsetDateTime now = OffsetDateTime.now(clock);
		return !now.isBefore(tariffRecord.vigenciaDesde()) && !now.isAfter(tariffRecord.vigenciaHasta());
	}

	private String buildTariffKey(String giroCode, String zonaCode, String garantiaCode) {
		return String.join("|", giroCode, zonaCode, garantiaCode);
	}
}
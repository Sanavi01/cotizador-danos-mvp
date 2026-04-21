package com.sofka.plataforma_core_ohs.application;

import java.util.List;

import com.sofka.plataforma_core_ohs.domain.CalculationParameters;
import com.sofka.plataforma_core_ohs.domain.CatalogItem;
import com.sofka.plataforma_core_ohs.domain.FolioSequence;
import com.sofka.plataforma_core_ohs.domain.TariffRecord;
import com.sofka.plataforma_core_ohs.domain.ZipCodeInfo;

public interface ReferenceCoreService {

	List<CatalogItem> listSubscribers();

	List<CatalogItem> listAgents();

	List<CatalogItem> listBusinessLines();

	List<CatalogItem> listRiskClassifications();

	List<CatalogItem> listGuarantees();

	CalculationParameters getActiveCalculationParameters();

	ZipCodeInfo getZipCode(String zipCode);

	ZipCodeInfo validateZipCode(String zipCode);

	FolioSequence getFolioSequence();

	TariffRecord getTariff(String tariffKey);

	TariffRecord upsertTariff(String tariffKey, TariffUpsertCommand command);
}
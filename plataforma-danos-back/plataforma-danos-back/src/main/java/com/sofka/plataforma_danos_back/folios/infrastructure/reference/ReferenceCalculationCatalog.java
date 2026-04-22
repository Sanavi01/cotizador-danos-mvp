package com.sofka.plataforma_danos_back.folios.infrastructure.reference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.sofka.plataforma_danos_back.folios.domain.ActiveCalculationParameters;

@Component
public class ReferenceCalculationCatalog {

    private static final ActiveCalculationParameters ACTIVE_PARAMETERS = new ActiveCalculationParameters(
            "CALC-2026-CORE",
            true,
            "Parametros activos de calculo",
            "1.0.0",
            Instant.parse("2026-04-20T00:00:00Z"),
            new BigDecimal("0.12"),
            new BigDecimal("0.05"),
            "COP",
            "HALF_UP"
    );

    private final Map<String, LookupRecord> coreTariffs = new LinkedHashMap<>();
    private final Map<String, LookupRecord> fireTariffs = new LinkedHashMap<>();
    private final Map<String, LookupRecord> catTariffs = new LinkedHashMap<>();
    private final Map<String, LookupRecord> fhmTariffs = new LinkedHashMap<>();
    private final Map<String, LookupRecord> electronicEquipmentFactors = new LinkedHashMap<>();

    public ReferenceCalculationCatalog() {
        loadCoreTariffs();
        loadFireTariffs();
        loadCatTariffs();
        loadFhmTariffs();
        loadElectronicEquipmentFactors();
    }

    public ActiveCalculationParameters activeParameters() {
        return ACTIVE_PARAMETERS;
    }

    public Optional<LookupRecord> findCoreTariff(String giroCode, String zonaCode, String garantiaCode) {
        return findByKey(coreTariffs, buildKey(giroCode, zonaCode, garantiaCode));
    }

    public Optional<LookupRecord> findFireTariff(String giroCode, String tipoConstructivo, String nivelTarifario) {
        return findByKey(fireTariffs, buildKey(giroCode, tipoConstructivo, nivelTarifario));
    }

    public Optional<LookupRecord> findCatTariff(String zonaTev, String garantiaCode) {
        return findByKey(catTariffs, buildKey(zonaTev, garantiaCode));
    }

    public Optional<LookupRecord> findFhmTariff(String zonaFhm, String grupo) {
        return findByKey(fhmTariffs, buildKey(zonaFhm, grupo));
    }

    public Optional<LookupRecord> findElectronicEquipmentFactor(String clase, String nivelTarifario) {
        return findByKey(electronicEquipmentFactors, buildKey(clase, nivelTarifario));
    }

    private Optional<LookupRecord> findByKey(Map<String, LookupRecord> source, String lookupKey) {
        return Optional.ofNullable(source.get(lookupKey));
    }

    private String buildKey(String... parts) {
        return String.join("|", parts);
    }

    private void loadCoreTariffs() {
        put(coreTariffs, new LookupRecord("tariffs", "GIRO-001|ZTEV-1|GAR-INC-ED", new BigDecimal("0.015"), new BigDecimal("1.2"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(coreTariffs, new LookupRecord("tariffs", "GIRO-002|ZTEV-2|GAR-INC-CONT", new BigDecimal("0.016"), new BigDecimal("1.18"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(coreTariffs, new LookupRecord("tariffs", "GIRO-003|ZTEV-3|GAR-INC-LUC", new BigDecimal("0.017"), new BigDecimal("1.16"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(coreTariffs, new LookupRecord("tariffs", "GIRO-001|ZTEV-3|GAR-INC-ED", new BigDecimal("0.0155"), new BigDecimal("1.19"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(coreTariffs, new LookupRecord("tariffs", "GIRO-001|ZTEV-3|GAR-INC-CONT", new BigDecimal("0.0165"), new BigDecimal("1.17"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(coreTariffs, new LookupRecord("tariffs", "GIRO-002|ZTEV-3|GAR-INC-ED", new BigDecimal("0.0156"), new BigDecimal("1.18"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(coreTariffs, new LookupRecord("tariffs", "GIRO-002|ZTEV-3|GAR-INC-CONT", new BigDecimal("0.0166"), new BigDecimal("1.16"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(coreTariffs, new LookupRecord("tariffs", "GIRO-003|ZTEV-3|GAR-INC-ED", new BigDecimal("0.0157"), new BigDecimal("1.17"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(coreTariffs, new LookupRecord("tariffs", "GIRO-003|ZTEV-3|GAR-INC-CONT", new BigDecimal("0.0167"), new BigDecimal("1.15"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
    }

    private void loadFireTariffs() {
        put(fireTariffs, new LookupRecord("fireTariffs", "GIRO-001|MAMP|BAS", new BigDecimal("0.012"), new BigDecimal("1.05"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(fireTariffs, new LookupRecord("fireTariffs", "GIRO-001|MAMP|MED", new BigDecimal("0.013"), new BigDecimal("1.08"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(fireTariffs, new LookupRecord("fireTariffs", "GIRO-002|MIX|BAS", new BigDecimal("0.014"), new BigDecimal("1.07"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(fireTariffs, new LookupRecord("fireTariffs", "GIRO-003|MIX|MED", new BigDecimal("0.015"), new BigDecimal("1.09"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(fireTariffs, new LookupRecord("fireTariffs", "GIRO-004|MET|BAS", new BigDecimal("0.011"), new BigDecimal("1.03"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(fireTariffs, new LookupRecord("fireTariffs", "GIRO-005|MET|ALT", new BigDecimal("0.017"), new BigDecimal("1.15"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
    }

    private void loadCatTariffs() {
        put(catTariffs, new LookupRecord("catTariffs", "ZTEV-1|GAR-INC-ED", new BigDecimal("0.006"), new BigDecimal("1.02"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(catTariffs, new LookupRecord("catTariffs", "ZTEV-2|GAR-INC-ED", new BigDecimal("0.007"), new BigDecimal("1.04"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(catTariffs, new LookupRecord("catTariffs", "ZTEV-3|GAR-INC-CONT", new BigDecimal("0.008"), new BigDecimal("1.06"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(catTariffs, new LookupRecord("catTariffs", "ZTEV-4|GAR-INC-LUC", new BigDecimal("0.009"), new BigDecimal("1.08"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(catTariffs, new LookupRecord("catTariffs", "ZTEV-2|GAR-ROBO", new BigDecimal("0.010"), new BigDecimal("1.10"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(catTariffs, new LookupRecord("catTariffs", "ZTEV-3|GAR-ROBO", new BigDecimal("0.0105"), new BigDecimal("1.11"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(catTariffs, new LookupRecord("catTariffs", "ZTEV-3|GAR-EL", new BigDecimal("0.011"), new BigDecimal("1.12"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(catTariffs, new LookupRecord("catTariffs", "ZTEV-3|GAR-TER", new BigDecimal("0.0115"), new BigDecimal("1.13"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(catTariffs, new LookupRecord("catTariffs", "ZTEV-4|GAR-TER", new BigDecimal("0.012"), new BigDecimal("1.14"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
    }

    private void loadFhmTariffs() {
        put(fhmTariffs, new LookupRecord("fhmTariffs", "ZFHM-1|GRP-A", new BigDecimal("0.004"), new BigDecimal("1.01"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(fhmTariffs, new LookupRecord("fhmTariffs", "ZFHM-1|GRP-B", new BigDecimal("0.005"), new BigDecimal("1.02"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(fhmTariffs, new LookupRecord("fhmTariffs", "ZFHM-2|GRP-A", new BigDecimal("0.006"), new BigDecimal("1.03"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(fhmTariffs, new LookupRecord("fhmTariffs", "ZFHM-2|GRP-B", new BigDecimal("0.007"), new BigDecimal("1.04"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(fhmTariffs, new LookupRecord("fhmTariffs", "ZFHM-3|GRP-A", new BigDecimal("0.008"), new BigDecimal("1.05"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(fhmTariffs, new LookupRecord("fhmTariffs", "ZFHM-3|GRP-C", new BigDecimal("0.009"), new BigDecimal("1.06"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
    }

    private void loadElectronicEquipmentFactors() {
        put(electronicEquipmentFactors, new LookupRecord("electronicEquipmentFactors", "EQ-OFI|BAS", null, new BigDecimal("1.02"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(electronicEquipmentFactors, new LookupRecord("electronicEquipmentFactors", "EQ-OFI|MED", null, new BigDecimal("1.05"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(electronicEquipmentFactors, new LookupRecord("electronicEquipmentFactors", "EQ-IND|BAS", null, new BigDecimal("1.08"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(electronicEquipmentFactors, new LookupRecord("electronicEquipmentFactors", "EQ-IND|MED", null, new BigDecimal("1.11"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(electronicEquipmentFactors, new LookupRecord("electronicEquipmentFactors", "EQ-TEL|BAS", null, new BigDecimal("1.14"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
        put(electronicEquipmentFactors, new LookupRecord("electronicEquipmentFactors", "EQ-TEL|ALT", null, new BigDecimal("1.18"), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-12-31T23:59:59Z")));
    }

    private void put(Map<String, LookupRecord> target, LookupRecord record) {
        target.put(record.lookupKey(), record);
    }

    public record LookupRecord(
            String source,
            String lookupKey,
            BigDecimal rate,
            BigDecimal factor,
            Instant vigenciaDesde,
            Instant vigenciaHasta
    ) {
    }
}
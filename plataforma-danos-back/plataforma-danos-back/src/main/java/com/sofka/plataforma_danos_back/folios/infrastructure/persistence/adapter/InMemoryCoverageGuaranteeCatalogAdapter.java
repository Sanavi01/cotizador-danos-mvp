package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteeDefinition;
import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteePreview;
import com.sofka.plataforma_danos_back.folios.domain.TechnicalPreviewSource;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageGuaranteeCatalogPort;

@Component
public class InMemoryCoverageGuaranteeCatalogAdapter implements CoverageGuaranteeCatalogPort {

    private static final Map<String, CoverageGuaranteeDefinition> DEFINITIONS = Map.of(
            "GAR-INC-ED", new CoverageGuaranteeDefinition("GAR-INC-ED", true, TechnicalPreviewSource.CORE_TARIFF),
            "GAR-ROBO", new CoverageGuaranteeDefinition("GAR-ROBO", true, TechnicalPreviewSource.CAT_TARIFF),
            "GAR-FHM-01", new CoverageGuaranteeDefinition("GAR-FHM-01", true, TechnicalPreviewSource.FHM_TARIFF),
            "GAR-EQUIP-01", new CoverageGuaranteeDefinition("GAR-EQUIP-01", true, TechnicalPreviewSource.ELECTRONIC_FACTOR),
            "GAR-INACTIVA", new CoverageGuaranteeDefinition("GAR-INACTIVA", false, TechnicalPreviewSource.UNRESOLVED)
    );

    private static final Set<String> CORE_TARIFF_KEYS = Set.of("GIRO-001|ZTEV-1|GAR-INC-ED");
    private static final Set<String> CAT_TARIFF_KEYS = Set.of("ZTEV-9|GAR-ROBO");
    private static final Set<String> FIRE_TARIFF_KEYS = Set.of("GIRO-001|CONCRETO|1|GAR-FIRE-01");

    @Override
    public Optional<CoverageGuaranteeDefinition> findByCode(String garantiaCode) {
        return Optional.ofNullable(DEFINITIONS.get(normalize(garantiaCode)));
    }

    @Override
    public CoverageGuaranteePreview resolvePreview(String garantiaCode, UbicacionCotizacion ubicacion) {
        CoverageGuaranteeDefinition definition = findByCode(garantiaCode).orElse(null);
        if (definition == null || !definition.activa()) {
            return unresolved(normalize(garantiaCode), "La garantia no esta disponible en el catalogo aprobado.");
        }

        UbicacionDetalle detalle = ubicacion.detalle();
        return switch (definition.fuenteTecnicaPreferida()) {
            case CORE_TARIFF -> resolveCoreTariff(definition.garantiaCode(), detalle);
            case FIRE_TARIFF -> resolveFireTariff(definition.garantiaCode(), detalle);
            case CAT_TARIFF -> resolveCatTariff(definition.garantiaCode(), detalle);
            case FHM_TARIFF -> unresolved(definition.garantiaCode(), "La ubicacion no tiene grupo o clase documentados para la ruta FHM.");
            case ELECTRONIC_FACTOR -> unresolved(definition.garantiaCode(), "La ubicacion no tiene grupo o clase documentados para el factor de equipo electronico.");
            case UNRESOLVED -> unresolved(definition.garantiaCode(), "No existe una ruta tecnica preliminar resoluble para la garantia.");
        };
    }

    private CoverageGuaranteePreview resolveCoreTariff(String garantiaCode, UbicacionDetalle detalle) {
        String giroCode = detalle.giro() == null ? null : normalize(detalle.giro().codigo());
        ZonaCatastrofica zonaCatastrofica = detalle.zonaCatastrofica();
        String zonaTev = zonaCatastrofica == null ? null : normalize(zonaCatastrofica.zonaTev());

        if (giroCode == null || zonaTev == null) {
            return unresolved(garantiaCode, "La ubicacion no tiene giro o zona TEV resolubles para previsualizar tarifas.");
        }

        String lookupKey = giroCode + "|" + zonaTev + "|" + garantiaCode;
        if (!CORE_TARIFF_KEYS.contains(lookupKey)) {
            return unresolved(garantiaCode, "No existe tarifa vigente para la combinacion actual de la ubicacion.");
        }

        return new CoverageGuaranteePreview(garantiaCode, true, TechnicalPreviewSource.CORE_TARIFF, lookupKey, List.of());
    }

    private CoverageGuaranteePreview resolveFireTariff(String garantiaCode, UbicacionDetalle detalle) {
        String giroCode = detalle.giro() == null ? null : normalize(detalle.giro().codigo());
        String tipoConstructivo = normalize(detalle.tipoConstructivo());
        Integer nivel = detalle.nivel();

        if (giroCode == null || tipoConstructivo == null || nivel == null) {
            return unresolved(garantiaCode, "La ubicacion no tiene giro, tipo constructivo o nivel tarifario resoluble.");
        }

        String lookupKey = giroCode + "|" + tipoConstructivo + "|" + nivel + "|" + garantiaCode;
        if (!FIRE_TARIFF_KEYS.contains(lookupKey)) {
            return unresolved(garantiaCode, "No existe tarifa de incendio vigente para la combinacion actual de la ubicacion.");
        }

        return new CoverageGuaranteePreview(garantiaCode, true, TechnicalPreviewSource.FIRE_TARIFF, lookupKey, List.of());
    }

    private CoverageGuaranteePreview resolveCatTariff(String garantiaCode, UbicacionDetalle detalle) {
        ZonaCatastrofica zonaCatastrofica = detalle.zonaCatastrofica();
        String zonaTev = zonaCatastrofica == null ? null : normalize(zonaCatastrofica.zonaTev());

        if (zonaTev == null) {
            return unresolved(garantiaCode, "La ubicacion no tiene zona TEV resoluble.");
        }

        String lookupKey = zonaTev + "|" + garantiaCode;
        if (!CAT_TARIFF_KEYS.contains(lookupKey)) {
            return unresolved(garantiaCode, "No existe tarifa vigente para la combinacion actual de la ubicacion.");
        }

        return new CoverageGuaranteePreview(garantiaCode, true, TechnicalPreviewSource.CAT_TARIFF, lookupKey, List.of());
    }

    private CoverageGuaranteePreview unresolved(String garantiaCode, String reason) {
        return new CoverageGuaranteePreview(garantiaCode, false, TechnicalPreviewSource.UNRESOLVED, null, List.of(reason));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
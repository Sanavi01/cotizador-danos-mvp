package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteeDefinition;
import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteePreview;
import com.sofka.plataforma_danos_back.folios.domain.TechnicalPreviewSource;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageGuaranteeCatalogPort;

@Component
public class InMemoryCoverageGuaranteeCatalogAdapter implements CoverageGuaranteeCatalogPort {

    private static final Map<String, CoverageGuaranteeDefinition> FALLBACK_DEFINITIONS = Map.ofEntries(
            Map.entry("GAR-INC-ED", new CoverageGuaranteeDefinition("GAR-INC-ED", true, TechnicalPreviewSource.CORE_TARIFF)),
            Map.entry("GAR-INC-CONT", new CoverageGuaranteeDefinition("GAR-INC-CONT", true, TechnicalPreviewSource.CORE_TARIFF)),
            Map.entry("GAR-INC-LUC", new CoverageGuaranteeDefinition("GAR-INC-LUC", true, TechnicalPreviewSource.CORE_TARIFF)),
            Map.entry("GAR-ROT", new CoverageGuaranteeDefinition("GAR-ROT", true, TechnicalPreviewSource.UNRESOLVED)),
            Map.entry("GAR-ROBO", new CoverageGuaranteeDefinition("GAR-ROBO", true, TechnicalPreviewSource.CAT_TARIFF)),
            Map.entry("GAR-TER", new CoverageGuaranteeDefinition("GAR-TER", true, TechnicalPreviewSource.CAT_TARIFF)),
            Map.entry("GAR-SIS", new CoverageGuaranteeDefinition("GAR-SIS", true, TechnicalPreviewSource.UNRESOLVED)),
            Map.entry("GAR-AGUA", new CoverageGuaranteeDefinition("GAR-AGUA", true, TechnicalPreviewSource.UNRESOLVED)),
            Map.entry("GAR-EL", new CoverageGuaranteeDefinition("GAR-EL", true, TechnicalPreviewSource.CAT_TARIFF)),
            Map.entry("GAR-RESP", new CoverageGuaranteeDefinition("GAR-RESP", true, TechnicalPreviewSource.UNRESOLVED)),
            Map.entry("GAR-VID", new CoverageGuaranteeDefinition("GAR-VID", true, TechnicalPreviewSource.UNRESOLVED)),
            Map.entry("GAR-EMP", new CoverageGuaranteeDefinition("GAR-EMP", true, TechnicalPreviewSource.UNRESOLVED)),
            Map.entry("GAR-INT", new CoverageGuaranteeDefinition("GAR-INT", true, TechnicalPreviewSource.UNRESOLVED)),
            Map.entry("GAR-ASIS", new CoverageGuaranteeDefinition("GAR-ASIS", true, TechnicalPreviewSource.UNRESOLVED)),
            Map.entry("GAR-FHM-01", new CoverageGuaranteeDefinition("GAR-FHM-01", true, TechnicalPreviewSource.FHM_TARIFF)),
            Map.entry("GAR-EQUIP-01", new CoverageGuaranteeDefinition("GAR-EQUIP-01", true, TechnicalPreviewSource.ELECTRONIC_FACTOR)),
            Map.entry("GAR-INACTIVA", new CoverageGuaranteeDefinition("GAR-INACTIVA", false, TechnicalPreviewSource.UNRESOLVED))
    );

    private static final Set<String> CAT_TARIFF_KEYS = Set.of("ZTEV-2|GAR-ROBO", "ZTEV-4|GAR-TER", "ZTEV-3|GAR-EL");

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI coreBaseUri;
    private volatile Map<String, CoverageGuaranteeDefinition> cachedDefinitions;

    public InMemoryCoverageGuaranteeCatalogAdapter(
            ObjectMapper objectMapper,
            @Value("${app.reference-core.base-url:http://localhost:8081}") String coreBaseUrl
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.coreBaseUri = URI.create(trimTrailingSlash(coreBaseUrl));
    }

    @Override
    public Optional<CoverageGuaranteeDefinition> findByCode(String garantiaCode) {
        return Optional.ofNullable(loadDefinitions().get(normalize(garantiaCode)));
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
            case FIRE_TARIFF -> unresolved(definition.garantiaCode(), "La ubicacion no tiene grupo o clase documentados para la ruta FHM.");
            case CAT_TARIFF -> resolveCatTariff(definition.garantiaCode(), detalle);
            case FHM_TARIFF -> unresolved(definition.garantiaCode(), "La ubicacion no tiene grupo o clase documentados para la ruta FHM.");
            case ELECTRONIC_FACTOR -> unresolved(definition.garantiaCode(), "La ubicacion no tiene grupo o clase documentados para el factor de equipo electronico.");
            case UNRESOLVED -> unresolved(definition.garantiaCode(), "No existe una ruta tecnica preliminar resoluble para la garantia.");
        };
    }

    private Map<String, CoverageGuaranteeDefinition> loadDefinitions() {
        Map<String, CoverageGuaranteeDefinition> currentDefinitions = cachedDefinitions;
        if (currentDefinitions != null) {
            return currentDefinitions;
        }

        synchronized (this) {
            if (cachedDefinitions != null) {
                return cachedDefinitions;
            }

            cachedDefinitions = fetchDefinitionsFromCore().orElse(FALLBACK_DEFINITIONS);
            return cachedDefinitions;
        }
    }

    private Optional<Map<String, CoverageGuaranteeDefinition>> fetchDefinitionsFromCore() {
        try {
            HttpResponse<String> response = sendGet("/v1/catalogs/guarantees");
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode dataNode = rootNode.path("data");
            List<CoreCatalogItem> catalogItems = objectMapper.convertValue(dataNode, new TypeReference<List<CoreCatalogItem>>() {});

            if (catalogItems == null || catalogItems.isEmpty()) {
                return Optional.empty();
            }

            Map<String, CoverageGuaranteeDefinition> definitions = new LinkedHashMap<>();
            for (CoreCatalogItem item : catalogItems) {
                String normalizedCode = normalize(item.codigo());
                if (normalizedCode == null) {
                    continue;
                }

                definitions.put(
                        normalizedCode,
                        new CoverageGuaranteeDefinition(normalizedCode, item.activo(), inferTechnicalSource(normalizedCode))
                );
            }

            return definitions.isEmpty() ? Optional.empty() : Optional.of(Map.copyOf(definitions));
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            return Optional.empty();
        }
    }

    private CoverageGuaranteePreview resolveCoreTariff(String garantiaCode, UbicacionDetalle detalle) {
        String giroCode = detalle.giro() == null ? null : normalize(detalle.giro().codigo());
        ZonaCatastrofica zonaCatastrofica = detalle.zonaCatastrofica();
        String zonaTev = zonaCatastrofica == null ? null : normalize(zonaCatastrofica.zonaTev());

        if (giroCode == null || zonaTev == null) {
            return unresolved(garantiaCode, "La ubicacion no tiene giro o zona TEV resolubles para previsualizar tarifas.");
        }

        String lookupKey = giroCode + "|" + zonaTev + "|" + garantiaCode;
        if (!hasCoreTariff(lookupKey)) {
            return unresolved(garantiaCode, "No existe tarifa vigente para la combinacion actual de la ubicacion.");
        }

        return new CoverageGuaranteePreview(garantiaCode, true, TechnicalPreviewSource.CORE_TARIFF, lookupKey, List.of());
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

    private boolean hasCoreTariff(String tariffKey) {
        try {
            HttpResponse<String> response = sendGet("/v1/tariffs/" + encodePathSegment(tariffKey));
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            return false;
        }
    }

    private HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(coreBaseUri.resolve(path))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private CoverageGuaranteePreview unresolved(String garantiaCode, String reason) {
        return new CoverageGuaranteePreview(garantiaCode, false, TechnicalPreviewSource.UNRESOLVED, null, List.of(reason));
    }

    private TechnicalPreviewSource inferTechnicalSource(String garantiaCode) {
        return switch (garantiaCode) {
            case "GAR-INC-ED", "GAR-INC-CONT", "GAR-INC-LUC" -> TechnicalPreviewSource.CORE_TARIFF;
            case "GAR-ROBO", "GAR-TER", "GAR-EL" -> TechnicalPreviewSource.CAT_TARIFF;
            case "GAR-FHM-01" -> TechnicalPreviewSource.FHM_TARIFF;
            case "GAR-EQUIP-01" -> TechnicalPreviewSource.ELECTRONIC_FACTOR;
            default -> TechnicalPreviewSource.UNRESOLVED;
        };
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8081";
        }

        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record CoreCatalogItem(String codigo, String nombre, boolean activo, String claveIncendio) {
    }

    @SuppressWarnings("unused")
    private record CoreTariffRecord(
            String tariffKey,
            String giroCode,
            String zonaCode,
            String garantiaCode,
            BigDecimal rate,
            BigDecimal factor,
            String moneda,
            OffsetDateTime vigenciaDesde,
            OffsetDateTime vigenciaHasta) {
    }
}
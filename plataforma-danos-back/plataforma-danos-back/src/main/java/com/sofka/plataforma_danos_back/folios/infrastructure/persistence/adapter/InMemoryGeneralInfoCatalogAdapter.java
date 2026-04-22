package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.plataforma_danos_back.folios.domain.port.GeneralInfoCatalogPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InMemoryGeneralInfoCatalogAdapter implements GeneralInfoCatalogPort {

    private static final Set<String> FALLBACK_ACTIVE_AGENTS = Set.of(
            "AG-101",
            "AG-102",
            "AG-103",
            "AG-104",
            "AG-105",
            "AG-106",
            "AG-107",
            "AG-108",
            "AG-109",
            "AG-110",
            "AG-111",
            "AG-112"
    );

            private static final Set<String> FALLBACK_ACTIVE_RISK_CLASSIFICATIONS = Set.of(
            "RISK-A",
            "RISK-B",
            "RISK-C",
            "RISK-D"
    );

            private static final Set<String> FALLBACK_ACTIVE_BUSINESS_LINES = Set.of(
            "GIRO-001",
            "GIRO-002",
            "GIRO-003",
            "GIRO-004",
            "GIRO-005",
            "GIRO-006",
            "GIRO-007",
            "GIRO-008",
            "GIRO-009",
            "GIRO-010",
            "GIRO-011",
            "GIRO-012",
            "GIRO-013",
            "GIRO-014",
            "GIRO-015",
            "GIRO-016",
            "GIRO-017",
            "GIRO-018",
            "GIRO-019",
            "GIRO-020"
    );

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI coreBaseUri;

    private volatile Set<String> cachedActiveAgents;
    private volatile Set<String> cachedActiveRiskClassifications;
    private volatile Set<String> cachedActiveBusinessLines;

    public InMemoryGeneralInfoCatalogAdapter(
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
    public boolean isActiveAgent(String codigoAgente) {
        return loadActiveAgents().contains(normalize(codigoAgente));
    }

    @Override
    public boolean isActiveRiskClassification(String clasificacionRiesgo) {
        return loadActiveRiskClassifications().contains(normalize(clasificacionRiesgo));
    }

    @Override
    public boolean isActiveBusinessLine(String tipoNegocio) {
        return loadActiveBusinessLines().contains(normalize(tipoNegocio));
    }

    private Set<String> loadActiveAgents() {
        Set<String> currentValue = cachedActiveAgents;
        if (currentValue != null) {
            return currentValue;
        }

        synchronized (this) {
            if (cachedActiveAgents != null) {
                return cachedActiveAgents;
            }

            cachedActiveAgents = fetchActiveCodes("/v1/agents").orElse(FALLBACK_ACTIVE_AGENTS);
            return cachedActiveAgents;
        }
    }

    private Set<String> loadActiveRiskClassifications() {
        Set<String> currentValue = cachedActiveRiskClassifications;
        if (currentValue != null) {
            return currentValue;
        }

        synchronized (this) {
            if (cachedActiveRiskClassifications != null) {
                return cachedActiveRiskClassifications;
            }

            cachedActiveRiskClassifications = fetchActiveCodes("/v1/catalogs/risk-classification")
                    .orElse(FALLBACK_ACTIVE_RISK_CLASSIFICATIONS);
            return cachedActiveRiskClassifications;
        }
    }

    private Set<String> loadActiveBusinessLines() {
        Set<String> currentValue = cachedActiveBusinessLines;
        if (currentValue != null) {
            return currentValue;
        }

        synchronized (this) {
            if (cachedActiveBusinessLines != null) {
                return cachedActiveBusinessLines;
            }

            cachedActiveBusinessLines = fetchActiveCodes("/v1/business-lines")
                    .orElse(FALLBACK_ACTIVE_BUSINESS_LINES);
            return cachedActiveBusinessLines;
        }
    }

    private Optional<Set<String>> fetchActiveCodes(String path) {
        try {
            HttpResponse<String> response = sendGet(path);
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isMissingNode() || !dataNode.isArray()) {
                return Optional.empty();
            }

            Set<CoreCatalogItem> catalogItems = objectMapper.convertValue(
                    dataNode,
                    new TypeReference<Set<CoreCatalogItem>>() {
                    }
            );

            if (catalogItems == null || catalogItems.isEmpty()) {
                return Optional.empty();
            }

            Set<String> activeCodes = catalogItems.stream()
                    .filter(CoreCatalogItem::activo)
                    .map(CoreCatalogItem::codigo)
                    .map(this::normalize)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());

            return activeCodes.isEmpty() ? Optional.empty() : Optional.of(activeCodes);
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            return Optional.empty();
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

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8081";
        }

        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record CoreCatalogItem(String codigo, String nombre, boolean activo, String claveIncendio) {
    }
}
package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import com.sofka.plataforma_danos_back.folios.domain.port.GeneralInfoCatalogPort;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class InMemoryGeneralInfoCatalogAdapter implements GeneralInfoCatalogPort {

    private static final Set<String> ACTIVE_AGENTS = Set.of(
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

    private static final Set<String> ACTIVE_RISK_CLASSIFICATIONS = Set.of(
            "RISK-A",
            "RISK-B",
            "RISK-C",
            "RISK-D"
    );

    private static final Set<String> ACTIVE_BUSINESS_LINES = Set.of(
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

    @Override
    public boolean isActiveAgent(String codigoAgente) {
        return ACTIVE_AGENTS.contains(codigoAgente);
    }

    @Override
    public boolean isActiveRiskClassification(String clasificacionRiesgo) {
        return ACTIVE_RISK_CLASSIFICATIONS.contains(clasificacionRiesgo);
    }

    @Override
    public boolean isActiveBusinessLine(String tipoNegocio) {
        return ACTIVE_BUSINESS_LINES.contains(tipoNegocio);
    }
}
package com.sofka.plataforma_danos_back.folios.domain;

public record CoverageGuaranteeDefinition(
        String garantiaCode,
        boolean activa,
        TechnicalPreviewSource fuenteTecnicaPreferida
) {
}
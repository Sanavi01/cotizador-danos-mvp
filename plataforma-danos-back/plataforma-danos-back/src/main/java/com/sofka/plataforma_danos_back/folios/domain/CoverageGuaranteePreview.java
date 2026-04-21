package com.sofka.plataforma_danos_back.folios.domain;

import java.util.List;

public record CoverageGuaranteePreview(
        String garantiaCode,
        boolean tariffablePreview,
        TechnicalPreviewSource fuenteTecnicaPreview,
        String lookupKeyPreview,
        List<String> motivosNoTarifable
) {
    public CoverageGuaranteePreview {
        motivosNoTarifable = motivosNoTarifable == null ? List.of() : List.copyOf(motivosNoTarifable);
    }
}
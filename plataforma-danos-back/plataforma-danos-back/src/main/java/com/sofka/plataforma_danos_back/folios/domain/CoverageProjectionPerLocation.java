package com.sofka.plataforma_danos_back.folios.domain;

import java.util.List;

public record CoverageProjectionPerLocation(
        Integer indice,
        List<CoverageGuaranteePreview> garantiasDerivadas,
        boolean calculablePreview
) {
    public CoverageProjectionPerLocation {
        garantiasDerivadas = garantiasDerivadas == null ? List.of() : List.copyOf(garantiasDerivadas);
    }
}
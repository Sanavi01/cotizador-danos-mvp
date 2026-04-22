package com.sofka.plataforma_danos_back.folios.domain;

import java.util.List;

public record CoverageOptions(
        Long cotizacionId,
        List<SelectedGuarantee> garantiasSeleccionadas,
        String observaciones
) {
    public CoverageOptions {
        garantiasSeleccionadas = garantiasSeleccionadas == null ? List.of() : List.copyOf(garantiasSeleccionadas);
        observaciones = normalize(observaciones);
    }

    public static CoverageOptions empty(Long cotizacionId) {
        return new CoverageOptions(cotizacionId, List.of(), null);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
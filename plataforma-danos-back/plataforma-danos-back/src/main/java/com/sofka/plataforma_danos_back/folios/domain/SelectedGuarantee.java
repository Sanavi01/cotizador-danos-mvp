package com.sofka.plataforma_danos_back.folios.domain;

import java.util.List;

public record SelectedGuarantee(
        String garantiaCode,
        List<String> terminos
) {
    public SelectedGuarantee {
        terminos = terminos == null ? List.of() : List.copyOf(terminos);
    }
}
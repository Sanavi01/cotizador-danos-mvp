package com.sofka.plataforma_danos_back.folios.domain;

import java.math.BigDecimal;
import java.util.List;

public record GarantiaCalculada(
        String garantiaCode,
        BigDecimal primaGarantia,
        List<ComponenteTecnicoCalculado> componentes
) {
    public GarantiaCalculada {
        componentes = componentes == null ? List.of() : List.copyOf(componentes);
    }
}
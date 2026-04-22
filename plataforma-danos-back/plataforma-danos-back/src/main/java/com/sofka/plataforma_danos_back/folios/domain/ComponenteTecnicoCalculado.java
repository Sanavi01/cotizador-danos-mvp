package com.sofka.plataforma_danos_back.folios.domain;

import java.math.BigDecimal;

public record ComponenteTecnicoCalculado(
        String tipo,
        String fuente,
        String lookupKey,
        BigDecimal rate,
        BigDecimal factor,
        BigDecimal monto
) {
}
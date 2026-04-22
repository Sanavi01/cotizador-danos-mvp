package com.sofka.plataforma_danos_back.folios.domain;

import java.math.BigDecimal;

public record ComponenteComercialCalculado(
        String tipo,
        BigDecimal porcentaje,
        BigDecimal base,
        BigDecimal monto
) {
}
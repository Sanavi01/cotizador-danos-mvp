package com.sofka.plataforma_danos_back.folios.domain;

import java.math.BigDecimal;
import java.util.List;

public record PrimaPorUbicacion(
        Integer indiceUbicacion,
        boolean ubicacionCalculable,
        BigDecimal primaNetaUbicacion,
        BigDecimal primaComercialUbicacion,
        List<GarantiaCalculada> garantiasCalculadas,
        List<ComponenteComercialCalculado> componentesComerciales,
        List<AlertaBloqueante> alertas
) {
    public PrimaPorUbicacion {
        garantiasCalculadas = garantiasCalculadas == null ? List.of() : List.copyOf(garantiasCalculadas);
        componentesComerciales = componentesComerciales == null ? List.of() : List.copyOf(componentesComerciales);
        alertas = alertas == null ? List.of() : List.copyOf(alertas);
    }
}
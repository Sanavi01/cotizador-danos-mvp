package com.sofka.plataforma_danos_back.folios.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Cotizacion(
        Long id,
        String numeroFolio,
        EstadoCotizacion estadoCotizacion,
        Long version,
        Instant fechaUltimaActualizacion,
        BigDecimal primaNeta,
        BigDecimal primaComercial
) {
    public static Cotizacion nueva(String numeroFolio, Instant now) {
        return new Cotizacion(null, numeroFolio, EstadoCotizacion.BORRADOR, 0L, now, null, null);
    }

    public Cotizacion withPersistence(Long id, Long version, Instant fechaUltimaActualizacion) {
        return new Cotizacion(id, numeroFolio, estadoCotizacion, version, fechaUltimaActualizacion, primaNeta, primaComercial);
    }

    public Cotizacion withEstado(EstadoCotizacion estadoCotizacion) {
        return new Cotizacion(id, numeroFolio, estadoCotizacion, version, fechaUltimaActualizacion, primaNeta, primaComercial);
    }
}

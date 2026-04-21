package com.sofka.plataforma_danos_back.folios.domain;

import java.time.Instant;
import java.util.List;

public record ConfiguracionLayout(
        Long id,
        Long cotizacionId,
        ModoCaptura modoCaptura,
        Integer cantidadUbicaciones,
        List<LayoutUbicacionSlot> ubicaciones,
        Instant createdAt,
        Instant updatedAt
) {
    public ConfiguracionLayout {
        ubicaciones = ubicaciones == null ? List.of() : List.copyOf(ubicaciones);
    }

    public static ConfiguracionLayout nueva(
            Long cotizacionId,
            ModoCaptura modoCaptura,
            Integer cantidadUbicaciones,
            List<LayoutUbicacionSlot> ubicaciones,
            Instant now
    ) {
        return new ConfiguracionLayout(null, cotizacionId, modoCaptura, cantidadUbicaciones, ubicaciones, now, now);
    }

    public ConfiguracionLayout withPersistence(Long id, Instant createdAt, Instant updatedAt) {
        return new ConfiguracionLayout(id, cotizacionId, modoCaptura, cantidadUbicaciones, ubicaciones, createdAt, updatedAt);
    }
}
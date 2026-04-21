package com.sofka.plataforma_danos_back.folios.domain;

import java.time.Instant;
import java.util.List;

public record UbicacionCotizacion(
        Long id,
        Long cotizacionId,
        UbicacionDetalle detalle,
        EstadoValidacion estadoValidacion,
        List<AlertaBloqueante> alertasBloqueantes,
        Instant createdAt,
        Instant updatedAt
) {
    public UbicacionCotizacion {
        alertasBloqueantes = alertasBloqueantes == null ? List.of() : List.copyOf(alertasBloqueantes);
    }

    public static UbicacionCotizacion nueva(Long cotizacionId, UbicacionEvaluacion evaluacion, Instant now) {
        return new UbicacionCotizacion(
                null,
                cotizacionId,
                evaluacion.detalle(),
                evaluacion.estadoValidacion(),
                evaluacion.alertasBloqueantes(),
                now,
                now
        );
    }

    public UbicacionCotizacion withPersistence(Long id, Instant createdAt, Instant updatedAt) {
        return new UbicacionCotizacion(id, cotizacionId, detalle, estadoValidacion, alertasBloqueantes, createdAt, updatedAt);
    }
}
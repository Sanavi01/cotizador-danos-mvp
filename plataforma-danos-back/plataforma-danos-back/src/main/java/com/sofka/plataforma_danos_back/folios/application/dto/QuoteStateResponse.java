package com.sofka.plataforma_danos_back.folios.application.dto;

import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;

import java.time.Instant;
import java.util.List;

public record QuoteStateResponse(
        String numeroFolio,
        EstadoCotizacion estadoCotizacion,
        boolean tieneAlertas,
        List<String> seccionesCompletadas,
        int ubicacionesCalculables,
        int ubicacionesIncompletas,
        Long version,
        Instant fechaUltimaActualizacion
) {
    public static QuoteStateResponse from(Cotizacion cotizacion) {
        return new QuoteStateResponse(
                cotizacion.numeroFolio(),
                cotizacion.estadoCotizacion(),
                false,
                List.of(),
                0,
                0,
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion()
        );
    }
}

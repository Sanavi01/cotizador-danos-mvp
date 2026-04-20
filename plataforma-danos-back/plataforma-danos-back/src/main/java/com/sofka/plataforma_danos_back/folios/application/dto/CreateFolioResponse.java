package com.sofka.plataforma_danos_back.folios.application.dto;

import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;

import java.time.Instant;

public record CreateFolioResponse(
        String numeroFolio,
        EstadoCotizacion estadoCotizacion,
        Long version,
        Instant fechaUltimaActualizacion
) {
    public static CreateFolioResponse from(Cotizacion cotizacion) {
        return new CreateFolioResponse(
                cotizacion.numeroFolio(),
                cotizacion.estadoCotizacion(),
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion()
        );
    }
}

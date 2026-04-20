package com.sofka.plataforma_danos_back.folios.application.dto;

import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record CreateFolioResponse(
    @Schema(description = "Numero de folio secuencial asignado a la cotizacion", example = "1000001")
        String numeroFolio,
    @Schema(description = "Estado actual de la cotizacion", example = "BORRADOR")
        EstadoCotizacion estadoCotizacion,
    @Schema(description = "Version optimista de la entidad persistida", example = "0")
        Long version,
    @Schema(description = "Marca de tiempo de la ultima actualizacion logica", example = "2026-04-20T00:00:00Z")
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

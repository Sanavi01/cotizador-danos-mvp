package com.sofka.plataforma_danos_back.folios.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sofka.plataforma_danos_back.folios.application.dto.LocationsSummaryResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@Service
public class GetLocationsSummaryUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final ConfiguracionLayoutRepository configuracionLayoutRepository;
    private final UbicacionCotizacionRepository ubicacionCotizacionRepository;

    public GetLocationsSummaryUseCase(
            CotizacionRepository cotizacionRepository,
            ConfiguracionLayoutRepository configuracionLayoutRepository,
            UbicacionCotizacionRepository ubicacionCotizacionRepository
    ) {
        this.cotizacionRepository = cotizacionRepository;
        this.configuracionLayoutRepository = configuracionLayoutRepository;
        this.ubicacionCotizacionRepository = ubicacionCotizacionRepository;
    }

    @Transactional(readOnly = true)
    public LocationsSummaryResponse handle(String numeroFolio) {
        Cotizacion cotizacion = cotizacionRepository.findByNumeroFolio(numeroFolio)
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));

        ConfiguracionLayout configuracionLayout = configuracionLayoutRepository.findByCotizacionId(cotizacion.id()).orElse(null);
        List<LayoutUbicacionSlot> slots = configuracionLayout == null ? List.of() : configuracionLayout.ubicaciones();
        return LocationsSummaryResponse.from(
                cotizacion.numeroFolio(),
                slots,
                ubicacionCotizacionRepository.findAllByCotizacionId(cotizacion.id())
        );
    }
}